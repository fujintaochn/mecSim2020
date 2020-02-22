package org.hu.algorithm;

import org.fog.entities.FogDevice;

import java.util.List;
import java.util.Random;

public class Utils {

    private static Random random = new Random();


    public static int getRandomDeviceId(List<FogDevice> edgeDeviceList) {
        int index = random.nextInt(edgeDeviceList.size());
        return edgeDeviceList.get(index).getId();
    }

    public static int getRandomDeviceIdFromIdList(List<Integer> fogResourceIdList) {
        int index = random.nextInt(fogResourceIdList.size());
        return fogResourceIdList.get(index);
    }
}
