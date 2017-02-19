package org.usfirst.frc.team1279.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class Vision {

	public static final String TABLE = "RaspberryPi";

	public static final String CAMERA_KEY = "camera";
	public static final int PI_CAMERA = 0;
	public static final int USB_CAMERA = 1;

	public static final String PROCESS_KEY = "process";
	public static final int NO_PROCESSING = 0;
	public static final int GEAR_SINGLE_PROCESSING = 1;
	public static final int GEAR_CONTINUOS_PROCESSING = 2;
	public static final int GEAR_MANUAL_PROCESSING = 3;
	public static final int CLIMBER_PROCESSING = 4;
	
	public static final String TURN_KEY = "turn";
	public static final double TURN_AMOUNT = 1;
	public static final double TURN_ERROR = 5;

	public NetworkTable table;

	public Vision() {

	}

	public void init() {
		table = NetworkTable.getTable(TABLE);
	}

	public void setCamera(int camera) {
		table.putNumber(CAMERA_KEY, camera);
	}
	
	public int getCamera(){
		return (int) table.getNumber(CAMERA_KEY, USB_CAMERA);
	}
	
	public int getProcess(){
		return (int) table.getNumber(PROCESS_KEY, NO_PROCESSING);
	}
	
	public void setProcess(int process){
		table.putNumber(PROCESS_KEY, process);
	}
	
	public double getTurn(){
		return table.getNumber("turn", 0);
	}
	
	public void doTurn(DriveTrain drive){
		double turn = getTurn();
		drive.arcadeDrive(0, TURN_AMOUNT * (turn >= 0 ? 1 : -1));
		Timer.delay(turn / 5);
		drive.arcadeDrive(0, 0);
	}
	
	public void doGearAdjust(DriveTrain drive){
		setProcess(GEAR_CONTINUOS_PROCESSING);
		Timer.delay(0.1);
		
		while(Math.abs(getTurn()) > TURN_ERROR){
			doTurn(drive);
		}
		
		setProcess(NO_PROCESSING);
	}
}
