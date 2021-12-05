package thief;

import algorithms.Algorithm;
import isula.aco.*;
import isula.aco.algorithms.antsystem.OfflinePheromoneUpdate;
import isula.aco.algorithms.antsystem.PerformEvaporation;
import isula.aco.algorithms.antsystem.RandomNodeSelection;
import isula.aco.algorithms.antsystem.StartPheromoneMatrix;
import isula.aco.tsp.*;
import model.Solution;
import model.TravelingThiefProblem;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ACOAlgorithm implements Algorithm {

    private static Logger logger = Logger.getLogger(ACOAlgorithm.class.getName());

    public List<Solution> solve(TravelingThiefProblem problem) {
        logger.info("In Ant Algorithm");
        List<Solution> solutions = new ArrayList<>();

        double[][] problemRepresentation = problem.coordinates;
        EdgeWeightType edgeWeightType = EdgeWeightType.EUCLIDEAN_DISTANCE;
        TravellingThiefEnvironment environment = new TravellingThiefEnvironment(edgeWeightType, problem);

        ThiefProblemConfiguration configurationProvider = new ThiefProblemConfiguration(environment);
        AntColony<Integer, TravellingThiefEnvironment> colony = getAntColony(configurationProvider);

        //AcoProblemSolver<Integer, TravellingThiefEnvironment> solver = new AcoProblemSolver<>();
        ThiefProblemSolver<Integer, TravellingThiefEnvironment> solver = new ThiefProblemSolver<>();
        try {
            solver.initialize(environment, colony, configurationProvider);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        solver.addDaemonActions(new StartPheromoneMatrix<>(),
                new PerformEvaporation<>());

        solver.addDaemonActions(getPheromoneUpdatePolicy());

        solver.getAntColony().addAntPolicies(new RandomNodeSelection<>());

        //this should be called after each ant has finished its tour
        solver.getAntColony().addAntPolicies(new PackingPlanCreator<>());
        try {
            solver.solveProblem();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        return solver.getBestSolutions();

    }

    /**
     * On TSP, the pheromone value update procedure depends on the distance of the generated routes.
     *
     * @return A daemon action that implements this procedure.
     */
    private static DaemonAction<Integer, TravellingThiefEnvironment> getPheromoneUpdatePolicy() {
        return new OfflinePheromoneUpdate<Integer, TravellingThiefEnvironment>() {
            @Override
            protected double getPheromoneDeposit(Ant<Integer, TravellingThiefEnvironment> ant,
                                                 Integer positionInSolution,
                                                 Integer solutionComponent,
                                                 TravellingThiefEnvironment environment,
                                                 ConfigurationProvider configurationProvider) {
                //sarah return 1 / ant.getSolutionCost(environment);
                AntForTravellingThief thiefAnt = (AntForTravellingThief)ant;
                //want to maximise profit and minimise time
                double pheromoneDeposit = (thiefAnt.getThiefSolution().profit)/(thiefAnt.getThiefSolution().time);
                //System.out.println("%%%%%%%%%%%%%%%" + pheromoneDeposit);
                return pheromoneDeposit;
            }
        };
    }

    /**
     * Produces an Ant Colony instance for the TSP problem.
     *
     * @param configurationProvider Algorithm configuration.
     * @return Ant Colony instance.
     */
    public static AntColony<Integer, TravellingThiefEnvironment> getAntColony(final ConfigurationProvider configurationProvider) {
        return new AntColony<Integer, TravellingThiefEnvironment>(configurationProvider.getNumberOfAnts()) {
            @Override
            protected Ant<Integer, TravellingThiefEnvironment> createAnt(TravellingThiefEnvironment environment) {
                return new AntForTravellingThief(environment.getNumberOfCities(), environment.getTravellingThiefProblem().numOfItems);
            }
        };
    }

}
