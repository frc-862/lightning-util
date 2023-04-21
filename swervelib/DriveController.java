package frc.thunder.swervelib;

public interface DriveController {
    void setReferenceSpeed(double speedMetersPerSecond);

    double getStateVelocity();

    double getStatePosition();

    double getVoltage();

    double getTemperature();

    double getAmperage();

    void setCurrentLimit(int amperage);
}
