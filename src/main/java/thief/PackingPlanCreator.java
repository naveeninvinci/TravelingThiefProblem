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
        //System.out.println("solution" + s);
        TravellingThiefEnvironment problem = (TravellingThiefEnvironment) environment;
        //System.out.println("no of items " + problem.getTravellingThiefProblem().numOfItems);

        //generate packing list of all ones just for now
        List<java.lang.Boolean> packingPlan = new ArrayList<>(problem.getTravellingThiefProblem().numOfItems);

        for (int i = 1; i <= problem.getTravellingThiefProblem().numOfItems; i++) {
            packingPlan.add(Boolean.TRUE);
        }

        Solution thiefSolution = problem.getTravellingThiefProblem().evaluate(s, packingPlan);
        a.setThiefSolution(thiefSolution);
        //a.setSolution(thiefSolution);

        return true;
    }

}

