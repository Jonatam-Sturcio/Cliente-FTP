package bit.clientetcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONObject;

public class Arquivo {

	private Socket sock;
	private DataOutputStream out;
	private DataInputStream in;

	public Arquivo(Socket socket) throws IOException {
		this.sock = socket;
		out = new DataOutputStream(sock.getOutputStream());
		in = new DataInputStream(sock.getInputStream());
	}

	private String getHash(String nomeArquivo) {
		File file = new File("src/archives/" + nomeArquivo);
		return "" + file.hashCode() + file.length();
	}
	
	public boolean arquivoExiste(JSONObject json) {
		File file = new File("src/archives");
		File[] listaArq = file.listFiles();
		for (File file1 : listaArq) {
			if(json.get("file").equals(file1.getName())){
				return true;
			}
		}
		return false;
	}
	
	public void listaArquivos() throws IOException {
		String msg="";
		File file = new File("src/archives");

		File[] listaArq = file.listFiles();
		for (File file1 : listaArq) {
			msg += file1.getName() + "\n";
		}
		System.out.println("\n"+msg);
	}
	public void recebeArquivo(JSONObject json) {
		try {

			System.out.println("\nObtendo hash...");

			JSONObject jsonHashRecebido = new JSONObject(in.readUTF());

			int bytesRead, current = 0;

			byte[] arrayBytes = new byte[20971520];

			FileOutputStream fos = new FileOutputStream("src/archives/" + json.getString("file")); 

			BufferedOutputStream bos = new BufferedOutputStream(fos); 

			System.out.println("Realizando download...");

			bytesRead = in.read(arrayBytes, 0, arrayBytes.length); 

			current = bytesRead; 

			do {
				Thread.sleep(1);
				bytesRead = in.read(arrayBytes, current, in.available());
				if (bytesRead >= 0) {
					current += bytesRead;
				}
			} while (in.available() != 0);

			bos.write(arrayBytes, 0, current); 
			bos.close();
			
			if (getHash(json.getString("file")).equals(jsonHashRecebido.get("hash"))) {
				System.out.println("Download concluido com sucesso!\n");
			} else {
				System.out.println("Falha no download, o arquivo sera removido do computador.");
				File file = new File("src/archives/" + json.getString("file"));
				file.delete();
			}

		} catch (IOException ex) {
			System.out.println("IOException na classe Archive: " + ex.getMessage());
		} catch (InterruptedException ex) {
			System.out.println("InterruptedException na classe Archive: " + ex.getMessage());
		} 
	}

	private void enviaHash(String nomeArquivo, String operation) throws IOException {
		File file = new File("src/archives/" + nomeArquivo);
		JSONObject json = new JSONObject();
		json.put("file", nomeArquivo);
		json.put("operation", operation);
		json.put("hash", "" + file.hashCode() + file.length());
		out.writeUTF(json.toString());
	}

	public void enviaArquivo(JSONObject json) {
		try {
			System.out.println("\nEnviando hash...");
			
			enviaHash(json.getString("file"), json.getString("command")); 

			File file = new File("src/archives/" + json.get("file")); 

			byte[] arrayBytes = new byte[(int) file.length()]; 

			FileInputStream fis = new FileInputStream(file); 

			BufferedInputStream bis = new BufferedInputStream(fis);

			bis.read(arrayBytes, 0, arrayBytes.length); 
			
			System.out.println("Enviando arquivo...");
			
			out.write(arrayBytes, 0, arrayBytes.length); 

			System.out.println("Aguardando status...");
			
			json = new JSONObject(in.readUTF());
			
			System.out.println("\tArquivo: "+json.getString("file")+"\n\tOperacao: "+json.getString("operation")+"\n\tStatus: "+json.getString("status")+"\n");
			
		} catch (FileNotFoundException ex) {
			System.out.println("FileNotFoundException na classe Arquivo: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("IOException na classe Arquivo: " + ex.getMessage());
		}
	}
}
