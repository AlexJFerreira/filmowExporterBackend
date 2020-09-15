package filmow.exporter.util;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.stereotype.Component;

@Component
public class ApplicationUtils {
	
	public static URL getFormatedUrl(String url, String... parameters) throws MalformedURLException {
		return new URL(format(url, parameters));
	}

}
