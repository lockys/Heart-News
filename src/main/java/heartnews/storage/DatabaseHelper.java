package heartnews.storage;

import heartnews.model.News;
import heartnews.model.Word;
import heartnews.service.PropertyManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

	private static DatabaseHelper instance;
	private static Connection conn;

	public static DatabaseHelper getInstance() {
		if (instance == null) {
			instance = new DatabaseHelper();
		}
		return instance;
	}

	private DatabaseHelper() {

	}

	public Connection connect() throws SQLException,
			ClassNotFoundException {

		if (conn == null) {
			String fileName = "jdbc_key.properties";
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
					PropertyManager.get(fileName, "HOST"),
					PropertyManager.get(fileName, "USERNAME"),
					PropertyManager.get(fileName, "PASSWORD"));
		}
		return conn;
	}

	public List<Word> getWordsInRange(long start, long end)
			throws ClassNotFoundException, SQLException {

		conn = connect();

		PreparedStatement pstmt = conn
				.prepareStatement("SELECT word, COUNT(*) AS count FROM words WHERE timestamp >= ? AND timestamp <= ? GROUP BY word");

		pstmt.setLong(1, start);
		pstmt.setLong(2, end);
		ResultSet rs = pstmt.executeQuery();
		List<Word> words = new ArrayList<Word>();
		while (rs.next()) {
			words.add(new Word(rs.getString(rs.findColumn("word")), rs
					.getInt(rs.findColumn("count"))));
		}

		conn.close();
		pstmt.close();

		return words;
	}

	public int getWordCountInRangeByWord(String word, long start, long end)
			throws ClassNotFoundException, SQLException {
		conn = connect();
		PreparedStatement pstmt = conn
				.prepareStatement("SELECT COUNT(*) AS count FROM words WHERE timestamp >= ? AND timestamp <= ? AND word = ?");

		pstmt.setLong(1, start);
		pstmt.setLong(2, end);
		pstmt.setString(3, word);
		ResultSet rs = pstmt.executeQuery();
		int count = 0;
		if (rs.next()) {
			count = rs.getInt(rs.findColumn("count"));
		}

		conn.close();
		pstmt.close();

		return count;
	}

	public List<Integer> getNewsCountInRangeForAllWord(long start, long end)
			throws SQLException, ClassNotFoundException {
		conn = connect();
		PreparedStatement pstmt = conn
				.prepareStatement("SELECT COUNT(*) AS count FROM (SELECT word, fromNewsId FROM words WHERE timestamp >= ? AND timestamp <= ? GROUP BY word, fromNewsId) T GROUP BY word");

		pstmt.setLong(1, start);
		pstmt.setLong(2, end);
		ResultSet rs = pstmt.executeQuery();
		List<Integer> countList = new ArrayList<Integer>();
		while (rs.next()) {
			countList.add(rs.getInt(rs.findColumn("count")));
		}

		conn.close();
		pstmt.close();

		return countList;
	}

	public int getNewsCountInRangeByWord(String word, long start, long end)
			throws SQLException, ClassNotFoundException {
		conn = connect();
		PreparedStatement pstmt = conn
				.prepareStatement("SELECT DISTINCT COUNT(*) AS count FROM words WHERE timestamp >= ? AND timestamp <= ? AND word = ?");

		pstmt.setLong(1, start);
		pstmt.setLong(2, end);
		pstmt.setString(3, word);
		ResultSet rs = pstmt.executeQuery();
		int count = 0;
		if (rs.next()) {
			count = rs.getInt(rs.findColumn("count"));
		}

		conn.close();
		pstmt.close();

		return count;
	}

	public int getNewsCountInRange(long start, long end)
			throws ClassNotFoundException, SQLException {
		conn = connect();
		PreparedStatement pstmt = conn
				.prepareStatement("SELECT DISTINCT COUNT(newsId) AS count FROM news WHERE timestamp >= ? AND timestamp <= ?");

		pstmt.setLong(1, start);
		pstmt.setLong(2, end);
		ResultSet rs = pstmt.executeQuery();
		int count = 0;
		if (rs.next()) {
			count = rs.getInt(rs.findColumn("count"));
		}

		conn.close();
		pstmt.close();

		return count;
	}

	public List<News> getNewsContainingMostWordInRangeByWord(String word,
	                                                         long start, long end) throws ClassNotFoundException, SQLException {
		conn = connect();
		PreparedStatement pstmt = conn
				.prepareStatement("SELECT * FROM news JOIN (SELECT fromNewsId, COUNT(*) AS count FROM words WHERE word = ? AND timestamp >= ? AND timestamp <= ? GROUP BY fromNewsId ORDER BY count DESC) T ON news.newsId = T.fromNewsId ORDER BY count DESC");

		pstmt.setString(1, word);
		pstmt.setLong(2, start);
		pstmt.setLong(3, end);
		ResultSet rs = pstmt.executeQuery();
		List<News> newsList = new ArrayList<News>();
		while (rs.next()) {
			long id = rs.getLong(rs.findColumn("newsId"));
			String title = rs.getString(rs.findColumn("title"));
			String url = rs.getString(rs.findColumn("url"));
			String source = rs.getString(rs.findColumn("source"));
			long timestamp = rs.getLong(rs.findColumn("timestamp"));
			newsList.add(new News(id, title, source, url, timestamp));
		}

		conn.close();
		pstmt.close();

		return newsList;
	}

	public List<Word> getWordsInSameNewsInRangeByWord(String word, long start,
	                                                  long end) throws ClassNotFoundException, SQLException {
		conn = connect();
		PreparedStatement pstmt = conn
				.prepareStatement("SELECT word, count(*) AS count FROM words WHERE fromNewsId IN (SELECT DISTINCT fromNewsId FROM words WHERE word = ? AND `timestamp` >= ? AND `timestamp` <= ?) AND word <> ? GROUP BY word ORDER BY count DESC");

		pstmt.setString(1, word);
		pstmt.setLong(2, start);
		pstmt.setLong(3, end);
		pstmt.setString(4, word);
		ResultSet rs = pstmt.executeQuery();
		List<Word> words = new ArrayList<Word>();
		while (rs.next()) {
			words.add(new Word(rs.getString(rs.findColumn("word")), rs
					.getInt(rs.findColumn("count"))));
		}

		conn.close();
		pstmt.close();

		return words;
	}
}
