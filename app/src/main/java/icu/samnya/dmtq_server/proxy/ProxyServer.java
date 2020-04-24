package icu.samnya.dmtq_server.proxy;

import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ProxyServer {

    private static String LOG_TAG = "ProxyServer";

	private ServerSocket serverSocket;

	private boolean running = false;

	static ArrayList<Thread> threads;

	private final int port;

	public ProxyServer(int port) {
		this.port = port;
		threads = new ArrayList<>();
	}

	public void start(){
		try {
			serverSocket = new ServerSocket(port);

			Log.i(LOG_TAG, "Proxy Server Running at " + serverSocket.getLocalPort());
			running = true;
		}

		catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}

		while(running){
			try {
				Socket socket = serverSocket.accept();
				Thread thread = new Thread(new RequestHandler(socket));

				threads.add(thread);
				
				thread.start();	
			} catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		}
	}

	public void stop() {
		Log.i(LOG_TAG, "Closing Proxy Server..");
		running = false;

		try {
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					Log.i(LOG_TAG, "Stopping proxy thread: " + thread.getId());
					thread.join();
				}
			}
		} catch (InterruptedException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}

		try {
			Log.i(LOG_TAG, "Closing Server Socket" );
			serverSocket.close();
			serverSocket = null;
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
	}

	public boolean isRunning() {
		return running;
	}

}
