import java.util.HashMap;

public class Equation {
    protected static HashMap<Double, Double> valuesAtPoints;
    protected static final int NUMBERS_TO_FIND_COUNT = 3;

    protected static boolean evaluateParameters() {
        return GA.BITS % NUMBERS_TO_FIND_COUNT == 0;
    }

    protected static void populateValuesAtPoints() {
        valuesAtPoints = quadraticEquationSolver(new int[]{3, 0, -4});
    }

    protected static HashMap<Double, Double> quadraticEquationSolver(int[] abc) {
        int howManyValues = 10;
        HashMap<Double, Double> hm = new HashMap<>();

        int a = abc[0];
        int b = abc[1];
        int c = abc[2];

        for (int x = -(howManyValues / 2); x <= (howManyValues / 2); x++) {
            double y = (a * (Math.pow(x, 2.0))) + (b * x) + c;
            hm.put((double) x, y);
        }

        return hm;
    }
}