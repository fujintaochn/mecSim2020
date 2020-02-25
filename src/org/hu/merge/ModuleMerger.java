package org.hu.merge;

import javafx.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;

import java.util.*;

public class ModuleMerger {

    private double cpuLengthThreshold = 0.3;

    /**
     * 返回分组后的module，同组的module分到同一个设备执行
     *
     * @return
     */
    public Map<Integer, List<String>> getMergedModuleGroups(Tuple tuple, int controllerId, int serversNum) {
        Application application = ((Controller) CloudSim.getEntity(controllerId)).getApplications().get(tuple.getAppId());
        int toProcessModuleNum = 0;
        List<String> toProcessModuleList = new ArrayList<>();
        for (String moduleName : tuple.getModuleCompletedMap().keySet()) {
            if (tuple.getModuleCompletedMap().get(moduleName)!=null) {
                toProcessModuleNum += 1;
                toProcessModuleList.add(moduleName);
            }
        }

        //生成汇聚用的EdgeList
//        List<EdgeForMerge> edgeList = new ArrayList<>();
//        for (AppEdge appEdge : application.getEdges()) {
//            EdgeForMerge edge = new EdgeForMerge();
//            edge.setCpuLength(appEdge.getTupleCpuLength());
//            edge.setNwLength(appEdge.getTupleNwLength());
//            edge.addToSrc(appEdge.getSource());
//            edge.addToDest(appEdge.getDestination());
//            edgeList.add(edge);
//        }
//        //对edgeList根据通信量排序
//        edgeList.sort(Comparator.reverseOrder());
//        EdgeForMerge maxEdge = edgeList.get(0);
//        /**
//         * 合并子任务
//         */
//        //删除最大通信量边
//        edgeList.remove(maxEdge);
//        /**
//         * 处理src的入边
//         */
//        for (EdgeForMerge edge : edgeList) {
//            if (maxEdge.getSrc().containsAll(edge.getDest())) {
//                edge.getDest().addAll(maxEdge.getSrc());
//            }
//        }
//        /**
//         * 处理dest的入边
//         */
//        for (EdgeForMerge edge : edgeList) {
//            if (maxEdge.getDest().containsAll(edge.getDest())) {
//                edge.getDest().addAll(maxEdge.getSrc());
//            }
//        }


        /**
         * 矩阵实现
         */
        List<AppEdge> edgeList = new ArrayList<>();
        for (AppEdge edge : application.getEdges()) {
            if (toProcessModuleList.contains(edge.getDestination())) {
                edgeList.add(edge);
            }
        }
        //通信量矩阵
        double[][] nwMatrix = new double[toProcessModuleList.size()][toProcessModuleList.size()];
        for (int i = 0; i < toProcessModuleList.size(); i++) {
            for (int j = 0; j < toProcessModuleList.size(); j++) {
                if (i == j) {
                    continue;
                }
                AppEdge edge = application.getEdgeBySrcAndDest
                        (toProcessModuleList.get(i), toProcessModuleList.get(j));
                if (edge == null) {
                    continue;
                }
                nwMatrix[i][j] = edge.getTupleNwLength();
            }
        }

        //临时测试使用
        serversNum = 3;
        while (toProcessModuleList.size() > serversNum) {
            //找到通信量最大的边的原宿
            Pair<Integer, Integer> srcAndDest = getMaxNwSrcAndDest(nwMatrix);
            String maxSrc = toProcessModuleList.get(srcAndDest.getKey());
            String maxDest = toProcessModuleList.get(srcAndDest.getValue());
            //新的moduleList
            List<String> newModuleList = new ArrayList<>();
            for (String moduleName : toProcessModuleList) {
                if (!(moduleName.equals(maxSrc) || moduleName.equals(maxDest))) {
                    newModuleList.add(moduleName);
                }
            }
            //新module
            newModuleList.add(maxSrc + "#" + maxDest);

            double[][] newNwMatrix = new double[nwMatrix.length - 1][nwMatrix.length - 1];

            for (int i = 0; i < nwMatrix.length; i++) {
                for (int j = 0; j < nwMatrix.length; j++) {
                    if (nwMatrix[i][j] > 0) {
                        //如果遍历到本次迭代汇聚的边
                        if (i == srcAndDest.getKey() && j == srcAndDest.getValue()) {
                            continue;
                        }
                        //in edge
                        if (toProcessModuleList.get(j).equals(maxSrc) || toProcessModuleList.get(j).equals(maxDest)) {
                            newNwMatrix[getIndexInModuleList(newModuleList, toProcessModuleList.get(i))]
                                    [newNwMatrix.length - 1] += nwMatrix[i][j];
                        } else if (toProcessModuleList.get(i).equals(maxSrc) || toProcessModuleList.get(i).equals(maxDest)) {
                            newNwMatrix[newNwMatrix.length - 1]
                                    [getIndexInModuleList(newModuleList, toProcessModuleList.get(j))] += nwMatrix[i][j];
                        } else {
                            newNwMatrix[getIndexInModuleList(newModuleList, toProcessModuleList.get(i))]
                                    [getIndexInModuleList(newModuleList, toProcessModuleList.get(j))] = nwMatrix[i][j];
                        }
                    }
                }
            }

            nwMatrix = newNwMatrix;
            toProcessModuleList = newModuleList;
        }


        Map<Integer, List<String>> resultMap = new HashMap<>();
        Integer groupIndex = 0;
        for (String modules : toProcessModuleList) {
            List<String> moduleGroup = new ArrayList<>();
            String[] modulesString = modules.split("#");
            for (int i = 0; i < modulesString.length; i++) {
                moduleGroup.add(modulesString[i]);
            }
            resultMap.put(groupIndex, moduleGroup);
            groupIndex += 1;
        }

        return resultMap;
    }

    private int getIndexInModuleList(List<String> moduleNameList, String moduleName) {
        for (int i = 0; i < moduleNameList.size(); i++) {
            String[] modules = moduleNameList.get(i).split("#");
            for (int j = 0; j < modules.length; j++) {
                if (moduleName.equals(modules[j])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Pair<Integer, Integer> getMaxNwSrcAndDest(double[][] nwMatrix) {
        double max = 0;
        int maxSrc = 0;
        int maxDest = 0;
        for (int i = 0; i < nwMatrix.length; i++) {
            for (int j = 0; j < nwMatrix.length; j++) {
                if (nwMatrix[i][j] > max) {
                    max = nwMatrix[i][j];
                    maxSrc = i;
                    maxDest = j;
                }
            }
        }
        return (new Pair<Integer, Integer>(maxSrc,maxDest));
    }

}
