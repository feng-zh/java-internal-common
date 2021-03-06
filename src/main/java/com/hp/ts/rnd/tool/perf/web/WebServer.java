package com.hp.ts.rnd.tool.perf.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class WebServer {

	private HttpServer httpServer;
	private int port;
	private List<WebContext> contexts = new ArrayList<WebContext>();

	public WebServer(int port) {
		this.port = port;
	}

	public void addResourceApplication(WebResourceApplication application) {
		contexts.add(new WebContext(application, httpServer));
	}

	public void start() throws IOException {
		httpServer = com.sun.net.httpserver.HttpServer.create();
		ServiceLoader<WebResourceApplication> services = ServiceLoader
				.load(WebResourceApplication.class);
		for (Iterator<WebResourceApplication> iterator = services.iterator(); iterator
				.hasNext();) {
			addResourceApplication(iterator.next());
		}
		httpServer.bind(new InetSocketAddress(port), 10);
		httpServer.start();
		System.out.println("Server started on port " + port
				+ ", please access following endpoints: ");
		for (Iterator<WebResourceApplication> iterator = services.iterator(); iterator
				.hasNext();) {
			System.out.println("- http:/" + httpServer.getAddress()
					+ iterator.next().getContextPath());
		}
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.nanoTime();
		int port = 7001;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Invalid port, use default value: " + port);
			}
		}
		final WebServer server = new WebServer(port);
		server.start();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				server.shutdown();
			}
		}));
		System.out.println("System started in "
				+ TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
				/ 1000.0 + " second.");
	}

	public void shutdown() {
		httpServer.stop(0);
	}

}
