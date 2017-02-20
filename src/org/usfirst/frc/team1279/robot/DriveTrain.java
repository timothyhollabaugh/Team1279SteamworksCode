package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;

public class DriveTrain implements Constants{

	CANTalon frontLeftMotor;
	CANTalon frontRightMotor;
	CANTalon rearLeftMotor;
	CANTalon rearRightMotor;
	
	public RobotDrive drive;
	
	double throttleScale = 1;
	double turnScale = 1;
	
	public DriveTrain(int leftFrontId, int leftRearId, int rightFrontId, int rightRearId){

		frontLeftMotor = new CANTalon(leftFrontId);
		frontLeftMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		frontLeftMotor.reverseSensor(true);
		frontLeftMotor.changeControlMode(TalonControlMode.PercentVbus);
		frontLeftMotor.configNominalOutputVoltage(+0.0, -0.0);
		frontLeftMotor.configPeakOutputVoltage(+12.0, -12.0);
		
		rearLeftMotor = new CANTalon(leftRearId);
		rearLeftMotor.changeControlMode(TalonControlMode.Follower);
		rearLeftMotor.set(LF_DRIVE_CAN_ID);
		rearLeftMotor.configNominalOutputVoltage(+0.0, -0.0);
		rearLeftMotor.configPeakOutputVoltage(+12.0, -12.0);
		
		frontRightMotor = new CANTalon(rightFrontId);
		frontRightMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		frontLeftMotor.reverseSensor(false);
		frontRightMotor.changeControlMode(TalonControlMode.PercentVbus);
		frontRightMotor.configNominalOutputVoltage(+0.0, -0.0);
		frontRightMotor.configPeakOutputVoltage(+12.0, -12.0);
		
		rearRightMotor = new CANTalon(rightRearId);
		rearRightMotor.changeControlMode(TalonControlMode.Follower);
		rearRightMotor.set(RF_DRIVE_CAN_ID);
		rearRightMotor.configNominalOutputVoltage(+0.0, -0.0);
		rearRightMotor.configPeakOutputVoltage(+12.0, -12.0);

		drive = new RobotDrive(frontLeftMotor, frontRightMotor);
		drive.setExpiration(0.1);
	}
	
	public void setReversed(boolean reverse){
		if(reverse){
			throttleScale = -Math.abs(throttleScale);
			turnScale = -Math.abs(turnScale);
		}else{
			throttleScale = Math.abs(throttleScale);
			turnScale = Math.abs(turnScale);
		}
	}
	
	public boolean getReversed(){
		return throttleScale < 0; 
	}
	
	public void drive(double throttle, double turn){
		drive.arcadeDrive(throttle*throttleScale, turn*turnScale);
	}
	
	public void encoderDistance(double speed, double distance){
		final int counts = (int) (distance * COUNTS_PER_INCH); // Number of encoder counts to move
		
		final double acc_step = speed / ACC_DISTANCE; // The amount to change speed by every encoder tick while accelerating / decelerating
		
		final int end_acc_counts = counts - ACC_DISTANCE;
		
		frontLeftMotor.setEncPosition(0);
		frontRightMotor.setEncPosition(0);
		
		boolean leftDone = false;
		boolean rightDone = false;
		
		double leftSpeed = 0;
		double rightSpeed = 0;
		
		while(!(leftDone && rightDone)){
			
			int leftPos = frontLeftMotor.getEncPosition();
			
			if(leftPos < counts){
				if(leftPos < ACC_DISTANCE){
					// Still accellerating
					frontLeftMotor.set((leftSpeed += acc_step)*throttleScale);
				}else if(leftPos > end_acc_counts){
					// Almost there, need to deccelerate
					frontLeftMotor.set(Math.max(leftSpeed -= acc_step, DECCEL_MIN_SPEED)*throttleScale);
				}else{
					// Not near an end, go at top speed
					frontLeftMotor.set(speed*throttleScale);
				}
			}else{
				// We got there!
				frontLeftMotor.set(0);
				leftDone = true;
				leftSpeed = 0;
			}


			int rightPos = frontRightMotor.getEncPosition();
			
			if(rightPos < counts){
				if(rightPos < ACC_DISTANCE){
					// Still accellerating
					frontRightMotor.set((rightSpeed += acc_step)*throttleScale);
				}else if(rightPos > end_acc_counts){
					// Almost there, need to deccelerate
					frontRightMotor.set(Math.max(rightSpeed -= acc_step, DECCEL_MIN_SPEED)*throttleScale);
				}else{
					// Not near an end, go at top speed
					frontRightMotor.set(speed*throttleScale);
				}
			}else{
				// We got there!
				frontRightMotor.set(0);
				rightDone = true;
				rightSpeed = 0;
			}
			
			Timer.delay(0.05);
			System.out.println(leftPos + ":" + rightPos);
		}
	}
}
