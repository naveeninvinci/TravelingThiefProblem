package thief;

import isula.aco.ConfigurationProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the algorithm configuration of the Ant System algorithm described in
 * Section 6.3 of the Clever Algorithms book by Jason Brownlee.
 */
public class ThiefProblemConfiguration implements ConfigurationProvider {

    private double initialPheromoneValue;

    /**
     * In the algorithm described in the book, the initial pheromone value was a function of the quality of a
     * random solution. That logic is included in this constructor.
     *
     * @param environment TSP environment with coordinate information.
     */
    public ThiefProblemConfiguration(TravellingThiefEnvironment environment) {
        List<Integer> randomSolution = new ArrayList<>();
        int numberOfCities = environment.getProblemRepresentation().length;

        for (int cityIndex = 0; cityIndex < numberOfCities; cityIndex += 1) {
            randomSolution.add(cityIndex);
        }

        Collections.shuffle(randomSolution);

        double randomQuality = AntForTravellingThief.getTotalDistance(randomSolution, environment);
        this.initialPheromoneValue = numberOfCities / randomQuality;
    }

    public int getNumberOfAnts() {
        //sarah
        //return 2;
        return 30;
    }

    public double getEvaporationRatio() {
        return 1 - 0.6;
    }

    public int getNumberOfIterations() {
        //sarah
        //return 1;
        return 500;
    }


    public double getInitialPheromoneValue() {
        return this.initialPheromoneValue;
    }

    @Override
    public double getHeuristicImportance() {
        return 2.5;
    }

    @Override
    public double getPheromoneImportance() {
        return 1.0;
    }


}
