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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * A registry of transports serving JSON-RPC-Client 
 */
public class TransportRegistry {

	private static TransportRegistry singleton;
	
	/** 
	 * @param registerDefault 'true' if to register default transports
	 */
	public TransportRegistry(boolean registerDefault) {
		if (registerDefault) {
			HTTPSession.register(this);
		}
	}
	
	public TransportRegistry() {
		this(true);
	}

	/** 
	 * Use this function when there is no IOC container to rely on creating the factory.
	 * 
	 * @return singleton instance of the class, created if necessary.
	 */
	public synchronized static TransportRegistry i() {
		if (singleton==null)
			singleton= new TransportRegistry();
		return singleton;
	}
	
	private HashMap<String, SessionFactory> registry= new HashMap<String, SessionFactory>();
	
	public interface SessionFactory {
		/**
		 * @param uri URI used to open this session
		 * @param client A client object requesting the session. Rarely used.
		 */
		Session newSession(URI uri);		
	}
	
	public void registerTransport(String scheme, SessionFactory factory) {
		registry.put(scheme, factory);
	}
	
	public Session createSession(String uriString) {
		try {
			URI uri= new URI(uriString);
			SessionFactory found= registry.get(uri.getScheme());
			if (found==null)
				throw new ClientError("Could not open URI '" + uriString + 
						"'. Unknown scheme - '" + uri.getScheme() + "'");
			return found.newSession(uri);
		} catch (URISyntaxException e) {
			throw new ClientError(e);
		}
	}

}
