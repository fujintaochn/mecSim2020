package org.hu.algorithm;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.placement.Controller;
import org.fog.test.perfeval.DCNSFog;
import org.hu.Enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomAllocationPolicy {

    public Map<String, Integer> getRandomAllocationPolicy(String appId,int controllerId) {
        List<FogDevice> edgeServerList = new ArrayList<>();

        Controller controller = (Controller) CloudSim.getEntity(controllerId);
        for (FogDevice fogDevice : controller.getFogDevices()) {
            if (fogDevice.getFogDeviceType() == Enums.EDGE_SERVER
                    ||fogDevice.getFogDeviceType() == Enums.CLOUD) {
                edgeServerList.add(fogDevice);
            }
        }
        Map<String, Application> applications = controller.getApplications();

        Map<String, Integer> allocationPolicyMap = new HashMap<>();

        Application application = applications.get(appId);

        for (AppModule module : application.getModules()) {
            int randomDeviceId = Utils.getRandomDeviceId(edgeServerList);
            allocationPolicyMap.put(module.getName(), randomDeviceId);
        }


        return allocationPolicyMap;
    }

    public Map<String, Integer> getRandomAllocationPolicyAfterMerged(String appId,Map<Integer, List<String>> moduleGroups,int controllerId) {
        List<FogDevice> edgeServerList = new ArrayList<>();

        for (FogDevice fogDevice : ((Controller)CloudSim.getEntity(controllerId)).getFogDevices()) {
            if (fogDevice.getFogDeviceType() == Enums.EDGE_SERVER
                    ||fogDevice.getFogDeviceType() == Enums.CLOUD) {
                edgeServerList.add(fogDevice);
            }
        }

        Map<String, Integer> allocationPolicyMap = new HashMap<>();

        for (int i = 0; i < moduleGroups.size(); i++) {
            int randomDeviceId = Utils.getRandomDeviceId(edgeServerList);
            List<String> moduleNamesInAGroup = moduleGroups.get(i);
            for (String moduleName : moduleNamesInAGroup) {
                allocationPolicyMap.put(moduleName, randomDeviceId);
            }
        }

        return allocationPolicyMap;
    }
}
