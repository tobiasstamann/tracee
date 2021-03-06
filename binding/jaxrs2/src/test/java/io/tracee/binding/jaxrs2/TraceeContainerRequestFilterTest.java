package io.tracee.binding.jaxrs2;

import io.tracee.SimpleTraceeBackend;
import io.tracee.TraceeBackend;
import io.tracee.TraceeConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TraceeContainerRequestFilterTest {

    private final TraceeBackend backend = SimpleTraceeBackend.createNonLoggingAllPermittingBackend();
    private final TraceeContainerRequestFilter unit = new TraceeContainerRequestFilter(backend);
    private final ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
    private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();

    @Before
    public void setUp() {
        when(requestContext.getHeaders()).thenReturn(headers);
        backend.clear();
    }

    @Test
    public void testFilterParsesContextFromHeaderToBackend() throws IOException {
        headers.putSingle(TraceeConstants.HTTP_HEADER_NAME, "foo=bar");
        unit.filter(requestContext);
        assertThat(backend.get("foo"), equalTo("bar"));
    }

    @Test
    public void testFilterParsesExistingRequestId() throws IOException {
        headers.putSingle(TraceeConstants.HTTP_HEADER_NAME, TraceeConstants.REQUEST_ID_KEY + "=foo");
        unit.filter(requestContext);
        assertThat(backend.get(TraceeConstants.REQUEST_ID_KEY), equalTo("foo"));
    }

    @Test
    public void testFilterCreatesRequestIdIfNotInHeaders() throws IOException {
        unit.filter(requestContext);
        assertThat(backend.get(TraceeConstants.REQUEST_ID_KEY), not(isEmptyOrNullString()));
    }
}
