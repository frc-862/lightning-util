package frc.thunder.swervelib;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;

public class SwerveModuleFactory<DriveConfiguration, SteerConfiguration> {
    private final ModuleConfiguration moduleConfiguration;
    private final DriveControllerFactory<?, DriveConfiguration> driveControllerFactory;
    private final SteerControllerFactory<?, SteerConfiguration> steerControllerFactory;

    public SwerveModuleFactory(ModuleConfiguration moduleConfiguration,
            DriveControllerFactory<?, DriveConfiguration> driveControllerFactory,
            SteerControllerFactory<?, SteerConfiguration> steerControllerFactory) {
        this.moduleConfiguration = moduleConfiguration;
        this.driveControllerFactory = driveControllerFactory;
        this.steerControllerFactory = steerControllerFactory;
    }

    public SwerveModule create(DriveConfiguration driveConfiguration,
            SteerConfiguration steerConfiguration) {
        var driveController =
                driveControllerFactory.create(driveConfiguration, moduleConfiguration);
        var steerController =
                steerControllerFactory.create(steerConfiguration, moduleConfiguration);

        return new ModuleImplementation(driveController, steerController);
    }

    public SwerveModule create(ShuffleboardLayout container, DriveConfiguration driveConfiguration,
            SteerConfiguration steerConfiguration) {
        var driveController =
                driveControllerFactory.create(container, driveConfiguration, moduleConfiguration);
        var steerContainer =
                steerControllerFactory.create(container, steerConfiguration, moduleConfiguration);

        return new ModuleImplementation(driveController, steerContainer);
    }

    private static class ModuleImplementation implements SwerveModule {
        private final DriveController driveController;
        private final SteerController steerController;

        private ModuleImplementation(DriveController driveController,
                SteerController steerController) {
            this.driveController = driveController;
            this.steerController = steerController;
        }

        @Override
        public double getDriveVelocity() {
            return driveController.getStateVelocity();
        }

        @Override
        public double getSteerAngle() {
            return steerController.getStateAngle();
        }

        @Override
        public SwerveModulePosition getPosition() {
            return new SwerveModulePosition(driveController.getStatePosition(),
                    new Rotation2d(getSteerAngle()));
        }

        private static double ensureAngleInRange0To2Pi(double angle) {
            return MathUtil.inputModulus(angle, 0d, Math.PI * 2d);
        }

        private static double ensureAngleInRangeNegPiToPi(double angle) {
            return MathUtil.inputModulus(angle, -Math.PI, Math.PI);
        }

        @Override
        public void set(double speedMetersPerSecond, double steerAngle) {
            steerAngle = ensureAngleInRange0To2Pi(steerAngle);

            double difference = ensureAngleInRangeNegPiToPi(steerAngle - ensureAngleInRange0To2Pi(getSteerAngle()));
            // Change the target angle so the difference is in the range [-pi, pi) instead of [0,
            // 2pi)
            // if (difference >= Math.PI) {
            //     steerAngle -= 2.0 * Math.PI;
            // } else if (difference < -Math.PI) {
            //     steerAngle += 2.0 * Math.PI;
            // }
            // difference = steerAngle - getSteerAngle(); // Recalculate difference



            // If the difference is greater than 90 deg or less than -90 deg the drive can be
            // inverted so the total
            // movement of the module is less than 90 deg
            if (difference > Math.PI / 2.0 || difference < -Math.PI / 2.0) {
                // Only need to add 180 deg here because the target angle will be put back into the
                // range [0, 2pi)
                steerAngle += Math.PI;
                speedMetersPerSecond *= -1.0;
            }

            // Put the target angle back into the range [0, 2pi)
            steerAngle = ensureAngleInRange0To2Pi(steerAngle);

            driveController.setReferenceSpeed(speedMetersPerSecond);
            steerController.setReferenceAngle(steerAngle);
        }

        @Override
        public void setEncoderAngle() {
            steerController.setMotorEncoderAngle();
        }

        @Override
        public double getDriveVoltage() {
            return driveController.getVoltage();
        }

        @Override
        public double getDriveTemperature() {
            return driveController.getTemperature();
        }

        @Override
        public double getSteerTemperature() {
            return steerController.getTemperature();
        }
    }
}
