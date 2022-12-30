// package frc.lightningUtil.swervelib.rev;

// import com.revrobotics.RelativeEncoder;
// import com.revrobotics.CANSparkMaxLowLevel.*;
// import com.revrobotics.jni.CANSparkMaxJNI;
// import com.revrobotics.CANSparkMax;
// import com.revrobotics.CANSparkMaxLowLevel;

// import frc.lightningUtil.swervelib.DriveController;
// import frc.lightningUtil.swervelib.DriveControllerFactory;
// import frc.lightningUtil.swervelib.ModuleConfiguration;

// public final class NeoDriveControllerFactoryBuilder {
//     private double nominalVoltage = Double.NaN;
//     private double currentLimit = Double.NaN;

//     public NeoDriveControllerFactoryBuilder withVoltageCompensation(double nominalVoltage) {
//         this.nominalVoltage = nominalVoltage;
//         return this;
//     }

//     public boolean hasVoltageCompensation() {
//         return Double.isFinite(nominalVoltage);
//     }

//     public NeoDriveControllerFactoryBuilder withCurrentLimit(double currentLimit) {
//         this.currentLimit = currentLimit;
//         return this;
//     }

//     public boolean hasCurrentLimit() {
//         return Double.isFinite(currentLimit);
//     }

//     public DriveControllerFactory<ControllerImplementation, Integer> build() {
//         return new FactoryImplementation();
//     }

//     private class FactoryImplementation implements DriveControllerFactory<ControllerImplementation, Integer> {
//         @Override
//         public ControllerImplementation create(Integer id, ModuleConfiguration moduleConfiguration) {
//             CANSparkMax motor = new CANSparkMax(id, MotorType.kBrushless);
//             motor.setInverted(moduleConfiguration.isDriveInverted());

//             // Setup voltage compensation
//             if (hasVoltageCompensation()) {
//                 motor.enableVoltageCompensation(nominalVoltage);
//             }

//             if (hasCurrentLimit()) {
//                 motor.setSmartCurrentLimit((int) currentLimit);
//             }

//             motor.setPeriodicFramePeriod(PeriodicFrame.kStatus0, 100);
//             motor.setPeriodicFramePeriod(PeriodicFrame.kStatus1, 20);
//             motor.setPeriodicFramePeriod(PeriodicFrame.kStatus2, 20);
//             // Set neutral mode to brake
//             motor.setIdleMode(CANSparkMax.IdleMode.kBrake);

//             // Setup encoder
//             RelativeEncoder encoder = motor.getEncoder();
//             double positionConversionFactor = Math.PI * moduleConfiguration.getWheelDiameter()
//                     * moduleConfiguration.getDriveReduction();
//             encoder.setPositionConversionFactor(positionConversionFactor);
//             encoder.setVelocityConversionFactor(positionConversionFactor / 60.0);

//             return new ControllerImplementation(motor, encoder);
//         }
//     }

//     private static class ControllerImplementation implements DriveController {
//         private final CANSparkMax motor;
//         private final RelativeEncoder encoder;

//         private ControllerImplementation(CANSparkMax motor, RelativeEncoder encoder) {
//             this.motor = motor;
//             this.encoder = encoder;
//         }

//         @Override
//         public void setReferenceVoltage(double voltage) {
//             motor.setVoltage(voltage);
//         }

//         @Override
//         public double getStateVelocity() {
//             return encoder.getVelocity();
//         }

//         @Override
//         public double getStatePosition() {
//             return encoder.getPosition();
//         }
//     }
// }
