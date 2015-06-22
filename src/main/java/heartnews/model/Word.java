package heartnews.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import heartnews.storage.DatabaseHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Word implements Comparable<Word> {

	private String word;
	private double score;
	private List<DayScore> dayScores;
	private int count;

	public Word(String word) {
		this.word = word;
	}

	public Word(String word, int count) {
		this.word = word;
		this.count = count;
	}

	@Override
	public int compareTo(Word o) {
		Word w = (Word) o;
		if (this.score > w.score)
			return 1;
		if (this.score < w.score)
			return -1;
		return 0;
	}

	public static Set<Word> getFrequentWords(long start, long end)
			throws ClassNotFoundException, SQLException {

		TreeSet<Word> frequentWords = new TreeSet<Word>(new Comparator<Word>() {
			@Override
			public int compare(Word w1, Word w2) {
				return -w1.compareTo(w2); // For descent order
			}
		});

		List<Word> words = DatabaseHelper.getInstance().getWordsInRange(start, end);
		// int n = DatabaseHelper.getNewsCountInRange(start, end);
		// List<Integer> niList = DatabaseHelper.getNewsCountForWord(start,
		// end);
		for (int i = 0; i < words.size(); i++) {
			Word w = words.get(i);
			int tf = w.count;
			// w.score = computeTfIdf(tf, n, niList.get(i));
			w.score = tf;
			if (frequentWords.size() >= 70) {
				if (w.compareTo(frequentWords.last()) < 0) {
					continue;
				} else {
					frequentWords.pollLast();
				}
			}
			frequentWords.add(w);
		}

		return frequentWords;
	}

	public List<DayScore> getWordTrend(long start, long end)
			throws ClassNotFoundException, SQLException {

		if (dayScores == null) {
			DateTimeZone timeZone = DateTimeZone.forID("Asia/Taipei");
			DateTime startDt = new DateTime(start, timeZone)
					.withTimeAtStartOfDay();
			DateTime endDt = new DateTime(end, timeZone).plusDays(1)
					.withTimeAtStartOfDay();

			dayScores = new ArrayList<DayScore>();
			for (int i = 0; startDt.plusDays(i).compareTo(endDt) < 0; i++) {
				int tf = DatabaseHelper.getInstance().getWordCountInRangeByWord(word, startDt
						.plusDays(i).getMillis(), startDt.plusDays(i + 1)
						.getMillis());
				// int n = DatabaseHelper.getNewsCountInRange(start, end);
				// int ni = DatabaseHelper.getNewsCountForWord(word, start,
				// end);
				// score = computeTfIdf(tf, n, ni);
				score = tf;
				dayScores.add(new DayScore(score, startDt.plusDays(i)
						.getMillis()));
			}
		}

		return dayScores;
	}

	public List<News> getRelatedNews(long start, long end)
			throws ClassNotFoundException, SQLException {

		return DatabaseHelper.getInstance().getNewsContainingMostWordInRangeByWord(word,
				start, end);

	}

	public Set<Word> getRelatedWord(long start, long end)
			throws ClassNotFoundException, SQLException {

		List<Word> wordCounts = DatabaseHelper.getInstance().getWordsInSameNewsInRangeByWord(
				word, start, end);

		TreeSet<Word> frequentWords = new TreeSet<Word>(new Comparator<Word>() {
			@Override
			public int compare(Word w1, Word w2) {
				return -w1.compareTo(w2); // For descent order
			}
		});
		for (Word w : wordCounts) {

			w.score = w.count;

			if (frequentWords.size() >= 5) {
				if (w.compareTo(frequentWords.last()) < 0) {
					continue;
				} else {
					frequentWords.pollLast();
				}
			}
			frequentWords.add(w);
		}

		return frequentWords;
	}

	private static double computeTfIdf(int tf, int n, int ni) {
		return (double) tf * Math.log((double) n / ni);
	}

	// private static double normalizeAndScaleTo10(TreeSet<Word> words){
	// normalized = (x-min(x))/(max(x)-min(x));
	// }
}
