package main;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GeneticLearning {

	private final static int MAX_POOL_SIZE = 12;
	private final static int NUMBER_GENERATIONS = 50;

	/** Holds the pool of potential attributes */
	private final TreeSet<Attributes> pool;

	/** Holds a list of all the words that are in the input. */
	private final List<String> allWords;

	/** Holds the input values list */
	private final List<InputRow> inputs;

	/** The two languages to split between */
	private final String languageOne, languageTwo;

	/** Random used for chances with mutation possibilities */
	private final Random r = new Random();

	/**
	 * Creates the learning pool with a genetic algorithm.
	 * @param inputs The input rows to use as part of the measure for fitness
	 * @param languageOne The first language.
	 * @param languageTwo The second language.
	 */
	private GeneticLearning(List<InputRow> inputs, String languageOne, String languageTwo) {
		this.languageOne = languageOne;
		this.languageTwo = languageTwo;

		this.inputs = inputs;
		// pool is sorted by the fitness of the feature
		pool = new TreeSet<>((o1, o2) -> {
			double o1D = o1.fitness(inputs, languageOne, languageTwo);
			double o2D = o2.fitness(inputs, languageOne, languageTwo);
			// if tie, keep only want to remove duplicates
			if (o1D == o2D) return o1.name().compareTo(o2.name());
			return Double.compare(o1D, o2D);
		});
		allWords = new ArrayList<>(inputs.size() * 20);
		for (InputRow row : inputs) {
			allWords.addAll(Arrays.asList(row.words));
		}

		// fill in the pool with some randomly drawn attributes
		Attributes noUse = new WordAttribute("a");
		for (int i = 0; i < 20; i++) {
			pool.add(noUse.mutate(allWords));
		}

		// TODO similar process for other attribute types once they are added

	}

	/**
	 * Proceeds to the next generation, performing mutation, crossover, and then trimming
	 * the results down to the pool size, keeping the most fit.
	 */
	private void nextGeneration() {
		// mutate some random ones, proportional to their fitness
		TreeSet<Attributes> newOnes = new TreeSet<>((o1, o2) -> {
			double o1D = o1.fitness(inputs, languageOne, languageTwo);
			double o2D = o2.fitness(inputs, languageOne, languageTwo);
			// if tie, keep only want to remove duplicates
			if (o1D == o2D) return o1.name().compareTo(o2.name());
			return Double.compare(o1D, o2D);
		});

		for (Attributes i : pool) {
			double chance = i.fitness(inputs, languageOne, languageTwo);
			if (r.nextDouble() < chance) {
				// do the mutation and add it to the pool
				newOnes.add(i.mutate(allWords));
			}
		}

		// TODO add the crossover once that's implemented


		// add the new ones to the list
		pool.addAll(newOnes);

		// trim the pool to MAX_POOL_SIZE (aka natural selection in the genetic sense)
		while (pool.size() > MAX_POOL_SIZE) {
			pool.pollFirst();
		}
	}


	public static void main(String[] args) throws IOException {
		ArrayList<InputRow> inputs = InputRow.loadExamples("training.txt");
		String languageOne = "English";
		String languageTwo = "Dutch";

		List<InputRow> englishOrDutch = inputs.stream()
			.filter(i -> i.outputValue.equals(languageOne) || i.outputValue.equals(languageTwo))
			.collect(Collectors.toList());

		// iterate 100 generations
		GeneticLearning learning = new GeneticLearning(englishOrDutch, languageOne, languageTwo);
		for (int i = 0; i < NUMBER_GENERATIONS; i++) {
			learning.nextGeneration();
		}

		// right now just print out the results to see if they're working
		for (Attributes i : learning.pool) {
			System.out.println(i + ": " + i.fitness(englishOrDutch, languageOne, languageTwo));
		}
	}
}