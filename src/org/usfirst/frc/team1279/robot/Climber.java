package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;

public class Climber implements Constants
{
   private CANTalon m_motor;
   private boolean  m_isShutDown = false;

   public Climber(final int CanID)
   {
      m_motor = new CANTalon(CanID);
      drive(0);

      // configure
      //m_motor.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
      m_motor.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      m_motor.enableBrakeMode(true);
      m_motor.configNominalOutputVoltage(+0.0f, -0.0f);

      // TODO: use the forward softlimit or limit the drive voltage
      // to disable forward direction?
      m_motor.configPeakOutputVoltage(+0.0f, -12.0f);
      // m_motor.setForwardSoftLimit(0);
      // m_motor.enableForwardSoftLimit(true);

      m_motor.setVoltageRampRate(24.0);

      // initialize forward and reverse limit switches as normally open
      m_motor.ConfigFwdLimitSwitchNormallyOpen(true);
      m_motor.ConfigRevLimitSwitchNormallyOpen(true);
   }

   public void drive(double output)
   {
      if (!m_isShutDown)
      {
         // motor climbs in reverse direction
         m_motor.set(-Math.abs(output));
      } else
      {
         m_motor.set(0);
      }

      System.out.println("Climber.drive(" + output + ")");
   }
   
   public void stop(){
	   m_motor.set(0);
   }

   // Allow going past the limit switches and unshutdown
   public void setOverride()
   {
      // override the forward and reverse limit switches
      m_motor.enableLimitSwitch(false, false);
      m_isShutDown = false;
   }

   public void disableOverride()
   {
     m_motor.enableLimitSwitch(true, true);
   }

   public void setShutDown()
   {
      m_isShutDown = true;
   }
}
