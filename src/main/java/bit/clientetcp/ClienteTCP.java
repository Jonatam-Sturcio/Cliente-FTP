package bit.clientetcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import org.json.JSONObject;

public class ClienteTCP {

	public static void main(String[] args) {
		JSONObject json;
		Scanner input = new Scanner(System.in);
		String[] cmd;
		try {
			InetAddress address = InetAddress.getByName("127.0.0.1");
			int srvPort = 50000;
			Socket sock = new Socket(address, srvPort);
			DataInputStream in = new DataInputStream(sock.getInputStream());
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			Arquivo arq = new Arquivo(sock);
			System.out.println("Conectado ao sevidor!");

			while (true) {
				json = new JSONObject();
				System.out.println("Insira o comando desejado:");
				System.out.println("!help para exibir a lista de comandos.");
				System.out.print("> ");

				cmd = input.nextLine().split(" ");
				if (!cmd[0].isEmpty()) {
					if ("PUT".equalsIgnoreCase(cmd[0]) || "GET".equalsIgnoreCase(cmd[0])) {
						json.put("command", cmd[0]);
						json.put("file", cmd[1]);
					} else {
						json.put("command", cmd[0]);
					}

					if (!json.get("command").equals("LISTLOCAL")) {
						out.writeUTF(json.toString());
					}
					if ("LIST".equalsIgnoreCase(json.getString("command"))) {
						json = new JSONObject(in.readUTF());
						System.out.println(json.get("list"));
					} else if ("LISTLOCAL".equalsIgnoreCase(json.getString("command"))) {
						arq.listaArquivos();
					} else if ("PUT".equalsIgnoreCase(json.getString("command"))) {
						if (arq.arquivoExiste(json)) {
							arq.enviaArquivo(json);
						} else {
							System.out.println("\nO arquivo nao existe no cliente!\nUse o comando LISTLOCAL para verificar os arquivos do cliente.\n");
						}
					} else if ("GET".equalsIgnoreCase(json.getString("command"))) {
						String msg = in.readUTF();
						if ("true".equals(msg)) {
							arq.recebeArquivo(json);
						} else {
							System.out.println("\nO arquivo nao existe no servidor!\nUse o comando LIST para verificar os arquivos do servidor.\n");
						}
					} else if ("!help".equalsIgnoreCase(json.getString("command"))) {
						System.out.println("------------------ COMANDOS ------------------");
						System.out.println("LIST - lista todos os arquivos armazenados no servidor.");
						System.out.println("LISTLOCAL - lista todos os arquivos armazenados no cliente.");
						System.out.println("PUT <file> - envia ao servidor o arquivo definido em <file> (20Mb limite)");
						System.out.println("GET <file> - faz o download do <file> armazenado no servidor.");
						System.out.println("!close - encerra a conexao com o servidor.");
						System.out.println("----------------------------------------------------\n");
					} else if ("!close".equalsIgnoreCase(json.getString("command"))) {
						System.out.print("\nFechando socket do cliente!");
						sock.shutdownInput();
						sock.shutdownOutput();
						break;
					} else {
						System.err.println("Comando desconhecido, por favor tente novamente!\n");
					}
				} else {
					System.err.println("Erro de escrita, por favor tenta novamente!\n");
				}

			}
			sock.close();
			System.exit(0);
		} catch (IOException ex) {
			System.out.println("IOException na classe ClienteTCP: " + ex.getMessage());
		}
	}

}
