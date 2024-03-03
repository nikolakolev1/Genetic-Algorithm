package Enums;

import Main.Individual;
import Problems.FTTx;

import static Main.GA.BITS;
import static Main.GA.MUTATION_PROBABILITY;

public enum MUTATION {
    Uniform_Bool {
        @Override
        public void mutation(Individual[] offspringPopulation) {
            for (Individual individual : offspringPopulation) {
                mutateUniform(individual.individualB);
            }
        }
    },

    SinglePoint_Bool {
        @Override
        public void mutation(Individual[] offspringPopulation) {
            for (Individual individual : offspringPopulation) {
                if (Math.random() < MUTATION_PROBABILITY) {
                    mutationSinglePoint(individual.individualB);
                }
            }
        }
    },

    Exchange_Tsp {
        @Override
        public void mutation(Individual[] offspringPopulation) {
            for (Individual individual : offspringPopulation) {
                if (Math.random() < MUTATION_PROBABILITY) {
                    mutateExchange(individual.individualI);
                }
            }
        }
    },

    Inversion_Tsp {
        @Override
        public void mutation(Individual[] offspringPopulation) {
            for (Individual individual : offspringPopulation) {
                if (Math.random() < MUTATION_PROBABILITY) {
                    mutateInversion(individual.individualI);
                }
            }
        }
    },

    Arithmetic_FTTx {
        @Override
        public void mutation(Individual[] offspringPopulation) {
            for (Individual individual : offspringPopulation) {
                if (Math.random() < MUTATION_PROBABILITY) {
                    mutateArithmeticFTTx(individual.individualI);
                }
            }
        }
    };


    // ------------------------------------- Methods -------------------------------------
    public void mutation(Individual[] offspringPopulation) {
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

    // (for Problems.TSP)
    private static void mutateExchange(int[] individualI) {
        // choose two random bits
        int bit1 = (int) (Math.random() * BITS);
        int bit2 = (int) (Math.random() * BITS);

        // swap them
        int temp = individualI[bit1];
        individualI[bit1] = individualI[bit2];
        individualI[bit2] = temp;
    }

    // (for Problems.TSP)
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

    private static void mutateArithmeticFTTx(int[] individualI) {
        double randOperation = Math.random();
        int randBitIndex = (int) (Math.random() * BITS);
        int randBit = individualI[randBitIndex];
        int randNewPeriod = (int) (Math.random() * FTTx.maxRolloutPeriod);

        double addition = 0.2;
        double subtraction = 0.4;
        double multiplication = 0.6;
        double division = 0.8;

        if (randOperation < addition) {
            if (randBit < FTTx.maxRolloutPeriod) randBit++;
            else randBit--;
        } else if (randOperation < subtraction) {
            if (randBit > 0) randBit--;
            else randBit++;
        } else if (randOperation < multiplication) {
            if (randBit == 0) randBit = 1;
            else if ((randBit * 2) <= FTTx.maxRolloutPeriod) randBit *= 2;
            else if (randBit != FTTx.maxRolloutPeriod) randBit += 1;
            else randBit = 0;
        } else if (randOperation < division) {
            if (randBit == 0) randBit = FTTx.maxRolloutPeriod;
            else if (randBit > 0) randBit /= 2;
            else randBit = randNewPeriod;
        } else { // random
            randBit = randNewPeriod;
        }

        individualI[randBitIndex] = randBit;
    }
}