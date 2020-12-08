package org.hu.experiment;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;
import org.hu.utils.Enums;

import java.util.*;

public class EnvironmentMonitoring {
    public static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    public static List<Application> allApplication = new ArrayList<>();

    static List<FogDevice> stations = new ArrayList<FogDevice>();
    static List<FogDevice> vehicles = new ArrayList<FogDevice>();
    static List<FogDevice> areaServerList = new ArrayList<>();

    private static double STATION_EMIT_TIME = 200;
    private static double VEHICLE_EMIT_TIME = 10000;

    public static int isMerge = 0;//random
    public static int isGa =0;
    public static int isMergeGa = 1;
    public static int mergedNum = 6;
    public static int isAllCloud = 0; //不适用于该场景
    public static int isQueueOpt = 0;
    public static int isNearOffload = 0;

    public static int isUnbalancedStationNum = 0;
    public static int recordIteration = 1;
    public static int recordTaskNo = 3;

    public static List<Sensor> sensors = new ArrayList<Sensor>();
    static List<Actuator> actuators = new ArrayList<Actuator>();
    static int numOfAreas = 8;
    static int numOfStationsPerArea =8;//7
    static int numOfVehiclesPerArea = 1;

    private static int[] numsOfStationInAArea = {16, 10, 8, 8, 8, 8, 8, 8, 25, 8};

    /**
     * 记录tuple处理位置
     *
     * @param args
     */
    public static Map<Integer, Integer> tupleProcessPositionRecord = new HashMap<>();

    public static void main(String[] args) {

        Log.printLine("Starting Experiment1...");
        //initial processPositionRecord
        tupleProcessPositionRecord.put(1, 0);
        tupleProcessPositionRecord.put(2, 0);
        tupleProcessPositionRecord.put(4, 0);

        try {
            Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events


            CloudSim.init(num_user, calendar, trace_flag);

            String appId0 = "StationMonitoring"; // identifier of the application
            String appId1 = "VehicleMonitoring"; // identifier of the application

            FogBroker broker0 = new FogBroker("broker_0");
            FogBroker broker1 = new FogBroker("broker_1");

            Application stationApplication = createStationApplication(appId0, broker0.getId());
            Application vehicleApplication = createVehicleApplication(appId1, broker1.getId());

            stationApplication.setUserId(broker0.getId());
            vehicleApplication.setUserId(broker1.getId());

            allApplication.add(stationApplication);
            allApplication.add(vehicleApplication);

            stationApplication.setUserId(broker0.getId());
            vehicleApplication.setUserId(broker1.getId());

            createFogDevices();

            createSensorAndActuatorsForStation(broker0.getId(), appId0);
            createSensorAndActuatorsForVehicle(broker1.getId(), appId1);

            Controller controller = null;

            ModuleMapping moduleMapping0 = ModuleMapping.createModuleMapping(); // initializing a module mapping
            for(FogDevice device : fogDevices){
                if(device.getName().startsWith("m")&&(!device.getName().endsWith("v"))){ // names of all Smart Cameras start with 'm'
                    moduleMapping0.addModuleToDevice("forStation", device.getName());  // fixing 1 instance of the Motion Detector module to each Smart Camera
                }
                if (device.getFogDeviceType() == Enums.CLOUD) {
                    moduleMapping0.addModuleToDevice("cloudTask", device.getName());
                    moduleMapping0.addModuleToDevice("dataPreprocess", device.getName());
                    moduleMapping0.addModuleToDevice("pmAnalyze", device.getName());
                    moduleMapping0.addModuleToDevice("no2So2Analyze", device.getName());
                    moduleMapping0.addModuleToDevice("pollutantAnalyze", device.getName());
                    moduleMapping0.addModuleToDevice("hotTimeCompute", device.getName());
                    moduleMapping0.addModuleToDevice("highValuePreprocess", device.getName());
                    moduleMapping0.addModuleToDevice("hotAreaAnalyze", device.getName());
                    moduleMapping0.addModuleToDevice("visualizationPre", device.getName());
                }
                if (device.getFogDeviceType() == Enums.PROXY) {
                    moduleMapping0.addModuleToDevice("dataPreprocess", device.getName());
                    moduleMapping0.addModuleToDevice("pmAnalyze", device.getName());
                    moduleMapping0.addModuleToDevice("no2So2Analyze", device.getName());
                    moduleMapping0.addModuleToDevice("pollutantAnalyze", device.getName());
                    moduleMapping0.addModuleToDevice("hotTimeCompute", device.getName());
                    moduleMapping0.addModuleToDevice("highValuePreprocess", device.getName());
                    moduleMapping0.addModuleToDevice("hotAreaAnalyze", device.getName());
                    moduleMapping0.addModuleToDevice("visualizationPre", device.getName());
                }
            }
            for (int i = 0; i < numOfAreas; i++) {
                //StationApp
                moduleMapping0.addModuleToDevice("dataPreprocess", "d-"+i);
                moduleMapping0.addModuleToDevice("pmAnalyze", "d-"+i);
                moduleMapping0.addModuleToDevice("no2So2Analyze", "d-"+i);
                moduleMapping0.addModuleToDevice("pollutantAnalyze", "d-"+i);
                moduleMapping0.addModuleToDevice("hotTimeCompute", "d-"+i);
                moduleMapping0.addModuleToDevice("highValuePreprocess", "d-"+i);
                moduleMapping0.addModuleToDevice("hotAreaAnalyze", "d-"+i);
                moduleMapping0.addModuleToDevice("visualizationPre", "d-"+i);
            }



            ModuleMapping moduleMapping1 = ModuleMapping.createModuleMapping(); // initializing a module mapping
            for(FogDevice device : fogDevices){
                if(device.getName().startsWith("m")&&(device.getName().endsWith("v"))){ // names of all Smart Cameras start with 'm'
                    moduleMapping1.addModuleToDevice("forVehicle", device.getName());  // fixing 1 instance of the Motion Detector module to each Smart Camera
                }
                if (device.getFogDeviceType() == Enums.CLOUD) {
                    moduleMapping1.addModuleToDevice("cloudTaskV", device.getName());
                    moduleMapping1.addModuleToDevice("DataPreprocess", device.getName());
                    moduleMapping1.addModuleToDevice("videoPreprocess", device.getName());
                    moduleMapping1.addModuleToDevice("pollutantMatching", device.getName());
                    moduleMapping1.addModuleToDevice("sourcePreprocess", device.getName());
                }
                if (device.getFogDeviceType() == Enums.PROXY) {
                    moduleMapping1.addModuleToDevice("DataPreprocess", device.getName());
                    moduleMapping1.addModuleToDevice("videoPreprocess", device.getName());
                    moduleMapping1.addModuleToDevice("pollutantMatching", device.getName());
                    moduleMapping1.addModuleToDevice("sourcePreprocess", device.getName());
                }
            }
            for (int i = 0; i < numOfAreas; i++) {
                moduleMapping1.addModuleToDevice("DataPreprocess", "d-"+i);
                moduleMapping1.addModuleToDevice("videoPreprocess", "d-" + i);
                moduleMapping1.addModuleToDevice("pollutantMatching", "d-" + i);
                moduleMapping1.addModuleToDevice("sourcePreprocess", "d-" + i);
            }

            controller = new Controller("master-controller", fogDevices, sensors,
                    actuators);

            controller.submitApplication(stationApplication, new ModulePlacementMapping(fogDevices, stationApplication, moduleMapping0));
            controller.submitApplication(vehicleApplication, new ModulePlacementMapping(fogDevices, vehicleApplication, moduleMapping1));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();



            Log.printLine("Environment Monitoring Task Simulation finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * 环境监测站 路边站
     * @param userId
     * @param appId
     */
    private static void createSensorAndActuatorsForStation(int userId, String appId) {
        for(FogDevice station : stations){
            String id = station.getName();
            Sensor environmentMonitoringSensor = new Sensor("s-"+appId+"-"+id, "environmentDataCollection", userId, appId, new DeterministicDistribution(STATION_EMIT_TIME)); // inter-transmission time of EEG sensor follows a deterministic distribution
            sensors.add(environmentMonitoringSensor);
            Actuator display = new Actuator("a-"+appId+"-"+id, userId, appId, "DISPLAY");
            actuators.add(display);
            environmentMonitoringSensor.setGatewayDeviceId(station.getId());
            environmentMonitoringSensor.setLatency(6.0);  // latency of connection between EEG sensors and the parent Smartphone is 6 ms
            display.setGatewayDeviceId(station.getId());
            display.setLatency(1.0);  // latency of connection between Display actuator and the parent Smartphone is 1 ms
        }
    }
//TODO 后期改为随机间隔卸载任务
    private static void createSensorAndActuatorsForVehicle(int userId, String appId) {
        for(FogDevice vehicle : vehicles){
            String id = vehicle.getName();
            Sensor environmentMonitoringSensor = new Sensor("s-" + appId + "-" + id + "v", "environmentDataAndVideo", userId, appId, new DeterministicDistribution(VEHICLE_EMIT_TIME)); // inter-transmission time of EEG sensor follows a deterministic distribution
            sensors.add(environmentMonitoringSensor);
            Actuator display = new Actuator("a-"+appId+"-"+id, userId, appId, "DISPLAY");
            actuators.add(display);
            environmentMonitoringSensor.setGatewayDeviceId(vehicle.getId());
            environmentMonitoringSensor.setLatency(6.0);  // latency of connection between EEG sensors and the parent Smartphone is 6 ms
            display.setGatewayDeviceId(vehicle.getId());
            display.setLatency(1.0);  // latency of connection between Display actuator and the parent Smartphone is 1 ms
        }
    }

    /**
     * 创建所有FogDevice
     */
    private static void createFogDevices() {
        FogDevice cloud = createFogDevice("cloud", 44*1000, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25); // creates the fog device Cloud at the apex of the hierarchy with level=0
        cloud.setParentId(-1);
        FogDevice proxy = createFogDevice("proxy-server", 2*1000, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333); // creates the fog device Proxy Server (level=1)
        proxy.setParentId(cloud.getId()); // setting Cloud as parent of the Proxy Server
        proxy.setUplinkLatency(100); // latency of connection from Proxy Server to the Cloud is 100 ms

        cloud.setFogDeviceType(Enums.CLOUD);
        proxy.setFogDeviceType(Enums.PROXY);

        fogDevices.add(cloud);
        fogDevices.add(proxy);

        for(int i=0;i<numOfAreas;i++){
            createArea(i+"", proxy.getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
        }
    }


    public static FogDevice createArea(String id, int parentId) {//mips 2*1000
        FogDevice areaEdgeServer = createFogDevice("d-"+id, 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);//83.4333
        areaEdgeServer.setFogDeviceType(Enums.EDGE_SERVER);
        fogDevices.add(areaEdgeServer);
        areaServerList.add(areaEdgeServer);
        areaEdgeServer.setParentId(parentId);
        areaEdgeServer.setUplinkLatency(4);
//        int stationNum = numsOfStationInAArea[Integer.parseInt(id)];

        if (isUnbalancedStationNum == 1) {
            int stationNum = numsOfStationInAArea[Integer.parseInt(id)];

            for (int i = 0; i < stationNum; i++) {
                String stationId = id + "-" + i;
                addStation(stationId, areaEdgeServer.getId());
            }

        }else{
            for (int i = 0; i < numOfStationsPerArea; i++) {
                String stationId = id + "-" + i;
                addStation(stationId, areaEdgeServer.getId());
            }
        }


        for (int i = 0; i < numOfVehiclesPerArea; i++) {
            String vehicleId = id + "-" + i + "v";
            addVehicle(vehicleId, areaEdgeServer.getId());
        }
        areaEdgeServer.setParentId(parentId);
        areaServerList.add(areaEdgeServer);
        return areaEdgeServer;
    }

    private static FogDevice addStation(String id, int parentId) {
        FogDevice station = createFogDevice("m-"+id, 1000, 1000, 10000, 10000, 3, 0, 87.53, 82.44);
        station.setParentId(parentId);
        station.setUplinkLatency(4);
        fogDevices.add(station);
        stations.add(station);
        return station;
    }

    private static FogDevice addVehicle(String id, int parentId) {
        FogDevice vehicle = createFogDevice("m-"+id, 1000, 1000, 10000, 10000, 3, 0, 87.53, 82.44);
        vehicle.setParentId(parentId);
        vehicle.setUplinkLatency(4);
        fogDevices.add(vehicle);
        vehicles.add(vehicle);
        return vehicle;
    }


    /**
     * Creates a vanilla fog device
     * @param nodeName name of the device to be used in simulation
     * @param mips MIPS
     * @param ram RAM
     * @param upBw uplink bandwidth
     * @param downBw downlink bandwidth
     * @param level hierarchy level of the device
     * @param ratePerMips cost rate per MIPS used
     * @param busyPower
     * @param idlePower
     * @return
     */
    private static FogDevice createFogDevice(String nodeName, long mips,
                                             int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
        int bw = 100000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fogdevice.setLevel(level);

        fogdevice.setAllFogDevices(fogDevices);
        return fogdevice;
    }

    /**
     * Function to create the Intelligent Surveillance application in the DDF model.
     * @param appId unique identifier of the application
     * @param userId identifier of the user of the application
     * @return
     */
    @SuppressWarnings({"serial" })
    private static Application createStationApplication(String appId, int userId){
        Application application = Application.createApplication(appId, userId);
        int CpuFactor = 5*100;//6
        int NwFactor = 50;//100
        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
//        application.addAppModule("environmentDataCollection", 10); //moveToSensor
        application.addAppModule("forStation", 10,500*CpuFactor);//500
        application.addAppModule("dataPreprocess", 10,100*CpuFactor);//100
        application.addAppModule("pmAnalyze", 10,800*CpuFactor);//800
        application.addAppModule("no2So2Analyze", 10,1000*CpuFactor);//1000
        application.addAppModule("pollutantAnalyze", 10,1000*CpuFactor);//1000
        application.addAppModule("hotTimeCompute", 10,300*CpuFactor);//300
        application.addAppModule("highValuePreprocess", 10,200*CpuFactor);//200
        application.addAppModule("hotAreaAnalyze", 10,200*CpuFactor);//200
        application.addAppModule("visualizationPre", 10,1000*CpuFactor);//2000
        application.addAppModule("cloudTask", 10,1000*CpuFactor);//2000

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */

        application.addAppEdge("environmentDataCollection", "forStation", 50*CpuFactor, 300*NwFactor, "environmentDataCollection", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("forStation", "dataPreprocess", 10*CpuFactor, 300*NwFactor, "1", Tuple.UP, AppEdge.MODULE); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("dataPreprocess", "highValuePreprocess", 20*CpuFactor, 200*NwFactor, "2", Tuple.UP, AppEdge.MODULE); // adding edge from Motion Detector to Object Detector module carrying tuples of type MOTION_VIDEO_STREAM
        application.addAppEdge("dataPreprocess", "hotTimeCompute", 20*CpuFactor, 200*NwFactor, "3", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to User Interface module carrying tuples of type DETECTED_OBJECT
        application.addAppEdge("dataPreprocess", "hotAreaAnalyze", 20*CpuFactor, 200*NwFactor, "4", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("dataPreprocess", "pmAnalyze", 80*CpuFactor, 200*NwFactor, "5", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("dataPreprocess", "no2So2Analyze", 100*CpuFactor, 300*NwFactor, "6", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("highValuePreprocess", "cloudTask", 100*CpuFactor, 500*NwFactor, "7", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("hotTimeCompute", "cloudTask", 100*CpuFactor, 500*NwFactor, "8", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("hotAreaAnalyze", "cloudTask", 100*CpuFactor, 500*NwFactor, "9", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("no2So2Analyze", "pollutantAnalyze", 100*CpuFactor, 600*NwFactor, "10", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("pmAnalyze", "pollutantAnalyze", 100*CpuFactor, 600*NwFactor, "11", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("pollutantAnalyze", "visualizationPre", 100*CpuFactor, 500*NwFactor, "12", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("hotAreaAnalyze", "visualizationPre", 100*CpuFactor, 500*NwFactor, "13", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("hotTimeCompute", "visualizationPre", 100*CpuFactor, 500*NwFactor, "14", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("highValuePreprocess", "visualizationPre", 100*CpuFactor, 500*NwFactor, "15", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("visualizationPre", "cloudTask", 100*CpuFactor, 1000*NwFactor, "16", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("pollutantAnalyze", "cloudTask", 100*CpuFactor, 1000*NwFactor, "17", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION

        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules.
         */
        application.addTupleMapping("forStation", "environmentDataCollection", "1", new FractionalSelectivity(1.0)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("dataPreprocess", "1", "2", new FractionalSelectivity(0.2)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("dataPreprocess", "1", "3", new FractionalSelectivity(0.2)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("dataPreprocess", "1", "4", new FractionalSelectivity(0.2)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("dataPreprocess", "1", "5", new FractionalSelectivity(0.2)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("dataPreprocess", "1", "6", new FractionalSelectivity(0.2)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA


        application.addTupleMapping("no2So2Analyze", "6", "10", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("pmAnalyze", "5", "11", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("hotAreaAnalyze", "4", "13", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("hotAreaAnalyze", "4", "9", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("hotTimeCompute", "3", "14", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("hotTimeCompute", "3", "8", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("highValuePreprocess", "2", "15", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("highValuePreprocess", "2", "7", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("pollutantAnalyze", "10", "17", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("pollutantAnalyze", "10", "12", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("pollutantAnalyze", "11", "12", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("pollutantAnalyze", "11", "17", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("visualizationPre", "12", "16", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("visualizationPre", "13", "16", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("visualizationPre", "14", "16", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("visualizationPre", "15", "16", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA


        /*
         * Defining application loops (maybe incomplete loops) to monitor the latency of.
         * Here, we add two loops for monitoring : Motion Detector -> Object Detector -> Object Tracker and Object Tracker -> PTZ Control
         */
        final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("dataPreprocess");add("hotAreaAnalyze");add("visualizationPre");}});
        final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{add("dataPreprocess");add("pmAnalyze");add("pollutantAnalyze");add("visualizationPre");}});
        final AppLoop loop3 = new AppLoop(new ArrayList<String>(){{add("dataPreprocess");add("highValuePreprocess");}});
        List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);add(loop2);add(loop3);}};

        application.setLoops(loops);
        return application;
    }

    public static Application createVehicleApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        application.addAppModule("forVehicle", 10);
        application.addAppModule("DataPreprocess", 10);
        application.addAppModule("videoPreprocess", 10);
        application.addAppModule("pollutantMatching", 10);
        application.addAppModule("sourcePreprocess", 10);
        application.addAppModule("cloudTaskV", 10);

        int factor = 3;
        int NwFactor = 2;
        application.addAppEdge("environmentDataAndVideo", "forVehicle", 500*factor, 2000*NwFactor, "environmentDataAndVideo", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("forVehicle", "DataPreprocess", 100*factor, 500*NwFactor, "1", Tuple.UP, AppEdge.MODULE); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("forVehicle", "videoPreprocess", 600*factor, 2000*NwFactor, "2", Tuple.UP, AppEdge.MODULE); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("DataPreprocess", "pollutantMatching", 400*factor, 500*NwFactor, "3", Tuple.UP, AppEdge.MODULE); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("pollutantMatching", "sourcePreprocess", 500*factor, 500*NwFactor, "5", Tuple.UP, AppEdge.MODULE); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("videoPreprocess", "sourcePreprocess", 500*factor, 2000*NwFactor, "4", Tuple.UP, AppEdge.MODULE); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("sourcePreprocess", "cloudTaskV", 1000*factor, 2000*NwFactor, "6", Tuple.UP, AppEdge.MODULE); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA

        application.addTupleMapping("forVehicle", "environmentDataAndVideo", "1", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("forVehicle", "environmentDataAndVideo", "2", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("DataPreprocess", "1", "3", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("videoPreprocess", "2", "4", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("pollutantMatching", "3", "5", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("sourcePreprocess", "4", "6", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("sourcePreprocess", "5", "6", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("forVehicle");add("DataPreprocess");add("pollutantMatching");add("sourcePreprocess");}});
        final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{add("forVehicle");add("videoPreprocess");add("sourcePreprocess");}});
        List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);add(loop2);}};

        application.setLoops(loops);
        return application;

    }

}
