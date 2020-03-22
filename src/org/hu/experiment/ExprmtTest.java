package org.hu.experiment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

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
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;
import org.hu.Enums;

/**
 * Simulation setup for case study 2 - Intelligent Surveillance
 * @author Harshit Gupta
 *
 */
public class ExprmtTest {
    public static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    public static List<Application> allApplication = new ArrayList<>();

    static List<Sensor> sensors = new ArrayList<Sensor>();
    static List<Actuator> actuators = new ArrayList<Actuator>();
    static int numOfAreas = 5;
    static int numOfCamerasPerArea = 10;


    private static boolean CLOUD = false;

    public static void main(String[] args) {

        Log.printLine("Starting Experiment1...");

        try {
            Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            String appId = "multiMudule"; // identifier of the application

            FogBroker broker = new FogBroker("broker");

            Application application = createApplication(appId, broker.getId());
            //

            allApplication.add(application);

            application.setUserId(broker.getId());

            createFogDevices(broker.getId(), appId);

            Controller controller = null;

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
            for(FogDevice device : fogDevices){
                if(device.getName().startsWith("m")){ // names of all Smart Cameras start with 'm'
                    moduleMapping.addModuleToDevice("motion_detector", device.getName());  // fixing 1 instance of the Motion Detector module to each Smart Camera
                }
            }
//			moduleMapping.addModuleToDevice("user_interface", "cloud"); // fixing instances of User Interface module in the Cloud

            moduleMapping.addModuleToDevice("A", "cloud");
            moduleMapping.addModuleToDevice("B", "cloud");
            moduleMapping.addModuleToDevice("C", "cloud");
            moduleMapping.addModuleToDevice("D", "cloud");
            moduleMapping.addModuleToDevice("E", "cloud");
            moduleMapping.addModuleToDevice("F", "cloud");


            moduleMapping.addModuleToDevice("A", "d-0");
            moduleMapping.addModuleToDevice("B", "d-0");
            moduleMapping.addModuleToDevice("C", "d-0");
            moduleMapping.addModuleToDevice("D", "d-0");
            moduleMapping.addModuleToDevice("E", "d-0");
            moduleMapping.addModuleToDevice("F", "d-0");

            moduleMapping.addModuleToDevice("A", "d-1");
            moduleMapping.addModuleToDevice("B", "d-1");
            moduleMapping.addModuleToDevice("C", "d-1");
            moduleMapping.addModuleToDevice("D", "d-1");
            moduleMapping.addModuleToDevice("E", "d-1");
            moduleMapping.addModuleToDevice("F", "d-1");

            moduleMapping.addModuleToDevice("A", "d-2");
            moduleMapping.addModuleToDevice("B", "d-2");
            moduleMapping.addModuleToDevice("C", "d-2");
            moduleMapping.addModuleToDevice("D", "d-2");
            moduleMapping.addModuleToDevice("E", "d-2");
            moduleMapping.addModuleToDevice("F", "d-2");

            moduleMapping.addModuleToDevice("A", "d-3");
            moduleMapping.addModuleToDevice("B", "d-3");
            moduleMapping.addModuleToDevice("C", "d-3");
            moduleMapping.addModuleToDevice("D", "d-3");
            moduleMapping.addModuleToDevice("E", "d-3");
            moduleMapping.addModuleToDevice("F", "d-3");

            moduleMapping.addModuleToDevice("A", "d-4");
            moduleMapping.addModuleToDevice("B", "d-4");
            moduleMapping.addModuleToDevice("C", "d-4");
            moduleMapping.addModuleToDevice("D", "d-4");
            moduleMapping.addModuleToDevice("E", "d-4");
            moduleMapping.addModuleToDevice("F", "d-4");

            moduleMapping.addModuleToDevice("A", "d-proxy");
            moduleMapping.addModuleToDevice("B", "d-proxy");
            moduleMapping.addModuleToDevice("C", "d-proxy");
            moduleMapping.addModuleToDevice("D", "d-proxy");
            moduleMapping.addModuleToDevice("E", "d-proxy");
            moduleMapping.addModuleToDevice("F", "d-proxy");
//			moduleMapping.addModuleToDevice("object_tracker", "d-proxy");

            controller = new Controller("master-controller", fogDevices, sensors,
                    actuators);

            controller.submitApplication(application,
                    (CLOUD)?(new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            :(new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            Log.printLine("VRGame finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Creates the fog devices in the physical topology of the simulation.
     * @param userId
     * @param appId
     */
    private static void createFogDevices(int userId, String appId) {
        FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);//83.25
        cloud.setFogDeviceType(Enums.CLOUD);
        cloud.setParentId(-1);
        fogDevices.add(cloud);
        FogDevice proxy = createFogDevice("d-proxy", 2800, 4000, 10000, 10000, 1, 0.0, 107.339,10 );//83.4333
        proxy.setFogDeviceType(Enums.EDGE_SERVER);
        proxy.setParentId(cloud.getId());
        proxy.setUplinkLatency(100); // latency of connection between proxy server and cloud is 100 ms
        fogDevices.add(proxy);
        for(int i=0;i<numOfAreas;i++){
            addArea(i+"", userId, appId, proxy.getId());
        }
    }

    private static FogDevice addArea(String id, int userId, String appId, int parentId){
        FogDevice router = createFogDevice("d-"+id, 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 10);//83.4333
        router.setFogDeviceType(Enums.EDGE_SERVER);
        fogDevices.add(router);
        router.setUplinkLatency(2); // latency of connection between router and proxy server is 2 ms
        for(int i=0;i<numOfCamerasPerArea;i++){
            String mobileId = id+"-"+i;
            FogDevice camera = addCamera(mobileId, userId, appId, router.getId()); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
            camera.setUplinkLatency(2); // latency of connection between camera and router is 2 ms
            fogDevices.add(camera);
        }
        router.setParentId(parentId);
        return router;
    }

    private static FogDevice addCamera(String id, int userId, String appId, int parentId){
        FogDevice camera = createFogDevice("m-"+id, 500, 1000, 10000, 10000, 3, 0, 87.53, 82.44);
        camera.setParentId(parentId);
        Sensor sensor = new Sensor("s-"+id, "CAMERA", userId, appId, new DeterministicDistribution(200)); // inter-transmission time of camera (sensor) follows a deterministic distribution
        sensors.add(sensor);
        Actuator ptz = new Actuator("ptz-"+id, userId, appId, "PTZ_CONTROL");
        actuators.add(ptz);
        sensor.setGatewayDeviceId(camera.getId());
        sensor.setLatency(1.0);  // latency of connection between camera (sensor) and the parent Smart Camera is 1 ms
        ptz.setGatewayDeviceId(camera.getId());
        ptz.setLatency(1.0);  // latency of connection between PTZ Control and the parent Smart Camera is 1 ms
        return camera;
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
        int bw = 10000;

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
    private static Application createApplication(String appId, int userId){

        Application application = Application.createApplication(appId, userId);
        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
        application.addAppModule("motion_detector", 10);
        application.addAppModule("A", 10);
        application.addAppModule("B", 10);
        application.addAppModule("C", 10);
        application.addAppModule("D", 10);
        application.addAppModule("E", 10);
        application.addAppModule("F", 10);

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */
        application.addAppEdge("CAMERA", "motion_detector", 10000, 20000, "CAMERA", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("motion_detector", "A", 10000, 20000, "m-a", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("A", "B", 8000, 15000, "a-b", Tuple.UP, AppEdge.MODULE); // adding edge from Motion Detector to Object Detector module carrying tuples of type MOTION_VIDEO_STREAM
        application.addAppEdge("A", "C", 5000, 10000, "a-c", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to User Interface module carrying tuples of type DETECTED_OBJECT
        application.addAppEdge("B", "D", 2000, 9000, "b-d", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("B", "E", 1000, 2000, "b-e", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("C", "F", 1000, 8000, "c-f", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("D", "F", 2000, 3000, "d-f", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
        application.addAppEdge("E", "F", 1500, 1000, "e-f", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION

        application.addAppEdge("F", "PTZ_CONTROL",  2800, 500, "PTZ_PARAMS", Tuple.DOWN, AppEdge.ACTUATOR);
        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules.
         */
        application.addTupleMapping("motion_detector", "CAMERA", "m-a", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("A", "m-a", "a-b", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("A", "m-a", "a-c", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("B", "a-b", "b-d", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
        application.addTupleMapping("B", "a-b", "b-e", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("C", "a-c", "c-f", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("D", "b-d", "d-f", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA

        application.addTupleMapping("E", "b-e", "e-f", new FractionalSelectivity(1)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA


        /*
         * Defining application loops (maybe incomplete loops) to monitor the latency of.
         * Here, we add two loops for monitoring : Motion Detector -> Object Detector -> Object Tracker and Object Tracker -> PTZ Control
         */
        final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("A");add("B");add("D");add("F");}});
        final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{add("A");add("B");add("E");add("F");}});
        final AppLoop loop3 = new AppLoop(new ArrayList<String>(){{add("A");add("C");add("F");}});
        List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);add(loop2);add(loop3);}};

        application.setLoops(loops);
        return application;
    }
}