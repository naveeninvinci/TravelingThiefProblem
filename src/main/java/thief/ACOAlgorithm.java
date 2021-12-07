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

        EdgeWeightType edgeWeightType = EdgeWeightType.EUCLIDEAN_DISTANCE;
        //sarah - contains configuration info for our thief problem, passing in the gecco object to the isula object
        TravellingThiefEnvironment environment = new TravellingThiefEnvironment(edgeWeightType, problem);

        //sarah - this has all the settings, like number of iterations, ants, alpha, beta params, evaporation rate
        ThiefProblemConfiguration configurationProvider = new ThiefProblemConfiguration(environment);
        AntColony<Integer, TravellingThiefEnvironment> colony = getAntColony(configurationProvider);

        //our thief problem solver, replacing the AcoProblemSolver
        ThiefProblemSolver<Integer, TravellingThiefEnvironment> solver = new ThiefProblemSolver<>();
        try {
            solver.initialize(environment, colony, configurationProvider);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        solver.addDaemonActions(new StartPheromoneMatrix<>(), new PerformEvaporation<>());

        solver.addDaemonActions(getPheromoneUpdatePolicy());

        //Sarah - RandomNodeSelection class creates the probability matrix and picks which node the Ant is going to visit next
        solver.getAntColony().addAntPolicies(new RandomNodeSelection<>());

        //Sarah - this should be called after each ant has finished its tour
        solver.getAntColony().addAntPolicies(new PackingPlanCreator<>());
        try {
            //run the algorithm
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
                //return 1 / ant.getSolutionCost(environment);
                AntForTravellingThief thiefAnt = (AntForTravellingThief)ant;
                //want to maximise profit and minimise time
                double pheromoneDeposit = thiefAnt.getThiefSolution().profit/thiefAnt.getThiefSolution().time;
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
                return new AntForTravellingThief(environment.getNumberOfCities());
            }
        };
    }

}
