package thief;

import isula.aco.*;
import isula.aco.exception.SolutionConstructionException;
import model.Solution;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ThiefPerformanceTracker<C, E extends Environment> extends PerformanceTracker<C, E> {

    private static Logger logger = Logger.getLogger(ThiefPerformanceTracker.class
            .getName());

    private List<C> bestSolution;
    private double bestSolutionCost;
    private String bestSolutionAsString;

    private long generatedSolutions;
    //sarah the non-dominated set of solutions
    public List<Solution> bestSolutions = new LinkedList<>();

    public List<Solution> getBestSolutions() {
        return bestSolutions;
    }

    /**
     * Updates the information of the best solution produced with the solutions
     * produced by the Colony.
     *
     * Modified by Sarah to hold a list of non-dominated solutions.
     *
     * @param iterationTimeInSeconds Time spent during the iteration.
     * @param environment            Environment where the solutions where produced.
     */
    public void updateIterationPerformance(AntColony<C, E> antColony, int iteration, long iterationTimeInSeconds,
                                           E environment) {
        logger.log(Level.FINE, "GETTING BEST SOLUTION FOUND");


        long iterationSolutions = antColony.getHive()
                .stream()
                .filter((ant) -> ant.isSolutionReady(environment))
                .count();

        this.generatedSolutions += iterationSolutions;

        Ant<C, E> bestAnt = antColony.getBestPerformingAnt(environment);

        if (!this.isStateValid(bestAnt, environment)) {
            throw new SolutionConstructionException("Performance Tracker is in an inconsistent state. Solution:"
                    + this.bestSolution + " Solution Cost: " + bestSolutionCost + " String representation: " +
                    bestSolutionAsString + " Colony index: " + antColony.getColonyIndex());
        }

        double bestIterationCost = bestAnt.getSolutionCost(environment);
        logger.fine("Iteration best cost: " + bestIterationCost);

        if (bestSolution == null
                || bestSolutionCost > bestIterationCost) {
            List<C> list = new ArrayList<>();
            for (C c : bestAnt.getSolution()) {
                list.add(c);
            }
            bestSolution = Collections.unmodifiableList(list);
            bestSolutionCost = bestIterationCost;
            bestSolutionAsString = bestAnt.getSolutionAsString();

            logger.fine("Best solution so far > Cost: " + bestSolutionCost
                    + ", Solution as string: " + bestSolutionAsString + " Stored solution: " + bestSolution);

        }

        logger.info(" Colony index: " + antColony.getColonyIndex() + " Current iteration: " + iteration + " Iteration solutions: " + iterationSolutions +
                " Iteration best: " + bestIterationCost + " Iteration Duration (s): " + iterationTimeInSeconds +
                " Global solution cost: " + bestSolutionCost);
        logger.fine(" Global solution cost: " + bestSolutionCost + " Stored solution: " + bestSolution + " Solution as String: " + bestSolutionAsString);

        //sarah -  thief modifications
        //try and add solution to our set of solutions
        AntForTravellingThief antThief = (AntForTravellingThief)bestAnt;
        Solution bestThiefSolution = antThief.getThiefSolution();
        Boolean isAdded = add(bestThiefSolution);

        if(isAdded) {
            //logger.info("Added solution: " + bestThiefSolution.toString());
        }
        else{
            //logger.info("Did not add solution: " + bestThiefSolution.toString());
        }

       // logger.info("Best solutions " + this.getBestSolutions());
        logger.info("Best solutions size " + this.getBestSolutions().size());
    }



    private boolean isStateValid(Ant<C, E> ant, E environment) {

        if (this.bestSolution == null && this.bestSolutionAsString == null && this.bestSolutionCost == 0.0) {
            return true;
        }

        double expectedSolutionCost = ant.getSolutionCost(environment, bestSolution);
        String expectedSolutionAsString = ant.getSolutionAsString(bestSolution);

        if (Math.abs(expectedSolutionCost - bestSolutionCost) <= 0.001 &&
                expectedSolutionAsString.equals(bestSolutionAsString)) {
            return true;
        }

        return false;
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

    public long getGeneratedSolutions() {
        return generatedSolutions;
    }

    public void setGeneratedSolutions(Long generatedSolutions) {
        this.generatedSolutions = generatedSolutions;
    }

    /**
     * Add a solution to the non-dominated set - this is the code from gecco
     * @param s The solution to be added.
     * @return true if the solution was indeed added. Otherwise false.
     */
    public boolean add(Solution s) {

        boolean isAdded = true;

        for (Iterator<Solution> it = this.bestSolutions.iterator(); it.hasNext();) {
            Solution other = it.next();

            int rel = s.getRelation(other);

            // if dominated by or equal in design space
            if (rel == -1 || (rel == 0 && s.equalsInDesignSpace(other))) {
                isAdded = false;
                break;
            } else if (rel == 1) it.remove();

        }

        if (isAdded) this.bestSolutions.add(s);

        return isAdded;

    }

}
