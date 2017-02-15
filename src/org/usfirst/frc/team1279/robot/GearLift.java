package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

public class GearLift {

  public static final double UP_VOLTAGE = 5;
  public static final double DOWN_VOLTAGE = -5;
  
  public CANTalon masterTalon;
  public CANTalon slaveTalon;
  
  GearClaw gearClaw;
  
  public GearLift(GearClaw claw, int master, int slave){
    this.gearClaw = claw;
    
    this.masterTalon = new CANTalon(master);
    this.masterTalon.changeControlMode(TalonControlMode.Voltage);
    this.masterTalon.setFeedbackDevice(FeedbackDevice.CtreMagEncoder_Relative);
    this.masterTalon.configNominalOutputVoltage(+0.0f, -0.0f);
    this.masterTalon.configPeakOutputVoltage(+12.0f, -12.0f);
    
    this.slaveTalon = new CANTalon(slave);
    this.slaveTalon.changeControlMode(TalonControlMode.Follower);
    this.slaveTalon.set(master);
    this.slaveTalon.configNominalOutputVoltage(+0.0f, -0.0f);
    this.slaveTalon.configPeakOutputVoltage(+12.0f, -12.0f);
    
  }
  
  public boolean raiseGear(){
    if(gearClaw.isClosedEnough()){
      masterTalon.set(UP_VOLTAGE);
      return true;
    }else{
      return false;
    }
    
  }
  
  public boolean lowerGear(){
    if(gearClaw.isClosedEnough()){
      masterTalon.set(DOWN_VOLTAGE);
      return true;
    }else{
      return false;
    }
    
  }
}
