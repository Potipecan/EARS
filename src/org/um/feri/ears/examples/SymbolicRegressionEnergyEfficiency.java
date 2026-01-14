package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.GPAlgorithm;
import org.um.feri.ears.algorithms.gp.ElitismGPAlgorithm;
import org.um.feri.ears.individual.representations.gp.Node;
import org.um.feri.ears.individual.representations.gp.Target;
import org.um.feri.ears.individual.representations.gp.symbolic.regression.*;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.gp.ProgramProblem;
import org.um.feri.ears.problems.gp.ProgramSolution;
import org.um.feri.ears.problems.gp.SymbolicRegressionProblem;
import org.um.feri.ears.util.random.RNG;

import java.io.*;
import java.util.*;

public class SymbolicRegressionEnergyEfficiency {
    public static String DatasetPath = "datasets/ENB2012_data.csv";
    public static String Delimiter = ",";
    public static String[] XVars = {"X1", "X2", "X3", "X4", "X5", "X6", "X7", "X8"};
    public static String YVar = "Y2";

    public static List<Double> solutionMAE(ProgramSolution solution, List<Target> testSet) {
        var retval = new ArrayList<Double>();
        for (var s : testSet) {
            var r = solution.getTree().evaluate(s.getContextState());
            var err = s.getTargetValue() - r;
            retval.add(err);
        }
        return retval;
    }

    public static void main(String[] args) {


        List<Target> dataset = new ArrayList<>();
        // import dataset
        try (BufferedReader br = new BufferedReader(new FileReader(DatasetPath))) {
            String line;
            Dictionary<String, Integer> varIndex = new Hashtable<>();
            if ((line = br.readLine()) != null) {
                String[] values = line.split(Delimiter);
                for (int i = 0; i < values.length; i++) {
                    var v = values[i].trim();
                    int st = 0, end = v.length();
                    if (v.startsWith("\"") || v.startsWith("'")) st++;
                    if (v.endsWith("\"") || v.endsWith("'")) end--;
                    varIndex.put(values[i].substring(st, end), i);
                }
            } else {
                System.err.println("Dataset file is empty");
                return;
            }

            var yVarI = varIndex.get(YVar);
            var xVarsI = new ArrayList<Integer>();
            for (String xVar : XVars) {
                xVarsI.add(varIndex.get(xVar));
            }
            while ((line = br.readLine()) != null) {
                String[] values = line.split(Delimiter);
                var contextState = new Hashtable<String, Double>();
                for (int i = 0; i < XVars.length; i++) {
                    contextState.put(XVars[i], Double.parseDouble(values[xVarsI.get(i)]));
                }

                var yVal = Double.parseDouble(values[yVarI]);
                dataset.add(new Target(contextState, yVal));
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        // split dataset
        int nTrain = (int) (dataset.size() * 0.8);
        int nTest = dataset.size() - nTrain;
        RNG.shuffle(dataset);

        var trainSet = dataset.subList(0, nTrain);
        var testSet = dataset.subList(nTest, dataset.size());

        //Define base function node types
        List<Class<? extends Node>> baseFunctionNodeTypes = Arrays.asList(
                AddNode.class,
                DivNode.class,
                MulNode.class,
                SubNode.class,
//                SinNode.class,
                LogNode.class
//                RootNode.class
        );

        //Define base terminal node types
        List<Class<? extends Node>> baseTerminalNodeTypes = Arrays.asList(
                ConstNode.class,
//                PiNode.class,
                VarNode.class
        );

        //Define variables
        VarNode.variables = Arrays.asList(XVars);

        SymbolicRegressionProblem sgpTrainingData = new SymbolicRegressionProblem(baseFunctionNodeTypes, baseTerminalNodeTypes, trainSet);

        SymbolicRegressionProblem sgpTestingData = new SymbolicRegressionProblem(baseFunctionNodeTypes, baseTerminalNodeTypes, testSet);

        Task<ProgramSolution, ProgramProblem> symbolicRegressionTask = new Task<>(sgpTrainingData, StopCriterion.EVALUATIONS, 200000, 0, 0);

        GPAlgorithm alg = new ElitismGPAlgorithm();
//        alg.setNumberOfTournaments(2);
//        alg.setElitismProbability(0.95);
//        alg.setCrossoverProbability(0.025);
//        alg.setMutationProbability(0.025);


        int nRepeats = 10;
        String testResultOutputRoot = "outputs";
        String testResultOutputGraphs = testResultOutputRoot + "/trees";
        List<Double> trainFitness = new ArrayList<>();
        List<Double> testFitness = new ArrayList<>();
        List<Integer> nFunctions = new ArrayList<>();
        List<Integer> nTerminals = new ArrayList<>();
        List<List<Double>> errors = new ArrayList<>();
        try {
            for (int i = 0; i < nRepeats; i++) {
                alg.resetToDefaultsBeforeNewRun();
                ProgramSolution solution = alg.execute(symbolicRegressionTask);
                nFunctions.add(solution.getTree().numberOfFunctions());
                nTerminals.add(solution.getTree().numberOfTerminals());
                var trF = solution.getEval();
                sgpTestingData.evaluate(solution);
                var teF = solution.getEval();
                trainFitness.add(trF);
                testFitness.add(teF);
                //Print solution fitness on training data
                System.out.println();
                //Print solution tree
                solution.getTree().displayTree(String.format("%s/tree%d", testResultOutputGraphs, i), false);
                //Print solution equation
                System.out.println(solution);
                errors.add(solutionMAE(solution, testSet));
            }

        } catch (StopCriterionException e) {
            e.printStackTrace();
        }
        // save stats
        try (var fw = new BufferedWriter(new FileWriter(testResultOutputRoot + "/stats.csv"))) {
            fw.write("solution,f_train,f_test,n_functions,n_terminals\n");

            for (int i = 0; i < nRepeats; i++) {
                fw.write(String.format("S%d,%f,%f,%d,%d\n", i, trainFitness.get(i), testFitness.get(i), nFunctions.get(i), nTerminals.get(i)));
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        // save errors
        try (var fw = new BufferedWriter(new FileWriter(testResultOutputRoot + "/errors.csv"))) {
            fw.write("S0");
            for (int i = 1; i < nRepeats; i++) fw.write(",S" + i);
            fw.newLine();
            for (int i = 0; i < nTest; i++) {
                fw.write(errors.get(0).get(i).toString());
                for (int j = 1; j < nRepeats; j++) fw.write("," + errors.get(j).get(i).toString());
                fw.newLine();
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
