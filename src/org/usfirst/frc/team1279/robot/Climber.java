package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DigitalInput;

public class Climber implements Constants
{
   public static final double kDefaultSensitivity = 0.5;
   public static final double kDefaultMaxOutput   = 1.0;
   private double             m_sensitivity;
   private double             m_maxOutput;
   private CANTalon           m_motor;
   private DigitalInput       m_limitSwitch;
   private boolean            m_isOverrideSet     = false;
   private boolean            m_isShutDown        = false;

   public Climber(final int CanID)
   {
      m_sensitivity = kDefaultSensitivity;
      m_maxOutput = kDefaultMaxOutput;
      m_motor = new CANTalon(CanID);
      m_limitSwitch = new DigitalInput(0);
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
      if (m_limitSwitch.get() && !m_isOverrideSet && !m_isShutDown)
      {
         m_motor.set(limit(output) * m_maxOutput);
      } else
      {
         m_motor.set(0);
      }

      System.out.println("Climber.drive(" + output + ")");
   }

   public int getDistance()
   {
      return m_motor.getEncPosition();
   }

   public void zeroEnc()
   {
      m_motor.setEncPosition(0);
   }

   public void setOverride()
   {
      m_isOverrideSet = true;
   }

   public void setShutDown()
   {
      m_isShutDown = true;
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
}
