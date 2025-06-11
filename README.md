# Genetic Algorithm

This repository contains several versions of a Java based genetic algorithm. The project started as an experiment and gradually evolved (v1.0 to v5.0) to support multiple optimisation problems and GA configurations.

## Why it exists

The code explores how a standard genetic algorithm can be applied to different domains such as:

- **Binary string optimisation** – simple problems like "most bits on".
- **Travelling Salesman Problem (TSP)** – searching for a short route through a distance matrix.
- **FTTx planning** – techno‑economic analysis for network deployment.
- **Sequential Covering** – a rule learning example.
- **Advent of Code day 5** – experiment with data from [Advent of Code](https://adventofcode.com/).

Each new folder (`GA v1.0`, `GA v2.0`, …, `GA v5.0`) represents an iteration with additional features and refactoring.

## Running the latest version (v5.0)

You only need a JDK (tested with Java 21). The easiest way is to compile and run from the `GA v5.0` directory so the relative paths to example files work.

```bash
cd "GA v5.0"
javac $(find src -name "*.java")
java -cp src Main.GA
```

By default the program loads a preset defined in `Main/GA.java`. Presets combine GA switches such as selection method, crossover type and fitness function. See `Main/Presets.java` for the available names.

## Repository layout

```
Documents/                 \-- notes, pseudocode and seminar material
GA v1.0/ … GA v5.0/         \-- each version of the GA
  ├── src/                  \-- Java source code
  └── files/                \-- data files (TSP matrices, FTTx parameters, etc.)
```

`src` is organised into packages:

- `Main` – main GA loop, presets and helper classes.
- `Enums` – enumerations describing selection, crossover, mutation, etc.
- `Problems` – implementations of specific fitness functions for the different domains.

## About the genetic algorithm

The algorithm follows the standard cycle:

1. **Initialisation** – create a random population.
2. **Evaluation** – compute fitness for each individual.
3. **Selection, Crossover and Mutation** – produce a new generation.
4. **Elitism/Termination** – optionally keep the best individuals and stop when a condition is met.

Many operators (tournament/roulette selection, several crossover and mutation strategies) can be chosen through the presets or programmatically via `GA.setSwitches` and `GA.setSettings`.

## Example workflow

1. Adjust a preset or create a new one in `Main/Presets.java`.
2. Compile and run the code as shown above.
3. Observe statistics printed for each generation and the best individual found.

Feel free to explore the earlier versions for a more lightweight implementation or to inspect how the project evolved.
