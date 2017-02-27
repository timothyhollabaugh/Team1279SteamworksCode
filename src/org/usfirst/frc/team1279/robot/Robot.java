package org.usfirst.frc.team1279.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;
//import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Timer;
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

	DigitalInput testInput = new DigitalInput(9);
	boolean test = false;

	//AHRS navx;

	boolean lastReverse = false;

	// NetworkTable table;

	public Robot() {
		// NOTE: All CAN channel and button IDs are defined in the Constants
		// interface. (Yeah, its not a true Java interface, but the construct
		// works...)
	}

	@Override
	public void robotInit() {
		SmartDashboard.putNumber("DB/Slider 0", 3.2);
		
		if(!testInput.get()) {
			test = true;
			SmartDashboard.putString("DB/String 5", "TEST ROBOT MODE");
		}else{
			SmartDashboard.putString("DB/String 5", "REAL ROBOT MODE");
		}
		
		if(!test){
			drive = new TalonDriveTrain(LF_DRIVE_CAN_ID, LR_DRIVE_CAN_ID, RF_DRIVE_CAN_ID, RR_DRIVE_CAN_ID);
			claw = new GearClaw(CLAW_CAN_ID);
			gearLift = new GearLift(claw, L_CLAW_LIFT_CAN_ID, R_CLAW_LIFT_CAN_ID);
			climber = new Climber(CLIMBER_CAN_ID);
		}else{
			drive = new TestDriveTrain(0, 1);
		}

		drvrStick = new Joystick(0);
		ctrlStick = new Joystick(1);

		vision = new Vision();
		vision.init();

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

		for (int i = 0; i < 5; i++) {
			dash = SmartDashboard.getString("DB/String " + Integer.toString(i), "b").toLowerCase();
			if (dash.length() > 0)
				break;
		}

		System.out.println(dash);

		if (dash.contains("base")) {
			dash = "b";
		} else if (dash.contains("gear")) {
			dash = "g";
		}

		switch (dash) {
		case "b": // Baseline
			drive.drive.setSafetyEnabled(false);
			drive.setReversed(true);
			drive.encoderDistance(0.2, 55, null);
			break;

		case "g": // Gear
			drive.drive.setSafetyEnabled(false);

			drive.setReversed(true);

			vision.setProcess(Vision.GEAR_CONTINUOS_PROCESSING);

			drive.encoderDistance(0.2, 50, vision);

			while (vision.getTurn() > Vision.TURN_ERROR) {
				drive.drive(0, vision.getTurn());
			}

			drive.encoderDistance(0.1, 12, vision);

			Timer.delay(2);

			drive.drive(-0.1, 0);
			Timer.delay(2);
			drive.drive(0, 0);
		}

		/*
		 * // if right turn if (dash.toLowerCase().equals("r")) {
		 * myRobot.setSafetyEnabled(false); // advance 10" (TBD) at 10% throttle
		 * myRobot.autoDistance(.10, 10); myRobot.drive(0.2, 0.25); // right
		 * turn at 20% throttle myRobot.drive(0.0, 0.0); // stop robot } // else
		 * if left turn else if (dash.toLowerCase().equals("l")) {
		 * myRobot.setSafetyEnabled(false); // advance 10" (TBD) at 10% throttle
		 * myRobot.autoDistance(.10, 10); myRobot.drive(0.2, -0.25); // left
		 * turn at 20% throttle myRobot.drive(0.0, 0.0); // stop robot } // else
		 * if straight or center else if ((dash.toLowerCase().equals("s")) ||
		 * (dash.toLowerCase().equals("c"))) { myRobot.setSafetyEnabled(false);
		 * // advance 8" (TBD) at 10% throttle myRobot.autoDistance(.10, 8);
		 * myRobot.drive(0.0, 0.0); // stop robot }
		 * 
		 * Timer.delay(2.0); // for 2 seconds
		 * 
		 * // initiate targeting where: // throttle: [+] forwards [-] backwards
		 * // rotation: [+] right [-] left double throttle = 0.1; double
		 * rotation = 0; while ((throttle != 0) && (rotation != 0) &&
		 * (isAutonomous() && isEnabled())) { myRobot.arcadeDrive(throttle,
		 * rotation);
		 * 
		 * // throttle = table.getNumber("throttle", 0); // rotation =
		 * table.getNumber("rotation", 0); System.out.println("throttle: " +
		 * throttle + " rotation:" + rotation); }
		 * 
		 * myRobot.drive(0.0, 0.0); // stop robot
		 *
		 */
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

		while (isOperatorControl() && isEnabled()) {
			double startTime = Timer.getFPGATimestamp();

			// Drive train controls
			if (drvrStick.getRawButton(REVERSE_BTN_ID)) {
				System.out.println("REVERSE BTN");
				// myRobot.reverseDirection();

				vision.flipCamera();
				drive.setReversed(!drive.getReversed());
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
			if(!test){
				if (ctrlStick.getRawButton(OPEN_CLAW_BTN)) {
					System.out.println("OPEN CLAW");
					claw.openClaw();
				}

				if (ctrlStick.getRawButton(CLOSE_CLAW_BTN)) {
					System.out.println("CLOSE CLAW");
					claw.closeClaw();
				}

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
						gearLift.driveGear(ctrlStick.getRawAxis(RUN_GEAR_LIFT_AXIS));
					}
				}

				// call the periodic claw control loop
				claw.periodic();
			}

			// Climber Controls
			if(!test){
				if (ctrlStick.getRawButton(RUN_CLIMBER_BTN) || ctrlStick.getRawAxis(RUN_CLIMBER_AXIS) > 0.1) {

					if (ctrlStick.getRawButton(RUN_CLIMBER_BTN)) {
						System.out.println("CLIMB BTN");
						climber.drive(.1);
					}

					if (ctrlStick.getRawAxis(RUN_CLIMBER_AXIS) > 0.1) // right trigger
					{
						System.out.println("CLIMB R TRIGGER");
						climber.drive(ctrlStick.getRawAxis(RUN_CLIMBER_AXIS));
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

		vision.setCamera(Vision.PI_CAMERA);
		vision.setProcess(Vision.GEAR_CONTINUOS_PROCESSING);

		while(isTest() && isEnabled()){
			drive.drive.arcadeDrive(0, vision.getTurn(), false);
		}

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
