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
	private String balancing = "0";

	public ProxyLB(int port) {
		try {
			init(port);
			run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void run() {
		ServerSocket socket;
		try {
			socket = new ServerSocket(Port);
			handle(socket);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void init(int port) throws IOException {

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

	private void handle(ServerSocket socketserver) {
		
		try {
			Socket socketClientProxy = socketserver.accept();
			final InputStream inputClientProxy = socketClientProxy.getInputStream();
			OutputStream outputClientProxy = socketClientProxy.getOutputStream();
			
			Socket socketProxyServeur = new Socket((InetAddress.getByName(workers.get(balancing).get("ip"))), Integer.parseInt(workers.get(balancing).get("port")));
			System.out.println("connect on serveur ip:"+workers.get(balancing).get("ip")+ " port: "+ workers.get(balancing).get("port"));
			InputStream inputProxyServeur = socketProxyServeur.getInputStream();
			final OutputStream outputProxyServeur = socketProxyServeur.getOutputStream();
			
			Thread t = new Thread() {
				public void run() {
			byte[] buffer = new byte[2048];
			int bytes_read;
			try {
				while ((bytes_read = inputClientProxy.read(buffer)) != 1) {
					outputProxyServeur.write(buffer, 0, bytes_read);
					outputProxyServeur.flush();
					System.out.println("écriture vers le serveur");
				}
			//outputProxyServeur.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			}};
			t.start();
			
			byte[] bufferT = new byte[2048];
			int bytes_readT;
			
			while ((bytes_readT = inputProxyServeur.read(bufferT)) != -1) {
				outputClientProxy.write(bufferT, 0, bytes_readT);
				outputClientProxy.flush();
				System.out.println("écriture vers le client");
			}
			//outputClientProxy.close();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	/*	if(balancing.equals("0"))
			balancing = "1";
		else
			balancing = "0";*/
	}

}
