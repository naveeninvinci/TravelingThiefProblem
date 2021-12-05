package thief;

import isula.aco.*;
import model.Solution;

import java.util.ArrayList;
import java.util.List;


public class PackingPlanCreator<C, E extends Environment> extends AntPolicy<C, E> {

    public PackingPlanCreator() { super(AntPolicyType.AFTER_SOLUTION_IS_READY);  }

    @Override
    public boolean applyPolicy(E environment, ConfigurationProvider configurationProvider) {

        //this gets called after every ant has finished its tour
        AntForTravellingThief a = (AntForTravellingThief)getAnt();
        List<Integer> s = a.getSolution();
        TravellingThiefEnvironment problem = (TravellingThiefEnvironment) environment;

        int maxWeight = problem.getTravellingThiefProblem().maxWeight;
        double[] itemWeights = problem.getTravellingThiefProblem().weight;
        int weightOfKnapsack = 0;

        //add items into the packing list until the max weight of the knapsack is reached
        List<java.lang.Boolean> packingPlan = new ArrayList<>(problem.getTravellingThiefProblem().numOfItems);

        for (int i = 0; i < problem.getTravellingThiefProblem().numOfItems; i++) {
            double itemWeight = itemWeights[i];
            weightOfKnapsack += itemWeight;
            if(weightOfKnapsack < maxWeight){
                packingPlan.add(Boolean.TRUE);
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

