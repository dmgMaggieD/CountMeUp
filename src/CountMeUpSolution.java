import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.runner.JUnitCore;

public class CountMeUpSolution {
	static final int VOTE_CAPACITY = 10000000;
	static final int CANDIDATE_CAPACITY = 5;
	static final int USER_CAPACITY = 5000000;

	static final List<Vote> VOTES_INPUT = new ArrayList<Vote>();
	static final Map<String, Double> CANDIDATES_INPUT = new HashMap<String, Double>();
	static int[][] VOTES_INPUT_ARRAY = new int[VOTE_CAPACITY][2];
	static double[] CANDIDATES_INPUT_ARRAY = new double[CANDIDATE_CAPACITY];
	static int correctInvalidVoteCount = 0;

	final static int[] userVoteCountArray = new int[USER_CAPACITY];
	final static int[] allCandidateVoteCountArray = new int[CANDIDATE_CAPACITY];
	final static int[] validCandidateVoteCountArray = new int[CANDIDATE_CAPACITY];
	

	static final Map<String, Integer> userVoteCount = new HashMap<String, Integer>();
	static final Map<String, Integer> candidateVoteCount = new HashMap<String, Integer>();

	static final Map<String, Integer> userVoteCountCapacity = new HashMap<String, Integer>(VOTE_CAPACITY * 2);
	static int invalidVoteCount = 0;

	static final int[] candidateVoteNum = new int[CANDIDATE_CAPACITY];
	static final Map<String, Integer> candidateIdMap = new HashMap<String, Integer>();

	static AtomicInteger atomInvalidVoteCount = new AtomicInteger(0);
	static final ConcurrentHashMap<String, AtomicInteger> concurrentUserVoteCount = new ConcurrentHashMap<String, AtomicInteger>(
			VOTE_CAPACITY * 2);
	static final ConcurrentHashMap<String, AtomicInteger> concurrentCandidateVoteCount = new ConcurrentHashMap<String, AtomicInteger>();

	static AtomicInteger atomUserId = new AtomicInteger(0);
	static final int[][] votesInNum = new int[VOTE_CAPACITY + 10][2];
	static final ConcurrentHashMap<String, Integer> concurrentUserIdMap = new ConcurrentHashMap<String, Integer>(
			VOTE_CAPACITY * 2);
	
	public static void main(String[] args) throws Exception {
		JUnitCore.main("CountMeUpSolutionTest");
	}
	
	public static void generateData() {
		invalidVoteCount = 0;
		for (int i = 0; i < VOTE_CAPACITY; i++) {
			int userId = (int) (Math.random() * USER_CAPACITY);
			int candidateId = (int) (Math.random() * CANDIDATE_CAPACITY);

			VOTES_INPUT_ARRAY[i][0] = userId;
			VOTES_INPUT_ARRAY[i][1] = candidateId;

			userVoteCountArray[userId]++;
			allCandidateVoteCountArray[candidateId]++;
			if (userVoteCountArray[userId] > 3) {
				correctInvalidVoteCount++;
			} else {
				validCandidateVoteCountArray[candidateId]++;
			}

			Vote vote = new Vote("User-" + userId, "Candidate-" + candidateId);
			VOTES_INPUT.add(vote);
		}

		for (int i = 0; i < CANDIDATE_CAPACITY; i++) {
			double candidatePercentage = allCandidateVoteCountArray[i] * 100.0 / VOTE_CAPACITY;
			CANDIDATES_INPUT.put("Candidate-" + i, candidatePercentage);
			CANDIDATES_INPUT_ARRAY[i] = candidatePercentage;
		}

		System.out.println("Correct: ");
		System.out.println("invalidVoteCount: " + correctInvalidVoteCount);
		for (int i = 0; i < CANDIDATE_CAPACITY; i++) {
			System.out.print("Candidate-" + i + ": " + validCandidateVoteCountArray[i] + "; ");
		}
		System.out.println();
		System.out.println();
	}
	
	
	public static Map<String, Integer> countMeUp_1_Simple(List<Vote> votes, Map<String, Double> candidatePercentage) {
		initCandidateVoteCount(candidatePercentage);

		ExecutorService service = Executors.newFixedThreadPool(1);
		CountMeUpSolution countMeUp = new CountMeUpSolution();
		service.execute(countMeUp.new Count_Simple());
		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return candidateVoteCount;
	}

	public static int[] countMeUp_2_Ideal(int[][] votes, double[] candidates) {
		int[] candidateVoteCount = new int[candidates.length];

		for (int i = 0; i < candidates.length; i++) {
			double percentage = candidates[i];
			candidateVoteCount[i] = (int) (VOTE_CAPACITY * percentage / 100);
		}

		ExecutorService service = Executors.newFixedThreadPool(1);
		CountMeUpSolution countMeUp = new CountMeUpSolution();
		service.execute(countMeUp.new Count_Ideal(votes, candidateVoteCount));
		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return candidateVoteCount;
	}

	public static Map<String, Integer> countMeUp_3_GivenCapacity(List<Vote> votes,
			Map<String, Double> candidatePercentage) {
		initCandidateVoteCount(candidatePercentage);

		ExecutorService service = Executors.newFixedThreadPool(1);
		CountMeUpSolution countMeUp = new CountMeUpSolution();
		service.execute(countMeUp.new Count_Capacity());
		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return candidateVoteCount;
	}

	public static Map<String, Integer> countMeUp_4_CandidateArray(List<Vote> votes,
			Map<String, Double> candidatePercentage) {
		initCandidateVoteCountToArray(candidatePercentage);

		ExecutorService service = Executors.newFixedThreadPool(1);
		CountMeUpSolution countMeUp = new CountMeUpSolution();
		service.execute(countMeUp.new Count_CandidateArray());
		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

		}

		for (Map.Entry<String, Integer> entry : candidateIdMap.entrySet()) {
			candidateVoteCount.put(entry.getKey(), candidateVoteNum[entry.getValue()]);
		}

		return candidateVoteCount;
	}

	public static Map<String, AtomicInteger> countMeUp_5_Parallel4(List<Vote> votes,
			Map<String, Double> candidatePercentage) {
		initConcurrentCandidateVoteCount(candidatePercentage);

		int threadNum = 8;
		int period = VOTE_CAPACITY / threadNum;
		ExecutorService service = Executors.newFixedThreadPool(threadNum);
		CountMeUpSolution countMeUp = new CountMeUpSolution();
		for (int i = 0; i < threadNum; i++) {
			service.execute(countMeUp.new Count_Parallel(i * period, (i + 1) * period));
		}
		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return concurrentCandidateVoteCount;
	}

	public static Map<String, AtomicInteger> countMeUp_6_Parallel8(List<Vote> votes,
			Map<String, Double> candidatePercentage) {
		initConcurrentCandidateVoteCount(candidatePercentage);

		int threadNum = 8;
		int period = VOTE_CAPACITY / threadNum;
		ExecutorService service = Executors.newFixedThreadPool(threadNum);
		CountMeUpSolution countMeUp = new CountMeUpSolution();
		for (int i = 0; i < threadNum; i++) {
			service.execute(countMeUp.new Count_Parallel(i * period, (i + 1) * period));
		}
		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return concurrentCandidateVoteCount;
	}

	public static Map<String, Integer> countMeUp_7_ParallelMapToArray(List<Vote> votes,
			Map<String, Double> candidatePercentage) {
		initCandidateVoteCountToArray(candidatePercentage);

		int threadNum = 8;
		int period = VOTE_CAPACITY / threadNum;
		ExecutorService service = Executors.newFixedThreadPool(threadNum);
		CountMeUpSolution countMeUp = new CountMeUpSolution();
		for (int i = 0; i < threadNum; i++) {
			service.execute(countMeUp.new MapToArray_Parallel(i * period, (i + 1) * period));
		}
		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

		}

		int[] userVoteCount = new int[VOTE_CAPACITY + 10];
		for (int i = 0; i < VOTES_INPUT.size(); i++) {
			userVoteCount[votesInNum[i][0]]++;
			if (userVoteCount[votesInNum[i][0]] > 3) {
				candidateVoteNum[votesInNum[i][1]]--;
				invalidVoteCount++;
			}
		}

		return getMapResultFromArray(candidatePercentage);
	}

	public static void initCandidateVoteCount(Map<String, Double> candidatePercentage) {
		for (Map.Entry<String, Double> entry : candidatePercentage.entrySet()) {
			double percentage = entry.getValue();
			candidateVoteCount.put(entry.getKey(), (int) (VOTE_CAPACITY * percentage / 100));
		}
	}

	public static void initCandidateVoteCountToArray(Map<String, Double> candidatePercentage) {
		int currentCandidateId = 0;
		for (Map.Entry<String, Double> entry : candidatePercentage.entrySet()) {
			double percentage = entry.getValue();
			candidateIdMap.put(entry.getKey(), currentCandidateId);
			candidateVoteNum[currentCandidateId] = (int) (VOTE_CAPACITY * percentage / 100);
			currentCandidateId++;
		}
	}

	public static void initConcurrentCandidateVoteCount(Map<String, Double> candidatePercentage) {
		for (Map.Entry<String, Double> entry : candidatePercentage.entrySet()) {
			double percentage = entry.getValue();
			int voteCount = (int) (VOTE_CAPACITY * percentage / 100);
			concurrentCandidateVoteCount.put(entry.getKey(), new AtomicInteger(voteCount));
		}
	}

	public static Map<String, Integer> getMapResultFromArray(Map<String, Double> candidatePercentage) {
		Map<String, Integer> candidateVoteCount = new HashMap<String, Integer>();
		for (Map.Entry<String, Double> entry : candidatePercentage.entrySet()) {
			String candidateName = entry.getKey();
			candidateVoteCount.put(candidateName, candidateVoteNum[candidateIdMap.get(candidateName)]);
		}
		return candidateVoteCount;
	}

	class Count_Simple implements Runnable {
		public void run() {
			for (int i = 0; i < VOTES_INPUT.size(); i++) {
				Vote vote = VOTES_INPUT.get(i);
				String userName = vote.getUser();
				String candidateName = vote.getCandidate();
				if (userVoteCount.containsKey(userName)) {
					int voteNum = userVoteCount.get(userName);
					if (voteNum >= 3) {
						candidateVoteCount.put(candidateName, candidateVoteCount.get(candidateName) - 1);
						invalidVoteCount++;
					}
					userVoteCount.put(userName, voteNum + 1);
				} else {
					userVoteCount.put(userName, 1);
				}
			}
		}
	}

	class Count_Ideal implements Runnable {
		private int[][] votes;
		private int[] userVoteCount;
		private int[] candidateVoteCount;

		Count_Ideal(int[][] votes, int[] candidateVoteCount) {
			this.votes = votes;
			this.candidateVoteCount = candidateVoteCount;
			this.userVoteCount = new int[VOTE_CAPACITY];
		}

		public void run() {
			for (int i = 0; i < VOTE_CAPACITY; i++) {
				userVoteCount[votes[i][0]]++;
				if (userVoteCount[votes[i][0]] > 3) {
					candidateVoteCount[votes[i][1]]--;
					invalidVoteCount++;
				}
			}
		}
	}

	class Count_Capacity implements Runnable {
		public void run() {
			for (int i = 0; i < VOTES_INPUT.size(); i++) {
				Vote vote = VOTES_INPUT.get(i);
				String userName = vote.getUser();
				String candidateName = vote.getCandidate();
				if (userVoteCountCapacity.containsKey(userName)) {
					int voteNum = userVoteCountCapacity.get(userName);
					if (voteNum >= 3) {
						candidateVoteCount.put(candidateName, candidateVoteCount.get(candidateName) - 1);
						invalidVoteCount++;
					}
					userVoteCountCapacity.put(userName, voteNum + 1);
				} else {
					userVoteCountCapacity.put(userName, 1);
				}
			}
		}
	}

	class Count_CandidateArray implements Runnable {
		public void run() {
			for (int i = 0; i < VOTES_INPUT.size(); i++) {
				Vote vote = VOTES_INPUT.get(i);
				String userName = vote.getUser();
				String candidateName = vote.getCandidate();
				if (userVoteCount.containsKey(userName)) {
					int voteNum = userVoteCount.get(userName);
					int candidateId = candidateIdMap.get(candidateName);
					if (voteNum >= 3) {
						candidateVoteNum[candidateId]--;
						invalidVoteCount++;
					}
					userVoteCount.put(userName, voteNum + 1);
				} else {
					userVoteCount.put(userName, 1);
				}
			}
		}
	}

	class Count_Parallel implements Runnable {
		private int start;
		private int end;

		Count_Parallel(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public void run() {
			for (int i = start; i < end; i++) {
				Vote vote = VOTES_INPUT.get(i);
				String userName = vote.getUser();
				String candidateName = vote.getCandidate();

				AtomicInteger userVote = concurrentUserVoteCount.putIfAbsent(userName, new AtomicInteger(1));
				if (userVote != null) {
					int voteNum = userVote.incrementAndGet();
					if (voteNum > 3) {
						concurrentCandidateVoteCount.get(candidateName).decrementAndGet();
						atomInvalidVoteCount.incrementAndGet();
					}
				}
			}
		}
	}

	class MapToArray_Parallel implements Runnable {
		private int start;
		private int end;

		MapToArray_Parallel(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public void run() {
			for (int i = start; i < end; i++) {
				Vote vote = VOTES_INPUT.get(i);
				String userName = vote.getUser();
				String candidateName = vote.getCandidate();

				int candidateId = candidateIdMap.get(candidateName);
				int userId = 0;
				if (concurrentUserIdMap.containsKey(userName)) {
					userId = concurrentUserIdMap.get(userName);
				} else {
					userId = atomUserId.incrementAndGet();
					Integer newUserId = concurrentUserIdMap.putIfAbsent(userName, userId);
					if (newUserId != null) {
						userId = newUserId.intValue();
					}
				}
				votesInNum[i][0] = userId;
				votesInNum[i][1] = candidateId;
			}
		}
	}
}