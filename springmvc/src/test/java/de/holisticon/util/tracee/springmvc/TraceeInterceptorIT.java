package de.holisticon.util.tracee.springmvc;

import de.holisticon.util.tracee.Tracee;
import de.holisticon.util.tracee.TraceeConstants;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Sven Bunge, Holisticon AG
 */
public class TraceeInterceptorIT {

	private static final int JETTY_PORT = 4205;

	private Server server;
	private static final String ENDPOINT_URL = "http://localhost:4205/testGet";

	@Before
	public void startJetty() throws Exception {
		Tracee.getBackend().clear();
		
		server = new Server(JETTY_PORT);
		ServletContextHandler context = new ServletContextHandler(null, "/", ServletContextHandler.NO_SECURITY);
		final DispatcherServlet dispatcherServlet = new DispatcherServlet();
		dispatcherServlet.setContextConfigLocation("classpath:/spring-itest-configuration.xml");
		context.addServlet(new ServletHolder(dispatcherServlet), "/");
		server.setHandler(context);
		server.start();
	}

	@After
	public void stopJetty() throws Exception {
		if (server != null) {
			server.stop();
		}
		Tracee.getBackend().clear();
	}
	
	@Test
	public void test() throws IOException {
		final Header traceeResponseHeader = get("{ \"inClient\":\"yes\" }").getFirstHeader(TraceeConstants.HTTP_HEADER_NAME);

		assertThat(traceeResponseHeader, notNullValue());
		assertThat(traceeResponseHeader.getValue(), containsString("\"inInterceptor\":\"yes\""));
		assertThat(traceeResponseHeader.getValue(), containsString("\"inClient\":\"yes\""));
	}

	private HttpResponse get(String traceeHeaderValue) throws IOException {
		final HttpClient client = new DefaultHttpClient();
		final HttpGet httpGet = new HttpGet(ENDPOINT_URL);
		if (traceeHeaderValue != null) {
			httpGet.setHeader(TraceeConstants.HTTP_HEADER_NAME, traceeHeaderValue);
		}
		return client.execute(httpGet);
	}

	@Controller
	public static class SillyController {
		
		@RequestMapping("/*")
		@ResponseStatus(HttpStatus.NO_CONTENT)
		public void handleGet(HttpServletRequest request) {
			if (request.getHeader(TraceeConstants.HTTP_HEADER_NAME) == null) {
				throw new AssertionError("No expected Header " + TraceeConstants.HTTP_HEADER_NAME + " in request set");
			}
			Tracee.getBackend().put("inInterceptor", "yes");
		}
	}
}
