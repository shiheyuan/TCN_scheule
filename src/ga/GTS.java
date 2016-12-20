package ga;

import static org.jenetics.engine.EvolutionResult.toBestPhenotype;
import static org.jenetics.engine.limit.bySteadyFitness;

import java.util.stream.Stream;

import org.jenetics.BitChromosome;
import org.jenetics.BitGene;
import org.jenetics.Mutator;
import org.jenetics.Phenotype;
import org.jenetics.RouletteWheelSelector;
import org.jenetics.SinglePointCrossover;
import org.jenetics.TournamentSelector;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionStatistics;

public class GTS {
	public static void GTS_step() {
		// 15个物品，箱子大小kssize
		final int nitems = 15;
		final double kssize = nitems * 100.0 / 3.0;

		final FF ff = new FF(Stream.generate(Item::random).limit(nitems).toArray(Item[]::new), (int) kssize);

		// Configure and build the evolution engine.
		final Engine<BitGene, Double> engine = Engine.builder(ff, BitChromosome.of(nitems, 0.5)).populationSize(500)
				.survivorsSelector(new TournamentSelector<>(5)).offspringSelector(new RouletteWheelSelector<>())
				.alterers(new Mutator<>(0.115), new SinglePointCrossover<>(0.16)).build();

		// Create evolution statistics consumer.
		final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

		final Phenotype<BitGene, Double> best = engine.stream()
				// Truncate the evolution stream after 7 "steady"
				// generations.
				.limit(bySteadyFitness(7))
				// The evolution will stop after maximal 100
				// generations.
				.limit(100)
				// Update the evaluation statistics after
				// each generation
				.peek(statistics)
				// Collect (reduce) the evolution stream to
				// its best phenotype.
				.collect(toBestPhenotype());

		System.out.println(statistics);
		System.out.println(best);

	}
}
