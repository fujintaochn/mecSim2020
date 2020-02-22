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
     * @return
     */
    public Map<Integer, List<String>> getMergedModuleGroups(Tuple tuple, int controllerId,int serversNum) {
        Application application = ((Controller) CloudSim.getEntity(controllerId)).getApplications().get(tuple.getAppId());
        int toProcessModuleNum = 0;
        List<String> toProcessModuleList = new ArrayList<>();
        for (String moduleName : tuple.getModuleCompletedMap().keySet()) {
            if (tuple.getModuleCompletedMap().get(moduleName).equals(0)) {
                toProcessModuleNum += 1;
                toProcessModuleList.add(moduleName);
            }
        }

        //生成汇聚用的EdgeList
        List<EdgeForMerge> edgeList = new ArrayList<>();
        for (AppEdge appEdge : application.getEdges()) {
            EdgeForMerge edge = new EdgeForMerge();
            edge.setCpuLength(appEdge.getTupleCpuLength());
            edge.setNwLength(appEdge.getTupleNwLength());
            edge.addToSrc(appEdge.getSource());
            edge.addToDest(appEdge.getDestination());
            edgeList.add(edge);
        }
        //对edgeList根据通信量排序
        edgeList.sort(Comparator.reverseOrder());
        EdgeForMerge maxEdge = edgeList.get(0);
        /**
         * 合并子任务
         */
        //删除最大通信量边
        edgeList.remove(maxEdge);
        /**
         * 处理src的入边
         */
        for (EdgeForMerge edge : edgeList) {
            if (maxEdge.getSrc().containsAll(edge.getDest())) {
                edge.getDest().addAll(maxEdge.getSrc());
            }
        }
        /**
         * 处理dest的入边
         */
        for (EdgeForMerge edge : edgeList) {
            if (maxEdge.getDest().containsAll(edge.getDest())) {
                edge.getDest().addAll(maxEdge.getSrc());
            }
        }









        Map<Integer, List<String>> resultMap = new HashMap<>();
        return resultMap;
    }

    private int getIndexInModuleList(List<String> moduleNameList, String moduleName) {
        for (int i = 0; i < moduleNameList.size(); i++) {
            if (moduleNameList.get(i).equals(moduleName)) {
                return i;
            }
        }
        return -1;
    }

    private Pair<String, String> getMaxNwSrcAndDest(double[][] nwMatrix,List<String> moduleNameList) {
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
        return (new Pair<String, String>(moduleNameList.get(maxSrc), moduleNameList.get(maxDest)));
    }

}
