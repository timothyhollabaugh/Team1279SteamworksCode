package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TalonDriveTrain extends DriveTrain implements Constants {

	CANTalon frontLeftMotor;
	CANTalon frontRightMotor;
	CANTalon rearLeftMotor;
	CANTalon rearRightMotor;


	public TalonDriveTrain(int leftFrontId, int leftRearId, int rightFrontId, int rightRearId) {

		//this.navx = navx;

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

		
		System.out.println("TalonDriveTrain: " + leftFrontId + ":" + rightFrontId);
	}
	
	public TalonDriveTrain(int leftFrontId, int rightFrontId) {

		//this.navx = navx;

		frontLeftMotor = new CANTalon(leftFrontId);
		frontLeftMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		frontLeftMotor.reverseSensor(false);
		frontLeftMotor.changeControlMode(TalonControlMode.PercentVbus);
		frontLeftMotor.configNominalOutputVoltage(+0.0, -0.0);
		frontLeftMotor.configPeakOutputVoltage(+12.0, -12.0);
		//frontLeftMotor.reverseOutput(false);

		frontRightMotor = new CANTalon(rightFrontId);
		frontRightMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		frontRightMotor.reverseSensor(false);
		frontRightMotor.changeControlMode(TalonControlMode.PercentVbus);
		frontRightMotor.configNominalOutputVoltage(+0.0, -0.0);
		frontRightMotor.configPeakOutputVoltage(+12.0, -12.0);
		//frontRightMotor.reverseOutput(true);

		drive = new RobotDrive(frontLeftMotor, frontRightMotor);
		drive.setExpiration(0.1);

		frontLeftMotor.setEncPosition(0);
		frontRightMotor.setEncPosition(0);

		
		System.out.println("TalonDriveTrain: " + leftFrontId + ":" + rightFrontId);
	}

	public void resetEncoders(){
		frontRightMotor.setEncPosition(0);
		frontLeftMotor.setEncPosition(0);
		if(rearRightMotor != null){
			rearRightMotor.setEncPosition(0);
		}
		if(rearLeftMotor != null){
			rearLeftMotor.setEncPosition(0);
		}
	}
	
	public int getAverageEncoders(){
		return (int)((frontRightMotor.getEncPosition() + -frontLeftMotor.getEncPosition())/2.0);
	}
	
	public void driveUntilDigital(double speed, Vision vision, DigitalInput input, double timeout){
		System.out.println("Driving until");
		double startTime = Timer.getFPGATimestamp();
		
		while((Timer.getFPGATimestamp() - startTime < timeout) && input.get()){
			System.out.println(input.get());
			
			double turn = 0;
			
			if(vision != null){
				turn = vision.getTurn();
				if(turn > VISION_MAX_TURN){
					turn = VISION_MAX_TURN;
				}else if(turn < -VISION_MAX_TURN){
					turn = -VISION_MAX_TURN;
				}
			}
			drive.drive(speed * throttleScale, turn);
		}
		drive.drive(0, 0);
		System.out.println("Done driving until");
	}

	public void encoderDistance(double speed, double distance, Vision vision, double timeout) {
		System.out.println("encoder Distanceing");
		int counts = (int) (distance * COUNTS_PER_INCH); // Number of encoder counts to move

		frontLeftMotor.setEncPosition(0);
		frontRightMotor.setEncPosition(0);

		SmartDashboard.putString("DB/String 7", Double.toString(counts));

		int leftPos = (int) throttleScale * frontLeftMotor.getEncPosition();
		int rightPos = (int) -throttleScale * frontRightMotor.getEncPosition();

		int averagePos = (int) (rightPos + leftPos) / 2;
		
		double startTime = Timer.getFPGATimestamp();
		
		while ((Timer.getFPGATimestamp() - startTime < timeout) && averagePos < counts) {

			leftPos = (int) throttleScale * frontLeftMotor.getEncPosition();
			rightPos = (int) -throttleScale * frontRightMotor.getEncPosition();

			averagePos = (int) (rightPos + leftPos) / 2;

			double turn = 0;
			
			if(vision != null){
				turn = vision.getTurn();
				if(turn > VISION_MAX_TURN){
					turn = VISION_MAX_TURN;
				}else if(turn < -VISION_MAX_TURN){
					turn = -VISION_MAX_TURN;
				}
			}

			drive.drive(speed * throttleScale, turn);

			SmartDashboard.putString("DB/String 3", Double.toString(-throttleScale * frontLeftMotor.get()));

			SmartDashboard.putString("DB/String 8", Double.toString(throttleScale * frontRightMotor.get()));

			SmartDashboard.putString("DB/String 4", Double.toString(throttleScale * frontLeftMotor.getEncPosition()));

			SmartDashboard.putString("DB/String 9", Double.toString(-throttleScale * frontRightMotor.getEncPosition()));

			//System.out.println(leftPos + ":" + rightPos);

			Timer.delay(0.05);
		}

		drive.drive(0, 0);
		
		System.out.println("Done encodering");
	}
}
