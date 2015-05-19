package proxyLB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
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
		private Socket socketClient;
    
		public Accepter_clients(ServerSocket s) {
			socketserver = s;
		}
 
		public void run() {
			
			try {
				while (true) {
					socketClient = socketserver.accept();
					System.out.println("Client connected to proxy");
					InputStream clientInp = socketClient.getInputStream();
				    OutputStream clientOut = socketClient.getOutputStream();
				    
					try {
						byte[] bufferIn = new byte[2048];
						byte[] bufferOut = new byte[2048];
				        int bytes_in;
				        int bytes_out;
						Socket socketServeur = new Socket(  (InetAddress.getByName(workers.get("0").get("ip"))), 8080);
						OutputStream serveurOut = socketServeur.getOutputStream();
				        InputStream serveurInp = socketServeur.getInputStream();
					     
				        while((bytes_in = clientInp.read(bufferIn)) != -1){
				            serveurOut.write(bufferIn, 0, bytes_in);
				            if((new String(Arrays.copyOf(bufferIn, bytes_in))).endsWith("\r\n\r\n")){
				                break;
				            }
				        }
				        while((bytes_out = serveurInp.read(bufferOut)) != -1){
				            clientOut.write(bufferOut, 0, bytes_out);
				        }
				        clientOut.close();
				        clientInp.close();
				        serveurInp.close();
				        serveurOut.close();
				        socketServeur.close();
				        socketClient.close();
				       
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				
				
					socketClient.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
