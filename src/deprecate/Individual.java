package deprecate;

import org.jenetics.IntegerChromosome;

public class Individual {
	public IntegerChromosome integerChromosome;

	public int val;

	public Individual(int len) {
		integerChromosome = new IntegerChromosome(0, 10, len);
	}
}
