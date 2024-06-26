package Main;

import Enums.*;
import Problems.*;

import java.util.*;

import static Main.Check.checkEverything;
import static Main.Presets.preset;

// Author: Nikola Kolev
// Date: 20.03.2024
// Version: 5.0

/**
 * <p>
 * To be fixed || implemented in next version:
 * <ul>
 *     <li>Implement using a termination condition</li>
 *     <li>Finish nPointCrossover, linking it with the nPointCrossoverPoints global var</li>
 *     <li>Implement MUTATION of type Tsp, which has 50% change of performing Exchange or Inversion mutation</li>
 *     <li>Work on preventing premature convergence (check out: preselection, crowding, fitness sharing, incest prevention)</li>
 *     <li>Incest prevention can be implemented through a variable parents[] on an Main.Individual level</li>
 *     <li>Crossover Uniform Simple doesn't give good results for some reason - debug if there is time</li>
 *     <li>Clean up code (refactor, rename methods and vars, add comments, remove junk)</li>
 * </ul>
 */
public class GA {
    // ------------------------------------- Switches -------------------------------------
    public static INDIVIDUAL_TYPE individualType;
    public static SELECTION selection;
    public static FITNESS_FUNC fitnessFunc;
    public static CROSSOVER crossover;
    public static MUTATION mutation;
    public static MIN_MAX minOrMax;


    // ------------------------------------- Elitism -------------------------------------
    public static boolean elitism;
    private static List<Individual> oldPopulationSorted;
    private static final int eliteIndividualsCount = 2;


    // ------------------------------------- Tournament sel - Ensure all selected become parents -------------------------------------
    public static boolean everyoneWasParent = false;
    public static Set<Integer> parents = new HashSet<>();


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
    public static int BITS;
    public static int POPULATION_SIZE;
    public static int TOURNAMENT_SIZE;
    public static int MAX_GENERATION;
    public static double CROSSOVER_PROBABILITY, MUTATION_PROBABILITY;


    // ------------------------------------- Global population -------------------------------------
    public static Individual[] population;
    public static double[] fitness;
    public static List<Double> prejudice;
    public static Individual bestIndividual_EntireRun;


    // ------------------------------------- Similarity -------------------------------------
    private static double[][] similarityMatrix; // similarity between each pair of individuals
    private static double[] similarityArray; // overall similarity of each individual to the rest of the population


    // ------------------------------------- Other -------------------------------------
    public static int nPointCrossoverPoints;
    private static final double terminationConditionFitness = -1;


    // ------------------------------------- Methods -------------------------------------
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        try {
            for (int testNo = 0; testNo < 1; testNo++) {
                // Set all the switches and settings for the Main.GA to run
                // (I suggest using presets or the setSwitches() and setSettings() methods)
                preset("TspTorTspPmxExcNoeTs2"); // BolTorMboSinUnfEltMed | BolTorLboSinUnfEltMed | BolTorQdeSinUnfEltQde | TspTorTspPmxInvEltTsp | FtxTorNvpFtxAriEltFtx | AocTorAocFtxAriEltAoc

                // Call Main.Check.checkEverything if everything is set correctly
                checkEverything();

                // The main algorithm
                initialise();
                evaluate();

                runGA();

                // Must do this at the end of each test
                Print.shortStats(testNo + 1);
                resetGlobals();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        System.out.println("\nExecution time: " + (endTime - startTime) / 1e6 + " ms");
    }

    /**
     * Run the Main.GA for a given number of generations
     * 1. Selection
     * 2. Crossover
     * 3. Mutation
     * 4. Elitism
     * 5. Evaluate
     * 6. Termination check
     */
    private static void runGA() throws Exception {
        for (int genNo = 0; genNo < MAX_GENERATION; genNo++) {
            Individual[] selected = selection(); // selection
            population = evolution(selected); // crossover and mutation

            if (elitism) reintroduceElite(); // elitism

            evaluate();

            // Must do this at the end of each generation
            recordBestIndividual_EntireRun();
            Print.generationStats(genNo + 1); // if (fitnessFunc != FITNESS_FUNC.Problems.SequentialCovering) Main.Print.generationStats(genNo + 1);

            // Termination condition
            if (terminationConditionMet()) break;

            /*
             * PLAYING AROUND: I've been playing around with it and the Ts2 population, with the following preset: TspTorTspPmxExcNoeTs2
             */
            if (genNo % 3 == 0) decreasePopulationSimilarity();
        }
    }

    // For debugging purposes
    private static void test() throws Exception {
        preset("FtxTorNvpFtxAriEltMaxFtx");

        double fitness1 = FTTx.npv(new int[]{1, 1, 3, 2, 2, 2, 1, 2, 3, 3, 1, 1, 2, 3, 1, 2, 3, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 0, 2, 3, 0, 1, 3, 2, 1, 0, 1, 1, 0, 2, 2, 2, 2, 1, 0, 1, 2, 2, 1, 2, 1, 2, 2, 2, 2, 0, 1, 2, 3, 2, 2, 1, 3, 0, 2, 1, 2, 2, 2, 2, 2, 0, 2, 1, 1, 2, 2, 1, 2, 1, 2, 1, 1, 1, 1, 2, 2, 2, 2, 1, 2, 0, 1, 2, 1, 2, 2, 2, 2, 1, 2, 0, 2, 2, 1, 2, 2, 2, 1, 1, 0, 1, 1, 1, 2, 2, 2, 2, 1, 0, 0, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 1, 2, 2, 1, 15, 2, 1, 2, 2, 2, 0, 1, 2, 2, 2, 1, 2, 1, 1, 1, 1, 1, 0, 1, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 0, 1, 2, 2, 2, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 2, 0, 1, 0, 2, 1, 2, 1, 2, 1, 1, 1, 2, 1, 2, 2, 2, 1, 0, 2, 2, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2, 1, 1, 0, 2, 1, 0, 2, 2, 1, 1, 2, 2, 1, 2, 1, 1, 1, 2, 2, 2, 1, 0, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 1, 1, 1, 2, 2, 0, 2, 2, 1, 2, 0, 1, 1, 2, 2, 2, 0, 0, 2, 2, 1, 2, 2, 0, 1, 1, 2});
        double fitness2 = FTTx.npv(new int[]{1, 1, 3, 2, 2, 2, 1, 2, 3, 3, 1, 1, 2, 3, 1, 2, 3, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 0, 2, 3, 0, 1, 3, 2, 1, 0, 1, 1, 0, 2, 2, 2, 2, 1, 0, 1, 2, 2, 1, 2, 1, 2, 2, 2, 2, 0, 1, 2, 3, 2, 2, 1, 3, 0, 2, 1, 2, 2, 2, 2, 2, 0, 2, 1, 1, 2, 2, 1, 2, 1, 2, 1, 1, 1, 1, 2, 2, 2, 2, 1, 2, 0, 1, 2, 1, 2, 2, 2, 2, 1, 2, 0, 2, 2, 1, 2, 2, 2, 1, 1, 0, 1, 1, 1, 2, 2, 2, 2, 1, 0, 0, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 1, 2, 2, 1, 0, 2, 1, 2, 2, 2, 0, 1, 2, 2, 2, 1, 2, 1, 1, 1, 1, 1, 0, 1, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 0, 1, 2, 2, 2, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 2, 0, 1, 0, 2, 1, 2, 1, 2, 1, 1, 1, 2, 1, 2, 2, 2, 1, 0, 2, 2, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2, 1, 1, 0, 2, 1, 0, 2, 2, 1, 1, 2, 2, 1, 2, 1, 1, 1, 2, 2, 2, 1, 0, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 1, 1, 1, 2, 2, 0, 2, 2, 1, 2, 0, 1, 1, 2, 2, 2, 0, 0, 2, 2, 1, 2, 2, 0, 1, 1, 2});
        double fitness3 = FTTx.npv(new int[]{1, 1, 2, 1, 1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 1, 2, 2, 1, 1, 2, 2, 1, 2, 2, 1, 0, 2, 2, 0, 1, 2, 1, 1, 0, 1, 2, 0, 2, 2, 2, 2, 1, 0, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 0, 1, 1, 2, 2, 1, 1, 2, 0, 2, 1, 2, 2, 2, 2, 1, 0, 2, 2, 1, 2, 1, 1, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2, 1, 2, 0, 1, 2, 1, 2, 2, 1, 2, 1, 2, 0, 3, 2, 1, 2, 1, 1, 2, 2, 0, 2, 1, 1, 2, 2, 2, 2, 1, 0, 0, 2, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 1, 2, 1, 0, 2, 1, 3, 2, 2, 0, 1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0, 1, 3, 2, 1, 2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2, 2, 6, 1, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 2, 2, 2, 2, 2, 2, 1, 1, 0, 2, 0, 1, 7, 2, 1, 2, 1, 2, 1, 1, 2, 2, 1, 2, 2, 2, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 0, 2, 1, 0, 2, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 2, 2, 1, 0, 1, 2, 2, 2, 1, 2, 2, 1, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1, 2, 2, 0, 2, 1, 2, 2, 0, 1, 1, 2, 2, 1, 0, 0, 2, 2, 2, 2, 2, 0, 1, 1, 2});
        double fitness4 = FTTx.npv(new int[]{1, 1, 2, 1, 1, 2, 2, 2, 2, 2, 1, 1, 2, 3, 1, 2, 2, 2, 1, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 11, 1, 2, 0, 1, 2, 1, 1, 12, 1, 2, 0, 2, 2, 2, 2, 1, 13, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 0, 1, 1, 2, 2, 1, 1, 2, 0, 2, 1, 2, 2, 2, 2, 1, 0, 2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 1, 2, 2, 2, 1, 2, 1, 2, 14, 1, 2, 1, 2, 1, 1, 2, 1, 2, 0, 3, 1, 1, 2, 2, 2, 2, 2, 12, 2, 2, 1, 2, 2, 1, 2, 1, 0, 14, 2, 5, 1, 1, 1, 2, 2, 1, 2, 2, 1, 1, 2, 1, 14, 2, 1, 12, 1, 2, 6, 2, 2, 2, 2, 2, 2, 1, 2, 1, 1, 1, 11, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 3, 1, 2, 2, 7, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 0, 1, 2, 2, 2, 2, 2, 2, 1, 1, 0, 2, 0, 1, 6, 2, 1, 2, 1, 2, 1, 1, 2, 2, 1, 2, 2, 3, 1, 0, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 1, 10, 2, 1, 0, 2, 2, 1, 1, 2, 2, 1, 2, 1, 1, 1, 1, 2, 2, 1, 0, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 2, 13, 2, 1, 2, 2, 0, 1, 1, 2, 2, 1, 12, 11, 2, 2, 2, 2, 2, 15, 2, 1, 2});
        double fitness5 = FTTx.npv(new int[]{1, 1, 2, 1, 1, 2, 2, 2, 2, 2, 1, 1, 2, 3, 1, 2, 2, 2, 1, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 0, 1, 2, 0, 1, 2, 1, 1, 0, 1, 2, 0, 2, 2, 2, 2, 1, 0, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 0, 1, 1, 2, 2, 1, 1, 2, 0, 2, 1, 2, 2, 2, 2, 1, 0, 2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 1, 2, 2, 2, 1, 2, 1, 2, 0, 1, 2, 1, 2, 1, 1, 2, 1, 2, 0, 3, 1, 1, 2, 2, 2, 2, 2, 0, 2, 2, 1, 2, 2, 1, 2, 1, 0, 0, 2, 5, 1, 1, 1, 2, 2, 1, 2, 2, 1, 1, 2, 1, 0, 2, 1, 0, 1, 2, 6, 2, 2, 2, 2, 2, 2, 1, 2, 1, 1, 1, 0, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 1, 2, 3, 1, 2, 2, 7, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 0, 1, 2, 2, 2, 2, 2, 2, 1, 1, 0, 2, 0, 1, 6, 2, 1, 2, 1, 2, 1, 1, 2, 2, 1, 2, 2, 3, 1, 0, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 1, 0, 2, 1, 0, 2, 2, 1, 1, 2, 2, 1, 2, 1, 1, 1, 1, 2, 2, 1, 0, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 2, 0, 2, 1, 2, 2, 0, 1, 1, 2, 2, 1, 0, 0, 2, 2, 2, 2, 2, 0, 2, 1, 2});

        System.out.println(fitness1 + "\n" + fitness2 + "\n" + fitness3 + "\n" + fitness4 + "\n" + fitness5);
    }


    // ------------------------------------- Switches and settings -------------------------------------
    public static void setSwitches(INDIVIDUAL_TYPE individualType,
                                   SELECTION selection,
                                   FITNESS_FUNC fitnessFunction,
                                   CROSSOVER crossover,
                                   MUTATION mutation,
                                   boolean elitism) throws Exception {
        GA.individualType = individualType;
        GA.selection = selection;
        GA.fitnessFunc = fitnessFunction;
        GA.crossover = crossover;
        GA.mutation = mutation;
        GA.elitism = elitism;
        GA.minOrMax = fitnessFunction.getMinMax();

        // variables that must be set, depending on the switches
        if (fitnessFunction == FITNESS_FUNC.QuadEquationBoolArray) {
            if (Equation.valuesAtPoints == null) Equation.populateValuesAtPoints();
        } else if (fitnessFunc == FITNESS_FUNC.Tsp) {
            if (TSP.costMatrix == null) {
                if (TSP.filename != null) TSP.loadMatrix(TSP.filename);
                else TSP.createMatrix();
            }
        } else if (fitnessFunc == FITNESS_FUNC.FTTxNVP) {
            if (FTTx.households == null) {
                if (FTTx.parametersFilename != null) {
                    FTTx.loadParameters();
                    FTTx.loadHouseholds();
                } else {
                    FTTx.defaultParams();
                }
            }
        } else if (fitnessFunction == FITNESS_FUNC.SequentialCovering) {
            if (SequentialCovering.filename == null) throw new Exception("Sequential Covering filename not set");
            else SequentialCovering.setTrainingData();
        } else if (fitnessFunction == FITNESS_FUNC.AocDay5) {
            if (AocDay5.filename == null) throw new Exception("AocDay5 filename not set");
            else AocDay5.loadData(AocDay5.filename);
        }
    }

    public static void setSettings(int BITS,
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
    private static void initialise() throws Exception {
        population = randomPopulation(); // create a random population
        fitness = new double[POPULATION_SIZE]; // initialise the fitness array

        bestIndividual_EntireRun = population[0]; // initialise the best individual

        if (elitism) oldPopulationSorted = new ArrayList<>(); // keep track of the best parents if using elitism
    }

    private static Individual[] randomPopulation() throws Exception {
        return individualType.randomPopulation();
    }


    // ------------------------------------- Fitness functions - Evaluate -------------------------------------

    /**
     * Evaluate the fitness of each individual in the population
     */
    private static void evaluate() throws Exception {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            fitness[i] = fitness(population[i]);
            population[i].fitness = fitness[i];
        }

        if (elitism) recordElite();
    }

    public static double fitness(Individual individual) throws Exception {
        return fitnessFunc.fitness(individual);
    }

    public static double totalFitness() {
        double totalFitness = 0.0;

        for (double fitness : fitness) {
            totalFitness += fitness;
        }

        return totalFitness;
    }

    public static double averageFitness() {
        return totalFitness() / POPULATION_SIZE;
    }

    public static double populationBestFitness() {
        double bestFitness = fitness[0];

        for (double fitness : fitness) {
            if (!compareFitness(bestFitness, fitness)) {
                bestFitness = fitness;
            }
        }

        return bestFitness;
    }


    // ------------------------------------- Selection -------------------------------------

    /**
     * Uses some algorithm (might be random) to select individuals from the population to be parents
     */
    private static Individual[] selection() throws Exception {
        return selection.select();
    }

    // using the fitness[] array
    private static Individual findBestIndividual(int from, int to) {
        int bestInd_index = from;

        for (int i = from + 1; i < to; i++) {
            bestInd_index = compareIndividuals(bestInd_index, i);
        }

        return population[bestInd_index];
    }

    // using the fitness[] array
    public static Individual findBestIndividual() {
        return findBestIndividual(0, POPULATION_SIZE);
    }


    // ------------------------------------- Population evolution -------------------------------------

    /**
     * Crossover and mutation
     */
    private static Individual[] evolution(Individual[] parentPopulation) throws Exception {
        Individual[] newPopulation = crossover(parentPopulation);
        mutation(newPopulation);

        return newPopulation;
    }

    /**
     * The getParent() method returns a parent from the selected parents based on a prejudice
     */
    private static Individual getParent(Individual[] parentPopulation) throws Exception {
        return selection.getParent(parentPopulation);
    }

    private static Individual[] crossover(Individual[] parentPopulation) throws Exception {
        Individual[] newPopulation = new Individual[POPULATION_SIZE];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual parent1 = getParent(parentPopulation);
            Individual parent2 = getParent(parentPopulation);

            // decrease premature convergence
            while (parent1 == parent2) parent2 = getParent(parentPopulation); // make sure the parents are different

            Individual[] offsprings = crossover.crossoverIndividuals(parent1, parent2);

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

    private static void mutation(Individual[] offspringPopulation) {
        mutation.mutation(offspringPopulation);
    }


    // ------------------------------------- Elitism -------------------------------------
    // Records the elite individuals from the current population (in the oldPopulationSorted list)
    private static void recordElite() throws Exception {
        if (population[0].fitness == null) throw new Exception("Fitness not calculated");

        oldPopulationSorted = new ArrayList<>(Arrays.asList(population));
        oldPopulationSorted.sort((o1, o2) -> {
            if (minOrMax == MIN_MAX.Min) {
                return Double.compare(o1.fitness, o2.fitness);
            } else {
                return Double.compare(o2.fitness, o1.fitness);
            }
        });
    }

    // Reintroduces x elite individuals into the new population (where x = eliteIndividualsCount)
    private static void reintroduceElite() throws Exception {
        for (int i = 0; i < eliteIndividualsCount; i++) {
            // choose a random individual from the new population
            int index = (int) (Math.random() * POPULATION_SIZE);

            // if the random individual is worse than the elite individual, replace it
            population[index] = compareIndividuals(population[index], oldPopulationSorted.get(i));
        }
    }

    // returns the better individual (NOT using the fitness[] array)
    private static Individual compareIndividuals(Individual individual1, Individual individual2) throws Exception {
        if (minOrMax == MIN_MAX.Min) {
            return fitness(individual1) < fitness(individual2) ? individual1 : individual2;
        } else {
            return fitness(individual1) > fitness(individual2) ? individual1 : individual2;
        }
    }

    // returns the index of the better individual (using the fitness[] array)
    private static int compareIndividuals(int ind1_index, int ind2_index) {
        if (minOrMax == MIN_MAX.Min) {
            return fitness[ind1_index] < fitness[ind2_index] ? ind1_index : ind2_index;
        } else {
            return fitness[ind1_index] > fitness[ind2_index] ? ind1_index : ind2_index;
        }
    }

    // returns true if the contender is better than the current
    private static boolean compareFitness(double current, double contender) {
        try {
            return minOrMax.compareFitness(current, contender);
        } catch (Exception e) {
            System.out.print("Error in " + Class.class.getName() + ": " + e.getMessage());
            return minOrMax == MIN_MAX.Min ? contender < current : contender > current;
        }
    }


    // ------------------------------------- Similarity -------------------------------------
    private static void updateSimilarity() {
        updateSimilarityMatrix();
        updateSimilarityArray();
    }

    // Updates the similarity matrix with the similarity between each pair of individuals
    private static void updateSimilarityMatrix() {
        if (similarityMatrix == null) initialiseSimilarityMatrix();

        // calculate the similarity between each pair of individuals
        for (int i = 0; i < POPULATION_SIZE; i++) {
            for (int j = i + 1; j < POPULATION_SIZE; j++) {
                similarityMatrix[i][j] = similarityMatrix[j][i] = population[i].similarity(population[j]);
            }
        }
    }

    private static void initialiseSimilarityMatrix() {
        similarityMatrix = new double[POPULATION_SIZE][POPULATION_SIZE];
    }

    // Updates the similarity array with the average similarity of each individual compared to all other individuals
    private static void updateSimilarityArray() {
        if (similarityArray == null) initialiseSimilarityArray();

        // calculate the average similarity of each individual to all other individuals
        for (int i = 0; i < POPULATION_SIZE; i++) {
            similarityArray[i] = Arrays.stream(similarityMatrix[i]).sum() / POPULATION_SIZE;
        }
    }

    private static void initialiseSimilarityArray() {
        similarityArray = new double[POPULATION_SIZE];
    }

    // Returns the average similarity of the population (using the similarityArray => must be called after updateSimilarityArray())
    private static double averageSimilarity() {
        return Arrays.stream(similarityArray).sum() / POPULATION_SIZE;
    }


    // ------------------------------------- Other -------------------------------------
    // Resets all global variables to their initial state
    private static void resetGlobals() {
        population = null;
        fitness = null;
        if (selection == SELECTION.Roulette) prejudice = null;
        bestIndividual_EntireRun = null;

        oldPopulationSorted = null;
        if (selection == SELECTION.Tournament) everyoneWasParent = false;
        parents = new HashSet<>();
    }

    private static void recordBestIndividual_EntireRun() throws Exception {
        Individual bestInGeneration = findBestIndividual();

        bestIndividual_EntireRun = compareIndividuals(bestIndividual_EntireRun, bestInGeneration.copyItself());
    }

    private static boolean terminationConditionMet() {
        // If the best fitness of the population == the termination condition fitness => the termination condition is met
        if (populationBestFitness() == terminationConditionFitness) {
            System.out.println("Termination condition met");
            return true;
        } else return false;
    }

    // NB: This method contains a lot of hard-coded values.
    private static void decreasePopulationSimilarity() {
        updateSimilarity();
        double averageSimilarity = averageSimilarity();

        if (averageSimilarity <= 33) {
            replacePercentOfPopulation(10);
        } else if (averageSimilarity > 33 && averageSimilarity <= 50) {
            replacePercentOfPopulation(35);
        } else if (averageSimilarity > 50 && averageSimilarity <= 60) {
            replacePercentOfPopulation(40);
        } else if (averageSimilarity > 60 && averageSimilarity <= 70) {
            replacePercentOfPopulation(45);
        } else if (averageSimilarity > 70 && averageSimilarity <= 80) {
            replacePercentOfPopulation(55);
        } else if (averageSimilarity > 80 && averageSimilarity <= 90) {
            replacePercentOfPopulation(66);
        } else if (averageSimilarity > 90 && averageSimilarity <= 95) {
            replacePercentOfPopulation(80);
        } else if (averageSimilarity > 95) {
            replacePercentOfPopulation(95);
        }

//        System.out.println("Average similarity: " + averageSimilarity);
    }

    // Replaces an APPROXIMATE percentage of the population with random individuals
    private static void replacePercentOfPopulation(int percent) {
        // Create a list of indexes of the population and shuffle it
        // (that is done to ensure we replace different individuals each time)
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);

        // Replace the first x individuals with random individuals (where x = percent of the population)
        int amount = (POPULATION_SIZE * percent) / 100;
        for (int i = 0; i < amount; i++) {
            population[indexes.get(i)] = individualType.randomIndividual();
        }
    }
}