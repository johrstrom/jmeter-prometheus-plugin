package com.github.johrstrom.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerInstantiator {
	private static final Logger log = LoggerFactory.getLogger(ServerInstantiator.class);
	private static Map<Integer, Server> server_instances;

	/**
	 * Retrieves or creates the server
	 * 
	 * @param port
	 */
	public static Server getInstance(int port) {
		// Build the hashmap if it's not there
		if (server_instances == null)
			server_instances = new HashMap<Integer, Server>();

		// build the server instance if it's not there
		if (server_instances.get(port) == null) {
			Server new_server_instance = new Server(port);
			server_instances.put(port, new_server_instance);
		}

		// return the server instance
		return server_instances.get(port);
	}

	/**
	 * Removes a single server
	 * 
	 * @param port
	 */
	public static void clear_server(int port) {
		if (server_instances.containsKey(port))
			try {
				server_instances.get(port).stop();
			} catch (Exception e) {
				log.error("Couldn't stop http server", e);
			}
		server_instances.remove(port);
	}

	/**
	 * Removes all the servers
	 */
	public static void clear_servers() {
		for (int port : server_instances.keySet())
			clear_server(port);
	}
}
