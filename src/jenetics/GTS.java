/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmst枚tter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmst枚tter (franz.wilhelmstoetter@gmx.at)
 */
package jenetics;

import static org.jenetics.engine.EvolutionResult.toBestPhenotype;
import static org.jenetics.engine.limit.bySteadyFitness;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jenetics.Genotype;
import org.jenetics.IntegerChromosome;
import org.jenetics.IntegerGene;
import org.jenetics.Mutator;
import org.jenetics.Phenotype;
import org.jenetics.RouletteWheelSelector;
import org.jenetics.SinglePointCrossover;
import org.jenetics.TournamentSelector;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.engine.EvolutionStatistics;

import requirement.DataSet;
import requirement.Dataflow;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz
 *         Wilhelmst枚tter</a>
 * @since 1.0
 * @version 3.5
 */
final class Fitness implements Function<Genotype<IntegerGene>, Integer> {
	private final DataSet dataSet;

	public Fitness(DataSet dataSet, int maxNodeNum) {
		this.dataSet = dataSet;
	}

	@Override
	public Integer apply(Genotype<IntegerGene> integerChromosome) {
		List<Dataflow> dataflows = dataSet.dataflows;
		int hyper = dataSet.hyper;
		int unit = dataSet.unit;
		int slot = hyper / unit;
		// [数据流][时间片]
		Boolean[][] coexist = new Boolean[dataflows.size()][slot];
		for (int i = 0; i < integerChromosome.length(); i++) {
			int phase = integerChromosome.get(i, 0).intValue();
			Dataflow dataflow = dataflows.get(i);
			List<Boolean> exist = dataflow.getExist(phase, hyper, unit);
			exist.toArray(coexist[i]);
		}

		// for (Boolean[] bs : coexist) {
		// System.out.println(Arrays.asList(bs));
		// }
		int beReserve = 0;
		int overlap = 0;

		// 对每一时刻
		for (int i = 0; i < coexist[0].length; i++) {
			// 当前时刻的共存数据流序列
			List<Dataflow> coFlowInSlot = new ArrayList<>();
			// 对每条数据流
			for (int j = 0; j < coexist.length; j++) {
				if (coexist[j][i] != null) {
					coFlowInSlot.add(dataflows.get(j));
				}
			}
			if (coFlowInSlot.isEmpty()) {
				beReserve++;
			}
			// 关键fitness function
			overlap += topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		int score = beReserve - overlap;
		return score;
	}

}

// The fitness function.

public class GTS {
	public static void main(String[] args) {
		// 数据流数量
		final int flowNum = 50;
		// 最大数据流周期
		final int minPeriod = 5;
		final int maxPeriod = 10;
		// 拓扑结构
		List<Integer> topoConfig = new ArrayList<>();
		topoConfig.add(0, 8);
		topoConfig.add(1, 24);
		int maxNodeNum = topoConfig.get(0) * topoConfig.get(1);
		DataSet dataSet = new DataSet(flowNum, minPeriod, maxPeriod, topoConfig);

		// 染色体初始化,随机生成符合时间约束的发出时间
		List<Integer> maxPhaseList = new ArrayList<>();
		final IntegerGene[] genes = new IntegerGene[flowNum];

		// 用integerGene or genoType
		for (int i = 0; i < genes.length; i++) {
			int maxPhase = dataSet.dataflows.get(i).maxLaunch;
			genes[i] = IntegerGene.of(0, maxPhase);
			maxPhaseList.add(maxPhase);
		}
		// genoType
		final List<IntegerChromosome> lic = maxPhaseList.stream().map((integer) -> IntegerChromosome.of(0, integer, 1))
				.collect(Collectors.toList());
		final Genotype<IntegerGene> gt = Genotype.of(lic);

		// IntegerChromosome
		// IntegerChromosome integerChromosome = IntegerChromosome.of(genes);

		final Fitness ff = new Fitness(dataSet, maxNodeNum);

		final Engine<IntegerGene, Integer> engine = Engine.builder(ff, gt).populationSize(500)
				.survivorsSelector(new TournamentSelector<>(3)).offspringSelector(new RouletteWheelSelector<>())
				.alterers(new Mutator<>(0.5), new SinglePointCrossover<>(0.6)).build();
		// Create evolution statistics consumer.
		final Consumer<? super EvolutionResult<IntegerGene, Integer>> statistics = EvolutionStatistics.ofNumber();

		final Phenotype<IntegerGene, Integer> best = engine.stream()
				// Truncate the evolution stream after 7 "steady"
				// generations.
				.limit(bySteadyFitness(10))
				// The evolution will stop after maximal 100
				// generations.
				.limit(100)
				// Update the evaluation statistics after
				// each generation
				.peek(statistics)
				// Collect (reduce) the evolution stream to
				// its best phenotype.
				.collect(toBestPhenotype());
		Genotype<IntegerGene> res = best.getGenotype();

		System.out.println(statistics);
		// 检查结果
		System.out.println("grain:\t" + dataSet.getFrain());
		System.out.println("slot:\t" + dataSet.hyper);
		System.out.println("be:\t" + beResev(dataSet, res));
		System.out.println("vio:\t" + overlap(dataSet, res));
	}

	public static int overlap(DataSet dataSet, Genotype<IntegerGene> integerChromosome) {
		List<Dataflow> dataflows = dataSet.dataflows;
		int hyper = dataSet.hyper;
		int unit = dataSet.unit;
		int slot = hyper / unit;
		// [数据流][时间片]
		Boolean[][] coexist = new Boolean[dataflows.size()][slot];
		for (int i = 0; i < integerChromosome.length(); i++) {
			int phase = integerChromosome.get(i, 0).intValue();
			Dataflow dataflow = dataflows.get(i);
			List<Boolean> exist = dataflow.getExist(phase, hyper, unit);
			exist.toArray(coexist[i]);
		}

		int overlap = 0;

		// 对每一时刻
		for (int i = 0; i < coexist[0].length; i++) {
			// 当前时刻的共存数据流序列
			List<Dataflow> coFlowInSlot = new ArrayList<>();
			// 对每条数据流
			for (int j = 0; j < coexist.length; j++) {
				if (coexist[j][i] != null) {
					coFlowInSlot.add(dataflows.get(j));
				}
			}
			if (coFlowInSlot.isEmpty()) {
			}
			// 关键fitness function
			overlap += topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		return overlap;
	}

	public static int beResev(DataSet dataSet, Genotype<IntegerGene> integerChromosome) {
		List<Dataflow> dataflows = dataSet.dataflows;
		int hyper = dataSet.hyper;
		int unit = dataSet.unit;
		int slot = hyper / unit;
		// [数据流][时间片]
		Boolean[][] coexist = new Boolean[dataflows.size()][slot];
		for (int i = 0; i < integerChromosome.length(); i++) {
			int phase = integerChromosome.get(i, 0).intValue();
			Dataflow dataflow = dataflows.get(i);
			List<Boolean> exist = dataflow.getExist(phase, hyper, unit);
			exist.toArray(coexist[i]);
		}

		// for (Boolean[] bs : coexist) {
		// System.out.println(Arrays.asList(bs));
		// }
		int beReserve = 0;
		// 对每一时刻
		for (int i = 0; i < coexist[0].length; i++) {
			// 当前时刻的共存数据流序列
			List<Dataflow> coFlowInSlot = new ArrayList<>();
			// 对每条数据流
			for (int j = 0; j < coexist.length; j++) {
				if (coexist[j][i] != null) {
					coFlowInSlot.add(dataflows.get(j));
				}
			}
			if (coFlowInSlot.isEmpty()) {
				beReserve++;
			}
			topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		return beReserve;
	}
}
