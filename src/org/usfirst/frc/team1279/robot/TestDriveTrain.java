package org.usfirst.frc.team1279.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TestDriveTrain extends DriveTrain implements Constants {

	VictorSP frontLeftMotor;
	VictorSP frontRightMotor;
	
	double resetTime = 0;

	public TestDriveTrain(int leftFrontId, int rightFrontId) {

		frontLeftMotor = new VictorSP(leftFrontId);

		frontRightMotor = new VictorSP(rightFrontId);

		drive = new RobotDrive(frontLeftMotor, frontRightMotor);
		drive.setExpiration(0.1);
		
		System.out.println("TestDriveTrain: " + leftFrontId + ":" + rightFrontId);
	}
	
	// Because there are no encoders on the test drive base, distance is now the number of seconds to move * 10
	public void encoderDistance(double speed, double distance, Vision vision, double timeout) {
		System.out.println("encoder Distanceing");

		double startTime = Timer.getFPGATimestamp();
		
		while (Timer.getFPGATimestamp() - startTime < distance/10) {

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

			Timer.delay(0.05);
		}

		drive.drive(0, 0);
	}

	@Override
	public void resetEncoders() {
		resetTime = Timer.getFPGATimestamp();
	}

	@Override
	public int getAverageEncoders() {
		// TODO Auto-generated method stub
		return (int)(Timer.getFPGATimestamp() - resetTime)/10;
	}

	@Override
	public void driveUntilDigital(double speed, Vision vision, DigitalInput input, double timeout) {
		// TODO Auto-generated method stub
		
	}
}
