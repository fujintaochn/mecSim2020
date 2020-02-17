package org.fog.placement;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModulePlacementAllModuleForEachFogDevice extends ModulePlacementPolicy {

    /**
     * List of sensors considered for placement
     */
    private List<Sensor> sensors;

    /**
     * List of actuators considered for placement
     */
    private List<Actuator> actuators;

    public ModulePlacementAllModuleForEachFogDevice(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, Application application){
        super();
        this.setFogDevices(fogDevices);
        this.setApplication(application);
        this.setSensors(sensors);
        this.setActuators(actuators);
        this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
        this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
    }


    @Override
    public List<ModulePlacement> computeModulePlacements(List<FogDeviceCharacteristics> fogDeviceCharacteristics,
                                                         List<SensorCharacteristics> sensorCharacteristics,
                                                         List<ActuatorCharacteristics> actuatorCharacteristics) {
        for (FogDeviceCharacteristics fc : fogDeviceCharacteristics) {
            getFogDeviceCharacteristics().put(fc.getId(), fc);
        }
        for (SensorCharacteristics sc : sensorCharacteristics) {
            getSensorCharacteristics().put(sc.getId(), sc);
        }
        for (ActuatorCharacteristics ac : actuatorCharacteristics) {
            getActuatorCharacteristics().put(ac.getId(), ac);
        }

        FogDeviceCharacteristics cloud = null;
        for (Integer fc : getFogDeviceCharacteristics().keySet()) {
            if (getFogDeviceCharacteristics().get(fc).isCloudDatacenter())
                cloud = getFogDeviceCharacteristics().get(fc);
        }
        if (cloud == null) {
            // If there is no cloud datacenter, the placement fails
            return null;
        }


        List<ModulePlacement> placements = new ArrayList<ModulePlacement>();

        for (int sensorId : getSensorCharacteristics().keySet()) {
            // For every sensor, there is a new placement instance (containing 1 instance of each app module)
            ModulePlacement placement = new ModulePlacement();
            // Adding sensor to the module placement instance
            placement.addSensorId(getSensorCharacteristics().get(sensorId).getTupleType(), sensorId);
            // Getting the actuator associated to this sensor
            ActuatorCharacteristics actuator = getCorresponsingActuator(getSensorCharacteristics().get(sensorId));
            // Adding the corresponding actuator to placement module instance
            placement.addActuatorId(actuator.getActuatorType(), actuator.getId());
            // Most important, mapping all application modules to the cloud datacenter
//            for (AppModule module : getApplication().getModules()) {
//                placement.addMapping(module.getName(), cloud.getId());
//            }
//            for (AppModule module : getApplication().getModules()) {
//                placement.addMapping(module.getName(),getDeviceByName("FD-0").getId());
//            }
            placement.addMapping("A", getDeviceByName("FD-0").getId());
            placement.addMapping("B", getDeviceByName("FD-0").getId());
            placement.addMapping("C", cloud.getId());
            placement.addMapping("D", getDeviceByName("FD-0").getId());
            placement.addMapping("E", getDeviceByName("FD-0").getId());






            // Adding this placement instance to the list of placement instances
            placements.add(placement);
        }

        return placements;

    }

    private ActuatorCharacteristics getCorresponsingActuator(
            SensorCharacteristics sensorCharacteristics) {
        // Look at the last part of name to check correspondence
        String suffix = CloudSim.getEntityName(sensorCharacteristics.getId()).substring(2);
        for (Map.Entry<Integer, ActuatorCharacteristics> e : getActuatorCharacteristics().entrySet()) {
            if (CloudSim.getEntityName(e.getKey()).contains(suffix))
                return e.getValue();
        }
        return null;
    }

    public List<Actuator> getActuators() {
        return actuators;
    }

    public void setActuators(List<Actuator> actuators) {
        this.actuators = actuators;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

}
