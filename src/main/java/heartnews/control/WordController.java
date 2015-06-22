package heartnews.control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import heartnews.model.DayScore;
import heartnews.model.News;
import heartnews.model.Word;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Path("words")
public class WordController implements ContainerResponseFilter {

	@GET
	@Produces(MediaType.APPLICATION_JSON + "p")
	public Response getWords(@QueryParam("start") Long start,
	                         @QueryParam("end") Long end) throws ClassNotFoundException,
			SQLException {

		long[] params = checkParameter(start, end);
		start = params[0];
		end = params[1];

		Set<Word> words = Word.getFrequentWords(start, end);
		return Response.ok(new Gson().toJson(words)).build();
	}

	@GET
	@Path("{word}")
	@Produces(MediaType.APPLICATION_JSON + "p")
	public Response getWordFrequencies(@PathParam("word") String word,
	                                   @QueryParam("start") Long start, @QueryParam("end") Long end)
			throws ClassNotFoundException, SQLException {

		long[] params = checkParameter(start, end);
		start = params[0];
		end = params[1];

		List<DayScore> trend = new Word(word).getWordTrend(start, end);
		return Response.ok(new Gson().toJson(trend)).build();
	}

	@GET
	@Path("{word}/related")
	@Produces(MediaType.APPLICATION_JSON + "p")
	public Response getRelatedWords(@PathParam("word") String word,
	                                @QueryParam("start") Long start, @QueryParam("end") Long end)
			throws ClassNotFoundException, SQLException {

		long[] params = checkParameter(start, end);
		start = params[0];
		end = params[1];

		Set<Word> words = new Word(word).getRelatedWord(start, end);

		return Response.ok(new Gson().toJson(words)).build();
	}

	@GET
	@Path("{word}/news")
	@Produces(MediaType.APPLICATION_JSON + "p")
	public Response getRelatedNews(@PathParam("word") String word,
	                               @QueryParam("start") Long start, @QueryParam("end") Long end)
			throws ClassNotFoundException, SQLException {

		long[] params = checkParameter(start, end);
		start = params[0];
		end = params[1];

		List<News> newsList = new Word(word).getRelatedNews(start, end);
		return Response.ok(new Gson().toJson(newsList)).build();

	}

	public long[] checkParameter(Long start, Long end) {

		DateTimeZone timeZone = DateTimeZone.forID("Asia/Taipei");
		DateTime now = DateTime.now(timeZone);

		if (start == null) {
			start = now.getMillis();
		}

		if (end == null) {
			end = now.getMillis();
		}

		if (start > end)
			throw new BadRequestException();

		start = new DateTime(start, timeZone).withTimeAtStartOfDay().getMillis();
		end = new DateTime(end, timeZone).plusDays(1).withTimeAtStartOfDay().getMillis() - 1;

		return new long[]{start, end};
	}

	@Override
	public void filter(ContainerRequestContext requestContext,
	                   ContainerResponseContext responseContext) throws IOException {

		//FIXME
		responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");

	}

}
