package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class GearClaw implements Constants {

	private CANTalon clawTalon;

	public enum Mode {
		CLOSING, // Beginning to close, before gear
		GRABBING, // Trying to grab
		MISSED, // Closed past a gear
		OPENING, // Opening
		OPEN, // All the way open
		STOPPED_OPENING, // Could not open
		STOPPED_CLOSING, // Could not close
	}

	private Mode state = Mode.OPEN;

	private boolean hasGear = false;

	public boolean up = false;
	public boolean down = false;

	private NetworkTable robotTable;

	private DigitalInput gearSwitch = new DigitalInput(CLAW_GEAR_SWITCH_PORT);

	private double lastSpeed = 0;

	public GearClaw(int clawPort, NetworkTable robotTable) {
		clawTalon = new CANTalon(clawPort);

		clawTalon.setFeedbackDevice(FeedbackDevice.AnalogPot);
		//clawTalon.reverseSensor(true);

		clawTalon.configPotentiometerTurns(1);

		clawTalon.configNominalOutputVoltage(+0.0f, -0.0f);
		clawTalon.configPeakOutputVoltage(+12.0f, -12.0f);

		clawTalon.setCurrentLimit(CLAW_MAX_CURRENT);
		clawTalon.EnableCurrentLimit(true);

		clawTalon.setForwardSoftLimit(CLAW_CLOSE_POS);
		clawTalon.setReverseSoftLimit(CLAW_OPEN_POS);

		clawTalon.changeControlMode(TalonControlMode.Voltage);
		//clawTalon.setVoltageRampRate(24);

		this.robotTable = robotTable;
	}

	public void openClaw() {
		if (state != Mode.OPENING) {
			state = Mode.OPENING;
		}
	}

	public void closeClaw() {
		if (state != Mode.CLOSING && state != Mode.GRABBING && state != Mode.MISSED) {
			state = Mode.CLOSING;
		}
	}

	public void auto() {
		if (!gearSwitch.get() && (state == Mode.OPEN || state == Mode.OPENING)) {
			state = Mode.CLOSING;
		}
	}

	public boolean isClosedEnough() {
		return clawTalon.getPosition() > CLAW_CLOSED_ENOUGH_POS;
	}

	public boolean hasGear() {
		return clawTalon.getPosition() >= CLAW_GEAR_MIN_POS && clawTalon.getPosition() <= CLAW_GEAR_MAX_POS;
	}

	public boolean isOpen() {
		return clawTalon.getPosition() < CLAW_GEAR_MIN_POS;
	}

	public void periodic() {
		double current = clawTalon.getOutputCurrent();
		double position = clawTalon.getPosition();
		double voltage = clawTalon.getOutputVoltage();
		double speed = clawTalon.getSpeed();
		boolean gear = !gearSwitch.get();

		System.out.println(up + ":" + down);

		switch (state) {

		case OPENING:
			hasGear = false;
			if (current > CLAW_MAX_CURRENT) {
				clawTalon.set(0);
			} else {
				if ((down || up) && voltage != CLAW_OPEN_VOLTAGE) {
					clawTalon.set(CLAW_OPEN_VOLTAGE);
				}
			}

			if (position <= CLAW_OPEN_POS) {
				state = Mode.OPEN;
			}
			
			if (lastSpeed != 0 && speed == 0){
				state = Mode.STOPPED_OPENING;
			}

			break;

		case OPEN:
			hasGear = false;
			if (position <= CLAW_OPEN_POS) {
				state = Mode.OPENING;
			} else {
				if (voltage != 0) {
					clawTalon.set(0);
				}
			}

			break;

		case STOPPED_OPENING:
			hasGear = false;
			if (voltage != 0) {
				clawTalon.set(0);
			}

			break;

		case CLOSING:
			hasGear = false;
			if (current > CLAW_TRIGGER_CURRENT || position >= CLAW_GEAR_MIN_POS) {
				state = Mode.GRABBING;
			} else {
				if (voltage != CLAW_CLOSE_VOLTAGE) {
					clawTalon.set(CLAW_CLOSE_VOLTAGE);
				}
			}

			if (lastSpeed != 0 && speed == 0){
				state = Mode.STOPPED_OPENING;
			}
			break;

		case STOPPED_CLOSING:
			hasGear = false;
			if (voltage != 0) {
				clawTalon.set(0);
			}

		case GRABBING:
			if (position > CLAW_GEAR_MAX_POS) {
				state = Mode.MISSED;
			} else {
				if (voltage != CLAW_GRAB_VOLTAGE) {
					clawTalon.set(CLAW_GRAB_VOLTAGE);
				}
				if (current >= CLAW_GEAR_CURRENT || speed == 0) {
					hasGear = true;
				} else {
					hasGear = false;
				}
			}
			break;

		case MISSED:
			hasGear = false;
			if (voltage != 0) {
				clawTalon.set(0);
			}
			break;
		}

		robotTable.putString("clawstate", state.toString());
		robotTable.putBoolean("hasgear", hasGear);
		robotTable.putNumber("clawvoltage", voltage);
		robotTable.putNumber("clawcurrent", current);
		robotTable.putNumber("clawposition", position);
		robotTable.putNumber("clawspeed", speed);
		robotTable.putBoolean("gearswitch", gear);

		lastSpeed = speed;
	}
}
