package algorithms;

import isula.aco.*;
import isula.aco.algorithms.antsystem.OfflinePheromoneUpdate;
import isula.aco.algorithms.antsystem.PerformEvaporation;
import isula.aco.algorithms.antsystem.RandomNodeSelection;
import isula.aco.algorithms.antsystem.StartPheromoneMatrix;
import isula.aco.tsp.AntForTsp;
import isula.aco.tsp.EdgeWeightType;
import isula.aco.tsp.TspEnvironment;
import model.Solution;
import model.TravelingThiefProblem;
import tsp.isula.sample.AcoTspWithIsula;
import tsp.isula.sample.TspProblemConfiguration;

import javax.naming.ConfigurationException;
import java.io.IOException;
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
        TspEnvironment environment = new TspEnvironment(problemRepresentation, edgeWeightType);

        TspProblemConfiguration configurationProvider = new TspProblemConfiguration(environment);
        AntColony<Integer, TspEnvironment> colony = getAntColony(configurationProvider);

        AcoProblemSolver<Integer, TspEnvironment> solver = new AcoProblemSolver<>();
        try {
            solver.initialize(environment, colony, configurationProvider);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        solver.addDaemonActions(new StartPheromoneMatrix<>(),
                new PerformEvaporation<>());

        solver.addDaemonActions(getPheromoneUpdatePolicy());

        solver.getAntColony().addAntPolicies(new RandomNodeSelection<>());
        try {
            solver.solveProblem();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }


        List<java.lang.Integer> tour = solver.getBestSolution();

        //generate packing list of all ones just for now
        List<java.lang.Boolean> packingPlan = new ArrayList<>(problem.numOfItems);

        for (int i = 1; i <= problem.numOfItems; i++) {
            packingPlan.add(Boolean.TRUE);
        }
        Solution singleSolution = problem.evaluate(tour, packingPlan);
        logger.info(singleSolution.toString());
        solutions.add(singleSolution);

        return solutions;

    }

    /**
     * On TSP, the pheromone value update procedure depends on the distance of the generated routes.
     *
     * @return A daemon action that implements this procedure.
     */
    private static DaemonAction<Integer, TspEnvironment> getPheromoneUpdatePolicy() {
        return new OfflinePheromoneUpdate<Integer, TspEnvironment>() {
            @Override
            protected double getPheromoneDeposit(Ant<Integer, TspEnvironment> ant,
                                                 Integer positionInSolution,
                                                 Integer solutionComponent,
                                                 TspEnvironment environment,
                                                 ConfigurationProvider configurationProvider) {
                return 1 / ant.getSolutionCost(environment);
            }
        };
    }

    /**
     * Produces an Ant Colony instance for the TSP problem.
     *
     * @param configurationProvider Algorithm configuration.
     * @return Ant Colony instance.
     */
    public static AntColony<Integer, TspEnvironment> getAntColony(final ConfigurationProvider configurationProvider) {
        return new AntColony<Integer, TspEnvironment>(configurationProvider.getNumberOfAnts()) {
            @Override
            protected Ant<Integer, TspEnvironment> createAnt(TspEnvironment environment) {
                return new AntForTsp(environment.getNumberOfCities());
            }
        };
    }

}
