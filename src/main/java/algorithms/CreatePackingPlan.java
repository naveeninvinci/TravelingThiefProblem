package algorithms;

import isula.aco.*;

import java.util.List;


public class CreatePackingPlan <C, E extends Environment> extends AntPolicy<C, E> {

    public CreatePackingPlan() { super(AntPolicyType.AFTER_SOLUTION_IS_READY);  }

    @Override
    public boolean applyPolicy(E environment, ConfigurationProvider configurationProvider) {
        //this gets called after every ant has finished its tour
        Ant a = getAnt();
        List<Integer> s = a.getSolution();
        System.out.println("solution" + s);

        return true;
    }

}

