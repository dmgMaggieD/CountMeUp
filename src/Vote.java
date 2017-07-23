public class Vote {
	private String user;
	private String candidate;

	public Vote(String user, String candidate) {
		this.user = user;
		this.candidate = candidate;
	}

	public String getUser() {
		return user;
	}

	public String getCandidate() {
		return candidate;
	}
}
