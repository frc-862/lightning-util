package frc.thunder.vision;

import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.VisionConstants;
import frc.thunder.limelightlib.LimelightHelpers;
import frc.thunder.shuffleboard.LightningShuffleboardPeriodic;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;

public class LimelightBase extends SubsystemBase {

    // Change "limelight" to whatever the name of the limelight you are using
    // we should rename the limelight names to something consistent later
    public String limelightName;

    // Periodic Shuffleboard
    private LightningShuffleboardPeriodic periodicShuffleboard;

    // Started logging
    private boolean loggingStarted = false;

    // position of the limelight relative to the center and bottom of the robot
    private Pose3d cameraPose;

    public LimelightBase(String limelightName, Pose3d cameraPose) {
        this.limelightName = limelightName;
        this.cameraPose = cameraPose;

        // Sets the appropriate camera position
        setCameraPose(cameraPose);

        setCameraMode();

        // Registers this as a proper Subsystem
        CommandScheduler.getInstance().registerSubsystem(this);
    }

    // Method to initialize shuffleboard with vision data\
    @SuppressWarnings("unchecked")
    private void initializeShuffleboard() {
        periodicShuffleboard =
                new LightningShuffleboardPeriodic("Vision", 0.17, new Pair<String, Object>(limelightName + "Vision bot pose TX", (DoubleSupplier) () -> LimelightHelpers.getBotPose(limelightName)[0]),
                        new Pair<String, Object>(limelightName + "Vision bot pose TY", (DoubleSupplier) () -> LimelightHelpers.getBotPose(limelightName)[1]),
                        new Pair<String, Object>(limelightName + "Vision bot pose RZ", (DoubleSupplier) () -> LimelightHelpers.getBotPose(limelightName)[5]),
                        new Pair<String, Object>(limelightName + "Vision bot pose Blue TX", (DoubleSupplier) () -> LimelightHelpers.getBotPose_wpiBlue(limelightName)[0]),
                        new Pair<String, Object>(limelightName + "Vision bot pose Blue TY", (DoubleSupplier) () -> LimelightHelpers.getBotPose_wpiBlue(limelightName)[1]),
                        new Pair<String, Object>(limelightName + "Vision bot pose Blue RZ", (DoubleSupplier) () -> LimelightHelpers.getBotPose_wpiBlue(limelightName)[5]),
                        new Pair<String, Object>(limelightName + "Vision bot pose Red TX", (DoubleSupplier) () -> LimelightHelpers.getBotPose_wpiRed(limelightName)[0]),
                        new Pair<String, Object>(limelightName + "Vision bot pose Red TY", (DoubleSupplier) () -> LimelightHelpers.getBotPose_wpiRed(limelightName)[1]),
                        new Pair<String, Object>(limelightName + "Vision bot pose Red RZ", (DoubleSupplier) () -> LimelightHelpers.getBotPose_wpiRed(limelightName)[5]),
                        new Pair<String, Object>(limelightName + "RR Tape Horizontal Offset", (DoubleSupplier) () -> getHorizontalOffset()),
                        new Pair<String, Object>(limelightName + "RR Tape Vertical Offset", (DoubleSupplier) () -> getVerticalOffset()),
                        new Pair<String, Object>(limelightName + "RR Tape Target Area", (DoubleSupplier) () -> getTargetArea()),
                        new Pair<String, Object>(limelightName + "Vision latency pipeline", (DoubleSupplier) () -> getLatencyPipline()),
                        new Pair<String, Object>(limelightName + "Vision latency capture", (DoubleSupplier) () -> getLatencyCapture()),
                        new Pair<String, Object>(limelightName + "Vision bot pose latency", (DoubleSupplier) () -> getLatencyBotPose()),
                        new Pair<String, Object>(limelightName + "Vision bot pose blue latency", (DoubleSupplier) () -> getLatencyBotPoseBlue()),
                        new Pair<String, Object>(limelightName + "Vision bot pose red latency", (DoubleSupplier) () -> getLatencyBotPoseRed()),
                        new Pair<String, Object>(limelightName + "Vision has vision", (BooleanSupplier) () -> hasVision()),
                        new Pair<String, Object>(limelightName + "isolated tag pose TX", (DoubleSupplier) () -> getIsolatedTagPose()[0]),
                        new Pair<String, Object>(limelightName + "isolated tag pose TY", (DoubleSupplier) () -> getIsolatedTagPose()[1]),
                        new Pair<String, Object>(limelightName + "isolated tag pose TZ", (DoubleSupplier) () -> getIsolatedTagPose()[2]),
                        new Pair<String, Object>(limelightName + "isolated tag num", (DoubleSupplier) () -> (double) getTagNum()));

    }

    /**
     * Gets robot pose based off the alliance color
     * 
     * @return Pose2d of the robot
     */
    public Pose2d getRobotPose() {
        Pose2d robotPose = new Pose2d();
        if (Constants.ALLIANCE == DriverStation.Alliance.Blue) {
            double[] botPose = LimelightHelpers.getBotPose_wpiBlue(limelightName);
            robotPose = new Pose2d(new Translation2d(botPose[0] - cameraPose.getX(), (botPose[1]) - cameraPose.getY()), Rotation2d.fromDegrees(botPose[5] + 180));
        } else if (Constants.ALLIANCE == DriverStation.Alliance.Red) {
            double[] botPose = LimelightHelpers.getBotPose_wpiRed(limelightName);
            robotPose = new Pose2d(new Translation2d(botPose[0] - cameraPose.getX(), botPose[1] - cameraPose.getY()), Rotation2d.fromDegrees(botPose[5] + 180));
        }
        return robotPose;
    }

    /**
     * Gets robot pose based off the alliance color
     * 
     * @return Pose2d of the robot
     */
    public Pose2d getRobotPose(double tagNum) {
        Pose2d robotPose = new Pose2d();
        if ((Constants.ALLIANCE == DriverStation.Alliance.Blue) && (tagNum == getTagNum())) {
            double[] botPose = LimelightHelpers.getBotPose_wpiBlue(limelightName);
            robotPose = new Pose2d(new Translation2d(botPose[0] - cameraPose.getX(), botPose[1] - cameraPose.getY()), Rotation2d.fromDegrees(botPose[5]));
        } else if (Constants.ALLIANCE == DriverStation.Alliance.Red) {
            double[] botPose = LimelightHelpers.getBotPose_wpiRed(limelightName);
            robotPose = new Pose2d(new Translation2d(botPose[0] - cameraPose.getX(), botPose[1] - cameraPose.getY()), Rotation2d.fromDegrees(botPose[5]));
        }
        return robotPose;
    }
    
    public int getTagNum(){
        if(hasVision()){
            int tagID = (int) LimelightHelpers.getFiducialID(limelightName);
            return tagID;
        }
        else{
            return 0;
        }
    }


    public Double[] getIsolatedTagPose(){
        if (hasVision()){
            // Pose2d botPose = new Pose2d();
            Double[] pose = LimelightHelpers.parseLimeLightData(LimelightHelpers.getJSONDump(limelightName), getTagNum());
            // botPose = new Pose2d(new Translation2d(pose[0] - cameraPose.getX(), pose[1] - cameraPose.getY()), Rotation2d.fromDegrees(pose[5]));
            
            if (Constants.ALLIANCE == DriverStation.Alliance.Blue) {
                pose[0] += VisionConstants.ISOLATEDTAGXOFFSET;
                // todo: add rotational offset for only blue alliance
            } else if (Constants.ALLIANCE == DriverStation.Alliance.Red) {
                pose[0] *= -1;
                pose[0] += VisionConstants.ISOLATEDTAGXOFFSET;
            }
            pose[1] += VisionConstants.ISOLATEDTAGYOFFSET;

            return pose;

        } else {
            return null;
        }
    }

    /**
     * Gets the horizontal offset of the target from the center of the screen
     * 
     * @return double base on the horizontal FOV of the limelight
     */
    public double getHorizontalOffset() {
        if (hasVision()) {
            return LimelightHelpers.getTX(limelightName);
        } else {
            return 0;
        }
    }

    /**
     * Gets the vertical offset of the target from the center of the screen
     * 
     * @return double base on the vertical FOV of the limelight
     */
    public double getVerticalOffset() {
        if (hasVision()) {
            return LimelightHelpers.getTX(limelightName);
        } else {
            return 0;
        }
    }

    /**
     * Gets the area of the target
     * 
     * @return double base on the area of the target
     */
    public double getTargetArea() {
        if (hasVision()) {
            return LimelightHelpers.getTX(limelightName);
        } else {
            return 0;
        }
    }

    public double getTargetVertical() {
        if (hasVision()) {
            return LimelightHelpers.getTX(limelightName);
        } else {
            return 0;
        }
    }

    public boolean hasVision() {
        return LimelightHelpers.getTV(limelightName);
    }

    /**
     * Gets the latency of the pipeline
     * 
     * @return latency of pipeline
     */
    public double getLatencyPipline() {
        if (hasVision()) {
            return LimelightHelpers.getLatency_Pipeline(limelightName);
        } else {
            return 0;
        }
    }

    /**
     * Gets the latency of the capture
     * 
     * @return latency of capture
     */
    public double getLatencyCapture() {
        if (hasVision()) {
            return LimelightHelpers.getLatency_Capture(limelightName);
        } else {
            return 0;
        }
    }

    /**
     * Gets the latency of the bot pose
     * 
     * @return latency of bot pose
     */
    public double getLatencyBotPose() {
        double[] pose = LimelightHelpers.getBotPose(limelightName);
        if (hasVision() && pose.length != 0) {
            return pose[6] / 1000;
        } else {
            return 0;
        }
    }

    /**
     * Gets the latency of the blue bot pose
     * 
     * @return latency of blue bot pose
     */
    public double getLatencyBotPoseBlue() {
        double[] pose = LimelightHelpers.getBotPose_wpiBlue(limelightName);
        if (hasVision() && pose.length != 0) {
            return pose[6] / 1000;
        } else {
            return 0;
        }
    }

    /**
     * Gets the latency of the red bot pose
     * 
     * @return latency of red bot pose
     */
    public double getLatencyBotPoseRed() {
        double[] pose = LimelightHelpers.getBotPose_wpiRed(limelightName);
        if (hasVision() && pose.length != 0) {
            return pose[6] / 1000;
        } else {
            return 0;
        }
    }

    /**
     * Sets the pipeline we're using on the limelight. The first is for april tag targetting The second
     * is for retroreflective tape.
     * 
     * @param pipelineNum The pipeline number being used on the limelight.
     */
    public void setPipelineNum(int pipelineNum) {
        LimelightHelpers.setPipelineIndex(limelightName, pipelineNum);
    }

    public int getPipelineNum() {
        if (hasVision()) {
            return (int) LimelightHelpers.getCurrentPipelineIndex(limelightName);
        } else {
            return 0;
        }
    }

    private void setCameraMode() {
        LimelightHelpers.setCameraMode_Driver(limelightName);
    }

    /**
     * Ensures that what we're receiving is actually a valid target (if it's outside of FOV, it can't
     * be)
     * 
     * @return Whether or not target offset is more than 29.8 degrees.
     */
    public boolean validAngle(double angle) {
        // 29.8d represents the LL2+'s max FOV, from center of camera to edge of frame.
        return Math.abs(angle) < Constants.VisionConstants.HORIZ_CAMERA_FOV;
    }

    /**
     * Function to tell us whether or not we're on target (centered on vision tape)
     * 
     * @param expectedAngle Angle we're supposed to be at according to offset of target supplied by
     *        Limelight
     * @return Whether we're within acceptable tolerance of the target.
     */
    public boolean isOnTarget(double expectedAngle) {
        // Should put consideration into how accurate we want to be later on.
        return expectedAngle < Constants.VisionConstants.HORIZ_DEGREE_TOLERANCE;
    }

    /**
     * setCameraPose
     *
     * @param pose the pose of the camera in the robot's coordinate space
     */
    private void setCameraPose(Pose3d pose) {
        //single use variable to avoid a comically long function call :)

        double[] entries = new double[6];
        entries[0] = pose.getTranslation().getX();
        entries[1] = pose.getTranslation().getY();
        entries[2] = pose.getTranslation().getZ();
        entries[3] = pose.getRotation().getX();
        entries[4] = pose.getRotation().getY();
        entries[5] = pose.getRotation().getZ();

        if (hasVision()) {
            LimelightHelpers.setCameraPose_RobotSpace(limelightName, entries[0], entries[1], entries[2], entries[3], entries[4], entries[5]);
        }
    }

    @Override
    public void periodic() {

        if (hasVision() && !loggingStarted) {
            // Initialize the shuffleboard values and start logging data
            initializeShuffleboard();
            loggingStarted = true;
        }

        if (loggingStarted) {
            periodicShuffleboard.loop();
        }
    }

}
