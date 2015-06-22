package heartnews.model;

public class News {

	private long id;
	private String title;
	private String source;
	private String url;
	private long timestamp;

	public News(long id, String title, String source, String url, long timestamp) {
		this.title = title;
		this.source = source;
		this.url = url;
		this.timestamp = timestamp;
	}

	public long getId(){
		return id;
	}
	
	public String getTitle() {
		return title;
	}

	public String getSource() {
		return source;
	}

	public String getUrl() {
		return url;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
