package frc.lib.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;

public class Limelight {
    private NetworkTable table;
    private String name;
    private String ip;
    private URL baseUrl;
    private final double ntDefaultDouble = 0.0;
    private final long ntDefaultInt = 0;
    private final String ntDefaultString = "";
    private final double[] ntDefaultArray = {};

    /**
     * Create a new Limelight object with the specified name and ip
     * @param name the name of the limelight used in network tables
     * @param ip the ip of the limelight (no slashes or http://)
     */
    public Limelight(String name, String ip) {
        this.name = name;
        this.table = NetworkTableInstance.getDefault().getTable(name);
        this.ip = ip;
        try {
            this.baseUrl = new URL("http://" + ip);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid IP");
        }
    }

    public String toString() {
        return "Limelight: " + name + " at " + ip;
    }

    /**
     * Create a new Limelight object with the specified name and default ip
     * @param name the name of the limelight used in network tables
     * ip defaults to 10.8.62.11
     * @see Limelight#Limelight(String, String)
     */
    public Limelight(String name) {
        this(name, "10.8.62.11");
    }

    /**
     * get a double from network tables with the specified key
     * @param key the key to get the value from
     * @return the value of the key, or ntDefaultDouble if the key does not exist or has some other error
     */
    private double getDoubleNT(String key) {
        return table.getEntry(key).getDouble(ntDefaultDouble);
    }

    /**
     * get a boolean from network tables with the specified key
     * @param key the key to get the value from
     * @return the value of the key, or ntDefaultDouble if the key does not exist or has some other error
     */
    private int getIntNT(String key) {
        return (int) table.getEntry(key).getInteger(ntDefaultInt);
    }

    /**
     * get a String from network tables with the specified key
     * @param key the key to get the value from
     * @return the value of the key, or ntDefaultString if the key does not exist or has some other error
     */
    private String getStringNT(String key) {
        return table.getEntry(key).getString(ntDefaultString);
    }

    /**
     * get a double array from network tables with the specified key
     * @param key the key to get the value from
     * @return the value of the key, or ntDefaultArray if the key does not exist or has some other error
     */
    private double[] getArrayNT(String key) {
        return table.getEntry(key).getDoubleArray(ntDefaultArray);
    }

    /**
     * set a double in network tables with the specified key
     * @param key the key to set the value of
     * @param value the value to set the key to (can be int or double)
     */
    private void setNumNT(String key, Number value) {
        table.getEntry(key).setNumber(value);
    }

    /**
     * set a String in network tables with the specified key
     * @param key the key to set the value of
     * @param value the value to set the key to
     */
    private void setArrayNT(String key, double[] value) {
        table.getEntry(key).setDoubleArray(value);
    }

    /**
     * @return Whether the limelight has any valid targets
     */
    public boolean hasTarget() {
        return getDoubleNT("tv") == 1.0;
    }

    /**
     * Horizontal Offset From Crosshair To Target
     * @return (LL1: -27 degrees to 27 degrees | LL2: -29.8 to 29.8 degrees | LL3: -30 to 30 degrees)
     */
    public double getTargetX() {
        return getDoubleNT("tx");
    }

    //TODO: add limelight 3 fov
    /**
     * Vertical Offset From Crosshair To Target
     * @return (LL1: -20.5 degrees to 20.5 degrees | LL2: -24.85 to 24.85 degrees | -24 to 24 degrees)
     */
    public double getTargetY() {
        return getDoubleNT("ty");
    }

    /**
     * @return Target Area (0% of image to 100% of image)
     */
    public double getTargetArea() {
        return getDoubleNT("ta");
    }

    /**
     * @return The pipeline’s latency contribution (ms)
     */
    public double getPipelineLatency() {
        return getDoubleNT("tl");
    }

    /**
     * @return Capture pipeline latency (ms). Time between the end of the exposure of the middle row of the sensor to the beginning of the tracking pipeline.
     */
    public double getCaptureLatency() {
        return getDoubleNT("cl");
    }

    /**
     * @return the total latency of the limelight (ms). This is the sum of the pipeline and capture latency.
     */
    public double getTotalLatency() {
        return getPipelineLatency() + getCaptureLatency();
    }

    /**
     * @return Sidelength of shortest side of the fitted bounding box (pixels)
     */
    public double getTShort() {
        return getDoubleNT("tshort");
    }

    /**
     * @return Sidelength of longest side of the fitted bounding box (pixels)
     */
    public double getTLong() {
        return getDoubleNT("tlong");
    }

    /**
     * @return Horizontal sidelength of the rough bounding box (0 - 320 pixels)
     */
    public double getTHor() {
        return getDoubleNT("thor");
    }

    /**
     * @return Vertical sidelength of the rough bounding box (0 - 320 pixels)
     */
    public double getTVert() {
        return getDoubleNT("tvert");
    }

    /**
     * @return True active pipeline index of the camera (0 .. 9)
     */
    public int getPipeline() {
        return getIntNT("getpipe");
    }

    /**
     * @return Full JSON dump of targeting results
     */
    public JsonNode getTargetJSON() {
        return parseJson(getStringNT("json"));
    }

    /**
     * @return Class ID of primary neural detector result or neural classifier result
     */
    public String getNeuralClassID() {
        return getStringNT("tclass");
    }

    /**
     * @return Get the average HSV color underneath the crosshair region as a NumberArray
     */
    public double[] getAverageHSV() {
        return getArrayNT("tc");
    }

    /**
     * Convert an array of 7 doubles to a Pose4d
     * @param ntValues array of 7 doubles containing translation (X,Y,Z) Rotation(Roll,Pitch,Yaw), total latency (cl+tl)
     * @return a new Pose4d object with the values from the array
     */
    private Pose4d toPose4d(double[] ntValues) {
        if (ntValues.length == 7){
            return new Pose4d(new Translation3d(ntValues[0], ntValues[1], ntValues[2]), new Rotation3d(Math.toRadians(ntValues[3]), Math.toRadians(ntValues[4]), Math.toRadians(ntValues[5])), ntValues[6]);
        } else {
            return null;
        }
    }

    /**
     * Convert an array of 6 doubles to a Pose3d
     * @param ntValues array of 6 doubles containing translation (X,Y,Z) Rotation(Roll,Pitch,Yaw)
     * @return a new Pose3d object with the values from the array
     */
    private Pose3d toPose3d(double[] ntValues) {
        if (ntValues.length == 6){
            return new Pose3d(new Translation3d(ntValues[0], ntValues[1], ntValues[2]), new Rotation3d(Math.toRadians(ntValues[3]), Math.toRadians(ntValues[4]), Math.toRadians(ntValues[5])));
        } else {
            return null;
        }
    }

    /**
     * Automatically return either the blue or red alliance pose based on which alliance the driver station reports
     * @see Limelight#getBotPoseBlue()
     * @see Limelight#getBotPoseRed()
     * Robot transform is in field-space (alliance color driverstation WPILIB origin)
     * @return Translation (X,Y,Z) Rotation(Roll,Pitch,Yaw), total latency (cl+tl)
     */
    public Pose4d getAlliancePose() {
        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue) {
            return toPose4d(getArrayNT("botpose_wpiblue"));
        } else {
            return toPose4d(getArrayNT("botpose_wpired"));
        }
    }


    /**
     * @return 3D transform of the camera in the coordinate system of the primary in-view AprilTag
     */
    public Pose3d getCamPoseTargetSpace() {
        return toPose3d(getArrayNT("camerapose_targetspace"));
    }

    /**
     * @return 3D transform of the camera in the coordinate system of the Robot
     */
    public Pose3d getCamPoseRobotSpace() {
        return toPose3d(getArrayNT("camerapose_robotspace"));
    }

    /**
     * @return 3D transform of the primary in-view AprilTag in the coordinate system of the Camera
     */
    public Pose3d getTargetPoseCameraSpace() {
        return toPose3d(getArrayNT("targetpose_cameraspace"));
    }

    /**
     * @return 3D transform of the primary in-view AprilTag in the coordinate system of the Robot
     */
    public Pose3d getTargetPoseRobotSpace() {
        return toPose3d(getArrayNT("targetpose_robotspace"));
    }

    /**
     * @return ID of the primary in-view apriltag
     */
    public int getApriltagID() {
        return getIntNT("tid");
    }

    public enum LEDMode {
        PIPELINE(0),
        OFF(1),
        BLINK(2),
        ON(3);

        private int value;

        LEDMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Sets limelight’s LED state
     * PIPELINE: use the LED Mode set in the current pipeline
     * OFF: force off
     * BLINK: force blink
     * ON: force on
     * @param mode LED Mode
     */
    public void setLEDMode(LEDMode mode) {
        setNumNT("ledMode", mode.getValue());
    }

    public enum CamMode {
        VISION(0),
        DRIVER(1);

        private int value;

        CamMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Sets limelight’s operation mode
     * VISION: use for vision processing
     * DRIVER: Driver Camera (Increases exposure, disables vision processing)
     * @param mode Cam Mode
     */
    public void setCamMode(CamMode mode) {
        setNumNT("camMode", mode.getValue());
    }

    public enum StreamMode {
        STANDARD(0),
        PIP_MAIN(1),
        PIP_SECONDARY(2);

        private int value;

        StreamMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Sets limelight’s streaming mode
     * STANDARD: Side-by-side streams if a webcam is attached to Limelight
     * PIP_MAIN: The secondary camera stream is placed in the lower-right corner of the primary camera stream
     * PIP_SECONDARY: The primary camera stream is placed in the lower-right corner of the secondary camera stream
     * @param mode Stream Mode
     */
    public void setStreamMode(StreamMode mode) {
        setNumNT("stream", mode.getValue());
    }

    /**
     * Sets limelight’s current pipeline
     */
    public void setPipeline(int pipeline) {
        setNumNT("pipeline", pipeline);
    }

    /**
     * Sets limelight’s crop rectangle. The pipeline must utilize the default crop rectangle in the web interface.
     * @param xMin the minimum x value of the crop rectangle (-1 to 1)
     * @param yMin the minimum y value of the crop rectangle (-1 to 1)
     * @param xMax the maximum x value of the crop rectangle (-1 to 1)
     * @param yMax the maximum y value of the crop rectangle (-1 to 1)
     */
    public void setCropSize(double xMin, double yMin, double xMax, double yMax) {
        setArrayNT("crop", new double[] {xMin, xMax, yMin, yMax});
    }

    /**
     * @deprecated use limelight pipeline instead
     * @param pose the camera's position, with X as front/back, Y as left/right, and Z as up/down, in meters
     */
    public void setCameraPoseRobotSpace(Pose3d pose) {
        double[] ntValues = new double[6];
        ntValues[0] = pose.getX();
        ntValues[1] = pose.getY();
        ntValues[2] = pose.getZ();
        ntValues[3] = pose.getRotation().getX();
        ntValues[4] = pose.getRotation().getY();
        ntValues[5] = pose.getRotation().getZ();

        setArrayNT("camerapose_robotspace_set", ntValues);
    }


    /**
     * @return a URL object containing the ip of the limelight
     */
    public URL getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param suffix the suffix to add to the base url
     * @return a new URL object with the suffix and port 5807 added to the base url
     */
    private URL generateURL(String suffix) {
        try {
            return new URL(baseUrl.toString() + ":5807/" + suffix);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Suffix");
        }
    }


    /**
     * generic http request to the limelight
     * @param suffix the suffix to add to the base url (eg "deletesnapshots", "capturesnapshot")
     * @param type the type of request to send (eg "GET", "POST")
     * @param headers the headers to send with the request
     * @return the response message from the limelight
     */
    private String httpRequest(String suffix, String type, ArrayList<Pair<String, String>> headers) {
        URL url = generateURL(suffix);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(type);
            int responseCode = connection.getResponseCode();
            for (Pair<String, String> header : headers) {
                connection.setRequestProperty(header.getFirst(), header.getSecond());
            }
            if (responseCode != 200) {
                System.err.println("Bad LL Request: " + responseCode + " " + connection.getResponseMessage());
            }

            //Chatgpt wrote this lol
            // Read the response content as a String
            StringBuilder content = new StringBuilder();
            try (InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
            }

            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("I'm on Crack");
        }
    }

    /**
     * send a GET request to the limelight with the specified suffix
     * @param suffix the suffix to add to the base url (eg "deletesnapshots", "capturesnapshot")
     * @return the response message from the limelight
     */
    private String getRequest(String suffix, ArrayList<Pair<String, String>> headers) {
        return httpRequest(suffix, "GET", headers);
    }

    private String getRequest(String suffix) {
        return getRequest(suffix, new ArrayList<Pair<String, String>>());
    }

    private String async(Supplier<Object> supplier) {
        try {
            return (String) CompletableFuture.supplyAsync(supplier).get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Take exactly one snapshot with the current limelight settings. Limited to 2 snapshots per second.
     * @param name the name of the snapshot
     */
    public void takeSnapshot(String name) {
        ArrayList<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
        headers.add(new Pair<String, String>("snapname", name));
        async(() -> { getRequest("capturesnapshot", headers); return null; });
    }

    /**
     * Take exactly one snapshot with the current limelight settings with default naming (name defaults to snap)
     * @see Limelight#SynchronousSnapshot(String)
     */
    public void takeSnapshot() {
        async(() -> { getRequest("capturesnapshot"); return null; });
    }

    /**
     * Return a list of filenames of all snapshots on the limelight
     * @return
     */
    public JsonNode getSnapshotNames() {
        String rawReport = async(() -> getRequest("snapshotmanifest"));
        return parseJson(rawReport);
    }

    /**
     * Delete all snapshots on the limelight
     * @see Limelight#synchronousDeleteAllSnapshots()
     */
    public void deleteAllSnapshots() {
        async(() -> { getRequest("deletesnapshots"); return null; });
    }

    /**
     * Return the limelight's current hardware report
     * @return a json object containing the hardware report
     */
    public JsonNode getHWReport() {
        String rawReport = async(() -> getRequest("hwreport"));
        return parseJson(rawReport);
    }

    private JsonNode parseJson(String raw) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
