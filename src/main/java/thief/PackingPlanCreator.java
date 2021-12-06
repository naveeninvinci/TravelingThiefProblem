package thief;

import isula.aco.*;
import model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PackingPlanCreator<C, E extends Environment> extends AntPolicy<C, E> {

    public PackingPlanCreator() { super(AntPolicyType.AFTER_SOLUTION_IS_READY);  }

    private Random random = new Random();

    @Override
    public boolean applyPolicy(E environment, ConfigurationProvider configurationProvider) {

        //this gets called after every ant has finished its tour
        AntForTravellingThief a = (AntForTravellingThief)getAnt();
        List<Integer> s = a.getSolution();
        TravellingThiefEnvironment problem = (TravellingThiefEnvironment) environment;

        int maxWeight = problem.getTravellingThiefProblem().maxWeight;
        double[] itemWeights = problem.getTravellingThiefProblem().weight;
        int weightOfKnapsack = 0;
        int numOfItems = problem.getTravellingThiefProblem().numOfItems;

        //add items into the packing list until the max weight of the knapsack is reached
        List<java.lang.Boolean> packingPlan = new ArrayList<>();

        for (int i = 0; i < numOfItems; i++) {
            double itemWeight = itemWeights[i];
            //randomly decide whether to pick this or not
            boolean pack = random.nextBoolean();
            if(pack & ((weightOfKnapsack + itemWeight) < maxWeight)){
                packingPlan.add(Boolean.TRUE);
                weightOfKnapsack += itemWeight;
            }
            else{
                packingPlan.add(Boolean.FALSE);
            }
        }
        Solution thiefSolution = problem.getTravellingThiefProblem().evaluate(s, packingPlan);
        a.setThiefSolution(thiefSolution);

        return true;
    }

}

