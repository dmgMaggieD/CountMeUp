import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountMeUpSolutionTest {
	@BeforeClass
	public static void generateInput() {
		CountMeUpSolution.generateData();
	}

	@Before
	public void reset() {
		CountMeUpSolution.userVoteCount.clear();
		CountMeUpSolution.userVoteCountCapacity.clear();
		CountMeUpSolution.candidateVoteCount.clear();
		CountMeUpSolution.candidateIdMap.clear();

		CountMeUpSolution.invalidVoteCount = 0;
		CountMeUpSolution.atomInvalidVoteCount.set(0);
		CountMeUpSolution.atomUserId.set(0);
		CountMeUpSolution.candidateIdMap.clear();

		CountMeUpSolution.concurrentUserVoteCount.clear();
		CountMeUpSolution.concurrentCandidateVoteCount.clear();
		CountMeUpSolution.concurrentUserIdMap.clear();
		System.gc();
	}

	@Test
	public void testCountMeUp_1_Simple() {
		long start = System.currentTimeMillis();
		Map<String, Integer> result = CountMeUpSolution.countMeUp_1_Simple(CountMeUpSolution.VOTES_INPUT,
				CountMeUpSolution.CANDIDATES_INPUT);
		long end = System.currentTimeMillis();
		System.out.println("1 Simple: " + (end - start) + "ms");
		System.out.println("invalidVoteCount: " + CountMeUpSolution.invalidVoteCount);
		for (int i = 0; i < CountMeUpSolution.CANDIDATE_CAPACITY; i++) {
			String candidateName = "Candidate-" + i;
			System.out.print(candidateName + ": " + result.get(candidateName) + "; ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testCountMeUp_2_Ideal() {
		long start = System.currentTimeMillis();
		int[] result = CountMeUpSolution.countMeUp_2_Ideal(CountMeUpSolution.VOTES_INPUT_ARRAY,
				CountMeUpSolution.CANDIDATES_INPUT_ARRAY);
		long end = System.currentTimeMillis();
		System.out.println("2 Ideal: " + (end - start) + "ms");
		System.out.println("invalidVoteCount: " + CountMeUpSolution.invalidVoteCount);
		for (int i = 0; i < CountMeUpSolution.CANDIDATE_CAPACITY; i++) {
			System.out.print("Candidate-" + i + ": " + result[i] + "; ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testCountMeUp_3_GivenCapacity() {
		long start = System.currentTimeMillis();
		Map<String, Integer> result = CountMeUpSolution.countMeUp_3_GivenCapacity(CountMeUpSolution.VOTES_INPUT,
				CountMeUpSolution.CANDIDATES_INPUT);
		long end = System.currentTimeMillis();
		System.out.println("3 Given Capacity: " + (end - start) + "ms");
		System.out.println("invalidVoteCount: " + CountMeUpSolution.invalidVoteCount);
		for (int i = 0; i < CountMeUpSolution.CANDIDATE_CAPACITY; i++) {
			String candidateName = "Candidate-" + i;
			System.out.print(candidateName + ": " + result.get(candidateName) + "; ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testCountMeUp_4_CandidateArray() {
		long start = System.currentTimeMillis();
		Map<String, Integer> result = CountMeUpSolution.countMeUp_4_CandidateArray(CountMeUpSolution.VOTES_INPUT,
				CountMeUpSolution.CANDIDATES_INPUT);
		long end = System.currentTimeMillis();
		System.out.println("4 Candidate Array: " + (end - start) + "ms");
		System.out.println("invalidVoteCount: " + CountMeUpSolution.invalidVoteCount);
		for (int i = 0; i < CountMeUpSolution.CANDIDATE_CAPACITY; i++) {
			String candidateName = "Candidate-" + i;
			System.out.print(candidateName + ": " + result.get(candidateName) + "; ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testCountMeUp_5_Parallel4() {
		long start = System.currentTimeMillis();
		Map<String, AtomicInteger> result = CountMeUpSolution.countMeUp_5_Parallel4(CountMeUpSolution.VOTES_INPUT,
				CountMeUpSolution.CANDIDATES_INPUT);
		long end = System.currentTimeMillis();
		System.out.println("5 Parallel 4 Threads: " + (end - start) + "ms");
		System.out.println("invalidVoteCount: " + CountMeUpSolution.atomInvalidVoteCount.get());
		for (int i = 0; i < CountMeUpSolution.CANDIDATE_CAPACITY; i++) {
			String candidateName = "Candidate-" + i;
			System.out.print(candidateName + ": " + result.get(candidateName) + "; ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testCountMeUp_6_Parallel8() {
		long start = System.currentTimeMillis();
		Map<String, AtomicInteger> result = CountMeUpSolution.countMeUp_6_Parallel8(CountMeUpSolution.VOTES_INPUT,
				CountMeUpSolution.CANDIDATES_INPUT);
		long end = System.currentTimeMillis();
		System.out.println("6 Parallel 8 Threads: " + (end - start) + "ms");
		System.out.println("invalidVoteCount: " + CountMeUpSolution.atomInvalidVoteCount.get());
		for (int i = 0; i < CountMeUpSolution.CANDIDATE_CAPACITY; i++) {
			String candidateName = "Candidate-" + i;
			System.out.print(candidateName + ": " + result.get(candidateName) + "; ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testCountMeUp_7_ParallelMapToArray() {
		long start = System.currentTimeMillis();
		Map<String, Integer> result = CountMeUpSolution.countMeUp_7_ParallelMapToArray(CountMeUpSolution.VOTES_INPUT,
				CountMeUpSolution.CANDIDATES_INPUT);
		long end = System.currentTimeMillis();
		System.out.println("7 Parallel Map To Array: " + (end - start) + "ms");
		System.out.println("invalidVoteCount: " + CountMeUpSolution.invalidVoteCount);
		for (int i = 0; i < CountMeUpSolution.CANDIDATE_CAPACITY; i++) {
			String candidateName = "Candidate-" + i;
			System.out.print(candidateName + ": " + result.get(candidateName) + "; ");
		}
		System.out.println();
		System.out.println();
	}
}
