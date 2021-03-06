package io.tracee.binding.cxf.interceptor;

import io.tracee.SimpleTraceeBackend;
import io.tracee.TraceeBackend;
import io.tracee.TraceeConstants;
import io.tracee.transport.jaxb.TpicMap;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.MessageImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IncomingRequestSoapMessageTest {

	private static final TraceeBackend backend = SimpleTraceeBackend.createNonLoggingAllPermittingBackend();

	private AbstractTraceeInInterceptor inInterceptor;

	private final SoapMessage soapMessage = new SoapMessage(new MessageImpl());

	@Before
	public void onSetup() throws Exception {
		backend.clear();
		inInterceptor = new TraceeRequestInInterceptor(backend);
	}

	@Test
	public void handleSoapMessageWithoutSoapHeaderAndGenerateRequestId() {
		inInterceptor.handleMessage(soapMessage);
		assertThat(backend.copyToMap(), hasKey(TraceeConstants.REQUEST_ID_KEY));
	}

	@Test
	public void shouldHandleMessageWithoutTraceeHeader() throws JAXBException {
		final Map<String, String> contextMap = new HashMap<String, String>();
		contextMap.put("mySoapKey", "mySoapContextValue");
		soapMessage.getHeaders().add(new Header(new QName(TraceeConstants.SOAP_HEADER_NAMESPACE, "SOME_OTHER"), contextMap, new JAXBDataBinding(HashMap.class)));

		inInterceptor.handleMessage(soapMessage);
		assertThat(backend.copyToMap(), not(hasKey("mySoapKey")));
	}

	@Test
	public void shouldHandleMessageWithTraceeHeader() throws SOAPException, JAXBException {
		final Map<String, String> contextMap = new HashMap<String, String>();
		contextMap.put("mySoapKey", "mySoapContextValue");
		final Element element = render(contextMap);
		final Header e = new Header(TraceeConstants.SOAP_HEADER_QNAME, element);
		soapMessage.getHeaders().add(e);

		inInterceptor.handleMessage(soapMessage);
		assertThat(backend.get("mySoapKey"), is("mySoapContextValue"));
	}

	private Element render(Map<String, String> context) throws JAXBException, SOAPException {
			final MessageFactory messageFactory = MessageFactory.newInstance();
		final SOAPHeaderElement dummyContainerElement = messageFactory.createMessage().getSOAPHeader().addHeaderElement(new QName("http://test", "elem"));


		DataWriter<Node> writer = new JAXBDataBinding(TpicMap.class).createWriter(Node.class);
			writer.write(TpicMap.wrap(context), dummyContainerElement);

		return (Element)dummyContainerElement.getFirstChild();
	}
}
