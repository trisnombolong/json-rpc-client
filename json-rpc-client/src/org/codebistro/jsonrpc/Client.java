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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.ParseException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.metaparadigm.jsonrpc.JSONRPCResult;
import com.metaparadigm.jsonrpc.JSONSerializer;
import com.metaparadigm.jsonrpc.SerializerState;

public class Client implements InvocationHandler {
	static Log log= LogFactory.getLog(Client.class);

	URI uri;
	JSONSerializer message2Object;
	
	public Client(String uriString) {
		try {
			uri= new URI(uriString);
			message2Object= new JSONSerializer();
			message2Object.registerDefaultSerializers();
		} catch (Exception e) {
			throw new ClientError(e);
		}
	}

	static class ProxyMap extends HashMap<Object, String> {}
	ProxyMap proxyMap= new ProxyMap();
	
	public<T> T openProxy(String tag, Class<T> klass) {
		Object result= java.lang.reflect.Proxy.newProxyInstance(klass.getClassLoader(), 
			new Class[] { klass }, this);
		proxyMap.put(result, tag);
		return klass.cast(result);
	}
	
	public void closeProxy(Object proxy) {
		proxyMap.remove(proxy);
	}

	public Object invoke(Object proxyObj, Method method, Object[] args) throws Throwable {
		String methodName= method.getName();
        if (methodName.equals("hashCode"))  {
            return new Integer(System.identityHashCode(proxyObj));   
        } else if (methodName.equals("equals")) {
            return (proxyObj == args[0] ? Boolean.TRUE : Boolean.FALSE);
        } else if (methodName.equals("toString")) {
            return proxyObj.getClass().getName() + '@' + Integer.toHexString(proxyObj.hashCode());
        }
		JSONObject message= new JSONObject();
		String tag= proxyMap.get(proxyObj);
		String methodTag= tag==null ? "" : tag + ".";
		methodTag+=methodName;
		message.put("method", methodTag);

		JSONArray params= new JSONArray();
		if (args!=null) {
			for(Object arg: args) {
				SerializerState state= new SerializerState();
				params.put(message2Object.marshall(state, arg));
			}
		}
		message.put("params", params);
		message.put("id", 1);
		JSONObject responseMessage= sendAndReceive(message);
		if (!responseMessage.has("result"))
			processException(responseMessage);
		Object rawResult= responseMessage.get("result");
		if (rawResult==null) {
			processException(responseMessage);
		}
		Class returnType= method.getReturnType();
		if (returnType.equals(Void.TYPE))
			return null;
		SerializerState state= new SerializerState();
		return message2Object.unmarshall(state, returnType, rawResult);
	}
	
	JSONObject sendAndReceive(JSONObject message) {
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

	void processException(JSONObject responseMessage) {
		JSONObject error= (JSONObject)responseMessage.get("error");
		if (error!=null) {
			String trace= error.has("trace") ? error.getString("trace") : null;
			throw new ErrorResponse((Integer)error.get("code"), 
				(String)error.get("msg"), trace);	
		} else
			throw new ErrorResponse(JSONRPCResult.CODE_ERR_PARSE, "Unknown response:" + responseMessage.toString(2), null);
	}

	HttpClient client;
	HttpState state;
	
	/** 
	 * An option to set state from the outside.
	 * for example, to provide existing session parameters.
	 */
	public void setState(HttpState state) {
		this.state= state;
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

}
