package Enums;

import Main.Individual;
import Problems.AocDay5;
import Problems.FTTx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static Main.GA.*;

public enum INDIVIDUAL_TYPE {
    boolArray {
        @Override
        public Individual[] randomPopulation() {
            return randPopulation_Normal();
        }
    },

    intArray {
        @Override
        public Individual[] randomPopulation() {
            return randPopulation_Normal();
        }
    },

    tspIntArray {
        @Override
        public Individual[] randomPopulation() {
            return randPopulation_Tsp();
        }
    },

    fttxIntArray {
        @Override
        public Individual[] randomPopulation() {
            return randPopulation_FTTx();
        }
    },

    aocIntArray {
        @Override
        public Individual[] randomPopulation() {
            return randPopulation_AOC();
        }
    };

    public Individual[] randomPopulation() throws Exception {
        throw new Exception("Not implemented");
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

            int city = 0;
            while (city < BITS) individualI.add(city++);

            // shuffle the ArrayList
            Collections.shuffle(individualI);

            // convert the ArrayList to int[]
            int[] individual = new int[BITS];
            city = 0;
            while (city < BITS) individual[city] = individualI.get(city++);

            population[i] = new Individual(individual);
        }

        return population;
    }

    private static Individual[] randPopulation_FTTx() {
        Individual[] population = new Individual[POPULATION_SIZE];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            int[] individualI = new int[FTTx.noOfAreas];

            for (int j = 0; j < individualI.length; j++) {
                individualI[j] = (int) (Math.random() * (FTTx.maxRolloutPeriod + 1));
            }

            population[i] = new Individual(individualI);
        }

        return population;
    }

    private static Individual[] randPopulation_AOC() {
        Individual[] population = new Individual[POPULATION_SIZE];

        Random random = new Random();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            int rangeIndex = random.nextInt(AocDay5.seedRanges.size());

            long start = AocDay5.seedRanges.get(rangeIndex).seedRangeStart();
            long finish = start + AocDay5.seedRanges.get(rangeIndex).seedRangeLength();

            long individual = random.nextLong(start, finish);

            population[i] = new Individual(AocDay5.convertLongToIntArr(individual, BITS));
        }

        return population;
    }
}