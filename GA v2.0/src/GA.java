import java.util.*;
import java.util.stream.IntStream;

// Author: Nikola Kolev
// Date: 21.10.2023
// Version: 2.0

/**
 * Issues to be fixed in this version:
 * <ul>
 *     <li>Implement adequate mutation for the TSP problem</li>
 *     <li>Implement another crossover for the TSP problem (only TSP at the moment)</li>
 *     <li>Change the TSP fitness func to be minimal (it is a minimization problem, not maximization)</li>
 * </ul>
 * <p>
 * Issues to be fixed in next version:
 * <ul>
 *     <li>Improve elitism to allow for more than one elite individual</li>
 *     <li>Add a method that uses a scanner and reads from input to set the switches</li>
 *     <li>Clean up code (refactor, rename methods and vars, add comments, remove junk)</li>
 *     <li>Implement using a termination condition</li>
 *     <li>Finish nPointCrossover, linking it with the nPointCrossoverPoints global var </li>
 * </ul>
 */
public class GA {
    // ------------------------------------- Switches -------------------------------------
    protected static INDIVIDUAL_TYPE individualType;
    protected static SELECTION selection;
    protected static FITNESS_FUNC fitnessFunc;
    protected static CROSSOVER crossover;
    protected static MUTATION mutation;
    protected static MIN_MAX minOrMax;


    // ------------------------------------- Elitism -------------------------------------
    protected static boolean elitism;
    private static ArrayList<Individual> oldPopulationSorted;
    private static final int eliteIndividualsCount = 3;


    // ------------------------------------- Tournament sel - Ensure all selected become parents -------------------------------------
    private static boolean everyoneWasParent = false;
    private static HashSet<Integer> parents = new HashSet<>();


    // ------------------------------------- Termination -------------------------------------
    private static final boolean usingTerminationCondition = false;
    private static boolean terminationConditionMet = false;


    // ------------------------------------- Settings -------------------------------------
    /**
     * BITS - number of bits in an individual
     * POPULATION_SIZE - number of individuals in the population (MUST BE DIVISIBLE BY 4)
     * TOURNAMENT_SIZE (often shown as 'k')- number of individuals in a tournament
     * <ul>
     *     <li> Used in tournament selection </li>
     *     <li> Must be >= 1 and <= POPULATION_SIZE (preferably neither, but in between) </li>
     * </ul>
     * MAX_GENERATION - number of generations the algorithm will run for
     */
    protected static int BITS, POPULATION_SIZE, TOURNAMENT_SIZE, MAX_GENERATION;
    protected static double CROSSOVER_PROBABILITY, MUTATION_PROBABILITY;


    // ------------------------------------- Global population -------------------------------------
    protected static Individual[] population;
    private static double[] fitness;
    public static ArrayList<Double> prejudice;
    protected static Individual bestIndividual_EntireRun;

    private static int nPointCrossoverPoints;


    public static void main(String[] args) {
        try {
            for (int testNo = 0; testNo < 1; testNo++) {
                // Set all the switches and settings for the GA to run
                // (I suggest using presets or the setSwitches() and setSettings() methods)
                Presets.preset("TspTorTspPmxExcEltMaxTsp");

                // Check if everything is set correctly
                Check.checkEverything();

                // The main algorithm
                initialise();
                evaluate();

                // A loop of selection, crossover, mutation and, possibly, elitism (+ evaluation)
                runGA();

                // Must do this at the end of each test
                Print.shortStats(testNo + 1);
                resetGlobals();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Run the GA for a given number of generations
     * 1. Selection
     * 2. Crossover
     * 3. Mutation
     * 4. Elitism
     * 5. Evaluate
     */
    private static void runGA() {
        for (int gen = 0; gen < MAX_GENERATION; gen++) {
            Individual[] selected = selection();
            population = evolution(selected);

            if (elitism) reintroduceElite();

            evaluate();

            recordBestIndividual_EntireRun();
//            Print.generationStats(gen + 1);

            if (usingTerminationCondition && terminationConditionMet) break;
        }
    }

    private static void test() {
        TSP.loadMatrix("tspFiles/groetschel.tsp");
        setSwitches(INDIVIDUAL_TYPE.tspIntArray, SELECTION.Tournament, FITNESS_FUNC.Tsp, CROSSOVER.PMX_Tsp, MUTATION.Exchange_Tsp, true, MIN_MAX.Min);
        setSettings(TSP.SIZE, 800, 20, 20, 0.95, 0.00);
    }


    // ------------------------------------- Switches and settings -------------------------------------
    protected static void setSwitches(INDIVIDUAL_TYPE individualType,
                                      SELECTION selection,
                                      FITNESS_FUNC fitnessFunction,
                                      CROSSOVER crossover,
                                      MUTATION mutation,
                                      boolean elitism,
                                      MIN_MAX minOrMax) {
        GA.individualType = individualType;
        GA.selection = selection;
        GA.fitnessFunc = fitnessFunction;
        GA.crossover = crossover;
        GA.mutation = mutation;
        GA.elitism = elitism;
        GA.minOrMax = minOrMax;

        if (fitnessFunction == FITNESS_FUNC.QuadEquationBoolArray) {
            if (Equation.valuesAtPoints == null) Equation.populateValuesAtPoints();
        } else if (fitnessFunc == FITNESS_FUNC.Tsp) {
            if (TSP.costMatrix == null) {
                if (TSP.filename != null) TSP.loadMatrix(TSP.filename);
                else TSP.createMatrix();
            }
        }
    }

    protected static void setSettings(int BITS,
                                      int POPULATION_SIZE,
                                      int TOURNAMENT_SIZE,
                                      int MAX_GENERATION,
                                      double CROSSOVER_PROBABILITY,
                                      double MUTATION_PROBABILITY) {
        GA.BITS = BITS;
        GA.POPULATION_SIZE = POPULATION_SIZE;
        GA.TOURNAMENT_SIZE = TOURNAMENT_SIZE;
        GA.MAX_GENERATION = MAX_GENERATION;
        GA.CROSSOVER_PROBABILITY = CROSSOVER_PROBABILITY;
        GA.MUTATION_PROBABILITY = MUTATION_PROBABILITY;
    }


    // ------------------------------------- Initial random population -------------------------------------

    /**
     * Initialise the population with random individuals and record the best individual in it
     */
    private static void initialise() {
        population = randomPopulation(); // create a random population
        fitness = new double[POPULATION_SIZE]; // initialise the fitness array

        bestIndividual_EntireRun = population[0]; // initialise the best individual

        if (elitism) oldPopulationSorted = new ArrayList<>();
    }

    private static Individual[] randomPopulation() {
        switch (individualType) {
            case boolArray, intArray -> {
                return randPopulation_Normal();
            }
            case tspIntArray -> {
                return randPopulation_Tsp();
            }
            default -> {
                return null;
            }
        }
    }

    private static Individual[] randPopulation_Normal() {
        Individual[] population = new Individual[POPULATION_SIZE];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            // create a random individual at index i
            population[i] = new Individual(individualType, BITS);
        }

        return population;
    }

    private static Individual[] randPopulation_Tsp() {
        Individual[] population = new Individual[POPULATION_SIZE];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            // create an ArrayList with all the cities (in order)
            ArrayList<Integer> individualI = new ArrayList<>();
            for (int bit = 0; bit < BITS; bit++) {
                individualI.add(bit);
            }

            // shuffle the ArrayList
            Collections.shuffle(individualI);

            // convert the ArrayList to int[]
            int[] individualIarr = new int[BITS];
            for (int bit = 0; bit < BITS; bit++) {
                individualIarr[bit] = individualI.get(bit);
            }

            population[i] = new Individual(individualIarr);
        }

        return population;
    }


    // ------------------------------------- Fitness functions - Evaluate -------------------------------------

    /**
     * Evaluate the fitness of each individual in the population
     */
    private static void evaluate() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            fitness[i] = fitness(population[i]);
        }

        if (elitism) recordElite();
    }


    protected static double fitness(Individual individual) {
        switch (fitnessFunc) {
            case MostBitsOn -> {
                return fitness_MostGenesOn(individual.individualB);
            }
            case LeastBitsOn -> {
                return fitness_LeastGenesOn(individual.individualB);
            }
            case QuadEquationBoolArray -> {
                return fitness_QuadraticEquation(individual.individualB);
            }
            case Tsp -> {
                return fitness_Tsp(individual.individualI);
            }
            default -> {
                System.out.println("Select a valid fitness function");
                return 0.0;
            }
        }
    }

    private static double fitness_MostGenesOn(boolean[] individual) {
        double fitness = 0.0;

        for (boolean bit : individual) {
            if (bit) fitness++;
        }

        return fitness;
    }

    private static double fitness_LeastGenesOn(boolean[] individual) {
        double fitness = 0.0;

        for (boolean bit : individual) {
            if (!bit) fitness++;
        }

        return fitness;
    }

    private static double fitness_QuadraticEquation(boolean[] individual) {
        if (!Equation.evaluateParameters()) System.out.println("Set appropriate parameters for the quadratic equation");

        int numberLengthBits = BITS / Equation.NUMBERS_TO_FIND_COUNT;
        int[] numbers = new int[Equation.NUMBERS_TO_FIND_COUNT];
        for (int i = 0; i < Equation.NUMBERS_TO_FIND_COUNT; i++) {
            int from = i * numberLengthBits;
            int to = from + numberLengthBits;
            numbers[i] = binaryToDecimal(Arrays.copyOfRange(individual, from, to));
        }

        double difference = 0.0;

        HashMap<Double, Double> results = Equation.quadraticEquationSolver(numbers);
        for (Double key : results.keySet()) {
            difference += Math.abs(Equation.valuesAtPoints.get(key) - results.get(key));
        }

        if (difference == 0) {
            terminationConditionMet = true;
            difference = 1;
        }

        return (Math.pow(2, numberLengthBits)) / difference;
    }

    private static double fitness_Tsp(int[] individual) {
        double fitness = 0.0;

        for (int i = 0; i < individual.length - 1; i++) {
            fitness -= TSP.costMatrix[individual[i]][individual[i + 1]];
        }
        fitness -= TSP.costMatrix[individual[individual.length - 1]][individual[0]];

        return fitness;
    }

    private static double totalFitness() {
        double totalFitness = 0.0;

        for (double fitness : fitness) {
            totalFitness += fitness;
        }

        return totalFitness;
    }

    protected static double averageFitness() {
        return totalFitness() / POPULATION_SIZE;
    }

    protected static double populationBestFitness() {
        double bestFitness = fitness[0];

        for (double fitness : fitness) {
            if (compareFitness(bestFitness, fitness)) {
                bestFitness = fitness;
            }
        }

        return bestFitness;
    }


    // ------------------------------------- Selection -------------------------------------
    private static Individual[] selection() {
        switch (selection) {
            case Roulette -> {
                return rouletteSelect();
            }
            case Tournament -> {
                return tournamentSelect();
            }
            default -> {
                System.out.println("Select a valid selection method");
                return null;
            }
        }
    }

    // only works with positive fitness
    private static Individual[] rouletteSelect() {
        prejudice = new ArrayList<>();

        double cumulative = 0.0;
        double totalFitness = totalFitness();

        for (Individual individual : population) {
            cumulative += fitness(individual) / totalFitness;
            prejudice.add(cumulative);
        }

        return population;
    }

    private static Individual[] tournamentSelect() {
        ArrayList<Individual> selectedIndividuals = new ArrayList<>();

        int[] shuffledIndexes = IntStream.range(0, POPULATION_SIZE).toArray();
        Random rand = new Random();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int randIndex = rand.nextInt(POPULATION_SIZE);
            int temp = shuffledIndexes[i];
            shuffledIndexes[i] = shuffledIndexes[randIndex];
            shuffledIndexes[randIndex] = temp;
        }

        // divide into groups
        for (int i = 0; i < population.length; i += TOURNAMENT_SIZE) {
            int[] group = Arrays.copyOfRange(shuffledIndexes, i, i + TOURNAMENT_SIZE);

            Individual mostFitInGroup = findBestIndividual(group);
            selectedIndividuals.add(mostFitInGroup.copyItself());
        }

        prejudice = noPrejudice(selectedIndividuals.size());
        return selectedIndividuals.toArray(new Individual[0]);
    }

    private static Individual findBestIndividual(int from, int to) {
        int bestInd_index = from;

        for (int i = from + 1; i < to; i++) {
            bestInd_index = compareIndividuals(bestInd_index, i);
        }

        return population[bestInd_index];
    }

    private static Individual findBestIndividual(int[] indexes) {
        int bestInd_index = indexes[0];

        for (int i = 1; i < indexes.length; i++) {
            bestInd_index = compareIndividuals(bestInd_index, indexes[i]);
        }

        return population[bestInd_index];
    }

    protected static Individual findBestIndividual() {
        return findBestIndividual(0, POPULATION_SIZE);
    }


    // ------------------------------------- Population evolution -------------------------------------

    /**
     * Crossover and mutation
     */
    private static Individual[] evolution(Individual[] parentPopulation) {
        Individual[] newPopulation = crossover(parentPopulation);
        mutation(newPopulation);

        return newPopulation;
    }

    /**
     * The getParent() method returns a parent based on a prejudice
     */
    private static Individual getParent(Individual[] parentPopulation) {
        if (selection == SELECTION.Tournament && !everyoneWasParent) {
            return getParent_TournamentSel(parentPopulation);
        } else {
            return getParent_Prejudice(parentPopulation);
        }
    }

    private static Individual getParent_TournamentSel(Individual[] parentPopulation) {
        int upperBound = parentPopulation.length;

        Random rand = new Random();
        int randIndex = rand.nextInt(upperBound);
        if (!parents.contains(randIndex)) {
            parents.add(randIndex);
            return parentPopulation[randIndex];
        }

        int closestIndexUp = closestIndexUp(randIndex, upperBound);
        if (closestIndexUp != -1) {
            parents.add(closestIndexUp);
            return parentPopulation[closestIndexUp];
        }

        int closestIndexDown = closestIndexDown(randIndex);
        if (closestIndexDown != -1) {
            parents.add(closestIndexDown);
            return parentPopulation[closestIndexDown];
        }

        everyoneWasParent = true;
        return getParent_Prejudice(parentPopulation);
    }

    private static int closestIndexUp(int index, int upperBound) {
        while (index < upperBound) {
            if (parents.contains(index)) index++;
            else return index;
        }

        // if the index is out of bounds
        return -1;
    }

    private static int closestIndexDown(int index) {
        while (index > -1) {
            if (parents.contains(index)) index--;
            else return index;
        }

        // if the index is out of bounds
        return -1;
    }

    private static Individual getParent_Prejudice(Individual[] parentPopulation) {

        double rand = Math.random();
        for (int individual = 0; individual < POPULATION_SIZE; individual++) {
            if (rand < prejudice.get(individual)) {
                return parentPopulation[individual];
            }
        }

        System.out.println("This line should not be reached, check for errors");
        return new Individual(individualType, BITS);
    }

    private static Individual[] crossover(Individual[] parentPopulation) {
        Individual[] newPopulation = new Individual[POPULATION_SIZE];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual parent1 = getParent(parentPopulation);
            Individual parent2 = getParent(parentPopulation);

            while (parent1 == parent2) parent2 = getParent(parentPopulation); // make sure the parents are different

            Individual[] offsprings = crossoverIndividuals(parent1, parent2);

            newPopulation[i] = offsprings[0];
            newPopulation[++i] = offsprings[1];
        }

        // reset the parents HashSet if using tournament selection
        if (SELECTION.Tournament == selection) {
            everyoneWasParent = false;
            parents = new HashSet<>();
        }

        return newPopulation;
    }

    private static Individual[] crossoverIndividuals(Individual parent1, Individual parent2) {
        switch (crossover) {
            case SinglePoint_Simple -> {
                return crossover_SinglePoint_Simple(parent1.copyItself(), parent2.copyItself());
            }
            case nPoint_Simple -> {
                return crossover_nPoint_Simple(parent1.copyItself(), parent2.copyItself());
            }
            case Uniform_Simple -> {
                return crossover_Uniform_Simple(parent1.copyItself(), parent2.copyItself());
            }
            case PMX_Tsp -> {
                return crossover_PartiallyMapped_Tsp(parent1.copyItself(), parent2.copyItself());
            }
            default -> {
                return null;
            }
        }
    }

    private static Individual[] crossover_SinglePoint_Simple(Individual parent1_Copy, Individual parent2_Copy) {
        if (Math.random() < CROSSOVER_PROBABILITY) {
            int crossoverPoint = (int) (Math.random() * BITS);
            boolean[] temp = parent1_Copy.individualB.clone();

            System.arraycopy(parent2_Copy.individualB, crossoverPoint, parent1_Copy.individualB, crossoverPoint, BITS - crossoverPoint);
            System.arraycopy(temp, crossoverPoint, parent2_Copy.individualB, crossoverPoint, BITS - crossoverPoint);
        }

        return new Individual[]{parent1_Copy, parent2_Copy};
    }

    private static Individual[] crossover_nPoint_Simple(Individual parent1_Copy, Individual parent2_Copy) {
        ArrayList<Integer> points = new ArrayList<>();
        Random rand = new Random();

        boolean[] parent1_array = parent1_Copy.individualB;
        boolean[] parent2_array = parent2_Copy.individualB;
        boolean[] temp = parent1_Copy.individualB.clone();

        points.add(rand.nextInt(0, BITS));
        while (points.size() < nPointCrossoverPoints) {
            int randomPoint = rand.nextInt(0, BITS);
            if (!points.contains(randomPoint)) points.add(randomPoint);
        }

        for (int point = 0; point < nPointCrossoverPoints; point++) {
            int from = points.get(point);

            if (point != nPointCrossoverPoints - 1) {
                int to = points.get(point + 1);

                System.arraycopy(parent2_array, from, parent1_array, from, (to - from));
                System.arraycopy(temp, from, parent1_array, from, (to - from));
            } else {
                System.arraycopy(parent2_array, from, parent1_array, from, (BITS - from));
                System.arraycopy(temp, from, parent1_array, from, (BITS - from));
            }
        }

        return new Individual[]{parent1_Copy, parent2_Copy};
    }

    private static Individual[] crossover_Uniform_Simple(Individual parent1_Copy, Individual parent2_Copy) {
        boolean[] parent1_array = parent1_Copy.individualB;
        boolean[] parent2_array = parent2_Copy.individualB;
        boolean[] temp = parent1_Copy.individualB.clone();

        for (int bit = 0; bit < BITS; bit++) {
            if (Math.random() < CROSSOVER_PROBABILITY) {
                System.arraycopy(parent2_array, bit, parent1_array, bit, 1);
                System.arraycopy(temp, bit, parent1_array, bit, 1);
            }
        }

        return new Individual[]{parent1_Copy, parent2_Copy};
    }

    private static Individual[] crossover_PartiallyMapped_Tsp(Individual parent1_Copy, Individual parent2_Copy) {
        int[] parent1 = parent1_Copy.individualI;
        int[] parent2 = parent2_Copy.individualI;

        int[] offspring1 = parent1.clone();
        int[] offspring2 = parent2.clone();

        int crossoverPoint1 = (int) (Math.random() * BITS);
        int crossoverPoint2 = (int) (Math.random() * BITS);

        // make sure crossoverPoint1 <= crossoverPoint2
        if (crossoverPoint1 > crossoverPoint2) {
            int temp = crossoverPoint1;
            crossoverPoint1 = crossoverPoint2;
            crossoverPoint2 = temp;
        }

        // record who is given what, to fix the duplicates later
        ArrayList<Integer> parent1GaveOffspr2 = new ArrayList<>();
        ArrayList<Integer> parent2GaveOffspr1 = new ArrayList<>();

        // crossover
        for (int i = crossoverPoint1; i < crossoverPoint2 + 1; i++) {
            offspring1[i] = parent2[i];
            offspring2[i] = parent1[i];

            parent1GaveOffspr2.add(parent1[i]);
            parent2GaveOffspr1.add(parent2[i]);
        }

        // fix duplicate genes
        handleDuplicateGenes(offspring1, parent1GaveOffspr2, parent2GaveOffspr1, crossoverPoint1, crossoverPoint2);
        handleDuplicateGenes(offspring2, parent2GaveOffspr1, parent1GaveOffspr2, crossoverPoint1, crossoverPoint2);

        return new Individual[]{new Individual(offspring1), new Individual(offspring2)};
    }

    private static void handleDuplicateGenes(int[] offspring, ArrayList<Integer> youGave, ArrayList<Integer> youReceived, int from, int to) {
        // fix duplicate genes in the beginning of the offspring DNA
        for (int i = 0; i < from; i++) {
            if (youReceived.contains(offspring[i])) {
                offspring[i] = youGave.get(youReceived.indexOf(offspring[i]));
                handleDuplicateGenes(offspring, youGave, youReceived, from, to);
                return;
            }
        }

        // fix duplicate genes after the second crossover point
        for (int i = to + 1; i < offspring.length; i++) {
            if (youReceived.contains(offspring[i])) {
                offspring[i] = youGave.get(youReceived.indexOf(offspring[i]));
                handleDuplicateGenes(offspring, youGave, youReceived, from, to);
                return;
            }
        }
    }

    private static void mutation(Individual[] offspringPopulation) {
        for (Individual individual : offspringPopulation) {
            if (mutation == MUTATION.Uniform_Bool) mutateUniform(individual.individualB);
            else if (Math.random() < MUTATION_PROBABILITY) {
                switch (mutation) {
                    case SinglePoint_Bool -> mutationSinglePoint(individual.individualB);
                    case Exchange_Tsp -> mutateExchange(individual.individualI);
                    case Inversion_Tsp -> mutateInversion(individual.individualI);
                }
            }
        }
    }

    // Simple mutation on a bit level - many bits in an individual may mutate (for boolean arrays)
    private static void mutateUniform(boolean[] individualB) {
        for (int bit = 0; bit < individualB.length; bit++) {
            if (Math.random() < MUTATION_PROBABILITY) {
                individualB[bit] = !individualB[bit];
            }
        }
    }

    // Simple mutation on an individual level - max 1 bit in an individual may mutate (for boolean arrays)
    private static void mutationSinglePoint(boolean[] individualB) {
        int mutationPoint = (int) (Math.random() * BITS);
        individualB[mutationPoint] = !individualB[mutationPoint];
    }

    // (for TSP)
    private static void mutateExchange(int[] individualI) {
        // choose two random bits
        int bit1 = (int) (Math.random() * BITS);
        int bit2 = (int) (Math.random() * BITS);

        // swap them
        int temp = individualI[bit1];
        individualI[bit1] = individualI[bit2];
        individualI[bit2] = temp;
    }

    // (for TSP)
    private static void mutateInversion(int[] individualI) {
        // choose two random bits
        int from = (int) (Math.random() * BITS);
        int to = (int) (Math.random() * BITS);

        // make sure from <= to
        if (from > to) {
            int temp = from;
            from = to;
            to = temp;
        }

        // extract the sub-array
        int[] temp = new int[to - from];
        System.arraycopy(individualI, from, temp, 0, to - from);

        // insert the sub-array back, but in reverse order
        for (int i = 0; i < temp.length; i++) {
            individualI[from + i] = temp[(temp.length - 1) - i];
        }
    }


    // ------------------------------------- Elitism -------------------------------------
    private static void recordElite() {
        oldPopulationSorted = new ArrayList<>(Arrays.asList(population));
        oldPopulationSorted.sort((o1, o2) -> {
            if (minOrMax == MIN_MAX.Min) {
                return Double.compare(fitness(o1), fitness(o2));
            } else {
                return Double.compare(fitness(o2), fitness(o1));
            }
        });
    }

    private static void reintroduceElite() {
        for (int i = 0; i < eliteIndividualsCount; i++) {
            // choose a random individual from the new population
            int index = (int) (Math.random() * POPULATION_SIZE);

            // if the random individual is worse than the elite individual, replace it
            population[index] = compareIndividuals(population[index], oldPopulationSorted.get(i).copyItself());
        }
    }

    // returns the better individual
    private static Individual compareIndividuals(Individual individual1, Individual individual2) {
        if (minOrMax == MIN_MAX.Min) {
            return fitness(individual1) < fitness(individual2) ? individual1 : individual2;
        } else {
            return fitness(individual1) > fitness(individual2) ? individual1 : individual2;
        }
    }

    // returns the index of the better individual
    private static int compareIndividuals(int ind1_index, int ind2_index) {
        if (minOrMax == MIN_MAX.Min) {
            return fitness[ind1_index] < fitness[ind2_index] ? ind1_index : ind2_index;
        } else {
            return fitness[ind1_index] > fitness[ind2_index] ? ind1_index : ind2_index;
        }
    }

    // returns true if the contender is better than the current
    private static boolean compareFitness(double current, double contender) {
        if (minOrMax == MIN_MAX.Min) {
            return contender < current;
        } else {
            return contender > current;
        }
    }


    // ------------------------------------- Other -------------------------------------
    private static void resetGlobals() {
        population = randPopulation_Normal();
        bestIndividual_EntireRun = population[0];
        terminationConditionMet = false;
        everyoneWasParent = false;
        parents = new HashSet<>();
    }

    private static void recordBestIndividual_EntireRun() {
        Individual bestInGeneration = findBestIndividual();

        bestIndividual_EntireRun = compareIndividuals(bestIndividual_EntireRun, bestInGeneration.copyItself());
    }

    private static ArrayList<Double> noPrejudice(int populationSize) {
        double equalChances = 1.0 / populationSize;
        double cumulative = equalChances;
        ArrayList<Double> equalPrejudice = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            equalPrejudice.add(cumulative);
            cumulative += equalChances;
        }

        return equalPrejudice;
    }

    protected static int binaryToDecimal(boolean[] binary) {
        int decimal = 0;
        boolean negative = false;

        for (int i = 0; i < binary.length; i++) {
            if (i != 0 && binary[i]) decimal += (int) Math.pow(2, binary.length - i - 1);
            else if (i == 0) negative = binary[i];
        }

        return negative ? (decimal * -1) : decimal;
    }
}