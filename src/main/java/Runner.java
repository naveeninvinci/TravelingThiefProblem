import thief.ACOAlgorithm;
import algorithms.Algorithm;
import model.Solution;
import model.TravelingThiefProblem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This class runs the algorithm against a given set of test instances for a given team name.
 *
 * Modified by Sarah to run our algorithm.
 */
class Runner {


    static final ClassLoader LOADER = Runner.class.getClassLoader();

    public static void main(String[] args) throws IOException {

        //uncomment an individual line to run a particular test instance
        //List<String> instanceToRun = Arrays.asList("a280-n279");
       // List<String> instanceToRun = Arrays.asList("a280-n1395");
       // List<String> instanceToRun = Arrays.asList("a280-n2790");
       // List<String> instanceToRun = Arrays.asList("fnl4461-n4460");
       // List<String> instanceToRun = Arrays.asList("fnl4461-n22300");
       // List<String> instanceToRun = Arrays.asList("fnl4461-n44600");
        //List<String> instanceToRun = Arrays.asList("pla33810-n33809");
        //List<String> instanceToRun = Arrays.asList("pla33810-n169045");
        //List<String> instanceToRun = Arrays.asList("pla33810-n338090");
       // List<String> instanceToRun = Arrays.asList("test-example-n4");
        List<String> instanceToRun = Competition.INSTANCES;

        String filePrefix = Competition.TEAM_NAME;

        //use experiment name to make it easier to parse results
        //String filePrefix = "5_30_500_Random_H2.5_P1";

        for (String instance : instanceToRun) {

            // readProblem the problem from the file
            String fname = String.format("resources/%s.txt", instance);
            InputStream is = LOADER.getResourceAsStream(fname);

            TravelingThiefProblem problem = Util.readProblem(is);
            problem.name = instance;

            // number of solutions that will be finally necessary for submission - not used here
            int numOfSolutions = Competition.numberOfSolutions(problem);

            // initialize your algorithm
            Algorithm algorithm = new ACOAlgorithm();

            // use it to to solve the problem and return the non-dominated set
            List<Solution> nds = algorithm.solve(problem);

            // sort by time and printSolutions it
            nds.sort(Comparator.comparing(a -> a.time));

            System.out.println(nds.size());
            for(Solution s : nds) {
                System.out.println(s.time + " " + s.profit);
            }

            Util.printSolutions(nds, true);
            System.out.println(problem.name + " " + nds.size());

            File dir = new File("results");
            if (!dir.exists()) dir.mkdirs();
            Util.writeSolutions("results", filePrefix, problem, nds);

        }



    }

}