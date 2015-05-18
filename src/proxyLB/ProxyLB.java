package proxyLB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ProxyLB {
	
	private Map<String, Map<String, String>> workers = new HashMap<String, Map<String, String>>();
	private int Port;
	
	public ProxyLB(int port){
		try {
			init(port);
			run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void run(){
		ServerSocket socket;
		try {
			socket = new ServerSocket(Port);
			
			Thread t = new Thread(new Accepter_clients(socket));
			t.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void init(int port) throws IOException{
		
		this.Port = port;
		Map<String, String> configFileIni = Ini.load("config.ini");

		Set<String> cles = configFileIni.keySet();
		Iterator<String> it = cles.iterator();

		while (it.hasNext()) {
			String cle = it.next();
			String valeur = configFileIni.get(cle);
			String indice = cle.split("\\.")[1];

			if (workers.get(indice) != null) {
				workers.get(indice).put(cle.split("\\.")[2], valeur);
			} else {
				Map<String, String> lineConfig = new HashMap<String, String>();
				lineConfig.put(cle.split("\\.")[2], valeur);
				workers.put(indice, lineConfig);
			}
		}
		System.out.println(workers);
	}
	
	class Accepter_clients implements Runnable {

		private ServerSocket socketserver;
		private Socket socket;
    
		public Accepter_clients(ServerSocket s) {
			socketserver = s;
		}
 
		public void run() {

			try {
				while (true) {
					socket = socketserver.accept();
					
					 InputStream from_client = socket.getInputStream();
				   OutputStream to_client = socket.getOutputStream();
				   

					try {
						 byte[] buffer = new byte[2048];
				          int bytes_read;
						Socket socketS = new Socket(  (InetAddress.getByName(workers.get("0").get("ip"))), 8080);
						InputStream from_server = socketS.getInputStream();
					     OutputStream to_server = socketS.getOutputStream();
					     
					 	while((bytes_read = from_client.read(buffer)) != -1) {
				              to_server.write(buffer, 0, bytes_read);
				              to_server.flush();
				            }
					 	
					 	byte[] bufferT = new byte[2048];
				          int bytes_readT;
				          
				            while((bytes_readT = from_server.read(bufferT)) != -1) {
				              to_client.write(bufferT, 0, bytes_readT);
				              to_client.flush();
				            }
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				
				
					socket.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
