package org.hu.algorithm.GA;

import javafx.util.Pair;

import java.util.List;

public class Individual {
    private List<Pair<String, Integer>> Chromosome;
    private double fitness;



    public List<Pair<String, Integer>> getChromosome() {
        return Chromosome;
    }

    public void setChromosome(List<Pair<String, Integer>> chromosome) {
        Chromosome = chromosome;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
