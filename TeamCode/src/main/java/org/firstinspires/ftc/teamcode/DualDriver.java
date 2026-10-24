package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(group = "$")
public class DualDriver extends OpMode {
    private DcMotorEx flywheelMotorOne,
            flywheelMotorTwo,
            intakeMotor,
            flippyMotor;

    private RevTouchSensor flippylimitSwitch;

    private Follower follower;

    @Override public void init() {
        follower = Constants.create(hardwareMap);

        flippylimitSwitch = hardwareMap.get(RevTouchSensor.class, "flippyLimitSwitch");

        flywheelMotorOne = hardwareMap.get(DcMotorEx.class, "flywheelMotorOne");
        flywheelMotorOne.setDirection(DcMotorSimple.Direction.REVERSE);

        flywheelMotorTwo = hardwareMap.get(DcMotorEx.class, "flywheelMotorTwo");

        intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        flippyMotor = hardwareMap.get(DcMotorEx.class, "flippyMotor");
        flippyMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        flippyMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flippyMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    private static final double NEAR_RPM = 1200;
    private static final double FAR_RPM  = 1800;

    private static final int UP_POSITION   = 420;
    private static final int DOWN_POSITION = 5;

    private double currentRpm = FAR_RPM;

    private boolean shooterOn = false;

    private TransferState state = TransferState.HOMING;

    enum TransferState {
        HOMING,
        UP,
        DOWN
    }

    @Override public void loop() {
        follower.manual(
                ManualDrive.fieldCentric(
                        -gamepad1.left_stick_y,
                        -gamepad1.left_stick_x,
                        -gamepad1.right_stick_x,
                        follower.pose().heading()
                )
        );
        follower.update();

        if (gamepad1.options) {
            follower.localizer.setHeading(0.0);
        }

        if (gamepad2.left_bumper && state != TransferState.HOMING) {
            state = TransferState.DOWN;
        }

        if (gamepad2.right_bumper && state != TransferState.HOMING) {
            state = TransferState.UP;
        }

        if (gamepad2.share) {
            state = TransferState.HOMING;
        }

        switch (state) {
            case HOMING:
                flippyMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                flippyMotor.setPower(-0.4);
                if (flippylimitSwitch.isPressed()) {
                    flippyMotor.setPower(0.0);
                    flippyMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    flippyMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                    state = TransferState.DOWN;
                }
                break;
            case DOWN:
                flippyMotor.setTargetPosition(DOWN_POSITION);
                flippyMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                flippyMotor.setPower(0.8);
                break;
            case UP:
                flippyMotor.setTargetPosition(UP_POSITION);
                flippyMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                flippyMotor.setPower(0.8);
                break;
        }

        if (gamepad2.dpad_down) {
            shooterOn = false;
        }
        if (gamepad2.dpad_up) {
            shooterOn = true;
        }

        if (gamepad2.triangle) {
            currentRpm = NEAR_RPM;
        } else if (gamepad2.cross) {
            currentRpm = FAR_RPM;
        }

        if (shooterOn) {
            flywheelMotorOne.setVelocity(currentRpm, AngleUnit.DEGREES);
            flywheelMotorTwo.setVelocity(currentRpm, AngleUnit.DEGREES);
        } else {
            flywheelMotorOne.setPower(0.0);
            flywheelMotorTwo.setPower(0.0);
        }

        double power = gamepad2.left_trigger - gamepad2.right_trigger;

        if (flippyMotor.getCurrentPosition() > 300) {
            intakeMotor.setPower(Math.max(0.0, power));
        } else {
            intakeMotor.setPower(power);
        }

        displayDebugInfo();
    }

    private void displayDebugInfo() {
        telemetry.addLine("Target RPM: " + currentRpm);
        telemetry.addLine("Actual RPM: " + flywheelMotorOne.getVelocity());

        if (shooterOn) {
            if (currentRpm == NEAR_RPM) {
                telemetry.addLine("Near Mode");
            } else if (currentRpm == FAR_RPM) {
                telemetry.addLine("Far Mode");
            }
        } else {
            telemetry.addLine("Shooter Off");
        }

        telemetry.addLine("Flipper Position: " + flippyMotor.getCurrentPosition());
        telemetry.addLine("Limit Switch Pressed: " + flippylimitSwitch.isPressed());
        telemetry.addLine("State: " + state);
    }
}