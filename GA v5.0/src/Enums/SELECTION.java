package Enums;

import Main.Individual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static Main.GA.*;

public enum SELECTION {
    Roulette {
        @Override
        public Individual[] select() throws Exception {
            prejudice = new ArrayList<>();

            double cumulative = 0.0;
            double totalFitness = totalFitness();

            for (Individual individual : population) {
                cumulative += fitness(individual) / totalFitness;
                prejudice.add(cumulative);
            }

            return null; // must return something
        }

        @Override
        public Individual getParent(Individual[] parentPopulation) throws Exception {
            double rand = Math.random();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                if (rand < prejudice.get(i)) {
                    return parentPopulation[i];
                }
            }

            throw new Exception("Couldn't find a parent");
        }
    },

    Tournament {
        @Override
        public Individual[] select() {
            ArrayList<Individual> selectedIndividuals = new ArrayList<>();

            // create a shuffled array of indexes
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

            return selectedIndividuals.toArray(new Individual[0]);
        }

        @Override
        public Individual getParent(Individual[] parentPopulation) throws Exception {
            int randIndex = new Random().nextInt(parentPopulation.length);

            if (!everyoneWasParent) {
                if (!parents.contains(randIndex)) {
                    parents.add(randIndex);
                    return parentPopulation[randIndex];
                }

                int closestIndexUp = closestIndexUp(randIndex, parentPopulation.length);
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
            }

            return parentPopulation[randIndex];
        }
    };

    public Individual[] select() throws Exception {
        throw new Exception("Not implemented");
    }

    public Individual getParent(Individual[] parentPopulation) throws Exception {
        throw new Exception("Not implemented");
    }

    // using the fitness[] array
    protected static Individual findBestIndividual(int from, int to) {
        int bestInd_index = from;

        for (int i = from + 1; i < to; i++) {
            bestInd_index = compareIndividuals(bestInd_index, i);
        }

        return population[bestInd_index];
    }

    // using the fitness[] array
    protected static Individual findBestIndividual(int[] indexes) {
        int bestInd_index = indexes[0];

        for (int i = 1; i < indexes.length; i++) {
            bestInd_index = compareIndividuals(bestInd_index, indexes[i]);
        }

        return population[bestInd_index];
    }

    // using the fitness[] array
    protected static Individual findBestIndividual() {
        return findBestIndividual(0, POPULATION_SIZE);
    }

    public static int compareIndividuals(int ind1_index, int ind2_index) {
        if (minOrMax == MIN_MAX.Min) {
            return fitness[ind1_index] < fitness[ind2_index] ? ind1_index : ind2_index;
        } else {
            return fitness[ind1_index] > fitness[ind2_index] ? ind1_index : ind2_index;
        }
    }

    private static int closestIndexUp(int index, int upperBound) {
        while (index < upperBound) {
            if (parents.contains(index)) index++;
            else return index;
        }

        return -1; // if the index is out of bounds
    }

    private static int closestIndexDown(int index) {
        while (index >= 0) {
            if (parents.contains(index)) index--;
            else return index;
        }

        return -1; // if the index is out of bounds
    }
}