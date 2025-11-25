package org.um.feri.ears.algorithms.so.aro;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.abc.ABC;
import org.um.feri.ears.algorithms.so.cs.CS;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.algorithms.so.gsa.GSA;
import org.um.feri.ears.algorithms.so.pso.PSO;
import org.um.feri.ears.algorithms.so.tlbo.TLBO;
import org.um.feri.ears.benchmark.SOBenchmark;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.unconstrained.*;

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
    public static void main(String[] args) {
        var bench =  new AROBenchmark();
        bench.initAllProblems();
        bench.addAlgorithm(new ARO());
        bench.addAlgorithm(new CS());
        bench.addAlgorithm(new DE());
        bench.addAlgorithm(new PSO());
        bench.addAlgorithm(new GSA());
        bench.addAlgorithm(new ABC());
        bench.addAlgorithm(new TLBO());
        bench.run(10);
    }
}
