/*
 * JSON-RPC-Client, a Java client extension to JSON-RPC-Java
 *
 * (C) Copyright CodeBistro 2007, Sasha Ovsankin <sasha@codebistro.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codebistro.jsonrpc;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codebistro.jsonrpc.TransportRegistry.SessionFactory;
import org.json.JSONObject;
import org.json.JSONTokener;

public class HTTPSession implements Session {
	private static Log log= LogFactory.getLog(HTTPSession.class);

	protected HttpClient client;
	protected HttpState state;
	protected URI uri;
	
	public HTTPSession(URI uri) {
		this.uri= uri;
	}
	/** 
	 * An option to set state from the outside.
	 * for example, to provide existing session parameters.
	 */
	public void setState(HttpState state) {
		this.state= state;
	}
	
	public JSONObject sendAndReceive(JSONObject message) {
		if (log.isDebugEnabled()) log.debug("Sending: " + message.toString(2));
		PostMethod postMethod = new PostMethod(uri.toString());
		postMethod.setRequestHeader("Content-Type", "text/plain");
		
		RequestEntity requestEntity= new StringRequestEntity(message.toString());
		postMethod.setRequestEntity(requestEntity);
		try {
			http().executeMethod(null, postMethod, state);
			int statusCode= postMethod.getStatusCode();
			if (statusCode!=HttpStatus. SC_OK)
				throw new ClientError("HTTP Status - " + 
					HttpStatus.getStatusText(statusCode) + " (" + statusCode + ")");
			JSONTokener tokener= new JSONTokener(postMethod.getResponseBodyAsString());
			Object rawResponseMessage= tokener.nextValue();
			JSONObject responseMessage= (JSONObject)rawResponseMessage;
			if (responseMessage==null)
				throw new ClientError("Invalid response type - " + rawResponseMessage.getClass());
			return responseMessage;		
		} catch (ParseException e) {
			throw new ClientError(e);
		} catch (HttpException e) {
			throw new ClientError(e);
		} catch (IOException e) {
			throw new ClientError(e);
		}
	}

	HttpClient http() {
		if (client==null) {
			client= new HttpClient();
			if (state==null)
				state= new HttpState();
			client.setState(state);
		}
		return client;
	}

	public void close() {
		state.clear();
		state= null;
	}

	static class Factory implements SessionFactory {
		public Session newSession(URI uri) {
			return new HTTPSession(uri);
		}		
	}
	
	/** 
	 * Register this transport in 'registry'
	 */
	public static void register(TransportRegistry registry) {
		registry.registerTransport("http", new Factory());
	}
}
