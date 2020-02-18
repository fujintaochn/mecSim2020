package org.hu.algorithm;

import org.fog.entities.FogDevice;

import java.util.List;
import java.util.Random;

public class Utils {



    public static int getRandomDeviceId(List<FogDevice> edgeDeviceList) {
        Random random = new Random();
        int index = random.nextInt(edgeDeviceList.size());
        return edgeDeviceList.get(index).getId();
    }
}
