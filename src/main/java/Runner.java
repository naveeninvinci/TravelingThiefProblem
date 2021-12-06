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

class Runner {


    static final ClassLoader LOADER = Runner.class.getClassLoader();

    public static void main(String[] args) throws IOException {

        //List<String> instanceToRun = Arrays.asList("a280-n279");
        //List<String> instanceToRun = Arrays.asList("a280-n1395");
          List<String> instanceToRun = Arrays.asList("a280-n2790");
        //List<String> instanceToRun = Arrays.asList("fnl4461-n4460");
        //List<String> instanceToRun = Arrays.asList("fnl4461-n22300");
        //List<String> instanceToRun = Arrays.asList("fnl4461-n44600");
        //List<String> instanceToRun = Arrays.asList("pla33810-n33809");
        //List<String> instanceToRun = Arrays.asList("pla33810-n169045");
        //List<String> instanceToRun = Arrays.asList("pla33810-n338090");
        //List<String> instanceToRun = Arrays.asList("test-example-n4");
        //List<String> instanceToRun = Competition.INSTANCES;

        for (String instance : instanceToRun) {

            // readProblem the problem from the file
            String fname = String.format("resources/%s.txt", instance);
            InputStream is = LOADER.getResourceAsStream(fname);

            TravelingThiefProblem problem = Util.readProblem(is);
            problem.name = instance;

            // number of solutions that will be finally necessary for submission - not used here
            int numOfSolutions = Competition.numberOfSolutions(problem);

            // initialize your algorithm
            //Algorithm algorithm = new RandomLocalSearch(100);
            //Algorithm algorithm = new ExhaustiveSearch();
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
            Util.writeSolutions("results", Competition.TEAM_NAME, problem, nds);


        }



    }

}