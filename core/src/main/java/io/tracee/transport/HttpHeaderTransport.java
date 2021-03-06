package io.tracee.transport;

import io.tracee.Tracee;
import io.tracee.TraceeLogger;
import io.tracee.TraceeLoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class HttpHeaderTransport {

	public static final String ENCODING_CHARSET = "UTF-8";
	private final TraceeLogger logger;

	public HttpHeaderTransport() {
		this(Tracee.getBackend().getLoggerFactory());
	}

	public HttpHeaderTransport(TraceeLoggerFactory loggerFactory) {
		this.logger = loggerFactory.getLogger(HttpHeaderTransport.class);
	}

	Map<String, String> parse(String serialized) {
		final StringTokenizer pairTokenizer = new StringTokenizer(serialized.trim(), ",");
		final Map<String, String> context = new HashMap<String, String>();
		while(pairTokenizer.hasMoreTokens()) {
			final String pairStr = pairTokenizer.nextToken();
			final String[] keyValuePair = pairStr.split("=");
			if (keyValuePair.length != 2) {
				continue;
			}
			try {
				final String key = URLDecoder.decode(keyValuePair[0], ENCODING_CHARSET);
				final String value = URLDecoder.decode(keyValuePair[1], ENCODING_CHARSET);
				context.put(key, value);
			} catch (UnsupportedEncodingException e) {
				logger.error("Charset not found", e);
			}
		}

		return context;
	}

	public Map<String, String> parse(List<String> serializedElements) {
		final Map<String, String> contextMap = new HashMap<String, String>();
		for (String serializedElement : serializedElements) {
			contextMap.putAll(parse(serializedElement));
		}

		return contextMap;
	}

	public String render(Map<String, String> context) {
		final StringBuilder sb = new StringBuilder(128);
		for (Iterator<Map.Entry<String, String>> iterator = context.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<String, String> entry = iterator.next();
			try {
				final String key = URLEncoder.encode(entry.getKey().trim(), ENCODING_CHARSET);
				final String value = URLEncoder.encode(entry.getValue().trim(), ENCODING_CHARSET);
				sb.append(key).append('=').append(value);
				if (iterator.hasNext()) {
					sb.append(',');
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("Charset not found", e);
			}
		}
		return sb.toString();
	}
}
