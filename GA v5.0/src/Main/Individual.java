package Main;

import Enums.INDIVIDUAL_TYPE;

import java.util.Random;

/**
 * The individual class is the representation of a single individual in the population.
 */
public class Individual {
    public INDIVIDUAL_TYPE arrayType;
    public boolean[] individualB;
    public int[] individualI;
    public int length;
    public Double fitness;

    public Individual(boolean[] individual) {
        individualB = individual;
        this.length = individual.length;
        arrayType = INDIVIDUAL_TYPE.boolArray;
    }

    public Individual(int[] individual) {
        individualI = individual;
        this.length = individual.length;
        arrayType = GA.individualType;
    }

    // This constructor doesn't work for the Problems.TSP problem
    public Individual(INDIVIDUAL_TYPE arrayType, int BITS) {
        Random rand = new Random();

        this.arrayType = arrayType;
        length = BITS;
        switch (arrayType) {
            case boolArray -> {
                individualB = new boolean[BITS];
                for (int i = 0; i < BITS; i++) {
                    individualB[i] = rand.nextBoolean();
                }
            }
            case intArray -> {
                individualI = new int[BITS];
                for (int i = 0; i < BITS; i++) {
                    individualI[i] = rand.nextInt();
                }
            }
            default -> {

            }
        }
    }

    public Individual copyItself() {
        switch (arrayType) {
            case boolArray -> {
                return new Individual(individualB.clone());
            }
            case intArray, tspIntArray, fttxIntArray, aocIntArray -> {
                return new Individual(individualI.clone());
            }
            default -> throw new IllegalStateException("Unexpected value: " + arrayType);
        }
    }

    public void print() {
        arrayType.print(this);
    }

    /**
     * Calculates the similarity between this individual and another individual.
     * Similarity is the proportion of matching elements in the individuals' arrays.
     * Throws an exception if the individuals are not of the same type or have different lengths.
     *
     * @param anotherIndividual the other individual to compare with this individual
     * @return the similarity between this individual and anotherIndividual as a percentage
     * @throws IllegalArgumentException if the two individuals are not of the same type or have different lengths
     * @throws IllegalStateException if the individuals' array type is not supported
     */
    public double similarity(Individual anotherIndividual) {
        // Check if the two individuals are of the same type
        if (arrayType != anotherIndividual.arrayType) {
            throw new IllegalArgumentException("The two individuals are not of the same type");
        }

        // Check if the two individuals have the same length
        if (length != anotherIndividual.length) {
            throw new IllegalArgumentException("The two individuals have different lengths");
        }

        // If checks pass, calculate the similarity
        int count = 0;

        switch (arrayType) {
            case boolArray -> {
                for (int i = 0; i < length; i++) {
                    if (individualB[i] == anotherIndividual.individualB[i]) {
                        count++;
                    }
                }
            }

            case intArray, tspIntArray -> {
                for (int i = 0; i < length; i++) {
                    if (individualI[i] == anotherIndividual.individualI[i]) {
                        count++;
                    }
                }
            }

            default -> throw new IllegalStateException("Unexpected value: " + arrayType);
        }

        return ((double) count / length) * 100;
    }
}