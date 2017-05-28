package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import util.IOUtil;

public class Client {
	private Socket socket;
	
	public Client(String host, int port) {
		// TODO Auto-generated constructor stub
		try {
			this.socket = new Socket(host, port);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("ERROR CREATING SOCKET");
		}
	}
	
	private boolean pendingExit(String msg) {
		if (msg == null)
			return false;
		if (msg.equals("quit"))
			return true;
		else return false;
	}
	
	public void startClient() {
		ReaderThread reader = new ReaderThread(socket);
		reader.setDaemon(true);
		reader.start();
		OutputStream outputStream = null;
		try {
			outputStream = socket.getOutputStream();
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("ERROR BUILDING STREAM");
			e.printStackTrace();
		}
		Scanner scanner = new Scanner(System.in);
		while (true) {
			String input = scanner.nextLine();
			if (pendingExit(input))
				break;
			try {
				IOUtil.writeSocketLine(socket.getOutputStream(), input);
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println("ERROR WRITING MSG");
				e.printStackTrace();
			}
		}
		
		try {
			outputStream.close();
			socket.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("ERROR CLOSING STREAM OR SOCKET");
			e.printStackTrace();
		}
	}
	
	class ReaderThread extends Thread {
		private InputStream inputStream;
		
		public ReaderThread(Socket socket) {
			// TODO Auto-generated constructor stub
			try {
				this.inputStream = socket.getInputStream();
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println("READER THREAD FAILED TO CREATE");
			}
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				while (!socket.isClosed()) {
					String message = IOUtil.readLine(inputStream);
					if (message != null)
						System.out.println(message);
				}
			} 
			catch (SocketException e) {
				System.out.println("CLOSE CONNECTION!");
			}
			catch (Exception e) {
				// TODO: handle exception
				System.err.println("ERROR RECEIVING MSG");
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Client client = new Client("127.0.0.1", 22222);
		client.startClient();
	}
}
