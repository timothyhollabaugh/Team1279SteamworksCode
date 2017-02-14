package org.usfirst.frc.team1279.robot;

import static java.util.Objects.requireNonNull;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.MotorSafety;
import edu.wpi.first.wpilibj.MotorSafetyHelper;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tInstances;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tResourceType;
import edu.wpi.first.wpilibj.hal.HAL;

public class DriveTrain implements Constants, MotorSafety
{
   protected MotorSafetyHelper m_safetyHelper;

   /**
    * The location of a motor on the robot for the purpose of driving.
    */
   public enum MotorType
   {
      kFrontLeft(0), kFrontRight(1), kRearLeft(2), kRearRight(3);

      @SuppressWarnings("MemberName")
      public final int value;

      private MotorType(int value)
      {
         this.value = value;
      }
   }

   public static final double kDefaultExpirationTime     = 0.1;
   public static final double kDefaultSensitivity        = 0.5;
   public static final double kDefaultMaxOutput          = 1.0;
   protected static final int kMaxNumberOfMotors         = 4;
   protected double           m_sensitivity;
   protected double           m_maxOutput;
   protected CANTalon         m_frontLeftMotor;
   protected CANTalon         m_frontRightMotor;
   protected CANTalon         m_rearLeftMotor;
   protected CANTalon         m_rearRightMotor;
   protected boolean          m_allocatedSpeedControllers;
   protected boolean          m_isInverted               = false;
   protected static boolean   kArcadeRatioCurve_Reported = false;
   protected static boolean   kTank_Reported             = false;
   protected static boolean   kArcadeStandard_Reported   = false;
   protected static boolean   kMecanumCartesian_Reported = false;
   protected static boolean   kMecanumPolar_Reported     = false;

   /**
    * Constructor for RobotDrive with 4 motors specified with channel numbers.
    * Set up parameters for a four wheel drive system where all four motor pwm
    * channels are specified in the call. This call assumes Talons for
    * controlling the motors.
    *
    * @param frontLeftMotor
    *           Front left motor channel number
    * @param rearLeftMotor
    *           Rear Left motor channel number
    * @param frontRightMotor
    *           Front right motor channel number
    * @param rearRightMotor
    *           Rear Right motor channel number
    */
   public DriveTrain(final int frontLeftMotor, final int rearLeftMotor, final int frontRightMotor,
         final int rearRightMotor)
   {
      m_sensitivity = kDefaultSensitivity;
      m_maxOutput = kDefaultMaxOutput;
      m_rearLeftMotor = new CANTalon(rearLeftMotor);
      m_rearRightMotor = new CANTalon(rearRightMotor);
      m_frontLeftMotor = new CANTalon(frontLeftMotor);
      m_frontRightMotor = new CANTalon(frontRightMotor);
      m_allocatedSpeedControllers = true;
      setupMotorSafety();
      drive(0, 0);

      // group Left side and configure
      m_rearLeftMotor.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
      m_rearLeftMotor.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      m_rearLeftMotor.setCloseLoopRampRate(0.50);
      m_rearLeftMotor.setVoltageRampRate(24.0);

      // group Left side and configure
      m_frontLeftMotor.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
      m_frontLeftMotor.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      m_frontLeftMotor.setCloseLoopRampRate(0.50);
      m_frontLeftMotor.setVoltageRampRate(24.0);
      // m_frontLeftMotor.changeControlMode(CANTalon.TalonControlMode.Follower);
      // m_frontLeftMotor.set(m_rearLeftMotor.getDeviceID());

      // group Right side and configure
      m_rearRightMotor.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
      m_rearRightMotor.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      m_rearRightMotor.setCloseLoopRampRate(0.50);
      m_rearRightMotor.setVoltageRampRate(24.0);

      m_frontRightMotor.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
      m_frontRightMotor.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      m_frontRightMotor.setCloseLoopRampRate(0.50);
      m_frontRightMotor.setVoltageRampRate(24.0);
      // m_frontRightMotor.changeControlMode(CANTalon.TalonControlMode.Follower);
      // m_frontRightMotor.set(m_rearRightMotor.getDeviceID());

      m_rearLeftMotor.setEncPosition(0);
      m_rearRightMotor.setEncPosition(0);
   }

   /**
    * Constructor for RobotDrive with 4 motors specified as CANTalon objects.
    * Speed controller input version of RobotDrive (see previous comments).
    *
    * @param rearLeftMotor
    *           The back left CANTalon object used to drive the robot.
    * @param frontLeftMotor
    *           The front left CANTalon object used to drive the robot
    * @param rearRightMotor
    *           The back right CANTalon object used to drive the robot.
    * @param frontRightMotor
    *           The front right CANTalon object used to drive the robot.
    */
   public DriveTrain(CANTalon frontLeftMotor, CANTalon rearLeftMotor, CANTalon frontRightMotor,
         CANTalon rearRightMotor)
   {
      m_frontLeftMotor = requireNonNull(frontLeftMotor, "frontLeftMotor cannot be null");
      m_rearLeftMotor = requireNonNull(rearLeftMotor, "rearLeftMotor cannot be null");
      m_frontRightMotor = requireNonNull(frontRightMotor, "frontRightMotor cannot be null");
      m_rearRightMotor = requireNonNull(rearRightMotor, "rearRightMotor cannot be null");
      m_sensitivity = kDefaultSensitivity;
      m_maxOutput = kDefaultMaxOutput;
      m_allocatedSpeedControllers = false;
      setupMotorSafety();
      drive(0, 0);
   }

   /**
    * Drive the motors at "outputMagnitude" and "curve". Both outputMagnitude
    * and curve are -1.0 to +1.0 values, where 0.0 represents stopped and not
    * turning. {@literal curve < 0 will turn left
    * and curve > 0} will turn right.
    *
    * <p>
    * The algorithm for steering provides a constant turn radius for any normal
    * speed range, both forward and backward. Increasing sensitivity causes
    * sharper turns for fixed values of curve.
    *
    * <p>
    * This function will most likely be used in an autonomous routine.
    *
    * @param outputMagnitude
    *           The speed setting for the outside wheel in a turn, forward or
    *           backwards, +1 to -1.
    * @param curve
    *           The rate of turn, constant for different forward speeds. Set
    *           {@literal
    *                        curve < 0 for left turn or curve > 0 for right turn.}
    *           Set curve = e^(-r/w) to get a turn radius r for wheelbase w of
    *           your robot. Conversely, turn radius r = -ln(curve)*w for a given
    *           value of curve and wheelbase w.
    */
   public void drive(double outputMagnitude, double curve)
   {
      final double leftOutput;
      final double rightOutput;

      if (!kArcadeRatioCurve_Reported)
      {
         HAL.report(tResourceType.kResourceType_RobotDrive, getNumMotors(),
               tInstances.kRobotDrive_ArcadeRatioCurve);
         kArcadeRatioCurve_Reported = true;
      }
      if (curve < 0)
      {
         double value = Math.log(-curve);
         double ratio = (value - m_sensitivity) / (value + m_sensitivity);
         if (ratio == 0)
         {
            ratio = .0000000001;
         }
         leftOutput = outputMagnitude / ratio;
         rightOutput = outputMagnitude;
      } else if (curve > 0)
      {
         double value = Math.log(curve);
         double ratio = (value - m_sensitivity) / (value + m_sensitivity);
         if (ratio == 0)
         {
            ratio = .0000000001;
         }
         leftOutput = outputMagnitude;
         rightOutput = outputMagnitude / ratio;
      } else
      {
         leftOutput = outputMagnitude;
         rightOutput = outputMagnitude;
      }
      setLeftRightMotorOutputs(leftOutput, rightOutput);
   }

   /**
    * Provide tank steering using the stored robot configuration. drive the
    * robot using two joystick inputs. The Y-axis will be selected from each
    * Joystick object.
    *
    * @param stick
    *           - Joystick to control the left and right side of the robot.
    */
   public void tankDrive(GenericHID stick)
   {
      if (stick == null)
      {
         throw new NullPointerException("Null HID provided");
      }
      tankDrive(stick.getRawAxis(1), stick.getRawAxis(5), true);
   }

   /**
    * Provide tank steering using the stored robot configuration. drive the
    * robot using two joystick inputs. The Y-axis will be selected from each
    * Joystick object.
    *
    * @param stick
    *           Joystick to control the left and right side of the robot.
    * @param squaredInputs
    *           Setting this parameter to true decreases the sensitivity at
    *           lower speeds
    */
   public void tankDrive(GenericHID stick, boolean squaredInputs)
   {
      if (stick == null)
      {
         throw new NullPointerException("Null HID provided");
      }
      tankDrive(stick.getRawAxis(1), stick.getRawAxis(5), squaredInputs);
   }

   /**
    * Provide tank steering using the stored robot configuration. This function
    * lets you pick the axis to be used on each Joystick object for the left and
    * right sides of the robot.
    *
    * @param stick
    *           Joystick to control the left and right side of the robot.
    * @param leftAxis
    *           The axis to select on the left side Joystick object.
    * @param rightAxis
    *           The axis to select on the right side Joystick object.
    */
   public void tankDrive(GenericHID stick, final int leftAxis, final int rightAxis)
   {
      if (stick == null)
      {
         throw new NullPointerException("Null HID provided");
      }
      tankDrive(stick.getRawAxis(leftAxis), stick.getRawAxis(rightAxis), true);
   }

   /**
    * Provide tank steering using the stored robot configuration. This function
    * lets you pick the axis to be used on each Joystick object for the left and
    * right sides of the robot.
    *
    * @param stick
    *           Joystick object to use for the left and right side of the robot.
    * @param leftAxis
    *           The axis to select on the left side Joystick object.
    * @param rightAxis
    *           The axis to select on the right side Joystick object.
    * @param squaredInputs
    *           Setting this parameter to true decreases the sensitivity at
    *           lower speeds
    */
   public void tankDrive(GenericHID stick, final int leftAxis, final int rightAxis,
         boolean squaredInputs)
   {
      if (stick == null)
      {
         throw new NullPointerException("Null HID provided");
      }
      tankDrive(stick.getRawAxis(leftAxis), stick.getRawAxis(rightAxis), squaredInputs);
   }

   /**
    * Provide tank steering using the stored robot configuration. This function
    * lets you directly provide joystick values from any source.
    *
    * @param leftValue
    *           The value of the left stick.
    * @param rightValue
    *           The value of the right stick.
    * @param squaredInputs
    *           Setting this parameter to true decreases the sensitivity at
    *           lower speeds
    */
   public void tankDrive(double leftValue, double rightValue, boolean squaredInputs)
   {

      if (!kTank_Reported)
      {
         HAL.report(tResourceType.kResourceType_RobotDrive, getNumMotors(),
               tInstances.kRobotDrive_Tank);
         kTank_Reported = true;
      }

      // TODO: add dead zone corrections to keep controllers from drifting in
      // nonlinear region?

      // square the inputs (while preserving the sign) to increase fine control
      // while permitting full power
      leftValue = limit(leftValue);
      rightValue = limit(rightValue);
      if (squaredInputs)
      {
         if (leftValue >= 0.0)
         {
            leftValue = leftValue * leftValue;
         } else
         {
            leftValue = -(leftValue * leftValue);
         }
         if (rightValue >= 0.0)
         {
            rightValue = rightValue * rightValue;
         } else
         {
            rightValue = -(rightValue * rightValue);
         }
      }
      setLeftRightMotorOutputs(leftValue, rightValue);
   }

   /**
    * Provide tank steering using the stored robot configuration. This function
    * lets you directly provide joystick values from any source.
    *
    * @param leftValue
    *           The value of the left stick.
    * @param rightValue
    *           The value of the right stick.
    */
   public void tankDrive(double leftValue, double rightValue)
   {
      tankDrive(leftValue, rightValue, true);
   }

   /**
    * Arcade drive implements single stick driving. Given a single Joystick, the
    * class assumes the Y axis for the move value and the X axis for the rotate
    * value. (Should add more information here regarding the way that arcade
    * drive works.)
    *
    * @param stick
    *           The joystick to use for Arcade single-stick driving. The Y-axis
    *           will be selected for forwards/backwards and the X-axis will be
    *           selected for rotation rate.
    * @param squaredInputs
    *           If true, the sensitivity will be decreased for small values
    */
   public void arcadeDrive(GenericHID stick, boolean squaredInputs)
   {
      // simply call the full-featured arcadeDrive with the appropriate values
      arcadeDrive(stick.getRawAxis(5), stick.getRawAxis(0), squaredInputs);
   }

   /**
    * Arcade drive implements single stick driving. Given a single Joystick, the
    * class assumes the Y axis for the move value and the X axis for the rotate
    * value. (Should add more information here regarding the way that arcade
    * drive works.)
    *
    * @param stick
    *           The joystick to use for Arcade single-stick driving. The Y-axis
    *           will be selected for forwards/backwards and the X-axis will be
    *           selected for rotation rate.
    */
   public void arcadeDrive(GenericHID stick)
   {
      arcadeDrive(stick, true);
   }

   /**
    * Arcade drive implements single stick driving. This function lets you
    * directly provide joystick values from any source.
    *
    * @param moveValue
    *           The value to use for forwards/backwards
    * @param rotateValue
    *           The value to use for the rotate right/left
    * @param squaredInputs
    *           If set, decreases the sensitivity at low speeds
    */
   public void arcadeDrive(double moveValue, double rotateValue, boolean squaredInputs)
   {
      // local variables to hold the computed PWM values for the motors
      if (!kArcadeStandard_Reported)
      {
         HAL.report(tResourceType.kResourceType_RobotDrive, getNumMotors(),
               tInstances.kRobotDrive_ArcadeStandard);
         kArcadeStandard_Reported = true;
      }

      double leftMotorSpeed;
      double rightMotorSpeed;

      moveValue = limit(moveValue);
      rotateValue = limit(rotateValue);

      if (squaredInputs)
      {
         // square the inputs (while preserving the sign) to increase fine
         // control
         // while permitting full power
         if (moveValue >= 0.0)
         {
            moveValue = moveValue * moveValue;
         } else
         {
            moveValue = -(moveValue * moveValue);
         }
         if (rotateValue >= 0.0)
         {
            rotateValue = rotateValue * rotateValue;
         } else
         {
            rotateValue = -(rotateValue * rotateValue);
         }
      }

      if (moveValue > 0.0)
      {
         if (rotateValue > 0.0)
         {
            leftMotorSpeed = moveValue - rotateValue;
            rightMotorSpeed = Math.max(moveValue, rotateValue);
         } else
         {
            leftMotorSpeed = Math.max(moveValue, -rotateValue);
            rightMotorSpeed = moveValue + rotateValue;
         }
      } else
      {
         if (rotateValue > 0.0)
         {
            leftMotorSpeed = -Math.max(-moveValue, rotateValue);
            rightMotorSpeed = moveValue + rotateValue;
         } else
         {
            leftMotorSpeed = moveValue - rotateValue;
            rightMotorSpeed = -Math.max(-moveValue, -rotateValue);
         }
      }

      setLeftRightMotorOutputs(leftMotorSpeed, rightMotorSpeed);
   }

   /**
    * Arcade drive implements single stick driving. This function lets you
    * directly provide joystick values from any source.
    *
    * @param moveValue
    *           The value to use for fowards/backwards
    * @param rotateValue
    *           The value to use for the rotate right/left
    */
   public void arcadeDrive(double moveValue, double rotateValue)
   {
      arcadeDrive(moveValue, rotateValue, true);
   }

   /**
    * Set the speed of the right and left motors. This is used once an
    * appropriate drive setup function is called such as twoWheelDrive(). The
    * motors are set to "leftSpeed" and "rightSpeed" and includes flipping the
    * direction of one side for opposing motors.
    *
    * @param leftOutput
    *           The speed to send to the left side of the robot.
    * @param rightOutput
    *           The speed to send to the right side of the robot.
    */
   public void setLeftRightMotorOutputs(double leftOutput, double rightOutput)
   {
      if (m_rearLeftMotor == null || m_rearRightMotor == null || m_frontLeftMotor == null
            || m_frontRightMotor == null)
      {
         throw new NullPointerException("Null motor provided");
      }

      m_frontLeftMotor.set(limit(leftOutput) * m_maxOutput);
      m_rearLeftMotor.set(limit(leftOutput) * m_maxOutput);

      m_frontRightMotor.set(-limit(rightOutput) * m_maxOutput);
      m_rearRightMotor.set(-limit(rightOutput) * m_maxOutput);

      if (m_safetyHelper != null)
      {
         m_safetyHelper.feed();
      }

      System.out.println(
            Timer.getFPGATimestamp() + ": DriveLeft:" + leftOutput + " DriveRight:" + rightOutput);
   }

   public int getDistance()
   {
      return m_rearLeftMotor.getEncPosition();
   }

   public void zeroEncs()
   {
      m_rearLeftMotor.setEncPosition(0);
      m_rearRightMotor.setEncPosition(0);
   }

   /**
    * Limit motor values to the -1.0 to +1.0 range.
    */
   protected static double limit(double num)
   {
      if (num > 1.0)
      {
         return 1.0;
      }
      if (num < -1.0)
      {
         return -1.0;
      }
      return num;
   }

   /**
    * Normalize all wheel speeds if the magnitude of any wheel is greater than
    * 1.0.
    */
   protected static void normalize(double[] wheelSpeeds)
   {
      double maxMagnitude = Math.abs(wheelSpeeds[0]);
      for (int i = 1; i < kMaxNumberOfMotors; i++)
      {
         double temp = Math.abs(wheelSpeeds[i]);
         if (maxMagnitude < temp)
         {
            maxMagnitude = temp;
         }
      }
      if (maxMagnitude > 1.0)
      {
         for (int i = 0; i < kMaxNumberOfMotors; i++)
         {
            wheelSpeeds[i] = wheelSpeeds[i] / maxMagnitude;
         }
      }
   }

   /**
    * Rotate a vector in Cartesian space.
    */
   @SuppressWarnings("ParameterName")
   protected static double[] rotateVector(double x, double y, double angle)
   {
      double cosA = Math.cos(angle * (3.14159 / 180.0));
      double sinA = Math.sin(angle * (3.14159 / 180.0));
      double[] out = new double[2];
      out[0] = x * cosA - y * sinA;
      out[1] = x * sinA + y * cosA;
      return out;
   }

   /**
    * Reverse robot direction. This is used to set all motors in the opposite
    * direction as the drive code would normally run. Motors that are direct
    * drive would be inverted, the drive code assumes that the motors are geared
    * with one reversal.
    */
   public void reverseDirection()
   {
      m_isInverted = !m_isInverted;
      System.out.println("DirectionInverted:" + Boolean.toString(m_isInverted));

      // stop robot before reversing direction
      drive(0.0, 0.0);

      m_frontLeftMotor.setInverted(m_isInverted);
      m_frontRightMotor.setInverted(m_isInverted);
      m_rearLeftMotor.setInverted(m_isInverted);
      m_rearRightMotor.setInverted(m_isInverted);

      Timer.delay(1.0);
   }

   /**
    * Invert a motor direction. This is used when a motor should run in the
    * opposite direction as the drive code would normally run it. Motors that
    * are direct drive would be inverted, the drive code assumes that the motors
    * are geared with one reversal.
    *
    * @param motor
    *           The motor index to invert.
    * @param isInverted
    *           True if the motor should be inverted when operated.
    */
   public void setInvertedMotor(MotorType motor, boolean isInverted)
   {
      switch (motor)
      {
         case kFrontLeft:
            m_frontLeftMotor.setInverted(isInverted);
            break;
         case kFrontRight:
            m_frontRightMotor.setInverted(isInverted);
            break;
         case kRearLeft:
            m_rearLeftMotor.setInverted(isInverted);
            break;
         case kRearRight:
            m_rearRightMotor.setInverted(isInverted);
            break;
         default:
            throw new IllegalArgumentException("Illegal motor type: " + motor);
      }
   }

   /**
    * Set the turning sensitivity.
    *
    * <p>
    * This only impacts the drive() entry-point.
    *
    * @param sensitivity
    *           Effectively sets the turning sensitivity (or turn radius for a
    *           given value)
    */
   public void setSensitivity(double sensitivity)
   {
      m_sensitivity = sensitivity;
   }

   /**
    * Configure the scaling factor for using RobotDrive with motor controllers
    * in a mode other than PercentVbus.
    *
    * @param maxOutput
    *           Multiplied with the output percentage computed by the drive
    *           functions.
    */
   public void setMaxOutput(double maxOutput)
   {
      m_maxOutput = maxOutput;
   }

   /**
    * Free the speed controllers if they were allocated locally.
    */
   public void free()
   {
      if (m_allocatedSpeedControllers)
      {
         if (m_frontLeftMotor != null)
         {
            // ((PWM) m_frontLeftMotor).free();
         }
         if (m_frontRightMotor != null)
         {
            // ((PWM) m_frontRightMotor).free();
         }
         if (m_rearLeftMotor != null)
         {
            // ((PWM) m_rearLeftMotor).free();
         }
         if (m_rearRightMotor != null)
         {
            // ((PWM) m_rearRightMotor).free();
         }
      }
   }

   @Override
   public void setExpiration(double timeout)
   {
      m_safetyHelper.setExpiration(timeout);
   }

   @Override
   public double getExpiration()
   {
      return m_safetyHelper.getExpiration();
   }

   @Override
   public boolean isAlive()
   {
      return m_safetyHelper.isAlive();
   }

   @Override
   public boolean isSafetyEnabled()
   {
      return m_safetyHelper.isSafetyEnabled();
   }

   @Override
   public void setSafetyEnabled(boolean enabled)
   {
      m_safetyHelper.setSafetyEnabled(enabled);
   }

   @Override
   public String getDescription()
   {
      return "Drive Train";
   }

   @Override
   public void stopMotor()
   {
      if (m_frontLeftMotor != null)
      {
         m_frontLeftMotor.stopMotor();
      }
      if (m_frontRightMotor != null)
      {
         m_frontRightMotor.stopMotor();
      }
      if (m_rearLeftMotor != null)
      {
         m_rearLeftMotor.stopMotor();
      }
      if (m_rearRightMotor != null)
      {
         m_rearRightMotor.stopMotor();
      }
      if (m_safetyHelper != null)
      {
         m_safetyHelper.feed();
      }
   }

   public void autoDistance(double speed, double distance)
   {
      double adj = 0.0;
      int count = 0;
      // TODO: recalibrate to replace prior year's factor
      final double COUNTS_PER_INCH = 78.0;
      double d = distance * COUNTS_PER_INCH;

      m_rearLeftMotor.setEncPosition(0);
      m_rearRightMotor.setEncPosition(0);

      while (DriverStation.getInstance().isAutonomous())
      {
         if (count <= 100)
            adj = (double) count / 100.0;
         else
            adj = 1.0;
         if (m_rearLeftMotor.getEncPosition() <= -d && m_rearRightMotor.getEncPosition() >= d)
         {
            m_rearLeftMotor.set(0);
            m_rearRightMotor.set(0);
            break;
         } else
         {
            m_rearRightMotor.set(-speed * adj);
            m_rearLeftMotor.set(speed * adj);
         }
         count++;
         Timer.delay(0.01);

         System.out.println(Timer.getFPGATimestamp() + ":RENC:" + m_rearRightMotor.getEncPosition()
               + " LENC:" + m_rearLeftMotor.getEncPosition());
      }
   }

   protected int getNumMotors()
   {
      int motors = 0;
      if (m_frontLeftMotor != null)
      {
         motors++;
      }
      if (m_frontRightMotor != null)
      {
         motors++;
      }
      if (m_rearLeftMotor != null)
      {
         motors++;
      }
      if (m_rearRightMotor != null)
      {
         motors++;
      }
      return motors;
   }

   private void setupMotorSafety()
   {
      m_safetyHelper = new MotorSafetyHelper(this);
      m_safetyHelper.setExpiration(kDefaultExpirationTime);
      m_safetyHelper.setSafetyEnabled(true);
   }
}
