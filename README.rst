GECCO 2019 Competition - Bi-objective Traveling Thief Problem
Nature Inspired Coursework, TEAM INVINCIBLES
============================================================

Requirements
------------------------------------------------------------
- Java 8
- (Maven)

To Run
------------------------------------------------------------
- The Test Instances to run the algorithm against are specified within the Runner.java class.  Default is to run the smaller test instances with 280 cities.
- Execute the Runner.java class (we did this within the IntelliJ IDE).
- This will create the following files in the results folder:
          - INVINCIBLES_<test instance name>.f - the time and profit for each solution
          - INVINCIBLES_<test instance name>.x - the packing plan and route plan

Structure
------------------------------------------------------------
We re-used the provided Gecco competition starting framework and extended the isula ACO framework.
Code written by Invincibles is in the thief folder.

::

    TravellingThiefProblem
    ├── docs:  Documentation for the GECCO competition
    ├── experiment_results:  Results of team experiments
    ├── results:  Output folder for results of running the algorithm
    ├── submissions:  Competition submissions from all teams, including the Invicibles submission.
    ├── target:  Compiled classes
    ├── src:
        ├── Runner.java: Execute an algorithm on specified test instances and save the file in the desired format.
        ├── Competition.java: Contains the instance names to be solved and the maximum limit of solutions to submit.
        ├── model
            ├── TravelingThiefProblem.java: The problem object used to evaluate the problem for a given tour and packing plan.
            ├── Solution.java: Object to store the results of the evaluate function.
            └── Solution.java: NonDominatedSet.java: Example implementation of a non-dominated set. Can be done faster/better.
        ├── algorithms
            ├── Algorithm: Interface for the algorithm to be implemented from.
            ├── ExhaustiveSearch: Solves the problem exhaustively which means iterating over all possible tours and packing plans.
            └── RandomLocalSearch: Example algorithm to randomly fix a tour and then iterate over possible packing plans.
        ├── thief:  this is the package for all INVINCIBLES code
            ├── ACOAlgorithm.java:  Extends the Gecco algorithm interface, takes the thief problem, sets up the Ant Colony and kicks them off.
            ├── AntForTravellingThief.java:  Extends the Isula Ant with Thief behaviours, such as our solution cost etc.
            ├── PackingPlanCreator.java:  Creates the packing plan.
            ├── ThiefPerformanceTracker.java:  Updates the set of non-dominated solutions after each iteration.
            ├── ThiefProblemConfiguration.java:  Contains configuration settings, such as global pheromone importance.
            ├── ThiefProblemSolver.java:  Is the one in charge of making a colony of ants to traverse an environment in order to generate solutions.
            ├── TravellingThiefEnvironment.java:  Stores problem specific information, stores the gecco thief problem.

