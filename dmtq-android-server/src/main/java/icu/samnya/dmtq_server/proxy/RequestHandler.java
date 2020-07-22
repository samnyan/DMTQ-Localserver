package icu.samnya.dmtq_server.proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequestHandler implements Runnable {

	private static String LOG_TAG = "ProxyRequestHandler";

	private Socket clientSocket;

	private BufferedReader toClientReader;
	private InputStream toClientStream;

	private BufferedWriter toClientWriter;

	private Context ctx;

	RequestHandler(Socket clientSocket, Context ctx){
		this.clientSocket = clientSocket;
		this.ctx = ctx;
		try{
			this.clientSocket.setSoTimeout(2000);
			toClientStream = clientSocket.getInputStream();
			toClientReader = new BufferedReader(new InputStreamReader(toClientStream));
			toClientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		// Get Request from client
		String requestString;
		try{
			requestString = readLine(toClientStream);
			if(requestString==null) {
				return;
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error reading request from client", e);
			return;
		}

		Log.i(LOG_TAG, "Request Received " + requestString);
		// Get the Request type
		String method = requestString.substring(0,requestString.indexOf(' '));

		// Get request url
		String urlString = requestString.substring(requestString.indexOf(' ')+1);

		// Remove everything past next space
		urlString = urlString.substring(0, urlString.indexOf(' '));

		// TODO: return pac
		if(urlString.startsWith("/pac")) {

		}

		// Prepend http:// if necessary to create correct URL
		if(!urlString.startsWith("http")){
			String temp = "http://";
			urlString = temp + urlString;
		}

		if(method.equals("CONNECT")){
			Log.i(LOG_TAG, "HTTPS Request for : " + urlString);
			handleHTTPSRequest(urlString);
		} else{
			handleHttpRequest(method, urlString);
		}
	}

	private void handleHttpRequest(String method, String urlString){
		try {
			if(matchUrl(urlString)) {
				int start = urlString.indexOf("http://");
				start = urlString.indexOf("/", start + 7);
				if(urlString.length() > start + 1) {
					urlString = urlString.substring(start + 1);
				} else {
					urlString = "";
				}
				SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
				String HOST = sharedPref.getString("HOST_ADDRESS", "localhost:3456");
//				String HOST = "192.168.101.208";
				urlString = "http://" + HOST + "/" + urlString;
				Log.i(LOG_TAG, "Redirect http to : " + urlString);
			}
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			if(!method.equalsIgnoreCase("get")) {
				conn.setDoOutput(true);
				conn.setRequestMethod(method);
			}

			// Add header to request
			List<String> headers = new LinkedList<>();
			try {
				String line;
				while ((line = readLine(toClientStream)) != null && !line.replace("\r", "").isEmpty()) {
					headers.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (String str : headers) {
				String[] kv = str.split(":");
				if (kv[1].length() > 0) {
					kv[1] = kv[1].substring(1);
					conn.setRequestProperty(kv[0], kv[1]);
				} else {
					conn.setRequestProperty(kv[0], "");
				}
			}

			// Send request body
			if(!method.equalsIgnoreCase("get")) {
				try {
					OutputStream out = conn.getOutputStream();
					byte[] buffer = new byte[4096];
					int read;
					do {
						read = toClientStream.read(buffer);
						if (read > 0) {
							out.write(buffer, 0, read);
							if (toClientStream.available() < 1) {
								out.flush();
								break;
							}
						}
					} while (read >= 0);

				}
				catch (Exception e) {
					Log.e(LOG_TAG, e.getMessage(), e);
				}
			}

			// Send response
			try {
				boolean isError = (conn.getResponseCode() >= 400);
				InputStream in = isError ? conn.getErrorStream() : conn.getInputStream();
				OutputStream out = clientSocket.getOutputStream();

				// Send header
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP/1.0 ");
				sb.append(conn.getResponseCode()).append(" ");
				sb.append(conn.getResponseMessage()).append("\r\n");
				sb.append("Proxy-Agent: DMTQProxy/0.2\r\n");

				for(Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
					String key = entry.getKey();
					List<String> values = entry.getValue();
					for (String val: values) {
						sb.append(key).append(": ").append(val).append("\r\n");
					}
				}
				sb.append("\r\n");
				toClientWriter.write(sb.toString());
				toClientWriter.flush();

				byte[] buffer = new byte[1024];
				int len = in.read(buffer);
				while (len != -1) {
					out.write(buffer, 0, len);
					len = in.read(buffer);
				}
				out.flush();
				out.close();
			} catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}

		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		} finally {
			try {
				toClientStream.close();
				clientSocket.close();
			} catch (Exception e) {

			}
		}
	}


	private void handleHTTPSRequest(String urlString){
		// Extract the URL and port of remote 
		String url = urlString.substring(7);
		String[] pair = url.split(":");
		url = pair[0];
		int port  = Integer.parseInt(pair[1]);

		// Check server address and redirect it to localhost
		if(matchUrl(url)) {
			SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
			String SSL = sharedPref.getString("SSL_ADDRESS", "localhost:3457");
			String[] addr = SSL.split(":");
			url = addr[0];
			port = Integer.parseInt(addr[1]);
		}

		try{
			// Only first line of HTTPS request has been read at this point (CONNECT *)
			// Read (and throw away) the rest of the initial data on the stream
			for(int i=0;i<5;i++){
				toClientReader.readLine();
			}

			InetAddress address = InetAddress.getByName(url);

			Socket proxyToServerSocket = new Socket(address, port);
			proxyToServerSocket.setSoTimeout(5000);

			// Send Connection established to the client
			String line = "HTTP/1.0 200 Connection established\r\n" +
					"Proxy-Agent: ProxyServer/1.0\r\n" +
					"\r\n";
			toClientWriter.write(line);
			toClientWriter.flush();

			// Start a new thread to handle client to server
			StreamTransfer streamTransferHttps =
					new StreamTransfer(clientSocket.getInputStream(), proxyToServerSocket.getOutputStream());

			Thread httpsThread = new Thread(streamTransferHttps);
			httpsThread.start();
			
			
			// Read data from server and send to client
			try {
				byte[] buffer = new byte[4096];
				int read;
				do {
					read = proxyToServerSocket.getInputStream().read(buffer);
					if (read > 0) {
						clientSocket.getOutputStream().write(buffer, 0, read);
						if (proxyToServerSocket.getInputStream().available() < 1) {
							clientSocket.getOutputStream().flush();
						}
					}
				} while (read >= 0);

			}
			catch (SocketTimeoutException e) {
				Log.i(LOG_TAG, "Close socket by timeout");
			}
			catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}

			if(proxyToServerSocket != null){
				proxyToServerSocket.close();
			}

			if(toClientWriter != null){
				toClientWriter.close();
			}


		} catch (SocketTimeoutException e) {
			String line = "HTTP/1.0 504 Timeout Occured after 10s\n" +
					"User-Agent: ProxyServer/1.0\n" +
					"\r\n";
			try{
				toClientWriter.write(line);
				toClientWriter.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} 
		catch (Exception e){
			Log.e(LOG_TAG, e.getMessage(), e);
		}
	}

	static class StreamTransfer implements Runnable{
		InputStream is;
		OutputStream os;

		StreamTransfer(InputStream is, OutputStream os) {
			this.is = is;
			this.os = os;
		}

		@Override
		public void run(){
			try {
				byte[] buffer = new byte[4096];
				int read;
				do {
					read = is.read(buffer);
					if (read > 0) {
						os.write(buffer, 0, read);
						if (is.available() < 1) {
							os.flush();
						}
					}
				} while (read >= 0);
			}
			catch (SocketTimeoutException ste) {
				Log.i(LOG_TAG, "Close socket by timeout");
			}
			catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		}
	}

	/**
	 * Manually read a line from input stream.
	 */
	private static String readLine(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		int c;
//		// Since browser send \r\n in header, check for \n is enough here
//		for (c = inputStream.read(); c != '\n' && c != -1 ; c = inputStream.read()) {
//			byteArrayOutputStream.write(c);
//		}
		int c = -1;
		do {
			if (inputStream.available() < 1) {
				break;
			}
			c = inputStream.read();
			if (c != '\n' && c != -1) {
				byteArrayOutputStream.write(c);

			} else {
				break;
			}
		} while (c >= 0);

		if (c == -1 && byteArrayOutputStream.size() == 0) {
			return null;
		}
		return byteArrayOutputStream.toString("UTF-8");
	}

	private boolean matchUrl(String url) {
		if(url.contains("pmang.com")) {
			return true;
		}
		if(url.contains("pmangplus.com")) {
			return true;
		}
		if(url.contains("neonapi.com")) {
			return true;
		}
		return false;
	}
}




