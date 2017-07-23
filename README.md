# BBC Count Me Up (Real Time)

## Feature: Counting Votes:
  As a BBC television presenter
  I want to see the counts for candidates within a given time frame
  So that I can announce the winner of the competition
  
## Acceptance Criteria:
- Count Me Up should be accurate. So if there is a total count of 100 votes and 60% are given to candidate-1 then Count Me Up should return 60 as the count of votes for candidate-1.
- The same user can vote multiple times, up to a maximum of 3 times for the same candidate or for different ones. Count Me Up should not count a vote if the same user already exceeded the maximum alloId number of votes (that is should not count user-1 vote for candidate-5 if user-1 already voted for candidate-1, candidate-2 and candidate-3). This is the reason why candidate-5 for example received "only" 3M votes instead of 4M.
- Count Me Up should be fast. Count Me Up will be used as a close to real-time tool to constantly show the results of the competition, so it should be invoked every second or so to show progress. It follows that it should respond in less than 1 second.


## Data
- The total number of votes: 10 million, given information.
- The total number of candidates: 5, given information.
- The total number of users : 5 million, assumption. Considering the total number of votes is 10 million and the max valid votes for each user is 3, plus the normal distributed curve, I assume the total number of user to be 5 million.
- Data types for inputs:
	-- List<Vote> VOTES_INPUT : store the 10 million votes received in the ascending order of voting time. Every note has two fields(user name, candidate name who the user vote for).
	-- Map<String, Integer> CANDIDATES_INPUT : store the votes percentage of each candidates, the key is candidate name, the value is the votes percentage of the specific candidate.
- Generate mechanism for inputs:
	-- all the following inputs are generated in unit test file: generate the following inputs once before the invokes of all the unit test methods as data preparation in the @BeforeClass method.
  	-- user name : Type(String), generate user name according to the total number of users.
  	-- candidate name : Type(String), generate candidate name according to the total number of candidates.
  	-- List<Vote> : Type(Vote), generate every vote by creating a random number in the range of candidate name.
  	-- Map<String, Integer> CANDIDATES_INPUT : use generated List<Vote> to calculate the percentage for each candidate.

## Solutions

### The running results and performance of all 7 solutions.
![Screenshot 1](https://drive.google.com/open?id=0B_Mt_BxY9NsQdGVUTUJTTVBkcHc)

- Solution 1: simple version
	-- Idea:
		--- use a HashMap to map userName to votesNum for each user, use a HashMap to map candidateName to votes count for each candidate. First calculate the votes count for each candidate according to the input candidate vote percentage. Then traversing the votes list to decrement the vote count of specific candidate when find votes count from one user is larger than 3.
	-- Performance: 
		--- About 4.6s
	-- BottleNeck or points can be improved: 
		--- The bottleneck of this solution is HashMap collisions. every bucket of the two Hash Map has too many entries which makes the map.get() and map.put() very expensive, especially the HashMap which maps user name to votes count with 7million user names. 

- Solution 2: ideal version
	-- Purpose:
		--- To see what upper bound of the best performance would be like and whether the requirement is feasible.
	-- Idea:
		--- The ideal solution is dealing with arrays instead of HashMap, and the arrays store the map information of user ID to votes count and candidate ID to votes count with ID as index and votes count as value, which just equals to HashMap without collions.
	-- Performance: 
		--- About 0.2s
	-- Problems: 
		--- This is the ideal solution with limited inputs which aren't given by the question. But through the try we can see the extreme performance is 0.2s, so even we cann't get there but we can try towards there. This is also this try's significance.

- Solution 3: givenCapacity version
 	-- Idea : 
 		--- Try to improve the HashMap collision bottleneck mentioned in solution 1, the solution 3 is one try which set a proper capacity when create the HashMap to avoid the space reallocated of HashMap. The default value of capacity is 16 and value of loadfactor is 0.75, when the number entries is larger than 16 * 0.75, HashMap will be reallocated to double size and need copy operations of all entries which cost a lot.
 	-- Performance: 
 		--- About 3.2s, enhance 1.8s compared to solution 1.
 	-- BottleNeck or points can be improved:
 		--- The bottleneck of this solution is also HashMap collisions. 
 		--- Every time finding a vote which its user has voted more than 3 times, I to dealt with HashMap expensively, including get the votes count of the corresponding candidate in the vote, and then put it back with decremented value. I can try to reduce HashMap hit.
 	
- Solution 4: candidateArray version
 	-- Idea : 
 		--- Based on the "points can be improved" in solution 3, I try to reduce the hit of HashMap by dealing with an array to decrement the votes count of the specific candidate when finding an invalid vote instead of HashMap. 
 		--- To get the array with the index as the candidate count and the value is the votes count of candidates, I need to map every candidate name to candidate ID (increment sequence).
 	-- Performance: 
 		--- About 3.2s, enhance 0s compared to solution 3, 1.8s compared to solution 1.
 		--- This solution doesn't have effect compared to Solution 3. The reason is that the candidate count is small(5), and the HashMap of candidate to vote count isn't the bottleneck of the question, so the improvement is not obvious to transfer this map to array, if considering the cost of transferring, the performance can be worse.
 	-- BottleNeck or points can be improved:
 		--- HashMap collisions still cost most, instead of reduce the HashMap collisions or hit of HashMap, I can try something parallel like multi-threads.

###
- Solution 5: parallel4 version
	-- Idea : 
 		--- Based on the "points can be improved" in solution 3, I try to use multi-threads to parallel operations to reduce the response time.
 		--- I use cocurrentHashMap instead of HashMap to allow multi-threads operate the one HashMap at the same time.
 		--- I partition the votes list to 4 parts and create 4 threads, ensure there is one thread being responsible for each list part.
 		--- to keep data consistency, I use atomic methods such as putIfAbsent(), and Type AtomicInteger to replace int type to operate increment and decrement by atomic methods of incrementAnGet(), decrementAndGet().
 	-- Performance: 
 		--- About 0.65s, which is below 1s, enhance 2.6s compared to solution 4, 2.6s compared to solution 3, 4s compared to solution 1.

- Solution 6: parallel8 version
	-- Idea : 
 		--- same with Solution 4: CocurrentHashMap + partitioned votes list + multi-threads,
 		--- difference : this solution partitions the votes list into 8 parts and creates 8 threads.
 		--- this solution is to compared with Solution 4 to see how they perform with different number of threads setting. 
 	-- Performance: 
 		--- About 0.59s, which is below 1s, enhance 0.06s compared to solution 5.
 		--- when you increase the number of threads, the performance is better, but it depends on your compute performance.

**Problems of Solution 5 and Solution 6:
	-- Solution 5 and 6 is a failed try : After I finished the two Solutions 5 and 6, I test out that the result(candidates' vote count) is different from other solutions. Finally I figure the reason out. 
	-- Failed reason : The problem happens when use multi-threads to operate the votes list parallel. Because the votes list contains the important information of the votes time order, and the rule is to treat the votes that more than 3 times for one user according time as invalid. However, we ignore the time order information when we parallel operate the votes list, which definitely leads to inaccurate result. 
	-- Solutions are Failed but are not nothing: Inspired by multi-threads + cocurrentHashMap, I think I can try them in some place where order doesn't matter, so that I come up with Solution 6.
###

- Solution 7: parellelMapToArray version (Submission Version)
	-- Idea : 
 		--- Inspired by solution 4, 5, 6, and combine them together.  
 		--- To reduce HashMap hit, we instead use arrays to stores map of user ID(as array index) to votes count(as array value) and map of candidate ID(as array index) to votes count(as array value). To get these two arrays, we need to generate user ID and candidate ID and HashMap that map name to ID for both user and candidate.
 		--- I use CocurrentHashMap + partitioned votes list + multi-threads mechanism for the part of user ID, candidate ID generation and transferring name to ID to create arrays, because the traversal of list votes list in this part does't care the time order of votes. 
 	-- Performance: 
 		--- About 0.9s, which is below 1s, enhance 2.3s compared to solution 4, 2.3s compared to solution 3, 3.7s compared to solution 1.
	

## Develop Process
- TDD

## Prerequisites
* [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Eclipse](https://www.eclipse.org/downloads/)

## Running
To run the application, go to the project directory and run the following command:
* java -Xms4g -Xmx6g -jar CountMeUp.jar

In terminal:
* Generating Data for CountMeUp ... 
* 
* Correct Numbers: 
* invalidVoteCount: 1090472
* Candidate-0: 1782535; Candidate-1: 1781974; Candidate-2: 1781103; Candidate-3: 1782446; Candidate-4: 1781470; 
* 
* Start CountMeUp!
* 
* Time Cost: 1001ms
* invalidVoteCount: 1090472
* Candidate-0: 1782535; Candidate-1: 1781974; Candidate-2: 1781103; Candidate-3: 1782446; Candidate-4: 1781470; 
* 
* Finished!

## Test
To run the JUnit test with all the solutions, go to the project directory and run the following command:
* java -Xms4g -Xmx6g -jar CountMeUp.jar 

In terminal:
JUnit version 4.12
Correct: 
invalidVoteCount: 1089484
Candidate-0: 1781643; Candidate-1: 1782003; Candidate-2: 1779691; Candidate-3: 1783321; Candidate-4: 1783858; 

.1 Simple: 4367ms
invalidVoteCount: 1089484
Candidate-0: 1781643; Candidate-1: 1782003; Candidate-2: 1779691; Candidate-3: 1783321; Candidate-4: 1783858; 

.2 Ideal: 154ms
invalidVoteCount: 1089484
Candidate-0: 1781643; Candidate-1: 1782003; Candidate-2: 1779691; Candidate-3: 1783321; Candidate-4: 1783858; 

.3 Given Capacity: 3038ms
invalidVoteCount: 1089484
Candidate-0: 1781643; Candidate-1: 1782003; Candidate-2: 1779691; Candidate-3: 1783321; Candidate-4: 1783858; 

.4 Candidate Array: 3475ms
invalidVoteCount: 1089484
Candidate-0: 1781643; Candidate-1: 1782003; Candidate-2: 1779691; Candidate-3: 1783321; Candidate-4: 1783858; 

.5 Parallel 4 Threads: 662ms
invalidVoteCount: 1089484
Candidate-0: 1781430; Candidate-1: 1781768; Candidate-2: 1780498; Candidate-3: 1783135; Candidate-4: 1783685; 

.6 Parallel 8 Threads: 568ms
invalidVoteCount: 1089484
Candidate-0: 1781397; Candidate-1: 1781834; Candidate-2: 1780466; Candidate-3: 1783135; Candidate-4: 1783684; 

.7 Parallel Map To Array: 897ms
invalidVoteCount: 1089484
Candidate-0: 1781643; Candidate-1: 1782003; Candidate-2: 1779691; Candidate-3: 1783321; Candidate-4: 1783858; 


Time: 50.413

OK (7 tests)

## Other problems That I Came Across
* Problem : Some time during the process of one solution, the JVM will interrupted to GC, which will add to the cost of this Solution. 
  Solution: force JVM to GC before every test. 


