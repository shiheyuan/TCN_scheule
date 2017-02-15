/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
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
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
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
 *         Wilhelmstötter</a>
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
		// [������][ʱ��Ƭ]
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

		// ��ÿһʱ��
		for (int i = 0; i < coexist[0].length; i++) {
			// ��ǰʱ�̵Ĺ�������������
			List<Dataflow> coFlowInSlot = new ArrayList<>();
			// ��ÿ��������
			for (int j = 0; j < coexist.length; j++) {
				if (coexist[j][i] != null) {
					coFlowInSlot.add(dataflows.get(j));
				}
			}
			if (coFlowInSlot.isEmpty()) {
				beReserve++;
			}
			// �ؼ�������������������ظ�����
			overlap += topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		// ��������
		int weight = 1;
		int score = beReserve / (1 + weight * overlap);
		return score;
	}

}

// The fitness function.

public class GTS {
	/**
	 * �����Ϣ���Է����ڴ��䣬�������µ���СBEͨ��
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
	 * �����Ϣ���Է����ڴ��䣬�������µ����BEͨ��
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
		// [������][ʱ��Ƭ]
		Boolean[][] coexist = new Boolean[dataflows.size()][slot];
		for (int i = 0; i < integerChromosome.length(); i++) {
			int phase = integerChromosome.get(i, 0).intValue();
			Dataflow dataflow = dataflows.get(i);
			List<Boolean> exist = dataflow.getExist(phase, hyper, unit);
			// exist.toArray(coexist[i]);
			coexist[i] = (Boolean[]) exist.toArray();
		}

		int overlap = 0;

		// ��ÿһʱ��
		for (int i = 0; i < coexist[0].length; i++) {
			// ��ǰʱ�̵Ĺ�������������
			List<Dataflow> coFlowInSlot = new ArrayList<>();
			// ��ÿ��������
			for (int j = 0; j < coexist.length; j++) {
				if (coexist[j][i] != null) {
					coFlowInSlot.add(dataflows.get(j));
				}
			}
			// �ؼ�fitness function
			overlap += topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		return overlap;
	}

	public static int failedFlow(DataSet dataSet, Genotype<IntegerGene> integerChromosome) {
		List<Dataflow> dataflows = dataSet.dataflows;
		int hyper = dataSet.hyper;
		int unit = dataSet.unit;
		int slot = hyper / unit;
		// [������][ʱ��Ƭ]
		Boolean[][] coexist = new Boolean[dataflows.size()][slot];
		for (int i = 0; i < integerChromosome.length(); i++) {
			int phase = integerChromosome.get(i, 0).intValue();
			Dataflow dataflow = dataflows.get(i);
			List<Boolean> exist = dataflow.getExist(phase, hyper, unit);
			// exist.toArray(coexist[i]);
			coexist[i] = (Boolean[]) exist.toArray();
		}
		Set<Dataflow> failedFlow = new HashSet<>();
		// ��ÿһʱ��
		for (int i = 0; i < coexist[0].length; i++) {
			// ��ǰʱ�̵Ĺ�������������
			List<Dataflow> coFlowInSlot = new ArrayList<>();
			// ��ÿ��������
			for (int j = 0; j < coexist.length; j++) {
				if (coexist[j][i] != null) {
					coFlowInSlot.add(dataflows.get(j));
				}
			}
			// ������ͻ��
			Map<Edge, Integer> edgeCounter = new HashMap<>();
			// ��ʼ��edgeCounter
			for (Dataflow dataflow : coFlowInSlot) {
				for (Edge edge : dataflow.edges) {
					edgeCounter.put(edge, 0);
				}
			}
			// ����������
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
	 * ���������µ�BEԤ��ʱ϶����
	 * 
	 * @param dataSet
	 * @param integerChromosome
	 * @return
	 */
	public static int beResev(DataSet dataSet, Genotype<IntegerGene> integerChromosome) {
		List<Dataflow> dataflows = dataSet.dataflows;
		int hyper = dataSet.hyper;
		int unit = dataSet.unit;
		// [������][ʱ��Ƭ]����ÿһ���������ĺ������ڴ������
		Boolean[][] coexist = new Boolean[dataflows.size()][hyper];
		// for (int i = 0; i < coexist.length; i++) {
		// for (int j = 0; j < coexist[i].length; j++) {
		// coexist[i][j] = false;
		// }
		// }

		// ���ڵ�i��������
		for (int i = 0; i < integerChromosome.length(); i++) {
			// ��flow�ķ���ʱ��
			int phase = integerChromosome.get(i, 0).intValue();

			// ����ʱ������������ڸ������������д���ʱ��
			Dataflow dataflow = dataflows.get(i);
			List<Boolean> exist = dataflow.getExist(phase, hyper, unit);

			// ���빲�����Ķ�ά����
			coexist[i] = (Boolean[]) exist.toArray();

			// for (int j = 0; j < exist.size(); j++) {
			// coexist[i][j] = exist.get(j);
			// }
		}
		// ��ĳһʱ��ĳһ����·���������������
		int beReserve = 0;
		// ��iʱ��
		for (int i = 0; i < hyper; i++) {
			// ��ǰʱ�̵Ĺ�������������
			List<Dataflow> coFlowInSlot = new ArrayList<>();
			// ��j������
			for (int j = 0; j < coexist.length; j++) {
				// ���j��������iʱ�̴���
				if (coexist[j][i] != null) {
					coFlowInSlot.add(dataflows.get(j));
				}
			}
			// �����ʱ������·�������������䣬����Թ�BE����
			if (coFlowInSlot.isEmpty()) {
				beReserve++;
			}
			// topology.FlowUtil.checkOverlap(coFlowInSlot);
		}
		return beReserve;
	}

	public static void main(String[] args) {
		// ����������
		final int flowNum = 100;
		// �������������
		final int minPeriod = 10;
		final int maxPeriod = 14;
		// ���˽ṹ
		List<Integer> topoConfig = new ArrayList<>();
		topoConfig.add(0, 8);
		topoConfig.add(1, 8);
		int maxNodeNum = topoConfig.get(0) * topoConfig.get(1);

		// ����������
		DataSet dataSet = new DataSet(flowNum, minPeriod, maxPeriod, topoConfig);

		// Ⱦɫ���ʼ��,������ɷ���ʱ��Լ���ķ���ʱ��
		List<Integer> maxPhaseList = new ArrayList<>();
		final IntegerGene[] genes = new IntegerGene[flowNum];

		// ��integerGene or genoType
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

		// �����
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
		// ��BE֡�������Сʱ϶�����ʱ϶����С����ÿ��ʱ��ֻ��һ֡���䣬��󣨲����У�����
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
