package thief;

import isula.aco.*;
import model.Solution;
import model.TravelingThiefProblem;

import javax.sound.midi.SysexMessage;
import java.util.*;
import java.util.stream.Collectors;


public class PackingPlanCreator<C, E extends Environment> extends AntPolicy<C, E> {

    public PackingPlanCreator() { super(AntPolicyType.AFTER_SOLUTION_IS_READY);  }

    private Random random = new Random();

    private Boolean useRandomPackingPlan = Boolean.FALSE;

    private int alphaPower = 2;

    @Override
    public boolean applyPolicy(E environment, ConfigurationProvider configurationProvider) {

        //this gets called after every ant has finished its tour
        AntForTravellingThief a = (AntForTravellingThief)getAnt();
        List<Integer> s = a.getSolution();
        TravellingThiefEnvironment thiefEnvironment = (TravellingThiefEnvironment) environment;
        //add items into the packing list until the max weight of the knapsack is reached
        List<java.lang.Boolean> packingPlan = new ArrayList<>();

        if(useRandomPackingPlan){
            this.getRandomPackingPlan(a, thiefEnvironment, packingPlan);
        }
        else{
            this.getWeightedPackingPlan(a, thiefEnvironment, packingPlan);
        }

        Solution thiefSolution = thiefEnvironment.getTravellingThiefProblem().evaluate(s, packingPlan);
        a.setThiefSolution(thiefSolution);

        return true;
    }

    private List<java.lang.Boolean> getRandomPackingPlan(AntForTravellingThief ant, TravellingThiefEnvironment environment, List<Boolean> packingPlan){

        TravelingThiefProblem problem = environment.getTravellingThiefProblem();
        int maxWeight = problem.maxWeight;
        double[] itemWeights = problem.weight;
        int weightOfKnapsack = 0;
        int numOfItems = problem.numOfItems;


        //add items into the packing list until the max weight of the knapsack is reached
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
        return packingPlan;
    }

    private List<java.lang.Boolean> getWeightedPackingPlan(AntForTravellingThief ant, TravellingThiefEnvironment environment, List<Boolean> packingPlan){
        TravelingThiefProblem problem = environment.getTravellingThiefProblem();
        int[] cityOfItem = problem.cityOfItem;

        //for each city, how far is it from the end of the route
        List<Double> distanceFromCityToEnd = new ArrayList(problem.numOfCities);
        List<Integer> route = ant.getSolution();

        List<Double> scores = new ArrayList<>(problem.numOfItems);
        HashMap<Integer, Double> scoresMap = new HashMap<Integer, Double>(problem.numOfItems);

      //  System.out.println("route" + route);

        double totalDistance = 0;
        //distance from last city to the end is zero
        distanceFromCityToEnd.add(0.0);

        //iterate backwards through the route
        for (int solutionIndex = route.size()-1; solutionIndex > 0; solutionIndex -= 1) {
            int previousSolutionIndex = solutionIndex - 1;
            double distanceBetween = AntForTravellingThief.getDistance(route.get(previousSolutionIndex), route.get(solutionIndex), environment);
            totalDistance += distanceBetween;
            distanceFromCityToEnd.add(0, totalDistance);
        }

     //   System.out.println("distanceFromCityToEnd final" + distanceFromCityToEnd);

        for (int i = 0; i < problem.numOfItems; i++) {
//            System.out.println("\nItem " + i);
              //System.out.println("Weight " + problem.weight[i]);
             // System.out.println("Profit " + problem.profit[i]);
//            System.out.println("City Of Item " + cityOfItem[i]);
            //System.out.println("Distance from city to end " + distanceFromCityToEnd.get(cityOfItem[i]));

            double weight = Math.pow(problem.weight[i], alphaPower);
            double profit = Math.pow(problem.profit[i], alphaPower);

            //System.out.println("Weight after " + weight + " alpha " + alphaPower);
           // System.out.println("Profit after" + profit);

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

            //System.out.println("Score" + score);
            scores.add(score);
            scoresMap.put(i, score);
        }

       // System.out.println("Scores" + scores);
       // System.out.println("Scores map" + scoresMap);

        //now sort items by scores
        HashMap<Integer, Double> sortedScores = this.sortByValueDescending(scoresMap);
        //System.out.println("Sorted scores map" + sortedScores);

        int weightOfKnapsack = 0;
        //fill plan to be false by default
        for (int i = 0; i < problem.numOfItems; i++) {
            packingPlan.add(Boolean.FALSE);
        }

     //   System.out.println("initial plan" + packingPlan);

        // Iterating HashMap through for loop
        for (Map.Entry<Integer, Double> set : sortedScores.entrySet()) {
            // Printing all elements of a Map
            double itemWeight = problem.weight[set.getKey()];

            if((weightOfKnapsack + itemWeight) < problem.maxWeight){
                packingPlan.set(set.getKey(), Boolean.TRUE);
                weightOfKnapsack += itemWeight;
            }

        }

      //  System.out.println("packing plan " + packingPlan);

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

    // function to sort hashmap by values
    public static HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> hm)
    {
        HashMap<Integer, Double> temp
                = hm.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> i1.getValue().compareTo(
                        i2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        return temp;
    }



}

