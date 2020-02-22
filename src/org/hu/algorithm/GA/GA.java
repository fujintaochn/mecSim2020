package org.hu.algorithm.GA;

import javafx.util.Pair;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.entities.Tuple;
import org.fog.test.perfeval.DCNSFog;
import org.hu.Enums;
import org.hu.algorithm.Utils;

import java.util.*;

public class GA {
    private List<Application> applicationList = DCNSFog.allApplication;

    private int populationSize = 600;
    private int iterations = 600;
    private double eliteRate = 0.2;
    private double survivalRate = 0.8;
    private double crossoverRate = 0.2;
    private double mutationRate = 0.15;


    public Map<String, Integer> getGAResourceAllocationPolicy(Tuple tuple, List<FogDevice> deviceList) {
        Map<String, Integer> resourceAllocationPolicy = new HashMap<>();

        //生成设备id列表，idToDeviceMap
        List<Integer> fogDeviceIdList = new ArrayList<>();
        Map<Integer, FogDevice> idToDeviceMap = new HashMap<>();
        for (FogDevice fogDevice : deviceList) {
            idToDeviceMap.put(fogDevice.getId(), fogDevice);
            fogDeviceIdList.add(fogDevice.getId());
        }
        //生成需要分配计算资源的application
        Application application = null;
        for (Application app : applicationList) {
            if (app.getAppId().equals(tuple.getAppId())) {
                application = app;
            }
        }

        List<AppModule> moduleList = application.getModules();
        List<String> moduleNameList = new ArrayList<>();
        for (AppModule module : moduleList) {
            if (tuple.getModuleCompletedMap().get(module.getName()) == 0) {
                moduleNameList.add(module.getName());
            }
        }
        List<FogDevice> fogResourceList = new ArrayList<>();
        List<Integer> fogResourceIdList = new ArrayList<>();
        for (FogDevice fogDevice : deviceList) {
            if (fogDevice.getFogDeviceType() == Enums.CLOUD || fogDevice.getFogDeviceType() == Enums.EDGE_SERVER) {
                fogResourceList.add(fogDevice);
                fogResourceIdList.add(fogDevice.getId());
            }
        }


        /**
         * 生成原始种群
         */
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Individual individual = getRandomIndividual(moduleNameList, fogResourceIdList);
            population.add(individual);
        }

        /**
         * 开始迭代
         */
        for (int i = 0; i < iterations; i++) {
            /**
             * 计算每一个体的适应度值
             */
            for (Individual individual : population) {
                double fitness = 0;

            }



        }







        return resourceAllocationPolicy;

    }

    /**
     * 生成一条随机染色体
     *
     * @return
     */
    private Individual getRandomIndividual(List<String> moduleNameList, List<Integer> fogResourceIdList) {
        Individual individual = new Individual();
        List<Pair<String, Integer>> chromosome = new ArrayList<>();
        for (String moduleName : moduleNameList) {
            int randomId = Utils.getRandomDeviceIdFromIdList(fogResourceIdList);
            Pair<String, Integer> pair = new Pair<>(moduleName, randomId);
            chromosome.add(pair);
        }
        individual.setChromosome(chromosome);
        return individual;
    }


}
