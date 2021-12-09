package thief;

import isula.aco.*;
import model.Solution;
import model.TravelingThiefProblem;

import java.util.*;

/**
 * This class works out a packing plan for a particular tour.
 * Can use either a random picker or a weighted packing algorithm based on Faulkner 2015 paper.
 * @param <C>
 * @param <E>
 * @author Sarah
 */
public class PackingPlanCreator<C, E extends Environment> extends AntPolicy<C, E> {

    public PackingPlanCreator() { super(AntPolicyType.AFTER_SOLUTION_IS_READY);  }

    private Random random = new Random();

    //switches between random and weighted algorithm
    private Boolean useRandomPackingPlan = Boolean.TRUE;

    //profit and time are raised to power of this coefficient
    private int alphaPower = 2;

    @Override
    public boolean applyPolicy(E environment, ConfigurationProvider configurationProvider) {

        //this gets called after every ant has finished its tour
        AntForTravellingThief a = (AntForTravellingThief)getAnt();
        List<Integer> s = a.getSolution();
        TravellingThiefEnvironment thiefEnvironment = (TravellingThiefEnvironment) environment;

        List<java.lang.Boolean> packingPlan = new ArrayList<>();

        if(useRandomPackingPlan){
            this.getRandomPackingPlan(thiefEnvironment, packingPlan);
        }
        else{
            this.getWeightedPackingPlan(a, thiefEnvironment, packingPlan);
        }

        //update the Ant with the solution
        Solution thiefSolution = thiefEnvironment.getTravellingThiefProblem().evaluate(s, packingPlan);
        a.setThiefSolution(thiefSolution);

        return true;
    }

    /**
     * This packs items randomly until the max knapsack weight is reached.
     * @param environment
     * @param packingPlan
     * @return
     */
    private List<java.lang.Boolean> getRandomPackingPlan(TravellingThiefEnvironment environment, List<Boolean> packingPlan){

        TravelingThiefProblem problem = environment.getTravellingThiefProblem();
        int maxWeight = problem.maxWeight;
        double[] itemWeights = problem.weight;
        int weightOfKnapsack = 0;

        //add items into the packing list until the max weight of the knapsack is reached
        for (int i = 0; i < problem.numOfItems; i++) {
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
        return packingPlan;
    }

    /**
     * This method gives each item a score, based on its profit, weight and how far it has to be carried until the
     * end of the tour.
     *
     * The list is then sorted and items with the highest score are picked until the knapsack weight is reached.
     *
     * @param ant
     * @param environment
     * @param packingPlan
     * @return
     */
    private List<java.lang.Boolean> getWeightedPackingPlan(AntForTravellingThief ant, TravellingThiefEnvironment environment, List<Boolean> packingPlan){
        TravelingThiefProblem problem = environment.getTravellingThiefProblem();
        int[] cityOfItem = problem.cityOfItem;

        List<Double> distanceFromCityToEnd = new ArrayList(problem.numOfCities);
        List<Integer> route = ant.getSolution();

        //List<Double> scores = new ArrayList<>(problem.numOfItems);
        HashMap<Integer, Double> scoresMap = new HashMap<Integer, Double>(problem.numOfItems);

        double totalDistance = 0;
        //distance from last city to the end is zero
        distanceFromCityToEnd.add(0.0);

        //iterate backwards through the route, working out how far each city is from the end of  the tour
        for (int solutionIndex = route.size()-1; solutionIndex > 0; solutionIndex -= 1) {
            int previousSolutionIndex = solutionIndex - 1;
            double distanceBetween = AntForTravellingThief.getDistance(route.get(previousSolutionIndex), route.get(solutionIndex), environment);
            totalDistance += distanceBetween;
            distanceFromCityToEnd.add(0, totalDistance);
        }

        //now go through and score each item
        for (int i = 0; i < problem.numOfItems; i++) {

            double weight = Math.pow(problem.weight[i], alphaPower);
            double profit = Math.pow(problem.profit[i], alphaPower);

            double distanceToEnd = distanceFromCityToEnd.get(cityOfItem[i]);
            Double score = 0.0;
            if(distanceToEnd != 0){
                score = profit / (weight * distanceToEnd);
            }
            else{
                //if distance to end is zero it means we are at the end so just use profit/weight otherwise
                //will be dividing by zero
                score = profit/weight;
            }
            scoresMap.put(i, score);
        }

        //now sort items by scores with best items at the top
        HashMap<Integer, Double> sortedScores = this.sortByValueDescending(scoresMap);

        int weightOfKnapsack = 0;

        //set everything to be not picked and then add items in
        for (int i = 0; i < problem.numOfItems; i++) {
            packingPlan.add(Boolean.FALSE);
        }

        //now pick from best times until max weight is reached
        for (Map.Entry<Integer, Double> set : sortedScores.entrySet()) {
            double itemWeight = problem.weight[set.getKey()];

            if((weightOfKnapsack + itemWeight) <= problem.maxWeight){
                packingPlan.set(set.getKey(), Boolean.TRUE);
                weightOfKnapsack += itemWeight;
            }

            //we are completely full
            if(weightOfKnapsack == problem.maxWeight){ break;}
        }

        return packingPlan;


    }

    public static HashMap<Integer, Double>  sortByValueDescending(HashMap<Integer, Double> unSortedMap){
        //LinkedHashMap preserve the ordering of elements in which they are inserted
        LinkedHashMap<Integer, Double> reverseSortedMap = new LinkedHashMap<>();

        //Use Comparator.reverseOrder() for reverse ordering
        unSortedMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        return reverseSortedMap;
    }
}

