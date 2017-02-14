package org.usfirst.frc.team1279.robot;

public interface Constants
{
   // CAN IDs
   public final int LF_DRIVE_CAN_ID    = 3;
   public final int LR_DRIVE_CAN_ID    = 4;
   public final int RF_DRIVE_CAN_ID    = 1;
   public final int RR_DRIVE_CAN_ID    = 2;
   public final int CLAW_CAN_ID        = 7;
   public final int L_CLAW_LIFT_CAN_ID = 5;
   public final int R_CLAW_LIFT_CAN_ID = 6;
   public final int CLIMBER_CAN_ID     = 8;

   // DriveTrain Xbox buttons - port 0
   public final int REVERSE_BTN_ID     = 1; // A button
   public final int L_BMPER_BTN_ID     = 5;
   public final int R_BMPER_BTN_ID     = 6;

   // Controls Xbox buttons - port 1
   public final int OPEN_CLAW_BTN      = 3; // X button
   public final int CLOSE_CLAW_BTN     = 1; // A button
   public final int RAISE_CLAW_BTN     = 4; // Y button
   public final int LOWER_CLAW_BTN     = 2; // B button
   public final int RUN_CLIMBER_BTN    = 5; // left button
   public final int KILL_CLIMBER_BTN   = 8; // start
   public final int MANUAL_OVERIDE_BTN = 7; // select (back is for nerds)

   // breakout box buttons
   // public final int OPEN_CLAW_BTN = 3; // blue button
   // public final int CLOSE_CLAW_BTN = 5; // green button
   // public final int RAISE_CLAW_BTN = 4; // white button
   // public final int LOWER_CLAW_BTN = 6; // black button
   // public final int RUN_CLIMBER_BTN = 10; // top left toggle switch
   // public final int KILL_CLIMBER_BTN = 8; // btm left toggle switch
   // public final int MANUAL_OVERIDE_BTN = 1; // top right toggle switch
}
