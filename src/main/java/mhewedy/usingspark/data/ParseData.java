package mhewedy.usingspark.data;

import java.util.List;

import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;
import org.parse4j.callback.SaveCallback;

public class ParseData implements Data {

	@Override
	public void saveURL(String shortUrl, String originalUrl, String clientIp) {

		ParseObject parseObject = new ParseObject(Columns.URL_MAPPING_CLASS);
		parseObject.put(Columns.SHORT_URL_COL, shortUrl);
		parseObject.put(Columns.ORIG_URL_COL, originalUrl);
		parseObject.put(Columns.IP_COL, clientIp);
		parseObject.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException parseException) {
				if (parseException != null) {
					System.out.printf("error saving original %s short %s in Parse.com: %s\n", originalUrl, shortUrl,
							parseException.getMessage());
				}
			}
		});
	}

	@Override
	public String getOriginalURL(String shortUrl) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Columns.URL_MAPPING_CLASS);
		try {
			List<ParseObject> list = query.whereEqualTo(Columns.SHORT_URL_COL, shortUrl).find();
			if (list != null) {
				return (String) list.stream().map(o -> o.get(Columns.ORIG_URL_COL)).findFirst().orElse("");
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
		}
		return "";
	}

	@Override
	public int getPriority() {
		return 10;
	}
}
