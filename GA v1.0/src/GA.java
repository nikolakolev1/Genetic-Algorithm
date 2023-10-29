import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// Author: Nikola Kolev
// Date: 10.10.2023
// Version: 1.0

/**
 * This is a Genetic Algorithm (GA) that uses either Roulette or Tournament selection and one of three fitness functions.
 * <p>
 * It is my first attempt at a GA, and it is not perfect, but it works.
 * Issues to be fixed in next version:
 * <ul>
 *     <li>Separate the different classes into different files</li>
 *     <li>Add more and better comments</li>
 *     <li>Improve elitism to allow for more than one elite individual</li>
 *     <li>Change how an individual is represented, so that it can be used for more than just binary numbers</li>
 *     <li>Introduce an array 'fitness' to keep the fitness of each individual</li>
 *     <li>Use this 'fitness' array to improve the efficiency of the program, because now does a lot of extra fitness calculation</li>
 *     <li>Add a method that uses a scanner and reads from input to set the switches</li>
 * </ul>
 * </p>
 */
public class GA {
    // ------------------------------------- Switches -------------------------------------
    private static final int selectionSwitch = 2;
    private static final String[] SELECTIONS = {"Roulette", "Tournament"};
    private static final int fitnessFunctionSwitch = 1; // if using the fitnessFunc for an equation - turn solvingEquation on
    private static final String[] FITNESS_FUNCTIONS = {"Most bits turned on", "Most bits turned off", "Quadratic equation"};
    private static final boolean switchesInBound = (selectionSwitch > 0 && selectionSwitch <= SELECTIONS.length) && (fitnessFunctionSwitch > 0 && fitnessFunctionSwitch <= FITNESS_FUNCTIONS.length);
    private static final boolean elitism = false;


    // ------------------------------------- Equation -------------------------------------
    private static final boolean solvingEquation = false;
    private static final HashMap<Double, Double> valuesAtPoints = solvingEquation ? populateValuesAtPoints() : null;
    private static final int NUMBERS_TO_FIND_COUNT = 3;


    // ------------------------------------- Termination -------------------------------------
    private static final boolean usingTerminationCondition = false;
    private static boolean terminationConditionMet = false;


    //--------------------------------------------------------------------------
    // Genes of an individual
    private static final int BITS = 100;

    // The population size (MUST BE DIVISIBLE BY 4)
    private static final int POPULATION_SIZE = 20;

    /*
    Used in tournament selection (usually represented as 'k')
    - In tournament selection the population is divided into groups and then the best fit individual in each group is
    selected. This variable represents the size of these groups.
    - Must be a number between 1 and the POPULATION_SIZE. Preferably neither of these two, but something between them.
     */
    private static final int TOURNAMENT_SIZE = 4;

    private static final int MAX_GENERATION = 10;
    private static final double CROSSOVER_PROBABILITY = 0.95;
    private static final double MUTATION_PROBABILITY = 0.03;
    private static boolean[][] population = new boolean[POPULATION_SIZE][BITS];
    private static boolean[] bestIndividual_EntireRun = new boolean[BITS];


    public static void main(String[] args) {
        if (switchesInBound) {

            initialise();

            for (int testNo = 0; testNo < 5; testNo++) {
                runGA();
                printShortStats((testNo + 1), population);
//                printPopulation(population);
                resetGlobals();
            }
        } else {
            System.out.println("You must first choose a fitness function and a selection method to use and then run the program again");
        }
    }

    /*
    select a type of GA and run it
    Input:
    - 1: for Roulette selection
    - 2: for Tournament selection
    - ...
     */
    private static void runGA() {
        // Evolve the population for a number of generations
        for (int gen = 0; gen < MAX_GENERATION; gen++) {
            // Create an offspring population from a selected parent population
            population = evolution(selection(population));
//            printPopulation(population);

            recordBestIndividual_EntireRun();

            if (elitism) population[(int) (Math.random() * POPULATION_SIZE)] = bestIndividual_EntireRun;
            if (usingTerminationCondition && terminationConditionMet) break;
        }
    }


    // ------------------------------------- Initial random population -------------------------------------
    private static void initialise() {
        population = randomPopulation();
        bestIndividual_EntireRun = findBestInGroup(population);
    }

    private static boolean[] randomIndividual() {
        boolean[] individual = new boolean[BITS];

        for (int i = 0; i < BITS; i++) {
            individual[i] = Math.random() < 0.5;
        }

        return individual;
    }

    private static boolean[][] randomPopulation() {
        boolean[][] population = new boolean[POPULATION_SIZE][BITS];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = randomIndividual();
        }

        return population;
    }


    // ------------------------------------- Fitness functions - Evaluate -------------------------------------
    private static double fitness(boolean[] individual) {
        switch (fitnessFunctionSwitch) {
            case 1 -> {
                return fitnessFunction_MostGenesOn(individual);
            }
            case 2 -> {
                return fitnessFunction_LeastGenesOn(individual);
            }
            case 3 -> {
                return fitnessFunction_QuadraticEquation(individual);
            }
            default -> {
                System.out.println("Select a valid fitness function");
                return 0.0;
            }
        }
    }

    private static double fitnessFunction_MostGenesOn(boolean[] individual) {
        double fitness = 0.0;

        for (boolean bit : individual) {
            if (bit) fitness++;
        }

        return fitness;
    }

    private static double fitnessFunction_LeastGenesOn(boolean[] individual) {
        double fitness = 0.0;

        for (boolean bit : individual) {
            if (!bit) fitness++;
        }

        return fitness;
    }

    private static double fitnessFunction_QuadraticEquation(boolean[] individual) {
        if (!solvingEquation) System.out.println("Turn the global var solvingEquation on");
        if (BITS % NUMBERS_TO_FIND_COUNT != 0) System.out.println("Set appropriate BITS and NUMBERS_TO_FIND_COUNT");

        int numberLengthBits = BITS / NUMBERS_TO_FIND_COUNT;
        int[] numbers = new int[NUMBERS_TO_FIND_COUNT];
        for (int i = 0; i < NUMBERS_TO_FIND_COUNT; i++) {
            int from = i * numberLengthBits;
            int to = from + numberLengthBits;
            numbers[i] = binaryToDecimal(Arrays.copyOfRange(individual, from, to));
        }

        double difference = 0.0;

        HashMap<Double, Double> results = quadraticEquationSolver(numbers);
        for (Double key : results.keySet()) {
            difference += Math.abs(valuesAtPoints.get(key) - results.get(key));
        }

        if (difference == 0) {
            terminationConditionMet = true;
            difference = 1;
        }

        return (Math.pow(2, numberLengthBits)) / difference;
    }

    private static double populationTotalFitness(boolean[][] population) {
        double totalFitness = 0;

        for (boolean[] individual : population) {
            totalFitness += fitness(individual);
        }

        return totalFitness;
    }

    private static double populationAverageFitness(boolean[][] population) {
        return populationTotalFitness(population) / population.length;
    }

    private static double populationBestFitness(boolean[][] population) {
        double bestFitness = 0.0;

        for (boolean[] individual : population) {
            double contenderFitness = fitness(individual);
            if (contenderFitness > bestFitness) {
                bestFitness = contenderFitness;
            }
        }

        return bestFitness;
    }


    // ------------------------------------- Selection -------------------------------------
    private static PopulationDetails selection(boolean[][] population) {
        switch (selectionSwitch) {
            case 1 -> {
                return rouletteSelection(population);
            }
            case 2 -> {
                return tournamentSelection(population);
            }
            default -> {
                System.out.println("Select a valid selection method");
                return null;
            }
        }
    }

    private static PopulationDetails rouletteSelection(boolean[][] population) {
        ArrayList<boolean[]> populationList = new ArrayList<>(Arrays.asList(population));
        ArrayList<Double> prejudice = new ArrayList<>();

        double cumulative = 0.0;
        double totalFitness = populationTotalFitness(population);

        for (boolean[] individual : population) {
            cumulative += fitness(individual) / totalFitness;
            prejudice.add(cumulative);
        }

        return new PopulationDetails(populationList, prejudice);
    }

    private static PopulationDetails tournamentSelection(boolean[][] population) {
        ArrayList<boolean[]> selectedIndividuals = new ArrayList<>();

        // divide into groups
        for (int i = 0; i < population.length; i += TOURNAMENT_SIZE) {
            boolean[] mostFitInGroup = findBestInGroup(Arrays.copyOfRange(population, i, (i + TOURNAMENT_SIZE)));
            selectedIndividuals.add(mostFitInGroup);
        }

        return new PopulationDetails(selectedIndividuals);
    }

    private static boolean[] findBestInGroup(boolean[][] group) {
        boolean[] bestIndividualInGroup = null;
        double bestIndividualFitness = 0.0;
        boolean[] contender;

        for (int i = 0; i < group.length; i++) {
            if (i == 0) {
                bestIndividualInGroup = Arrays.copyOf(group[0], BITS);
                bestIndividualFitness = fitness(bestIndividualInGroup);
            } else {
                contender = Arrays.copyOf(group[i], BITS);
                if (fitness(contender) > bestIndividualFitness) {
                    bestIndividualInGroup = Arrays.copyOf(contender, BITS);
                    bestIndividualFitness = fitness(bestIndividualInGroup);
                }
            }
        }

        return bestIndividualInGroup;
    }


    // ------------------------------------- Population evolution -------------------------------------
    // Crossover and mutation
    private static boolean[][] evolution(PopulationDetails parentPopulation) {
        boolean[][] newPopulation = crossover(parentPopulation);
        mutation(newPopulation);

        return newPopulation;
    }

    private static boolean[] chooseBasedOnPrejudice(PopulationDetails populationDetails) {
        double rand = Math.random();
        for (int individual = 0; individual < populationDetails.size; individual++) {
            if (rand < populationDetails.prejudice.get(individual)) {
                return populationDetails.population.get(individual);
            }
        }

        System.out.println("This line should not be reached, check for errors");
        return null;
    }

    private static boolean[][] crossover(PopulationDetails parentPopulation) {
        boolean[][] newPopulation = new boolean[POPULATION_SIZE][BITS];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            boolean[] parent1 = chooseBasedOnPrejudice(parentPopulation);
            boolean[] parent2 = chooseBasedOnPrejudice(parentPopulation);

            boolean[][] offsprings = crossoverIndividuals(parent1, parent2);

            newPopulation[i] = offsprings[0];
            newPopulation[++i] = offsprings[1];
        }

        return newPopulation;
    }

    private static boolean[][] crossoverIndividuals(boolean[] parent1, boolean[] parent2) {
        boolean[] offspring1 = Arrays.copyOf(parent1, BITS);
        boolean[] offspring2 = Arrays.copyOf(parent2, BITS);

        double shouldCrossover = Math.random();
        if (shouldCrossover < CROSSOVER_PROBABILITY) {
            int crossoverPoint = (int) (Math.random() * BITS);

            if (BITS - crossoverPoint >= 0) {
                System.arraycopy(parent2, crossoverPoint, offspring1, crossoverPoint, BITS - crossoverPoint);
                System.arraycopy(parent1, crossoverPoint, offspring2, crossoverPoint, BITS - crossoverPoint);
            }
        }

        return new boolean[][]{offspring1, offspring2};
    }

    private static void mutation(boolean[][] population) {
        for (boolean[] individual : population) {
            mutateIndividual(individual);
        }
    }

    private static void mutateIndividual(boolean[] individual) {
        for (int bit = 0; bit < individual.length; bit++) {
            if (Math.random() < MUTATION_PROBABILITY) {
                individual[bit] = !individual[bit];
            }
        }
    }


    // ------------------------------------- For the stats in the end -------------------------------------
    private static void printDetailedStats(int testNumber, boolean[][] population) {
        printShortStats(testNumber, population);
        printSettings();
        System.out.println();
    }

    private static void printShortStats(int testNumber, boolean[][] population) {
        System.out.println("--------- TEST " + testNumber + " ---------");
        System.out.println("Average fitness last population: " + populationAverageFitness(population));
        if (!elitism) System.out.println("Best fitness last population: " + populationBestFitness(population));
        System.out.println("Best fitness entire run: " + fitness(bestIndividual_EntireRun));
        System.out.print("Best individual entire run: ");
        printIndividual(bestIndividual_EntireRun);
        System.out.println();
    }

    private static void printSettings() {
        System.out.print("This was a GA using " + SELECTIONS[selectionSwitch - 1] + " selection and the '" + FITNESS_FUNCTIONS[fitnessFunctionSwitch - 1] + "' fitness function");
        if (elitism) System.out.println(", with Elitism turned on.");
        else System.out.println(".");
    }


    // ------------------------------------- For visualization -------------------------------------
    // Visualize a population (print all individuals' genes)
    private static void printPopulation(boolean[][] population) {
        for (int i = 0; i < population.length; i++) {
            System.out.print(i + 1 + ": ");
            printIndividual(population[i]);
        }
        System.out.println();
    }

    private static void printIndividual(boolean[] individual) {
        for (boolean bit : individual) {
            System.out.print(bit ? "1" : "0");
        }
        System.out.println();
    }


    // ------------------------------------- Other -------------------------------------
    private static void resetGlobals() {
        population = randomPopulation();
        bestIndividual_EntireRun = findBestInGroup(population);

        terminationConditionMet = false;
    }

    private static HashMap<Double, Double> populateValuesAtPoints() {
        return quadraticEquationSolver(new int[]{3, 0, -4});
    }

    private static HashMap<Double, Double> quadraticEquationSolver(int[] abc) {
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

    private static int binaryToDecimal(boolean[] binary) {
        int decimal = 0;
        boolean negative = false;

        for (int i = 0; i < binary.length; i++) {
            if (i != 0 && binary[i]) decimal += (int) Math.pow(2, binary.length - i - 1);
            else if (i == 0) negative = binary[i];
        }

        return negative ? (decimal * -1) : decimal;
    }

    private static void recordBestIndividual_EntireRun() {
        boolean[] bestInGeneration = findBestInGroup(population);
        if (fitness(bestInGeneration) > fitness(bestIndividual_EntireRun)) {
            bestIndividual_EntireRun = bestInGeneration;
        }
    }
}

class PopulationDetails {
    public ArrayList<boolean[]> population;
    public int size;

    /*
     The cumulative chance that an individual gets selected

     For example, if there are five individuals and their chances of selection are as follows:
        - A | 10%
        - B | 25%
        - C | 20%
        - D | 15%
        - E | 30%

     In the array list it is going to be represented like that:
        - A | 0.1
        - B | 0.35
        - C | 0.55
        - D | 0.70
        - E | 1
     */
    public ArrayList<Double> prejudice;

    public PopulationDetails(ArrayList<boolean[]> population, ArrayList<Double> prejudice) {
        this.population = population;
        this.prejudice = prejudice;
        size = population.size();
    }

    public PopulationDetails(ArrayList<boolean[]> population) {
        this.population = population;
        prejudice = noPrejudice(population.size());
        size = population.size();
    }

    private ArrayList<Double> noPrejudice(int populationSize) {
        double equalChances = 1.0 / populationSize;
        double cumulative = equalChances;
        ArrayList<Double> equalPrejudice = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            equalPrejudice.add(cumulative);
            cumulative += equalChances;
        }

        return equalPrejudice;
    }
}