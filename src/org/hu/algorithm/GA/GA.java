package org.hu.algorithm.GA;

import com.sun.org.apache.xerces.internal.impl.dv.xs.IntegerDV;
import javafx.util.Pair;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.test.perfeval.DCNSFog;
import org.hu.Enums;
import org.hu.algorithm.Utils;

import java.util.*;

public class GA {

    private int populationSize = 100;
    private int iterations = 100;
    private double eliteRate = 0.2;
    private double survivalRate = 0.8;
    private double crossoverRate = 0.2;
    private double mutationRate = 0.15;


    public Map<String, Integer> getGAResourceAllocationPolicy(Tuple tuple, List<FogDevice> deviceList,int controllerId) {
        Map<String, Integer> resourceAllocationPolicy = new HashMap<>();

//        //生成设备id列表，idToDeviceMap
//        List<Integer> fogDeviceIdList = new ArrayList<>();
//        Map<Integer, FogDevice> idToDeviceMap = new HashMap<>();
//        for (FogDevice fogDevice : deviceList) {
//            idToDeviceMap.put(fogDevice.getId(), fogDevice);
//            fogDeviceIdList.add(fogDevice.getId());
//        }
        //生成需要分配计算资源的application
        Controller controller = (Controller) CloudSim.getEntity(controllerId);
        Application application = controller.getApplications().get(tuple.getAppId());


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
        List<Individual> population = new LinkedList<>();
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
                individual.setFitness(getFitness(individual));
            }
            Collections.sort(population,Collections.reverseOrder());
            //死亡群体不进入下一代
            int survivalNum = (int)(survivalRate * populationSize);
            List<Individual> survivors = population.subList(0, survivalNum);
            List<Individual> newPopulation = new LinkedList<>();
            /**
             * 精英保持不变
             * 交配产生新个体
             * 淘汰死亡群体
             */
//            //精英保持不变放入新种群
            int indexElite = (int) (population.size() * eliteRate);
//            newPopulation.addAll(population.subList(0, indexElite));
            //交配
            int indexCrossover = survivors.size();
            int j = 0;
            List<Individual> offsprings = new LinkedList<>();
            while (j + 1 < indexCrossover) {
                Individual father = survivors.get(j);
                Individual mother = survivors.get(j + 1);
                List<Individual> offspring = getOffspring(father, mother);
                for (Individual individual : offspring) {
                    offsprings.add(individual);
                }
                j += 2;
            }
            /**
             * 变异
             */
            for (int p = indexElite; p < survivors.size(); p++) {
                Individual individual = survivors.get(p);
//                newPopulation.add(individual);
                List<Pair<String, Integer>> chromosome = individual.getChromosome();
                int mutationNum = (int) (mutationRate * chromosome.size());
                List<Integer> indexList = getRandomChromosomeIndex(mutationNum, chromosome.size());
                for (Integer index : indexList) {
                    Pair<String, Integer> oldPair = chromosome.get(index);
                    int randomDeviceId = Utils.getRandomDeviceId(fogResourceList);
                    Pair<String, Integer> pair = new Pair<>(oldPair.getKey(), randomDeviceId);
                    chromosome.set(index, pair);
                }
            }
            newPopulation.addAll(survivors);

            population = newPopulation;


        }
        Individual topIndividual = population.get(0);
        for (Pair<String, Integer> pair : topIndividual.getChromosome()) {
            resourceAllocationPolicy.put(pair.getKey(), pair.getValue());
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


    private List<Individual> getOffspring(Individual father, Individual mother) {
        List<Pair<String, Integer>> chromosomeF = father.getChromosome();
        List<Pair<String, Integer>> chromosomeM = mother.getChromosome();
        int crossoverNum = (int) (crossoverRate * chromosomeF.size());

        List<Integer> indexList = getRandomChromosomeIndex(crossoverNum, chromosomeM.size());

        Individual child1 = new Individual();
        Individual child2 = new Individual();
        List<Pair<String, Integer>> chromosomeChild1 = new ArrayList<>();
        List<Pair<String, Integer>> chromosomeChild2 = new ArrayList<>();
        //交换选中的基因位
        for (Integer index : indexList) {
            chromosomeChild1.set(index, chromosomeM.get(index));
            chromosomeChild2.set(index, chromosomeF.get(index));
        }
        for (int i = 0; i < chromosomeChild1.size(); i++) {
            if (!indexList.contains(i)) {
                chromosomeChild1.set(i, chromosomeF.get(i));
                chromosomeChild2.set(i, chromosomeM.get(i));
            }
        }
        child1.setChromosome(chromosomeChild1);
        child2.setChromosome(chromosomeChild2);
        List<Individual> offspring = new ArrayList<>();
        offspring.add(child1);
        offspring.add(child2);
        return offspring;
    }

    private List<Integer> getRandomChromosomeIndex(int crossoverNum, int chromosomeSize) {
        List<Integer> indexList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < crossoverNum; i++) {
            Integer index = random.nextInt(chromosomeSize);
            if (!indexList.contains(index)) {
                indexList.add(index);
            }
        }
        return indexList;
    }
    //当前适应度函数为每个基因位上的module 到目标device后，等待任务数量
    private double getFitness(Individual individual) {
        List<Pair<String, Integer>> chromosome = individual.getChromosome();
        double waitingTupleNum = 0;
        for (Pair<String, Integer> genBit : chromosome) {
            FogDevice targetDevice = (FogDevice) CloudSim.getEntity(genBit.getValue());
            List<AppModule> moduleList = targetDevice.getHost().getVmList();
            for (AppModule appModule : moduleList) {
                if (appModule.getName().equals(genBit.getKey())) {
                    CloudletSchedulerTimeShared cloudletScheduler = (CloudletSchedulerTimeShared) appModule.getCloudletScheduler();
                    waitingTupleNum += cloudletScheduler.getCloudletExecList().size();
                }
            }
        }
        return waitingTupleNum;
    }
}
