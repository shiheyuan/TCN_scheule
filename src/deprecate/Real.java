package deprecate;

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static org.jenetics.engine.EvolutionResult.toBestPhenotype;
import static org.jenetics.engine.limit.bySteadyFitness;

import org.jenetics.DoubleChromosome;
import org.jenetics.Genotype;
import org.jenetics.MeanAlterer;
import org.jenetics.Mutator;
import org.jenetics.Optimize;
import org.jenetics.Phenotype;
import org.jenetics.engine.Engine;

public class Real {
	// Definition of the fitness function.
	private static Double eval(final Genotype gt) {
		final double x = (double) gt.getGene().getAllele();
		return Math.cos(0.5 + sin(x)) * Math.cos(x);
	}

	public static void main(String[] args) {
		// Create/configuring the engine via its builder.
		final Engine engine = Engine.builder(Real::eval, DoubleChromosome.of(0.0, 2.0 * PI)).populationSize(500)
				.optimize(Optimize.MINIMUM).alterers(new Mutator<>(0.03), new MeanAlterer<>(0.6)).build();

		// Execute the GA (engine).
		final Phenotype<?, ?> result = (Phenotype<?, ?>) engine.stream()
				// Truncate the evolution stream if no better individual could
				// be found after 5 consecutive generations.
				.limit(bySteadyFitness(5))
				// Terminate the evolution after maximal 100 generations.
				.limit(100).collect(toBestPhenotype());
		System.out.println(result);
	}
}
