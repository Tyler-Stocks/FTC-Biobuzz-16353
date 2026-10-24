package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(group = "Test")
public class FlippyPositionTester extends OpMode {
    private DcMotorEx flippyMotor;

    @Override public void init() {
        flippyMotor = hardwareMap.get(DcMotorEx.class, "flippyMotor");
    }

    @Override public void loop() {
        flippyMotor.setPower(gamepad1.left_stick_y);
        telemetry.addLine("Position: " + flippyMotor.getCurrentPosition());

        if (gamepad1.triangle) {
            flippyMotor.setPower(0.0);
            flippyMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            flippyMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }
}
