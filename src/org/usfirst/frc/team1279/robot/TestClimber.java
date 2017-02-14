package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

// To test the climber without limit switches

public class TestClimber {
	
	public CANTalon climbTalon;
	
	public TestClimber(int talonId){
		this.climbTalon = new CANTalon(talonId);
		this.climbTalon.changeControlMode(TalonControlMode.PercentVbus);
	}
	
	public void drive(double amount){
		this.climbTalon.set(amount);
	}	
}
