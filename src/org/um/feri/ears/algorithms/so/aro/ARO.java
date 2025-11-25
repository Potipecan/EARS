package org.um.feri.ears.algorithms.so.aro;

import org.apache.commons.lang.ArrayUtils;
import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.problems.*;
import org.um.feri.ears.util.BoundaryControl;
import org.um.feri.ears.util.annotation.AlgorithmParameter;
import org.um.feri.ears.util.random.RNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ARO extends NumberAlgorithm {
    @AlgorithmParameter(name = "population size")
    private int popSize;
    private int itI, maxIt;
    private ArrayList<NumberSolution<Double>> population;
    private List<Double> upperBounds, lowerBounds, spans;

    public boolean isDebug = false;

    public ARO(){
        this(100);
    }

    public ARO(int popSize) {
        super();
        this.popSize = popSize;

        au = new Author("potipecan", "andraz.podpecan1@student.um.si");
        ai = new AlgorithmInfo("ARO", "Artificial Rabbit Optimization", "");

    }

    private static double[] vectorAddInplace(double[] a, double[] b){
        assert a.length == b.length;

        for(int i = 0; i < a.length; i++){
            a[i] += b[i];
        }
        return a;
    }
    private static double[] vectorSubInplace(double[] a, double[] b){
        assert a.length == b.length;

        for(int i = 0; i < a.length; i++){
            a[i] -= b[i];
        }
        return a;
    }

    private static double[] vectorMulInplace(double[] a, double f){
        for(int i = 0; i < a.length; i++){
            a[i] *= f;
        }
        return a;
    }

    private static double[] vectorMulInplace(double[] a, double[] b){
        assert a.length == b.length;

        for(int i = 0; i < a.length; i++){
            a[i] *= b[i];
        }
        return a;
    }

    /***
     * Does this:
     *     Dim=length(X);
     *     S=(X>Up)+(X<Low);
     *     X=(rand(1,Dim).*(Up-Low)+Low).*S+X.*(~S);
     * @param x array to bound
     */
    private void spaceBoundInplace(double[] x){
        var dim = x.length;
        for(int i = 0; i < dim; i++){
            var v = x[i];
            if (v <= upperBounds.get(i) && v >= lowerBounds.get(i)) continue;
            x[i] = RNG.nextUniform() * spans.get(i) + lowerBounds.get(i);
        }
    }

    private void initPopulation() throws StopCriterionException{
        population = new ArrayList<>();

        for (int i = 0; i < popSize; i++) {
            if (task.isStopCriterion())
                break;
            population.add(task.generateRandomEvaluatedSolution());
        }
    };

    private double[] getRunningOperator(int len, double L){
        var doubles = new double[task.problem.getNumberOfDimensions()];
        var perm = RNG.randomPermutation(len);
        for (int i = 0; i < len; i++) {
            doubles[perm[i]] = L;
        }
        return doubles;
    }

    private int getRandomOtherRabbit(int skipIndex){
        int i = RNG.nextInt(popSize - 1);
        if(i >= skipIndex) i++;
        return i;
    }

    private double[] forage(double[] rabbit1, double[] rabbit2, double[] R){
        assert rabbit1.length == rabbit2.length;
        var newRabbit = new double[rabbit1.length];
        // newPopPos=PopPos(RandInd,:)+R.*( PopPos(i,:)-PopPos(RandInd,:)) + round(0.5*(0.05+rand))*randn; %Eq.(1)
        for (int i = 0; i < rabbit1.length; i++) {
            newRabbit[i] = rabbit2[i]
                    + R[i] * (rabbit1[i] - rabbit2[i])
                    + Math.round(0.5 * (0.05 + RNG.nextUniform())) * RNG.nextGaussian();
        }
        return newRabbit;
    }

    /***
     * Does this, but not stupidly:
     *             Direct2(i,ceil(rand*Dim))=1;
     *             gr=Direct2(i,:); %Eq.(12)
     *             H=((MaxIt-It+1)/MaxIt)*randn; %Eq.(8)
     *             b=PopPos(i,:)+H*gr.*PopPos(i,:); %Eq.(13)
     *             newPopPos=PopPos(i,:)+ R.*(rand*b-PopPos(i,:)); %Eq.(11)
     * @param rabbit rabbit
     * @return new Rabbit position
     */
    private double[] burrow(double[] rabbit, double[] R){
        int index = (int)Math.ceil((rabbit.length - 1) * RNG.nextUniform());
        double H = ((maxIt - itI + 2) / (double)maxIt) * RNG.nextGaussian();
        var b = rabbit.clone();
        b[index] *= H; // H*gr.*PopPos(i,:)
        vectorAddInplace(b, rabbit); // b=PopPos(i,:)+H*gr.*PopPos(i,:);

        vectorMulInplace(b, RNG.nextUniform()); //rand*b
        vectorSubInplace(b, rabbit); // rand*b-PopPos(i,:)
        vectorMulInplace(b, R); // R.*(rand*b-PopPos(i,:))

        return vectorAddInplace(b, rabbit); // newPopPos=PopPos(i,:)+ R.*(rand*b-PopPos(i,:))
    }

    @Override
    public NumberSolution<Double> execute(Task<NumberSolution<Double>, DoubleProblem> task) throws StopCriterionException {
        this.task = task;
        lowerBounds = task.problem.getLowerLimit();
        upperBounds = task.problem.getUpperLimit();
        spans = new ArrayList<>(lowerBounds.size());
        for(int i = 0; i < lowerBounds.size(); i++){
            spans.add(upperBounds.get(i) -  lowerBounds.get(i));
        }

        switch(task.getStopCriterion()){
            case EVALUATIONS -> maxIt = (task.getMaxEvaluations() - popSize) / popSize;
            case ITERATIONS -> maxIt = task.getMaxIterations();
        }

        NumberSolution<Double> bestSolution = null;

        initPopulation();
        ArrayList<double[]> popBuffer = new ArrayList<>(popSize);
        for(int i = 0; i < popSize; i++){
            popBuffer.add(ArrayUtils.toPrimitive(population.get(i).getVariables().toArray(new Double[]{})));
            if(task.problem.isFirstBetter(population.get(i), bestSolution)){
                bestSolution = population.get(i);
            }
        }


        for(itI = 0; itI < maxIt && !task.isStopCriterion();itI++){
            double theta = 2.0 * (1 - (double)(itI + 1) / maxIt);

            for(int ri = 0; ri < popSize && !task.isStopCriterion(); ri++){
                // L=(exp(1)-exp(((It-1)/MaxIt)^2))*(sin(2*pi*rand)); %Eq.(3)
                double L = (Math.E - Math.exp(Math.pow((double)itI / maxIt, 2))) * Math.sin(2 * Math.PI * RNG.nextUniform() * theta);
                int rd = (int)Math.ceil(RNG.nextUniform() * task.problem.getNumberOfDimensions()); //rd=ceil(rand*(Dim));
                var R = getRunningOperator(rd, L);
                var A = 2 * Math.log(1 / RNG.nextUniform()) * theta;  //A=2*log(1/rand)*theta;%Eq.(15)

                var rabbit1 = popBuffer.get(ri);
                double[] newRabbit;
                if(A > 1.0){
                    var rabbit2 = popBuffer.get(getRandomOtherRabbit(ri));
                    newRabbit = forage(rabbit1, rabbit2, R);
                }
                else{
                    newRabbit = burrow(rabbit1, R);
                }
                // feasibility check & evaluation
                var newSpec = new NumberSolution<>(Arrays.stream(newRabbit).boxed().collect(Collectors.toList()));
                if(!task.problem.isFeasible(newSpec)){
                    task.problem.makeFeasible(newSpec, BoundaryControl.BoundaryControlMethod.RANDOM_RESET);
                }
                task.eval(newSpec);

                if(task.problem.isFirstBetter(newSpec, population.get(ri))){
                    population.set(ri, newSpec);
                    popBuffer.set(ri, newRabbit);
                    if(task.problem.isFirstBetter(newSpec, bestSolution)){
                        bestSolution = newSpec;
                        if(isDebug){
                            System.out.printf("New best fitness: %f, iteration: %d", bestSolution.getEval(), itI + 1);
                        }
                    }
                }
            }
            task.incrementNumberOfIterations();
        }
        return bestSolution;
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {

    }
}
