package org.hu.algorithm;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.test.perfeval.DCNSFog;
import org.hu.Enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomAllocationPolicy {
    private List<FogDevice> deviceList = DCNSFog.fogDevices;
    private List<Application> applicationList = DCNSFog.allApplication;

    public Map<String, Integer> getRandomAllocationPolicy(String appId) {
        List<FogDevice> edgeServerList = new ArrayList<>();
        for (FogDevice fogDevice : deviceList) {
            if (fogDevice.getFogDeviceType() == Enums.EDGE_SERVER) {
                edgeServerList.add(fogDevice);
            }
        }

        Map<String, Integer> allocationPolicyMap = new HashMap<>();
        for (Application application : applicationList) {
            if (application.getAppId().equals(appId)) {
                for (AppModule module : application.getModules()) {
                    int randomDeviceId = Utils.getRandomDeviceId(edgeServerList);
                    allocationPolicyMap.put(module.getName(), randomDeviceId);
                }
            }
        }
        return allocationPolicyMap;
    }
}
