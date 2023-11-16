import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Sequential covering procedure that uses a GA to create rules.
 *
 * @author Fernando Otero
 * @version 2.0 (Nikola Kolev version)
 */
public class SeCoGA {
    /**
     * Number of bits of the individual encoding.
     */
    private static int BITS;

    /**
     * The population size.
     */
    private static final int POPULATION_SIZE = 100;

    /**
     * The number of generations.
     */
    private static final int MAX_GENERATION = 50;

    /**
     * Probability of the mutation operator.
     */
    private static final double MUTATION_PROBABILITY = 0.1;

    /**
     * Probability of the crossover operator.
     */
    private static final double CROSSOVER_PROBABILITY = 0.9;

    /**
     * Random number generation.
     */
    private final Random random = new Random();

    /**
     * The current population.
     */
    private boolean[][] population = new boolean[POPULATION_SIZE][];

    /**
     * Fitness values of each individual of the population.
     */
    private final double[] fitness = new double[POPULATION_SIZE];

    public static void main(String[] args) {
        try {
            SeCoGA seco = new SeCoGA();
            seco.covering("SequentialCovering/weather.arff");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * The sequential covering procedure.
     *
     * @param filename the dataset file name.
     */
    private void covering(String filename) throws IOException {
        ArrayList<boolean[]> trainingData = Dataset.read(filename);
        BITS = trainingData.get(0).length;

        int minimum = (int) Math.ceil(trainingData.size() * 0.1);

        do {
            // 1) finds a well performing rule using the GA
            boolean[] best = run(trainingData);

            // prints the rule
            System.out.println(Dataset.toString(best));

            // 2) removes covered instances
            trainingData.removeIf(instance -> covers(best, instance));

            // 3) checks if we have remaining training data
        } while (trainingData.size() >= minimum);
    }

    // Genetic Algorithm -----------------------------------------------//

    /**
     * Starts the execution of the GA.
     */
    private boolean[] run(ArrayList<boolean[]> trainingData) {
        //--------------------------------------------------------------//
        // initialises the population                                   //
        //--------------------------------------------------------------//
        initialise();

        //--------------------------------------------------------------//
        // evaluates the population                                    //
        //--------------------------------------------------------------//
        evaluate(trainingData);

        for (int g = 0; g < MAX_GENERATION; g++) {
            //----------------------------------------------------------//
            // creates a new population                                 //
            //----------------------------------------------------------//

            boolean[][] newPopulation = new boolean[POPULATION_SIZE][BITS];
            // index of the current individual to be created
            int current = 0;

            while (current < POPULATION_SIZE) {
                double probability = random.nextDouble();

                // should we perform mutation?
                if (probability <= MUTATION_PROBABILITY || (POPULATION_SIZE - current) == 1) {
                    int parent = select();

                    boolean[] offspring = mutation(parent);
                    // copies the offspring to the new population
                    newPopulation[current++] = offspring;
                }
                // otherwise we perform a crossover
                else {
                    int first = select();
                    int second = select();

                    boolean[][] offspring = crossover(first, second);
                    // copies the offspring to the new population
                    newPopulation[current++] = offspring[0];
                    newPopulation[current++] = offspring[1];
                }
            }

            population = newPopulation;

            //----------------------------------------------------------//
            // evaluates the new population                             //
            //----------------------------------------------------------//
            evaluate(trainingData);
        }

        // prints the value of the best individual
        int best = 0;

        for (int i = 1; i < POPULATION_SIZE; i++) {
            if (fitness[best] < fitness[i]) {
                best = i;
            }
        }

        return population[best];
    }

    /**
     * Returns the index of the selected parent using a roulette wheel.
     *
     * @return the index of the selected parent using a roulette wheel.
     */
    private int select() {
        // prepares for roulette wheel selection
        double[] roulette = new double[POPULATION_SIZE];
        double total = 0;

        for (int i = 0; i < POPULATION_SIZE; i++) {
            total += fitness[i];
        }

        double cumulative = 0.0;

        for (int i = 0; i < POPULATION_SIZE; i++) {
            roulette[i] = cumulative + (fitness[i] / total);
            cumulative = roulette[i];
        }

        roulette[POPULATION_SIZE - 1] = 1.0;

        int parent = -1;
        double probability = random.nextDouble();

        //selects a parent individual
        for (int i = 0; i < POPULATION_SIZE; i++) {
            if (probability <= roulette[i]) {
                parent = i;
                break;
            }
        }

        return parent;
    }

    /**
     * [Task 1] Initialises the population.
     */
    private void initialise() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = new boolean[BITS];

            for (int j = 0; j < BITS; j++) {
                population[i][j] = random.nextBoolean();
            }
        }
    }

    /**
     * [Task 2] Calculates the fitness of each individual.
     */
    private void evaluate(ArrayList<boolean[]> trainingData) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            boolean[] individual = population[i];

            int truePositives = 0;
            int falsePositives = 0;

            // calculate how many true positives and false positives
            for (boolean[] instance : trainingData) {
                if (covers(individual, instance)) {
                    // use target() to get the target value of the instance
                    if (target(individual) == target(instance)) {
                        truePositives++;
                    } else {
                        falsePositives++;
                    }
                }
            }

            // calculate the precision (fitness)
            if (truePositives + falsePositives == 0) fitness[i] = 0;
            else fitness[i] = (double) truePositives / (truePositives + falsePositives);
        }
    }

    /**
     * Point mutation operator.
     *
     * @param parent index of the parent individual from the population.
     */
    private boolean[] mutation(int parent) {
        boolean[] offspring = new boolean[BITS];
        int point = random.nextInt(BITS);

        for (int i = 0; i < BITS; i++) {
            if (i == point) {
                offspring[i] = random.nextBoolean();
            } else {
                offspring[i] = population[parent][i];
            }
        }

        return offspring;
    }

    /**
     * One-point crossover operator. Note that the crossover generates two offsprings,
     * so both current and current+1 position in the new population must be filled.
     *
     * @param first  index of the first parent individual from the population.
     * @param second index of the second parent individual from the population.
     */
    private boolean[][] crossover(int first, int second) {
        boolean[][] offspring = new boolean[2][BITS];
        int point = random.nextInt(BITS);

        for (int i = 0; i < BITS; i++) {
            if (i == point) {
                int k = first;
                first = second;
                second = k;
            }

            offspring[0][i] = population[first][i];
            offspring[1][i] = population[second][i];

        }

        return offspring;
    }

    // -----------------------------------------------------------------//
    private boolean covers(boolean[] individual, boolean[] instance) {
        return Dataset.covers(individual, instance);
    }

    private boolean target(boolean[] encoding) {
        return Dataset.target(encoding);
    }
}