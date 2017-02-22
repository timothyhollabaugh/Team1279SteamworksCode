package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GearClaw {

	// CHANGE ALL CONSTANTS TO SANE VALUES

	public static final double CLOSE_VOLTAGE = 2.5;
	public static final double OPEN_VOLTAGE = -2.5;

	/**
	 * The minimum position for grabbing a gear
	 */
	public static final double CLAW_GEAR_MIN_POS = -0.440;

	/**
	 * The maximum poition for grabbing a gear
	 */
	public static final double CLAW_GEAR_MAX_POS = -0.390;

	/**
	 * The minimum position for raising or lowering the claw
	 */
	public static final double CLAW_CLOSED_ENOUGH_POS = CLAW_GEAR_MIN_POS;

	/**
	 * The current at which to trigger going into current control mode to hold
	 * the gear
	 */
	public static final double CLAW_TRIGGER_CURRENT = 0.5;

	/**
	 * The voltage to target when grabbing a gear
	 */
	public double CLAW_GRAB_VOLTAGE = 3.2;

	/**
	 * The current limit on the talon
	 */
	public static final int CLAW_MAX_CURRENT = 1;

	private CANTalon clawTalon;

	private boolean open = true;
	private boolean hasGear = false;

	public GearClaw(int clawPort) {
		clawTalon = new CANTalon(clawPort);

		clawTalon.setFeedbackDevice(FeedbackDevice.AnalogPot);
		clawTalon.reverseSensor(true);

		clawTalon.configPotentiometerTurns(1);

		clawTalon.configNominalOutputVoltage(+0.0f, -0.0f);
		clawTalon.configPeakOutputVoltage(+12.0f, -12.0f);

		clawTalon.setCurrentLimit(CLAW_MAX_CURRENT);
		clawTalon.EnableCurrentLimit(true);

		clawTalon.changeControlMode(TalonControlMode.Voltage);

	}

	public void openClaw() {
		if (!open) {
			this.open = true;
			this.hasGear = false;
			// clawTalon.changeControlMode(TalonControlMode.Position);
			// clawTalon.setProfile(POS_PROFILE);
			// clawTalon.set(OPEN_POS);

			clawTalon.set(OPEN_VOLTAGE);
		}
	}

	public void closeClaw() {
		if (open) {
			SmartDashboard.putString("DB/String 2", "Closing");
			this.open = false;
			// clawTalon.set(CLOSED_POS);
			clawTalon.changeControlMode(TalonControlMode.Voltage);
			clawTalon.set(CLOSE_VOLTAGE);
		}
	}

	public boolean isClosedEnough() {
		return clawTalon.getPosition() > CLAW_CLOSED_ENOUGH_POS;
	}

	public void periodic() {
		SmartDashboard.putString("DB/String 0",
				Double.toString(clawTalon.getOutputCurrent()));
		SmartDashboard.putString("DB/String 1",
				Double.toString(clawTalon.getPosition()));
		SmartDashboard.putString("DB/String 2",
				Double.toString(clawTalon.getOutputVoltage()));
		
		CLAW_GRAB_VOLTAGE = SmartDashboard.getNumber("DB/Slider 0", 2);
		System.out.println(CLAW_GRAB_VOLTAGE);
		
		if (!open) {
			if (!hasGear) {
				if (clawTalon.getOutputCurrent() >= CLAW_TRIGGER_CURRENT) {
					if (clawTalon.getPosition() < CLAW_GEAR_MIN_POS) {
						// The claw got stuck before it got to the gear
						SmartDashboard.putString("DB/String 3", "Got Stuck!?");
						openClaw();
						// TODO
					} else if (clawTalon.getPosition() > CLAW_GEAR_MAX_POS) {
						// There was no gear
						SmartDashboard.putString("DB/String 3", "No Gear :(");
						// TODO
					} else {
						// There is a gear
						SmartDashboard
								.putString("DB/String 3", "Found a Gear!");
						this.hasGear = true;
						clawTalon.set(CLAW_GRAB_VOLTAGE);
					}
				}
			}
		}
	}
}
