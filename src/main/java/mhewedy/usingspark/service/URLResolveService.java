package mhewedy.usingspark.service;

import java.util.List;

import mhewedy.usingspark.data.Columns;
import mhewedy.usingspark.data.Data;

import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;
import org.parse4j.callback.FindCallback;

import spark.Request;
import spark.Response;

public class URLResolveService extends Service {

	@Override
	public String doService(Request request, Response response) {
		System.out.println("GET /:shortUrl");
		String shortUrl = request.params("shortUrl");

		if (shortUrl != null && !shortUrl.isEmpty()) {

			String originalUrl = Data.getOriginalURL(new Data[] { inMemoryData, parseData }, shortUrl);

			if (originalUrl != null && !originalUrl.isEmpty()) {
				System.out.println("redirect to: " + originalUrl);

				saveUrlHitsAsync(shortUrl, request.ip());
				response.redirect(originalUrl);
			} else {
				System.err.printf("short url %s not found!\n", shortUrl);
			}

		} else {
			System.err.println("shortenUrl is null");
		}
		return "";
	}

	void saveUrlHitsAsync(String shortUrl, String ip) {

		ParseQuery<ParseObject> query = ParseQuery.getQuery(Columns.URL_MAPPING_CLASS).whereEqualTo(
				Columns.SHORT_URL_COL, shortUrl);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> list, ParseException parseException) {

				if (parseException == null && list != null) {
					ParseObject parseObject = list.get(0);

					ParseObject hitsObj = new ParseObject(Columns.HIT_DETAILS_CLASS);
					hitsObj.put(Columns.IP_COL, ip);
					hitsObj.put(Columns.URL_REL_COL, parseObject);
					hitsObj.saveInBackground();

					parseObject.increment(Columns.HIT_COUNT_COL);
					parseObject.saveInBackground();
				} else {
					System.err.println("ex?: " + parseException);
				}
			}
		});
	}
}
