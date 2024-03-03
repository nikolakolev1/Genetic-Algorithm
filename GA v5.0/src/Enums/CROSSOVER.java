package Enums;

import Main.Individual;

import java.util.ArrayList;
import java.util.Random;

import static Main.GA.*;

public enum CROSSOVER {
    SinglePoint_Simple {
        @Override
        public Individual[] crossoverIndividuals(Individual parent1, Individual parent2) {
            Individual parent1_Copy = parent1.copyItself();
            Individual parent2_Copy = parent2.copyItself();

            return crossover_SinglePoint_Simple(parent1_Copy, parent2_Copy);
        }
    },

    nPoint_Simple {
        @Override
        public Individual[] crossoverIndividuals(Individual parent1, Individual parent2) {
            Individual parent1_Copy = parent1.copyItself();
            Individual parent2_Copy = parent2.copyItself();

            return crossover_nPoint_Simple(parent1_Copy, parent2_Copy);
        }
    },

    Uniform_Simple {
        @Override
        public Individual[] crossoverIndividuals(Individual parent1, Individual parent2) {
            Individual parent1_Copy = parent1.copyItself();
            Individual parent2_Copy = parent2.copyItself();

            return crossover_Uniform_Simple(parent1_Copy, parent2_Copy);
        }
    },

    PMX_Tsp {
        @Override
        public Individual[] crossoverIndividuals(Individual parent1, Individual parent2) {
            Individual parent1_Copy = parent1.copyItself();
            Individual parent2_Copy = parent2.copyItself();

            return crossover_PartiallyMapped_Tsp(parent1_Copy, parent2_Copy);
        }
    },

    SinglePoint_FTTx {
        @Override
        public Individual[] crossoverIndividuals(Individual parent1, Individual parent2) {
            Individual parent1_Copy = parent1.copyItself();
            Individual parent2_Copy = parent2.copyItself();

            return crossover_SinglePoint_FTTx(parent1_Copy, parent2_Copy);
        }
    };

    // ------------------------------------- Methods -------------------------------------
    public Individual[] crossoverIndividuals(Individual parent1, Individual parent2) throws Exception {
        throw new Exception("Not implemented");
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
        boolean[] temp = parent1_array.clone();

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
        ArrayList<Integer> parent1GaveOffspring2 = new ArrayList<>();
        ArrayList<Integer> parent2GaveOffspring1 = new ArrayList<>();

        // crossover
        for (int i = crossoverPoint1; i < crossoverPoint2 + 1; i++) {
            offspring1[i] = parent2[i];
            offspring2[i] = parent1[i];

            parent1GaveOffspring2.add(parent1[i]);
            parent2GaveOffspring1.add(parent2[i]);
        }

        // fix duplicate genes
        handleDuplicateGenes(offspring1, parent1GaveOffspring2, parent2GaveOffspring1, crossoverPoint1, crossoverPoint2);
        handleDuplicateGenes(offspring2, parent2GaveOffspring1, parent1GaveOffspring2, crossoverPoint1, crossoverPoint2);

        return new Individual[]{new Individual(offspring1), new Individual(offspring2)};
    }

    private static void handleDuplicateGenes(int[] offspring, ArrayList<Integer> youGave, ArrayList<Integer> youReceived, int from, int to) {
        // fix duplicate genes in the beginning of the offspring DNA
        for (int i = 0; i < from; i++) {
            if (youReceived.contains(offspring[i])) {
                offspring[i] = youGave.get(youReceived.indexOf(offspring[i]));
                i = -1;
            }
        }

        // fix duplicate genes after the second crossover point
        for (int i = to + 1; i < offspring.length; i++) {
            if (youReceived.contains(offspring[i])) {
                offspring[i] = youGave.get(youReceived.indexOf(offspring[i]));
                i = to;
            }
        }
    }

    private static Individual[] crossover_SinglePoint_FTTx(Individual parent1_Copy, Individual parent2_Copy) {
        if (Math.random() < CROSSOVER_PROBABILITY) {
            int crossoverPoint = (int) (Math.random() * BITS);
            int[] temp = parent1_Copy.individualI.clone();

            System.arraycopy(parent2_Copy.individualI, crossoverPoint, parent1_Copy.individualI, crossoverPoint, BITS - crossoverPoint);
            System.arraycopy(temp, crossoverPoint, parent2_Copy.individualI, crossoverPoint, BITS - crossoverPoint);
        }

        return new Individual[]{parent1_Copy, parent2_Copy};
    }
}