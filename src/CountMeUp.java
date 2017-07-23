import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CountMeUp {
	static final int VOTE_CAPACITY = 10000000; // The total number of votes
	static final int CANDIDATE_CAPACITY = 5; // The total number of candidates
	static final int USER_CAPACITY = 5000000; // The total number of users (assumption)

	static final List<Vote> VOTES_INPUT = new ArrayList<Vote>(); // The input list of votes
	static final Map<String, Double> CANDIDATES_INPUT = new HashMap<String, Double>(); // Map candidate name to votes count
	
	// Store correct information which is used to check the final results
	static int correctInvalidVoteCount = 0; // The number of invalid votes 
	final static int[] userVoteCountArray = new int[USER_CAPACITY]; // Stores the map information that usreId to vote count, user ID as the index, vote count as the element
	final static int[] allCandidateVoteCountArray = new int[CANDIDATE_CAPACITY]; // Store the map information that candidateId to all vote count, candidateId as the index, vote count as the element
	final static int[] validCandidateVoteCountArray = new int[CANDIDATE_CAPACITY]; // Store the map information that candidate to valid vote count, candidateId as the index, vote count as the element

	static int invalidVoteCount = 0; // Count the total invalid vote count

	static final int[] candidateVoteNum = new int[CANDIDATE_CAPACITY]; // Store the map information that candidate to vote count candidate ID as the index, vote count as the element
	static final Map<String, Integer> candidateIdMap = new HashMap<String, Integer>(); // Map candidate name to candidate ID

	static AtomicInteger atomUserId = new AtomicInteger(0); // Used in parallel user ID generation
	static final int[][] votesInNum = new int[VOTE_CAPACITY + 10][2]; // Store vote information, every row stands for one vote, votesInNum[i][0]: user ID, votesInNum[i][1]: candidate ID
	static final ConcurrentHashMap<String, Integer> concurrentUserIdMap = new ConcurrentHashMap<String, Integer>(
			VOTE_CAPACITY * 2); // Map user name to user ID

	public static void main(String[] args) {
		generateData();
		
		// Start time
		System.out.println("Start CountMeUp!");
		System.out.println();
		long start = System.currentTimeMillis();
		
		Map<String, Integer> result = countMeUp_7_ParallelMapToArray(VOTES_INPUT, CANDIDATES_INPUT);
		
		// End time
		long end = System.currentTimeMillis();
		
		// Print the result
		System.out.println("Time Cost: " + (end - start) + "ms");
		System.out.println("invalidVoteCount: " + invalidVoteCount);
		for (int i = 0; i < CANDIDATE_CAPACITY; i++) {
			String candidateName = "Candidate-" + i;
			System.out.print(candidateName + ": " + result.get(candidateName) + "; ");
		}
		System.out.println();
		
		System.out.println();
		System.out.println("Finished!");
	}
	
	// Generate the data as input
	public static void generateData() {
		System.out.println("Generating Data for CountMeUp ... ");
		System.out.println();
		
		invalidVoteCount = 0;
		for (int i = 0; i < VOTE_CAPACITY; i++) {
			int userId = (int) (Math.random() * USER_CAPACITY);
			int candidateId = (int) (Math.random() * CANDIDATE_CAPACITY);

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
		}

		System.gc();
		System.out.println("Correct Numbers: ");
		System.out.println("invalidVoteCount: " + correctInvalidVoteCount);
		for (int i = 0; i < CANDIDATE_CAPACITY; i++) {
			System.out.print("Candidate-" + i + ": " + validCandidateVoteCountArray[i] + "; ");
		}
		System.out.println();
		System.out.println();
	}
	
	// CountMeUp solution using CocurrentHashMap + multi-Threads, and dealing with arrays transfered from HashMap
	public static Map<String, Integer> countMeUp_7_ParallelMapToArray(List<Vote> votes,
			Map<String, Double> candidatePercentage) {
		initCandidateVoteCountToArray(candidatePercentage);

		int threadNum = 10;
		int period = VOTE_CAPACITY / threadNum;
		ExecutorService service = Executors.newFixedThreadPool(threadNum);
		CountMeUp countMeUp = new CountMeUp();
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
	
	// Calculate all vote count (including invalid votes) from input of candidate votes percentage.
	public static Map<String, Integer> getMapResultFromArray(Map<String, Double> candidatePercentage) {
		Map<String, Integer> candidateVoteCount = new HashMap<String, Integer>();
		for (Map.Entry<String, Double> entry : candidatePercentage.entrySet()) {
			String candidateName = entry.getKey();
			candidateVoteCount.put(candidateName, candidateVoteNum[candidateIdMap.get(candidateName)]);
		}
		return candidateVoteCount;
	}

	// Transfer the int array of candidate vote count to a map so it could be returned.
	public static void initCandidateVoteCountToArray(Map<String, Double> candidatePercentage) {
		int currentCandidateId = 0;
		for (Map.Entry<String, Double> entry : candidatePercentage.entrySet()) {
			double percentage = entry.getValue();
			candidateIdMap.put(entry.getKey(), currentCandidateId);
			candidateVoteNum[currentCandidateId] = (int) (VOTE_CAPACITY * percentage / 100);
			currentCandidateId++;
		}
	}

	// Thread used to map Vote object to a int pair contains user id and candidate id.
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
