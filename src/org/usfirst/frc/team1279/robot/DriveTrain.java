package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DriveTrain implements Constants{

	CANTalon frontLeftMotor;
	CANTalon frontRightMotor;
	CANTalon rearLeftMotor;
	CANTalon rearRightMotor;
	
	public RobotDrive drive;
	
	double throttleScale = 1;
	double turnScale = -1;
	
	public DriveTrain(int leftFrontId, int leftRearId, int rightFrontId, int rightRearId){

		frontLeftMotor = new CANTalon(leftFrontId);
		frontLeftMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		frontLeftMotor.reverseSensor(false);
		frontLeftMotor.changeControlMode(TalonControlMode.PercentVbus);
		frontLeftMotor.configNominalOutputVoltage(+0.0, -0.0);
		frontLeftMotor.configPeakOutputVoltage(+12.0, -12.0);
		//frontLeftMotor.reverseOutput(false);
		
		rearLeftMotor = new CANTalon(leftRearId);
		rearLeftMotor.changeControlMode(TalonControlMode.Follower);
		rearLeftMotor.set(LF_DRIVE_CAN_ID);
		rearLeftMotor.configNominalOutputVoltage(+0.0, -0.0);
		rearLeftMotor.configPeakOutputVoltage(+12.0, -12.0);
		//rearLeftMotor.reverseOutput(false);
		
		frontRightMotor = new CANTalon(rightFrontId);
		frontRightMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		frontRightMotor.reverseSensor(false);
		frontRightMotor.changeControlMode(TalonControlMode.PercentVbus);
		frontRightMotor.configNominalOutputVoltage(+0.0, -0.0);
		frontRightMotor.configPeakOutputVoltage(+12.0, -12.0);
		//frontRightMotor.reverseOutput(true);
		
		rearRightMotor = new CANTalon(rightRearId);
		rearRightMotor.changeControlMode(TalonControlMode.Follower);
		rearRightMotor.set(RF_DRIVE_CAN_ID);
		rearRightMotor.configNominalOutputVoltage(+0.0, -0.0);
		rearRightMotor.configPeakOutputVoltage(+12.0, -12.0);
		//rearRightMotor.reverseOutput(false);

		drive = new RobotDrive(frontLeftMotor, frontRightMotor);
		drive.setExpiration(0.1);

		frontLeftMotor.setEncPosition(0);
		frontRightMotor.setEncPosition(0);
	}
	
	public void setReversed(boolean reverse){
		if(reverse){
			throttleScale = -Math.abs(throttleScale);
		}else{
			throttleScale = Math.abs(throttleScale);
		}
	}
	
	public boolean getReversed(){
		return throttleScale < 0; 
	}
	
	public void drive(double throttle, double turn){
		drive.arcadeDrive(throttle*throttleScale, turn*turnScale);

		SmartDashboard.putString("DB/String 4",
				Double.toString(-throttleScale * frontLeftMotor.getEncPosition()));

		SmartDashboard.putString("DB/String 9",
				Double.toString(throttleScale * frontRightMotor.getEncPosition()));
	}
	
	public void encoderDistance(double speed, double distance){
		System.out.println("encoder Distanceing");
		final int counts = (int) (distance * COUNTS_PER_INCH); // Number of encoder counts to move
		
		final double acc_step = speed / ACC_DISTANCE; // The amount to change speed by every encoder tick while accelerating / decelerating
		
		final int end_acc_counts = counts - ACC_DISTANCE;
		
		frontLeftMotor.setEncPosition(0);
		frontRightMotor.setEncPosition(0);
		
		boolean leftDone = false;
		boolean rightDone = false;
		
		double leftSpeed = 0;
		double rightSpeed = 0;
		

		SmartDashboard.putString("DB/String 7",
				Double.toString(counts));
		
		while(!(leftDone && rightDone)){
			
			int leftPos = (int) throttleScale * frontLeftMotor.getEncPosition();
			
			if(leftPos < counts){
				if(leftPos < ACC_DISTANCE){
					// Still accellerating
					leftSpeed += acc_step;
				}else if(leftPos > end_acc_counts){
					// Almost there, need to deccelerate
					leftSpeed -= acc_step;
				}else{
					// Not near an end, go at top speed
					leftSpeed = speed;
				}
			}else{
				// We got there!
				leftDone = true;
				leftSpeed = 0;
			}


			int rightPos = (int) -throttleScale * frontRightMotor.getEncPosition();
			
			if(rightPos < counts){
				if(rightPos < ACC_DISTANCE){
					// Still accellerating
					rightSpeed += acc_step;
				}else if(rightPos > end_acc_counts){
					// Almost there, need to deccelerate
					rightSpeed -= acc_step;
				}else{
					// Not near an end, go at top speed
					rightSpeed = speed;
				}
			}else{
				// We got there!
				rightDone = true;
				rightSpeed = 0;
			}
			
			System.out.println("About to drive");
			
			drive.tankDrive(Math.max(leftSpeed, DECCEL_MIN_SPEED) * throttleScale, Math.max(rightSpeed, DECCEL_MIN_SPEED) * throttleScale);


			SmartDashboard.putString("DB/String 3",
					Double.toString(-throttleScale * frontLeftMotor.get()));

			SmartDashboard.putString("DB/String 8",
					Double.toString(throttleScale * frontRightMotor.get()));

			SmartDashboard.putString("DB/String 4",
					Double.toString(throttleScale * frontLeftMotor.getEncPosition()));

			SmartDashboard.putString("DB/String 9",
					Double.toString(-throttleScale * frontRightMotor.getEncPosition()));
			System.out.println(leftPos + ":" + rightPos);

			
			Timer.delay(0.05);
		}
	}
}
