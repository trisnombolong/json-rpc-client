package org.codebistro.jsonrpc;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.metaparadigm.jsonrpc.test.Test.Waggle;
import com.metaparadigm.jsonrpc.test.Test.Wiggle;

import junit.framework.TestCase;

public class TestClient extends TestCase {
	Client client;
	HttpState state;
	// Note: make sure JSON-RPC server tests are running at this URL
	String rootURL= "http://localhost:8080/jsonrpc";
	
	protected void setUp() throws Exception {
		setupServerTestEnvironment(rootURL + "/test.jsp");
		client= new Client(rootURL + "/JSON-RPC");
		// Pass the the session set up earlier
		client.setState(state);
	}
	
	/**
	 * JSON-RPC tests need this setup to operate propely.
	 * This call invokes registerObject("test", ...) from the  JSP
	 */
	void setupServerTestEnvironment(String url) throws HttpException, IOException {
		HttpClient client= new HttpClient();
		state= new HttpState();
		client.setState(state);
		GetMethod method= new GetMethod(url);
		int status= client.executeMethod(method);
		if (status!=HttpStatus.SC_OK)
			throw new RuntimeException("Setup did not succeed. Make sure the JSON-RPC-Java test application is running on " 
					+ rootURL);
	}
	
	public void testBadClient() {
		Client badClient= new Client("http://non-existing-server:99");
		try {
			Test badTest= badClient.openProxy("test", Test.class);
			badTest.voidFunction();
			fail();
		} catch(ClientError err) {
			// Cool, we got error!
		}
	}
	
	public void testClientBasic() {
		Test test= client.openProxy("test", Test.class);
		test.voidFunction();
		assertEquals("hello", test.echo("hello"));
		assertEquals(1234, test.echo(1234));
		int[] ints= { 1, 2, 3 };
		assertTrue(Arrays.equals(ints, test.echo(ints)));
		String[] strs= { "foo", "bar", "baz" };
		assertTrue(Arrays.equals(strs, test.echo(strs)));
		Wiggle wiggle= new Wiggle();
		assertEquals(wiggle.toString(), test.echo(wiggle).toString());
		Waggle waggle= new Waggle();
		assertEquals(waggle.toString(), test.echo(waggle).toString());
		assertEquals('?', test.echoChar('?'));
		Integer into= new Integer(1234567890);
		assertEquals(into, test.echoIntegerObject(into));
		Long longo= new Long(1099511627776L);
		assertEquals(longo, test.echoLongObject(longo));
		Float floato= new Float(3.3F);
		assertEquals(floato, test.echoFloatObject(floato));
		Double doublo= new Double(3.1415926F);
		assertEquals(doublo, test.echoDoubleObject(doublo));
	}

}
