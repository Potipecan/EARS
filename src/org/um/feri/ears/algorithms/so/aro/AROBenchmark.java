package org.um.feri.ears.algorithms.so.aro;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.abc.ABC;
import org.um.feri.ears.algorithms.so.cs.CS;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.algorithms.so.de.jade.JADE;
import org.um.feri.ears.algorithms.so.gsa.GSA;
import org.um.feri.ears.algorithms.so.gwo.GWO;
import org.um.feri.ears.algorithms.so.pso.PSO;
import org.um.feri.ears.algorithms.so.random.RandomSearch;
import org.um.feri.ears.algorithms.so.tlbo.TLBO;
import org.um.feri.ears.benchmark.*;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.unconstrained.*;

import java.io.Console;
import java.util.ArrayList;

public class AROBenchmark extends SOBenchmark<NumberSolution<Double>, NumberSolution<Double>, DoubleProblem, NumberAlgorithm> {
    public AROBenchmark() {
        super();
        stopCriterion = StopCriterion.ITERATIONS;
        maxIterations = 1000;
        maxEvaluations = 0;
        timeLimit = 0;
    }

    @Override
    protected void addTask(DoubleProblem problem, StopCriterion stopCriterion, int maxEvaluations, long time, int maxIterations) {
        tasks.add(new Task<>(problem, stopCriterion, maxEvaluations, time, maxIterations));
    }

    @Override
    public void initAllProblems() {
        addTask(new Sphere(30), stopCriterion, maxIterations, timeLimit, maxIterations);
        addTask(new Schwefel222(30), stopCriterion, maxIterations, timeLimit, maxIterations);
        addTask(new Schwefel12(30), stopCriterion, maxIterations, timeLimit, maxIterations);
        addTask(new Schwefel221(), stopCriterion, maxIterations, timeLimit, maxIterations);
        addTask(new RosenbrockD2a(30), stopCriterion, maxIterations, timeLimit, maxIterations);
        addTask(new Step2(30), stopCriterion, maxIterations, timeLimit, maxIterations);
        addTask(new Quartic(30), stopCriterion, maxIterations, timeLimit, maxIterations);
    }
}

class AroBenchmarkRun{
    private static ArrayList<NumberAlgorithm> getAroAlgorithms(){
        var algorithms = new ArrayList<NumberAlgorithm>();
        algorithms.add(new ARO());
        algorithms.add(new CS());
        algorithms.add(new DE());
        algorithms.add(new PSO());
        algorithms.add(new GSA());
        algorithms.add(new ABC());
        algorithms.add(new TLBO());
        return algorithms;
    }

    private static ArrayList<NumberAlgorithm> getTestAlgorithms(){
        var algorithms = new ArrayList<NumberAlgorithm>();
        algorithms.add(new ARO());
        algorithms.add(new RandomSearch());
        algorithms.add(new ABC());
        algorithms.add(new PSO());
        algorithms.add(new GWO());
        algorithms.add(new JADE());
        return algorithms;
    }

    public static void main(String[] args) {
        SOBenchmark<NumberSolution<Double>, NumberSolution<Double>, DoubleProblem, NumberAlgorithm> bench = null;
        ArrayList<NumberAlgorithm> algoritms = new ArrayList<>();
        switch (args[0]){
            case "aro":{
                bench = new AROBenchmark();
                algoritms = getAroAlgorithms();
                break;
            }
            case "rpuoed30":{
                bench = new RPUOed30Benchmark();
                algoritms = getTestAlgorithms();
                break;
            }
            case "cec2017":{
                bench = new CEC2017Benchmark();
                algoritms = getTestAlgorithms();
                break;
            }
            default:{
                System.exit(1);
            }
        }

        bench.addAlgorithms(algoritms);
        bench.initAllProblems();
        bench.setDisplayRatingCharts(true);
        bench.setDisplayAdvancedStats(true);



        bench.run(10);
        bench.
//        System.exit(0);
    }
}
