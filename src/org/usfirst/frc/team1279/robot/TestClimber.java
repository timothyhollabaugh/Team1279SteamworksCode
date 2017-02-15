package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

// To test the climber without limit switches

public class TestClimber {
	
	public CANTalon climbTalon;
	
	public TestClimber(int talonId){
		this.climbTalon = new CANTalon(talonId);
		this.climbTalon.changeControlMode(TalonControlMode.PercentVbus);
		
	    this.climbTalon.configNominalOutputVoltage(+0.0f, -0.0f);
	    this.climbTalon.configPeakOutputVoltage(+0.0f, -12.0f);
		
		// To climb, the motor must go backwards
		//this.climbTalon.reverseOutput(true);
	}
	
	public void drive(double amount){
		this.climbTalon.set(-Math.abs(amount));
	}	
}
