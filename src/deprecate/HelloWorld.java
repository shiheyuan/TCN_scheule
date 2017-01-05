package deprecate;

import org.jenetics.BitChromosome;
import org.jenetics.BitGene;
import org.jenetics.Genotype;
import org.jenetics.IntegerChromosome;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.util.Factory;

public class HelloWorld {
	// 2.) Definition of the fitness function.
	private static Integer eval(Genotype<BitGene> gt) {
		return ((BitChromosome) gt.getChromosome()).bitCount();
	}

	public static void main(String[] args) {
		IntegerChromosome integerChromosome = new IntegerChromosome(0, 10, 10);
		System.out.println(integerChromosome);

		// 1.) Define the genotype (factory) suitable
		// for the problem.
		Factory<Genotype<BitGene>> gtf = Genotype.of(BitChromosome.of(10, 0.5));

		// 3.) Create the execution environment.
		Engine<BitGene, Integer> engine = Engine.builder(HelloWorld::eval, gtf).build();

		// 4.) Start the execution (evolution) and
		// collect the result.
		Genotype<BitGene> result = engine.stream().limit(100).collect(EvolutionResult.toBestGenotype());

		System.out.println("Hello World:\n" + result);
	}
}
