package org.hu.algorithm.GA;

import javafx.util.Pair;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.scheduler.TupleScheduler;
import org.hu.Enums;
import org.hu.algorithm.Utils;

import java.util.*;

public class GA {

    private int populationSize = 200;
    private int iterations = 400;
    private double eliteRate = 0.2;
    private double survivalRate = 0.4;
    private double crossoverRate = 0.35;
    private double mutationBitRate = 0.17;
    private double mutationRate = 0.07;

    private List<FogDevice> fogDevicesList;


    public Map<String, Integer> getGAResourceAllocationPolicy(int currentDeviceId, Tuple tuple, List<FogDevice> deviceList, int controllerId) {
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

        fogDevicesList = deviceList;

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
            if (fogDevice.getFogDeviceType() == Enums.CLOUD
                    || fogDevice.getFogDeviceType() == Enums.EDGE_SERVER
                    || fogDevice.getFogDeviceType() == Enums.PROXY) {
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
                individual.setFitness(getFitness(currentDeviceId, individual, application));
            }
            Collections.sort(population);
            //死亡群体不进入下一代
            int survivalNum = (int) (survivalRate * populationSize);
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
                if (mutation()) {
                    Individual individual = survivors.get(p);
//                newPopulation.add(individual);
                    List<Pair<String, Integer>> chromosome = individual.getChromosome();
                    int mutationNum = (int) (mutationBitRate * chromosome.size());
                    List<Integer> indexList = getRandomChromosomeIndex(mutationNum, chromosome.size());
                    for (Integer index : indexList) {
                        Pair<String, Integer> oldPair = chromosome.get(index);
                        int randomDeviceId = Utils.getRandomDeviceId(fogResourceList);
                        Pair<String, Integer> pair = new Pair<>(oldPair.getKey(), randomDeviceId);
                        chromosome.set(index, pair);
                    }
                }

            }
            newPopulation.addAll(survivors);

            population = newPopulation;


        }
        Individual topIndividual = population.get(0);
        for (Pair<String, Integer> pair : topIndividual.getChromosome()) {
            resourceAllocationPolicy.put(pair.getKey(), pair.getValue());
        }

        System.out.println(resourceAllocationPolicy);
        System.out.println(getFitness(currentDeviceId, topIndividual, application));
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
        /**
         * 初始化染色体
         */
        for (int i = 0; i < chromosomeF.size(); i++) {
            chromosomeChild1.add(new Pair<String, Integer>("",0));
            chromosomeChild2.add(new Pair<String, Integer>("",0));
        }


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
    private double getFitness(int currentDeviceId, Individual individual,Application application) {
        List<Pair<String, Integer>> chromosome = individual.getChromosome();
        //TODO 适应度函数改为时间 cpulength/mips + bwlength/bw + waitingTime

        double executeTime = getExecuteTime(chromosome, application);
        double transTime = getTransTime(currentDeviceId, chromosome, application);
        double waitingTime = getQueueWaitingTime(chromosome, application);
        double fitness = executeTime + transTime + waitingTime;

//        double waitingTupleNum = 0;
//        for (Pair<String, Integer> genBit : chromosome) {
//            FogDevice targetDevice = (FogDevice) CloudSim.getEntity(genBit.getValue());
//            List<Vm> vmList = targetDevice.getHost().getVmList();
//            for (Vm vm : vmList) {
//                AppModule module = (AppModule) vm;
//                if (module.getName().equals(genBit.getKey())) {
//                    List<ResCloudlet> execList = ((TupleScheduler) vm.getCloudletScheduler())
//                            .getCloudletExecList();
//                    waitingTupleNum += execList.size();
//
//
//                }
//
//            }
//        }
        return fitness;
    }



    /**
     * 评估Tuple的计算时间开销
     * @param chromosome
     * @param application
     * @return
     */
    private double getExecuteTime(List<Pair<String, Integer>> chromosome, Application application) {
        double executeTime = 0;
        for (Pair<String, Integer> pair : chromosome) {
            List<AppEdge> edgeList = application.getEdgeByDestModuleName(pair.getKey());
            if (edgeList.size() > 0) {
                FogDevice targetDevice = getFogDeviceById(pair.getValue());
                double mips = targetDevice.getModuleMipsByModuleName(pair.getKey());
                int cpuLength = 0;
                for (AppEdge edge : edgeList) {
                    cpuLength += edge.getTupleCpuLength();
                }
                executeTime += cpuLength / mips;
            }
        }
        return executeTime;
    }

    /**
     * 评估参数染色体对应策略下的tuple等待处理时间
     * @param chromosome
     * @param application
     * @return
     */
    private double getQueueWaitingTime(List<Pair<String, Integer>> chromosome, Application application) {
        double waitingTime = 0;
        for (Pair<String, Integer> pair : chromosome) {
            String moduleName = pair.getKey();
            FogDevice targetDevice = getFogDeviceById(pair.getValue());
            int tupleCpuLength = 0;
            double moduleMips = 0;
            for (Vm vm : targetDevice.getVmList()) {
                AppModule module = (AppModule) vm;
                if (module.getName().equals(moduleName)) {
                    moduleMips = targetDevice.getModuleMipsByModuleName(pair.getKey());
                    TupleScheduler tupleScheduler = (TupleScheduler) module.getCloudletScheduler();
                    List<ResCloudlet> resCloudletList = tupleScheduler.getCloudletExecList();

                    for (ResCloudlet resCloudlet : resCloudletList) {
                        Tuple tuple = (Tuple) resCloudlet.getCloudlet();
                        tupleCpuLength += tuple.getCloudletLength();
                    }
                }
            }
            waitingTime += (tupleCpuLength / moduleMips);
        }
        return waitingTime;
    }

    /**
     * 评估参数染色体对应的传输时延
     *
     * @param chromosome
     * @param application
     * @return
     */
    private double getTransTime(int currentDeviceId, List<Pair<String, Integer>> chromosome, Application application) {
        double transTime = 0;
        for (AppEdge edge : application.getEdges()) {
            int targetDevice = getTargetDeviceByModuleNameInChromosome(chromosome, edge.getDestination());
            if (targetDevice > 0) {
                int srcDeviceId = getTargetDeviceByModuleNameInChromosome(chromosome, edge.getSource());
                if (srcDeviceId > 0) {
                    transTime += getLatencyByDeviceIds(srcDeviceId, targetDevice, edge);
                } else {
                    // GA处理的入口module
                    transTime += getLatencyByDeviceIds(currentDeviceId, targetDevice, edge);
                }
            }
        }
        return transTime;
    }

    private int getTargetDeviceByModuleNameInChromosome(List<Pair<String, Integer>> chromosome, String moduleName) {
        for (Pair<String, Integer> pair : chromosome) {
            if (pair.getKey().equals(moduleName)) {
                return pair.getValue();
            }
        }
        return -1;
    }



    /**
     * 返回入参edge从src发往dest的传输时延，不考虑发送队列等待时间
     * @param srcId
     * @param destId
     * @param edge
     * @return
     */
    private double getLatencyByDeviceIds(int srcId, int destId, AppEdge edge) {
        if (srcId == destId) {
            return 0;
        }
        double latency = 0;
        int srcType = getFogDeviceById(srcId).getFogDeviceType();
        int destType = getFogDeviceById(destId).getFogDeviceType();
        if (srcType == Enums.EDGE_SERVER) {
            //Edge -> Edge
            if (destType == Enums.EDGE_SERVER || destType == Enums.PROXY) {
                double transTime = edge.getTupleNwLength() / getFogDeviceById(srcId).getUplinkBandwidth();
                return latency += transTime;
            } else {
                //Edge -> Cloud
                latency += edge.getTupleNwLength() / getFogDeviceById(srcId).getUplinkBandwidth();
                int proxyId = getFogDeviceById(srcId).getParentId();
                latency += edge.getTupleNwLength() / getFogDeviceById(proxyId).getUplinkBandwidth();
                return latency;
            }
        } else if (srcType == Enums.PROXY) {
            // proxy -> cloud
            if (destType == Enums.CLOUD) {
                return latency += edge.getTupleNwLength() / getFogDeviceById(srcId).getUplinkBandwidth();
            } else {
                // proxy -> edge
                return latency += edge.getTupleNwLength() / getFogDeviceById(srcId).getDownlinkBandwidth();
            }
        } else {
            // cloud -> proxy
            if (destType == Enums.PROXY) {
                return edge.getTupleNwLength() / getFogDeviceById(srcId).getDownlinkBandwidth();
            }else{
                latency += edge.getTupleNwLength() / getFogDeviceById(srcId).getDownlinkBandwidth();
                //TODO 暂时默认仿真环境在一个proxy下
                int proxyId = getFogDeviceById(srcId).getChildrenIds().get(0);
                latency += edge.getTupleNwLength() / getFogDeviceById(proxyId).getDownlinkBandwidth();
                return latency;
            }
        }
    }



    private int checkDeviceType(int deviceId) {
        for (FogDevice fogDevice : fogDevicesList) {
            if (fogDevice.getId() == deviceId) {
                return fogDevice.getFogDeviceType();
            }
        }
        return -1;
    }

    private FogDevice getFogDeviceById(int fogDeviceId) {
        for (FogDevice fogDevice : fogDevicesList) {
            if (fogDevice.getId() == fogDeviceId) {
                return fogDevice;
            }
        }
        return null;
    }


    /**
     * 是否进行变异
     * @return
     */
    private boolean mutation() {
        int hitRange = (int) mutationRate * 10000;
        Random random = new Random();
        int luckyNum = random.nextInt(10000);
        if (luckyNum < hitRange) {
            return true;
        } else {
            return false;
        }
    }

}
