package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.IOUtil;

public class Server {
	private final int port;
	private Map<Socket, String> socketMap = new HashMap<>();
	
	public Server(int port) {
		// TODO Auto-generated constructor stub
		this.port = port;
	}
	
	class AcceptThread extends Thread {
		private InputStream inputStream;
		private OutputStream outputStream;
		private Socket socket;
		private String key;
		
		public AcceptThread(Socket socket) {
			// TODO Auto-generated constructor stub
			this.socket = socket;
			this.key = socket.getInetAddress()
					.getHostName() + ":" + socket.getPort();
			socketMap.put(socket, key);
		}
		
        @Override
        public void run() {
            try {
                this.inputStream = socket.getInputStream();
                this.outputStream = socket.getOutputStream();

                System.out.println(this.key + " is on line");
                while (true) {

                    String line = IOUtil.readLine(this.inputStream);
                    if (line == null)
                        break;
                    System.out.println(this.key + " says: " + line);
                    for (Map.Entry<Socket, String> entry : socketMap.entrySet()) {
                        try {
                            Socket tempSocket = entry.getKey();
                            if (tempSocket != null && !this.key.equals(entry.getValue()))
                                IOUtil.writeSocketLine(tempSocket.getOutputStream(), this.key + " says: " + line);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    System.out.println(this.key + " is off line");
                    socketMap.remove(this.socket);
                    if (inputStream != null) 
                        inputStream.close();
                    if (outputStream != null)
                        outputStream.close();
                    if (socket != null)
                        socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	public void startServer() throws IOException {
		ServerSocket serverSocket = new ServerSocket(port);
		ExecutorService service = Executors.newCachedThreadPool();
		while (true) {
			Thread AcceptThread = new AcceptThread(serverSocket.accept());
			service.execute(AcceptThread);
		}
	}
	
	public static void main(String[] args) throws IOException {
		Server server = new Server(22222);
		server.startServer();
	}
}
