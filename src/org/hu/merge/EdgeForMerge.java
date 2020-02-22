package org.hu.merge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdgeForMerge implements Comparable<EdgeForMerge>{
    private Set<String> src = new HashSet<>();
    private Set<String> dest = new HashSet<>();
    private double cpuLength;
    private double nwLength;


    public void addToSrc(String moduleName) {
        this.src.add(moduleName);
    }

    public void addToDest(String moduleName) {
        this.dest.add(moduleName);
    }

    public Set<String> getSrc() {
        return src;
    }

    public void setSrc(Set<String> src) {
        this.src = src;
    }

    public Set<String> getDest() {
        return dest;
    }

    public void setDest(Set<String> dest) {
        this.dest = dest;
    }

    public double getCpuLength() {
        return cpuLength;
    }

    public void setCpuLength(double cpuLength) {
        this.cpuLength = cpuLength;
    }

    public double getNwLength() {
        return nwLength;
    }

    public void setNwLength(double nwLength) {
        this.nwLength = nwLength;
    }

    @Override
    public int compareTo(EdgeForMerge o) {
        return (int)(this.nwLength-o.nwLength);
    }
}
