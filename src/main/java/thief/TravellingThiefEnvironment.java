package thief;

import isula.aco.Environment;
import isula.aco.tsp.EdgeWeightType;
import model.TravelingThiefProblem;

import java.util.logging.Logger;

/**
 * The Environment type is for storing problem-specific information. In the TSP scenario, is only related
 * to the number of cities.  This contains the TravellingThiefProblem class which holds all info for that problem
 */
public class TravellingThiefEnvironment extends Environment {

    private static Logger logger = Logger.getLogger(TravellingThiefEnvironment.class.getName());

    private final double[][] problemRepresentation;
    private final EdgeWeightType edgeWeightType;
    private final TravelingThiefProblem travellingThiefProblem;

    public TravellingThiefEnvironment(EdgeWeightType edgeWeightType, TravelingThiefProblem travellingThiefProblem) {
        super();
        this.problemRepresentation = travellingThiefProblem.coordinates;
        this.travellingThiefProblem = travellingThiefProblem;
        this.setPheromoneMatrix(createPheromoneMatrix());
        this.edgeWeightType = edgeWeightType;

        int numberOfCities = travellingThiefProblem.numOfCities;

        logger.info("Number of cities: " + numberOfCities);
        logger.info("Edge weight type: " + this.edgeWeightType);
        logger.info("Number of items: " + this.travellingThiefProblem.numOfItems);

    }

    public TravelingThiefProblem getTravellingThiefProblem() { return this.travellingThiefProblem; }

    public int getNumberOfCities() {
        return getProblemRepresentation().length;
    }

    public double[][] getProblemRepresentation() {
        return this.problemRepresentation;
    }


    /**
     * The pheromone matrix in the TSP problem stores a pheromone value per city and per position of this city on
     * the route. That explains the dimensions selected for the pheromone matrix.
     *
     * @return Pheromone matrix instance.
     */
    @Override
    protected double[][] createPheromoneMatrix() {
        if (this.problemRepresentation != null) {
            int numberOfCities = getNumberOfCities();
            return new double[numberOfCities][numberOfCities];
        }

        return null;

    }

    public EdgeWeightType getEdgeWeightType() {
        return edgeWeightType;
    }
}
