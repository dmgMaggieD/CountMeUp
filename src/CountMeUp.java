import java.util.HashMap;
import java.util.Map;


public class CountMeUp {
	public static void main(String[] args) {
		final int capacity = 10000000;
		final int candidateNum = 10;
		final int userNum = 7000000;
		
		int[] candidateVoteNum = new int[candidateNum];
		int[] candidateVotePercentage = new int[candidateNum];
		int[][] votes = new int[capacity][2];
		for(int i = 0; i < capacity; i ++) {
			votes[i][0] = (int) (Math.random() * userNum);
			votes[i][1] = (int) (Math.random() * candidateNum);
			candidateVoteNum[votes[i][1]] ++;
		}
		
		for(int i = 0; i < candidateVoteNum.length; i ++) {
			candidateVotePercentage[i] = candidateVoteNum[i] * 100 / capacity;
		}
		
		// CMU
		long start = System.currentTimeMillis();
		int count = 0;
		int[] voteResult = new int[candidateNum];
		for(int i = 0; i < candidateVoteNum.length; i ++) {
			voteResult[i] = candidateVotePercentage[i] * capacity / 100;
		}
		//Map<Integer, Integer> map = new HashMap<Integer, Integer>(20000000); 
		int userIndex = 0;
		int[] userVote = new int[capacity];
		for(int i = 0; i < votes.length; i ++) {
			userIndex = votes[i][0];
			if(userVote[userIndex] >= 3) {
				count ++;
				voteResult[votes[i][1]]--;
			}
			userVote[userIndex] ++;
//			if(map.containsKey(votes[i][0])) {
//				int user = map.get(votes[i][0]);
//				if(userVote[userIndex] >= 3) {
//					count++;
//					voteResult[votes[i][1]]--;
//				}
//				userVote[userIndex]++;
//			} else {
//				map.put(votes[i][0], userIndex);
//				userVote[userIndex] = 1;
//				userIndex++;
//			}
			
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		System.out.println(count);
	}
}