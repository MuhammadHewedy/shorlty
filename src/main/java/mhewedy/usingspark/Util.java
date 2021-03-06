package mhewedy.usingspark;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import mhewedy.usingspark.data.Base64Ops;
import mhewedy.usingspark.data.Data;
import spark.Request;
import spark.Response;

import com.myapps.iplookup.util.IpInfo;
import com.myapps.iplookup.util.IpLookupHelper;

public class Util {
	private Util() {
	}
	
	public static enum Source{
		WEB, API
	}

	public static String createCookie(Request request, Response response) {
		String cookie = request.cookie(Constants.COOKIE_NAME);
		if (cookie == null) {
			cookie = UUID.randomUUID().toString();
			response.cookie(Constants.COOKIE_NAME, cookie, Integer.MAX_VALUE);
		}
		return cookie;
	}

	public static String qualifyShortUrl(Request request, String shortUrlToken) {
		return request.scheme() + "://" + request.host() + "/" + shortUrlToken;
	}

	public static String shortenUrl(Request request, Response response,
			List<Data> dataList, Source source) {
		String url = request.queryParams("url");
		boolean isCookieEnabled = "true".equalsIgnoreCase(request.queryParams("isCookieEnabled"));

		if (url != null && !url.isEmpty()) {
			String shortUrl = Base64Ops.increment();
			Data.saveURL(dataList, shortUrl, url, request.ip(),
					Util.createCookie(request, response), source, isCookieEnabled);
			shortUrl = Util.qualifyShortUrl(request, shortUrl);
			return shortUrl;
		}
		return "";
	}

	public static String ipToCountry(String ip) {
		System.out.println("ipToCountry: " + ip);
		IpInfo ipInfo = IpLookupHelper.getIpInfo(ip);
		String errorMsg = ipInfo.getErrorMsg();

		if (errorMsg == null) {
			return toProperCase(ipInfo.getCountry()) + " "
					+ toProperCase(ipInfo.getCity()) + " "
					+ toProperCase(ipInfo.getRegion());
		} else {
			System.out.println("Error: " + errorMsg);
			return "";
		}
	}

	public static String toProperCase(String s) {
		if (s != null && !s.isEmpty() && s.length() > 1) {
			return s.substring(0, 1).toUpperCase()
					+ s.substring(1).toLowerCase();
		}
		return "";
	}

	public static String getIpAddress(Request request) {
		HttpServletRequest servletRequest = request.raw();
		// http://en.wikipedia.org/wiki/X-Forwarded-For
		String xForwardForHeader = servletRequest.getHeader("X-Forwarded-For");
		if (xForwardForHeader != null && !xForwardForHeader.isEmpty()) {
			String[] ips = xForwardForHeader.split(",");
			return ips[0].trim();
		} else {
			return request.ip();
		}
	}
}
