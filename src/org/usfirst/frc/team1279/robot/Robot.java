package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;
//import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class. The
 * SampleRobot class is the base of a robot application that will automatically
 * call your Autonomous and OperatorControl methods at the right time as
 * controlled by the switches on the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends SampleRobot implements Constants {
	// RobotDrive myRobot;
	DriveTrain drive;
	Joystick drvrStick;
	Joystick ctrlStick;
	GearClaw claw;
	GearLift gearLift;
	Climber climber;
	Vision vision;
	NetworkTable robotTable;
	DigitalInput distance;

	DigitalInput testInput = new DigitalInput(TEST_INPUT_PORT);
	boolean test = false;

	boolean oldReverse = false;

	//AHRS navx;

	boolean lastReverse = false;

	int autoState = 0;

	// NetworkTable table;

	public Robot() {
		// NOTE: All CAN channel and button IDs are defined in the Constants
		// interface. (Yeah, its not a true Java interface, but the construct
		// works...)
	}

	@Override
	public void robotInit() {
		if (!testInput.get()) {
			test = true;
			SmartDashboard.putString("DB/String 5", "TEST ROBOT MODE");
		} else {
			SmartDashboard.putString("DB/String 5", "REAL ROBOT MODE");
		}

		robotTable = NetworkTable.getTable("Robot");

		if (!test) {
			drive = new TalonDriveTrain(LF_DRIVE_CAN_ID, LR_DRIVE_CAN_ID, RF_DRIVE_CAN_ID, RR_DRIVE_CAN_ID);
			claw = new GearClaw(CLAW_CAN_ID, robotTable);
			gearLift = new GearLift(claw, L_CLAW_LIFT_CAN_ID, robotTable);
			climber = new Climber(CLIMBER_CAN_ID);
		} else {
			//drive = new TestDriveTrain(0, 1);
			drive = new TalonDriveTrain(2, 3, 1, 4);
			claw = new GearClaw(1, robotTable);
			gearLift = new GearLift(claw, 2, robotTable);
		}

		drvrStick = new Joystick(0);
		ctrlStick = new Joystick(1);

		vision = new Vision();
		vision.init();

		distance = new DigitalInput(DISTANCE_PORT);

		drive.setReversed(false);
	}

	/**
	 * package org.usfirst.frc.team1279.robot; This autonomous (along with the
	 * chooser code above) shows how to select between different autonomous
	 * modes using the dashboard. The sendable chooser code works with the Java
	 * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the
	 * chooser code and uncomment the getString line to get the auto name from
	 * the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * if-else structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomous() {

		vision.setCamera(Vision.PI_CAMERA);

		String dash = "";

		/*
		for (int i = 0; i < 5; i++) {
			dash = SmartDashboard.getString("DB/String " + Integer.toString(i), "").toLowerCase();
			if (dash.length() > 0)
				break;
		}
		*/

		if (robotTable.containsKey("automode")) {
			dash = robotTable.getString("automode", "b").toLowerCase();
		}

		System.out.println(dash);

		if (dash.contains("base")) {
			dash = "b";
		} else if (dash.contains("gear")) {
			dash = "g";
		}

		System.out.println(dash);
		/*
				double startTime = Timer.getFPGATimestamp();
				double now = startTime;
		
				while (isAutonomous() && (now - startTime < 15)) {
					now = Timer.getFPGATimestamp();
					System.out.println(autoState);
					System.out.println(drive.getAverageEncoders());
					robotTable.putNumber("autoState", autoState);
		
					switch (dash) {
		
					case "b": // Baseline Auto
		
						switch(autoState){
						case 0:
							drive.resetEncoders();
							autoState = 1;
							break;
						case 1:
							if(drive.getAverageEncoders() < 72){
								drive.drive(0.2, 0);
							}else{
								autoState = 2;
							}
							break;
						case 2:
							drive.drive(0, 0);
						}
						break;
						
					case "g": // Middle Gear
						
						switch (autoState){
						case 0: // Reset state
							drive.resetEncoders();
							autoState = 1;
							break;
						case 1: // Drive to peg
							if(drive.getAverageEncoders() < 60){
								if(vision.getLock()){
									drive.drive(0.2, vision.getTurn());
								}else{
									drive.drive(0.2, 0);
								}
							}else{
								autoState = 2;
							}
							break;
						case 2: // Final line up
							double turn = vision.getTurn();
							if(Math.abs(turn) > VISION_MAX_TURN){
								drive.drive(0, turn);
							}else{
								autoState = 3;
							}
							break;
						case 3:
							drive.resetEncoders();
							autoState = 4;
							break;
						case 4:
							if(drive.getAverageEncoders() < 12){
								drive.drive(0.15, 0);
							}else{
								autoState = 5;
							}
							break;
						case 5:
							drive.drive(0, 0);
						}
						break;
					}
				}
				*/

		switch (dash) {
		case "b": // Baseline
			System.out.println("Baseline");
			drive.drive.setSafetyEnabled(false);
			drive.setReversed(true);
			drive.encoderDistance(0.15, 72, null, 10);

			break;

		case "g": // Gear
			drive.drive.setSafetyEnabled(false);

			drive.setReversed(true);

			vision.setProcess(Vision.GEAR_CONTINUOS_PROCESSING);

			//drive.encoderDistance(0.2, 60, vision, 10);
			
			drive.driveUntilDigital(0.2, null, distance, 10);

			//double time = Timer.getFPGATimestamp();

			//while(vision.getDistance() < 93){
			//	double turn = vision.getTurn();
			//	if(turn > VISION_MAX_TURN){
			//		turn = VISION_MAX_TURN;
			//	}else if(turn < -VISION_MAX_TURN){
			//		turn = -VISION_MAX_TURN;
			//	}

			//	System.out.println(turn);
			//
			//	drive.drive.arcadeDrive(-0.1, turn, false);
			//}

			//Timer.delay(1);
			double startTime = Timer.getFPGATimestamp();
			while ((Timer.getFPGATimestamp() - startTime < 2) && isAutonomous() && Math.abs(vision.getTurn()) > Vision.TURN_ERROR) {
				double turn = vision.getTurn();
				if (turn > VISION_MAX_TURN) {
					turn = VISION_MAX_TURN;
				} else if (turn < -VISION_MAX_TURN) {
					turn = -VISION_MAX_TURN;
				}

				System.out.println(turn);

				drive.drive.arcadeDrive(0, turn, false);
			}

			//Timer.delay(1);

			//drive.encoderDistance(0.1, 12, null, 2);

			//Timer.delay(2);

			//drive.drive(-0.2, 0);
			//Timer.delay(2);
			//drive.drive(0, 0);
		}

	}

	/**
	 * Runs the motors with tank steering.
	 */
	@Override
	public void operatorControl() {
		drive.drive.setSafetyEnabled(true);

		// reverse initial direction
		drive.setReversed(false);

		vision.setCamera(Vision.PI_CAMERA);
		vision.setProcess(Vision.NO_PROCESSING);

		claw.openClaw();

		while (isOperatorControl() && isEnabled()) {
			double startTime = Timer.getFPGATimestamp();

			// Drive train controls
			boolean reverse = drvrStick.getRawButton(REVERSE_BTN_ID);

			if (reverse != oldReverse) {
				if (reverse) {
					if (drvrStick.getRawButton(REVERSE_BTN_ID)) {
						System.out.println("REVERSE BTN");
						// myRobot.reverseDirection();

						vision.flipCamera();
						drive.setReversed(!drive.getReversed());
					}
				}
				oldReverse = reverse;
			}

			if (drvrStick.getRawButton(L_BMPER_BTN_ID)) {
				System.out.println("L BUMPER BTN");
				// myRobot.arcadeDrive(0, -0.4);
				drive.drive(0, -0.5);
			}

			if (drvrStick.getRawButton(R_BMPER_BTN_ID)) {
				System.out.println("R BUMPER BTN");
				// myRobot.arcadeDrive(0, 0.4);
				drive.drive(0, 0.5);
			}

			if (drvrStick.getRawAxis(2) > 0.1) {
				System.out.println("L TRIGGER");
				// myRobot.arcadeDrive(0, -1 * drvrStick.getRawAxis(2));
				drive.drive(0, -1 * drvrStick.getRawAxis(2));
			}

			if (drvrStick.getRawAxis(3) > 0.1) {
				System.out.println("R TRIGGER");
				// myRobot.arcadeDrive(0, drvrStick.getRawAxis(3));
				drive.drive(0, drvrStick.getRawAxis(3));
			}

			// myRobot.arcadeDrive(drvrStick);
			// myRobot.tankDrive(drvrStick.getRawAxis(1),
			// drvrStick.getRawAxis(5));
			drive.drive(drvrStick.getRawAxis(5), drvrStick.getRawAxis(0));

			// Claw controls
			if (claw != null) {
				if (ctrlStick.getRawButton(OPEN_CLAW_BTN)) {
					System.out.println("OPEN CLAW");
					claw.openClaw();
				} else if (ctrlStick.getRawButton(CLOSE_CLAW_BTN)) {
					System.out.println("CLOSE CLAW");
					claw.closeClaw();
				} else {
					//claw.auto();
				}
				// call the periodic claw control loop
				claw.periodic();
			}

			if (gearLift != null) {
				if (ctrlStick.getRawButton(RAISE_CLAW_BTN) || ctrlStick.getRawButton(LOWER_CLAW_BTN)) {
					if (ctrlStick.getRawButton(RAISE_CLAW_BTN)) {
						System.out.println("RAISE GEAR BTN");
						gearLift.raiseGear();
					}

					if (ctrlStick.getRawButton(LOWER_CLAW_BTN)) {
						System.out.println("LOWER GEAR BTN");
						gearLift.lowerGear();
					}
				} else {
					if (Math.abs(ctrlStick.getRawAxis(RUN_GEAR_LIFT_AXIS)) > 0.1) {
						System.out.println("Running gear lift");
						gearLift.driveGear(-ctrlStick.getRawAxis(RUN_GEAR_LIFT_AXIS));
					} else {
						gearLift.stopGear();
					}
				}
				gearLift.periodic();
			}

			// Climber Controls
			if (climber != null) {
				if (ctrlStick.getRawButton(RUN_CLIMBER_BTN) || ctrlStick.getRawAxis(RUN_CLIMBER_AXIS) > 0.1 || ctrlStick.getRawAxis(RUN_CLIMBER_SLOW_AXIS) > 0.1) {

					if (ctrlStick.getRawButton(RUN_CLIMBER_BTN)) {
						System.out.println("CLIMB BTN");
						climber.drive(0.2);
					}

					if (ctrlStick.getRawAxis(RUN_CLIMBER_AXIS) > 0.1) // right trigger
					{
						System.out.println("CLIMB R TRIGGER");
						climber.drive(ctrlStick.getRawAxis(RUN_CLIMBER_AXIS));
					} else if (ctrlStick.getRawAxis(RUN_CLIMBER_SLOW_AXIS) > 0.1) {
						System.out.println("CLIMB L TRIGGER");
						climber.drive(ctrlStick.getRawAxis(RUN_CLIMBER_SLOW_AXIS) / 2);
					}

				} else {
					climber.stop();
				}

				if (ctrlStick.getRawButton(KILL_CLIMBER_BTN)) {
					System.out.println("KILL BTN");
					climber.setShutDown();
				}

				if (ctrlStick.getRawButton(MANUAL_OVERIDE_BTN)) {
					System.out.println("MANOVRD BTN");
					climber.setOverride();
				}
			}

			// adjust polling on 20 ms intervals (was 5 mSec in template)
			double endTime = Timer.getFPGATimestamp();
			double deltaTime = endTime - startTime;

			if (deltaTime <= 0.020)
				Timer.delay(0.020 - deltaTime);

			/*
			 * System.out.println(Timer.getFPGATimestamp() + ": " + "X[" +
			 * stick.getY() + "]" + "Y[" + stick.getRawAxis(3) + "]");
			 */
		}
	}

	/**
	 * Runs during test mode
	 */
	@Override
	public void test() {

		DigitalInput input = new DigitalInput(1);
		while (isTest() && isEnabled()) {
			robotTable.putBoolean("distance", input.get());
		}

		/*
		vision.setCamera(Vision.PI_CAMERA);
		vision.setProcess(Vision.GEAR_CONTINUOS_PROCESSING);
		
		while (isTest() && isEnabled()) {
			double turn = vision.getTurn();
			if (turn > VISION_MAX_TURN) {
				turn = VISION_MAX_TURN;
			} else if (turn < -VISION_MAX_TURN) {
				turn = -VISION_MAX_TURN;
			}
		
			System.out.println(turn);
		
			drive.drive.arcadeDrive(0, turn, false);
		}
		*/

		/*
		 * TalonTest leftLift = new TalonTest(5); TalonTest rightLift = new
		 * TalonTest(6);
		 * 
		 * while (isTest() && isEnabled()) { // set throttle per top rotary
		 * switch // double throttle = (panel.getRawAxis(0) + 1.0) / 2;
		 * 
		 * // set throttle per XBox left trigger // double throttle =
		 * stick.getRawAxis(2);
		 * 
		 * // set throttle per XBox left stick double throttle =
		 * drvrStick.getRawAxis(0);
		 * 
		 * leftLift.drive(throttle); rightLift.drive(throttle); // int distance
		 * = can1.getDistance(); // System.out.println( //
		 * Timer.getFPGATimestamp() + ": throttle: " + throttle + " distance:"
		 * // + distance);
		 * 
		 * Timer.delay(0.020); // was 5 mSec }
		 */
	}
}
