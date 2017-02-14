package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;

public class TalonTest implements Constants
{
   public static final double kDefaultSensitivity = 0.5;
   public static final double kDefaultMaxOutput   = 1.0;
   protected double           m_sensitivity;
   protected double           m_maxOutput;
   protected CANTalon         m_motor;

   public TalonTest(final int CanID)
   {
      m_sensitivity = kDefaultSensitivity;
      m_maxOutput = kDefaultMaxOutput;
      m_motor = new CANTalon(CanID);
      drive(0);

      // configure
      m_motor.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
      m_motor.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      m_motor.setCloseLoopRampRate(0.50);
      m_motor.setVoltageRampRate(24.0);
      m_motor.setEncPosition(0);
   }

   public void drive(double output)
   {
      m_motor.set(limit(output) * m_maxOutput);
   }

   public int getDistance()
   {
      return m_motor.getEncPosition();
   }

   public void zeroEnc()
   {
      m_motor.setEncPosition(0);
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

   public void stop()
   {
      // m_motor.stopMotor();
      m_motor.set(0);
   }

   public void testDistance(double speed, double distance)
   {
      double adj = 0.0;
      int count = 0;
      // TODO: recalibrate to replace prior year's factor
      final double COUNTS_PER_INCH = 78.0;
      double d = distance * COUNTS_PER_INCH;

      m_motor.setEncPosition(0);

      while (DriverStation.getInstance().isTest())
      {
         if (count <= 100)
            adj = (double) count / 100.0;
         else
            adj = 1.0;
         if (m_motor.getEncPosition() >= d)
         {
            m_motor.set(0);
            break;
         } else
         {
            m_motor.set(-speed * adj);
         }
         count++;
         Timer.delay(0.01);
      }
   }
}
