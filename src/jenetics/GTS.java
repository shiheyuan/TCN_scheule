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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jenetics.Genotype;
import org.jenetics.IntegerChromosome;
import org.jenetics.IntegerGene;
import org.jenetics.MultiPointCrossover;
import org.jenetics.Mutator;
import org.jenetics.Phenotype;
import org.jenetics.RouletteWheelSelector;
import org.jenetics.TournamentSelector;
import org.jenetics.engine.Engine;
import org.jenetics.engine.Engine.Builder;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.engine.EvolutionStatistics;

import requirement.DataSet;
import requirement.Dataflow;
import topology.Edge;

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
		int slot = hyper;
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
			// 关键：检查给定数据流间的重复次数
			overlap += topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		// 分数给定
		int weight = 1;
		int score = beReserve / (1 + weight * overlap);
		return score;
	}

}

// The fitness function.

public class GTS {
	/**
	 * 如果信息可以非周期传输，则此情况下的最小BE通道
	 * 
	 * @param dataSet
	 * @return
	 */
	public static int minBeSlot(DataSet dataSet) {
		int hyper = dataSet.hyper;
		int sum = 0;
		List<Dataflow> list = dataSet.dataflows;
		for (Dataflow dataflow : list) {
			sum += hyper / dataflow.period * dataflow.duration;
		}
		return hyper - sum;
	}

	/**
	 * 如果信息可以非周期传输，则此情况下的最大BE通道
	 * 
	 * @param dataSet
	 * @return
	 */
	public static int maxBeSlot(DataSet dataSet) {

		List<Dataflow> list = dataSet.dataflows;
		int max = 0;

		for (Dataflow dataflow : list) {
			max = Math.max(max, dataSet.hyper / dataflow.period * dataflow.duration);

		}
		return dataSet.hyper - max;
	}

	/**
	 * 
	 * @param dataSet
	 * @param integerChromosome
	 * @return
	 */
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
			// exist.toArray(coexist[i]);
			coexist[i] = (Boolean[]) exist.toArray();
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
			// 关键fitness function
			overlap += topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		return overlap;
	}

	public static int failedFlow(DataSet dataSet, Genotype<IntegerGene> integerChromosome) {
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
			// exist.toArray(coexist[i]);
			coexist[i] = (Boolean[]) exist.toArray();
		}
		Set<Dataflow> failedFlow = new HashSet<>();
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
			// 看看冲突？
			Map<Edge, Integer> edgeCounter = new HashMap<>();
			// 初始化edgeCounter
			for (Dataflow dataflow : coFlowInSlot) {
				for (Edge edge : dataflow.edges) {
					edgeCounter.put(edge, 0);
				}
			}
			// 数据流计数
			for (Dataflow dataflow : coFlowInSlot) {
				for (Edge edge : dataflow.edges) {
					if (edgeCounter.containsKey(edge) && edgeCounter.get(edge) == 0) {
						edgeCounter.put(edge, 1);
					} else {
						failedFlow.add(dataflow);
					}
				}
			}
		}
		return failedFlow.size();
	}

	/**
	 * 给定调度下的BE预留时隙个数
	 * 
	 * @param dataSet
	 * @param integerChromosome
	 * @return
	 */
	public static int beResev(DataSet dataSet, Genotype<IntegerGene> integerChromosome) {
		List<Dataflow> dataflows = dataSet.dataflows;
		int hyper = dataSet.hyper;
		int unit = dataSet.unit;
		// [数据流][时间片]，即每一条数据流的宏周期内存在情况
		Boolean[][] coexist = new Boolean[dataflows.size()][hyper];
		// for (int i = 0; i < coexist.length; i++) {
		// for (int j = 0; j < coexist[i].length; j++) {
		// coexist[i][j] = false;
		// }
		// }

		// 对于第i条数据流
		for (int i = 0; i < integerChromosome.length(); i++) {
			// 该flow的发出时刻
			int phase = integerChromosome.get(i, 0).intValue();

			// 根据时刻求出宏周期内该数据流的所有存在时刻
			Dataflow dataflow = dataflows.get(i);
			List<Boolean> exist = dataflow.getExist(phase, hyper, unit);

			// 加入共存流的二维数组
			coexist[i] = (Boolean[]) exist.toArray();

			// for (int j = 0; j < exist.size(); j++) {
			// coexist[i][j] = exist.get(j);
			// }
		}
		// 求某一时刻某一段链路的数据流争用情况
		int beReserve = 0;
		// 对i时刻
		for (int i = 0; i < hyper; i++) {
			// 当前时刻的共存数据流序列
			List<Dataflow> coFlowInSlot = new ArrayList<>();
			// 对j数据流
			for (int j = 0; j < coexist.length; j++) {
				// 如果j数据流在i时刻存在
				if (coexist[j][i] != null) {
					coFlowInSlot.add(dataflows.get(j));
				}
			}
			// 如果此时所有链路均无数据流传输，则可以供BE传输
			if (coFlowInSlot.isEmpty()) {
				beReserve++;
			}
			// topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		return beReserve;
	}

	public static void main(String[] args) {
		// 数据流数量
		final int flowNum = 100;
		// 最大数据流周期
		final int minPeriod = 10;
		final int maxPeriod = 14;
		// 拓扑结构
		List<Integer> topoConfig = new ArrayList<>();
		topoConfig.add(0, 8);
		topoConfig.add(1, 8);
		int maxNodeNum = topoConfig.get(0) * topoConfig.get(1);

		// 生成数据流
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
		// final Engine<IntegerGene, Integer> engine = Engine.builder(ff,
		// gt).populationSize(500)
		// .survivorsSelector(new TournamentSelector<>(3)).offspringSelector(new
		// RouletteWheelSelector<>())
		// .alterers(new Mutator<>(1), new MultiPointCrossover<>(1)).build();
		// final Engine<IntegerGene, Integer> engine = Engine.builder(ff,
		// gt).populationSize(500)
		// .survivorsSelector(new TournamentSelector<>(3)).offspringSelector(new
		// RouletteWheelSelector<>())
		// .alterers(new Mutator<>(1), new SinglePointCrossover<>(1)).build();
		final Builder<IntegerGene, Integer> builder = Engine.builder(ff, gt).populationSize(500)
				.survivorsSelector(new TournamentSelector<>(3)).offspringSelector(new RouletteWheelSelector<>());
		double crossover_percent = 0.1;
		// for (int i = 0; i < flowNum * crossover_percent; i++) {
		// builder.alterers(new Mutator<>(1), new MeanAlterer<>());
		builder.alterers(new Mutator<>(1), new MultiPointCrossover<>(1, (int) Math.ceil(flowNum * crossover_percent)));
		// }
		final Engine<IntegerGene, Integer> engine = builder.build();
		// Create evolution statistics consumer.
		final Consumer<? super EvolutionResult<IntegerGene, Integer>> statistics = EvolutionStatistics.ofNumber();

		final Phenotype<IntegerGene, Integer> best = engine.stream()
				// Truncate the evolution stream after 7 "steady"
				// generations.
				.limit(bySteadyFitness(5))
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
		System.out.println(res);

		// 检查结果
		System.out.println("flowNum:\t" + flowNum);
		System.out.println("topology:\t" + topoConfig);
		System.out.println("temporal grain:\t" + dataSet.getTemporalGrain());
		System.out.println("hyper:\t" + dataSet.hyper);
		System.out.println("percentage of temporal:\t" + 1.0 * dataSet.getTemporalGrain() / dataSet.hyper);
		// System.out.println("grain:\t" + dataSet.getGrain());
		int all = (topoConfig.get(0) - 1 + topoConfig.get(1) * topoConfig.get(0)) * dataSet.hyper;
		// System.out.println("all:\t" + all);
		System.out.println("percentage of temporal and spatial:\t" + dataSet.getGrain() * 1.0 / all);
		// System.out.println("unit:\t" + dataSet.unit);
		// 供BE帧传输的最小时隙与最大时隙，最小就是每个时刻只有一帧传输，最大（不可行）就是
		System.out.println("min slot for BE:\t" + minBeSlot(dataSet));
		System.out.println("max slot for BE:\t" + maxBeSlot(dataSet));
		System.out
				.println("BE reverse:\t" + beResev(dataSet, res) + '\t' + 1.0 * beResev(dataSet, res) / dataSet.hyper);
		System.out.println(
				"BE reverse improvement:\t" + 1.0 * (beResev(dataSet, res) - minBeSlot(dataSet)) / minBeSlot(dataSet));
		System.out.println("vio:\t" + overlap(dataSet, res));
		System.out.println(failedFlow(dataSet, res));
	}

}
