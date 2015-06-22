package heartnews.model;

public class DayScore {

	private double score;
	private long timestamp;
	
	public DayScore(double score, long timestamp){
		this.score = score;
		this.timestamp = timestamp;
	}

	public double getScore() {
		return score;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
}
