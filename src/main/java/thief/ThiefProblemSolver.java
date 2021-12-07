package thief;

import isula.aco.*;
import model.Solution;

import javax.naming.ConfigurationException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main component of the framework: Is the one in charge of making a colony
 * an ants to traverse an environment in order to generate solutions.
 * <p>
 * <p>The solveProblem() method is the one that starts the optimization process.
 * Previously, you have to properly configure your solver by assigning it a
 * Colony, an Environment and Daemon Actions (if required).
 *
 * Changes made by Sarah
 * - Holds a list of non-dominated solutions as well as the best tour
 * - Calls our ThiefPerformanceTracker to update metrics based on non-dominated solutions
 *
 * @param <C> Class for components of a solution.
 * @param <E> Class representing the Environment.
 * @author Carlos G. Gavidia
 */
public class ThiefProblemSolver<C, E extends Environment> extends AcoProblemSolver<C, E> {

    private static Logger logger = Logger.getLogger(thief.ThiefProblemSolver.class
            .getName());

    private List<C> bestSolution;
    private double bestSolutionCost = 0.0;
    private double executionTime = 0.0;
    private String bestSolutionAsString = "";

    //sarah the non-dominated set of solutions
    public List<Solution> bestSolutions = new LinkedList<>();

    public List<Solution> getBestSolutions() {
        return bestSolutions;
    }

    private E environment;
    private AntColony<C, E> antColony;

    // TODO(cgavidia): Maybe we should handle a list of configuration providers.
    private ConfigurationProvider configurationProvider;

    private List<DaemonAction<C, E>> daemonActions = new ArrayList<>();
    private long totalGeneratedSolutions;

    /**
     * Prepares the solver for problem resolution.
     *
     * @param environment Environment instance, with problem-related information.
     * @param colony      The Ant Colony with specialized ants.
     * @param config      Algorithm configuration.
     */
    public void initialize(E environment, AntColony<C, E> colony, ConfigurationProvider config)
            throws ConfigurationException {

        initialize(environment, colony, config, null);
    }


    public void initialize(E environment, AntColony<C, E> colony, ConfigurationProvider config, Duration timeLimit)
            throws ConfigurationException {

        this.setConfigurationProvider(config);
        this.setEnvironment(environment);

        if (colony == null) {
            throw new ConfigurationException("The problem solver needs an instance of AntColony to be initialized");
        }

        this.configureAntColony(colony, environment, timeLimit);
        this.setAntColony(colony);

    }

    protected void configureAntColony(AntColony<C, E> colony, E environment, Duration timeLimit) {
        colony.buildColony(environment);
        colony.setTimeLimit(timeLimit);
    }


    /**
     * Adds a Daemon Action for the current solver.
     *
     * @param daemonAction Daemon action.
     */
    protected void addDaemonAction(DaemonAction<C, E> daemonAction) {

        this.configureDaemonAction(antColony, daemonAction, environment);
        daemonActions.add(daemonAction);

    }

    protected void configureDaemonAction(AntColony<C, E> antColony, DaemonAction<C, E> daemonAction, E environment) {
        daemonAction.setAntColony(antColony);
        daemonAction.setEnvironment(environment);
        daemonAction.setProblemSolver(this);
    }

    /**
     * Solves an optimization problem using a Colony of Ants.
     *
     * @throws ConfigurationException If algorithm parameters aren't properly configured.
     */
    public void solveProblem() throws ConfigurationException {
        logger.info("Starting computation at: " + new Date());
        Instant executionStartTime = Instant.now();

        ThiefPerformanceTracker<C, E> performanceTracker = (ThiefPerformanceTracker<C, E>) kickOffColony(this.antColony, this.environment,
                executionStartTime);

        //Sarah - changed to call thief update - this updates non-dominated solutions
        this.updateThiefGlobalMetrics(executionStartTime, performanceTracker);


    }

    public PerformanceTracker<C, E> kickOffColony(AntColony<C, E> antColony, E environment,
                                                  Instant executionStartTime)
            throws ConfigurationException {
        applyDaemonActions(antColony, DaemonActionType.INITIAL_CONFIGURATION);

        logger.info(" Colony index: " + antColony.getColonyIndex() + " STARTING ITERATIONS");
        int numberOfIterations = configurationProvider.getNumberOfIterations();

        if (numberOfIterations < 1) {
            throw new ConfigurationException(
                    "No iterations are programed for this solver. Check your Configuration Provider.");
        }

        logger.info(" Colony index: " + antColony.getColonyIndex() + " Number of iterations: " + numberOfIterations);

        int iteration = 0;
        //sarah
        PerformanceTracker<C, E> performanceTracker = new ThiefPerformanceTracker<>();
        while (iteration < numberOfIterations) {
            Instant iterationStart = Instant.now();

            antColony.clearAntSolutions();
            boolean terminateExecution = antColony.buildSolutions(environment, configurationProvider,
                    executionStartTime);

            // TODO(cgavidia): This should reference the Update Pheromone routine. Maybe with the Policy hierarchy.
            applyDaemonActions(antColony, DaemonActionType.AFTER_ITERATION_CONSTRUCTION);
            Instant iterationEnd = Instant.now();
            long iterationTime = Duration.between(iterationStart, iterationEnd).getSeconds();

            performanceTracker.updateIterationPerformance(antColony, iteration, iterationTime, environment);
            iteration++;

            if (terminateExecution) {
                break;
            }
        }
        return performanceTracker;
    }

    /**
     * Added by Sarah to update the list of non-dominated solutions
     * @param executionStartTime
     * @param performanceTracker
     */
    protected void updateThiefGlobalMetrics(Instant executionStartTime, ThiefPerformanceTracker<C, E> performanceTracker) {
        this.bestSolution = performanceTracker.getBestSolution();
        this.bestSolutionCost = performanceTracker.getBestSolutionCost();
        this.bestSolutionAsString = performanceTracker.getBestSolutionAsString();
        this.totalGeneratedSolutions = performanceTracker.getGeneratedSolutions();
        this.bestSolutions = performanceTracker.getBestSolutions();

        logger.info("Finishing computation at: " + new Date());
        Instant executionEndTime = Instant.now();
        executionTime = Duration.between(executionStartTime, executionEndTime).getSeconds();
        logger.info("Duration (in seconds): " + executionTime);

        logger.info("EXECUTION FINISHED");
        logger.info("Solutions generated: " + totalGeneratedSolutions);
       // logger.info("Best solution cost: " + bestSolutionCost);
        logger.info("No----- of non-dominated solutions:" + this.bestSolutions.size());

    }

    /**
     * Applies all daemon actions of a specific type.
     */
    private void applyDaemonActions(AntColony<C, E> antColony, DaemonActionType daemonActionType) {
        for (DaemonAction<C, E> daemonAction : daemonActions) {
            if (daemonAction.getAntColony().equals(antColony) && daemonActionType.equals(daemonAction.getAcoPhase())) {
                daemonAction.applyDaemonAction(this.getConfigurationProvider());
            }
        }
    }

    public E getEnvironment() {
        return environment;
    }

    public void setEnvironment(E environment) {
        this.environment = environment;
    }

    public AntColony<C, E> getAntColony() {
        return antColony;
    }

    public void setAntColony(AntColony<C, E> antColony) {
        this.antColony = antColony;
    }

    public ConfigurationProvider getConfigurationProvider() {
        if (this.configurationProvider == null) {
            throw new isula.aco.exception.ConfigurationException(
                    "No Configuration Provider was associated with this solver");
        }

        return configurationProvider;
    }

    public void setConfigurationProvider(
            ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }


    public List<C> getBestSolution() {
        return bestSolution;
    }

    public double getBestSolutionCost() {
        return bestSolutionCost;
    }

    public String getBestSolutionAsString() {
        return bestSolutionAsString;
    }

    public List<DaemonAction<C, E>> getDaemonActions() {
        return daemonActions;
    }


    @Override
    public String toString() {
        return "AcoProblemSolver{" +
                "bestSolution=" + bestSolution +
                ", bestSolutionCost=" + bestSolutionCost +
                ", executionTime=" + executionTime +
                ", environment=" + environment +
                ", antColony=" + antColony +
                ", configurationProvider=" + configurationProvider +
                ", daemonActions=" + daemonActions +
                ", totalGeneratedSolutions=" + totalGeneratedSolutions +
                '}';
    }



}
