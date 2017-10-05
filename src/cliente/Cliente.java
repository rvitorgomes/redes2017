package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
public class Cliente extends Thread {
    // parte que controla a recep��o de mensagens do cliente
    private Socket conexao;
    // construtor que recebe o socket do cliente
    public Cliente(Socket socket) {
        this.conexao = socket;
    }
    public static void main(String args[])
    {
        try {
            //Instancia do atributo conexao do tipo Socket,
            // conecta a IP do Servidor, Porta
            Socket socket = new Socket("127.0.0.1", 5555);
            //Instancia do atributo saida, obtem os objetos que permitem
            // controlar o fluxo de comunica��o
            PrintStream saida = new PrintStream(socket.getOutputStream());
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Digite seu nome: ");
            String meuNome = teclado.readLine();
            //envia o nome digitado para o servidor
            saida.println(meuNome.toUpperCase());
            //instancia a thread para ip e porta conectados e depois inicia ela
            Thread thread = new Cliente(socket);
            thread.start();
            //Cria a variavel msg responsavel por enviar a mensagem para o servidor
            String msg;
            while (true)
            {
                // cria linha para digita��o da mensagem e a armazena na variavel msg
                System.out.print("Mensagem > ");
                msg = teclado.readLine();
                // envia a mensagem para o servidor
                saida.println(msg);
            }
        } catch (IOException e) {
            System.out.println("Falha na Conexao... .. ." + " IOException: " + e);
        }
    }
    // execu��o da thread
    public void run()
    {
        try {
            //recebe mensagens de outro cliente atrav�s do servidor
            BufferedReader entrada =
                    new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
            //cria variavel de mensagem
            String msg;
            while (true)
            {
                // pega o que o servidor enviou
                msg = entrada.readLine();



                //se a mensagem contiver dados, passa pelo if,
                // caso contrario cai no break e encerra a conexao
                if (msg == null) {
                    System.out.println("Conex�o encerrada!");
                    System.exit(0);
                }
                System.out.println();
                //imprime a mensagem recebida
                if (msg.contains("asdfg")){
                    String ip = msg.split("/")[1];
                    ip = ip.split(":")[0];

                    System.out.println("ip: "+ip);

                    cria_cliente(ip);
                }
                else {
                    System.out.println("Mensagem recebida = "+msg);
                    //cria uma linha visual para resposta
                    System.out.print("Responder > ");
                }
            }
        } catch (IOException e) {
            // caso ocorra alguma exce��o de E/S, mostra qual foi.
            System.out.println("Ocorreu uma Falha... .. ." +
                    " IOException: " + e);
        }
    }

    public void cria_cliente (String ip)

            throws UnknownHostException, IOException {
        try (Socket cliente = new Socket(ip, 12346)) {
            System.out.println("O cliente se conectou ao servidor 12346!");

            Scanner teclado = new Scanner(System.in);
            PrintStream saida = new PrintStream(cliente.getOutputStream());

            while (teclado.hasNextLine()) {
                saida.println(teclado.nextLine());
            }

            saida.close();
            teclado.close();
        }

    }
    public void cria_server () throws IOException
    {

        ServerSocket servidor = new ServerSocket(12345);
        System.out.println("Porta 12345 aberta!");

        Socket cliente = servidor.accept();
        System.out.println("Nova conex�o com o cliente " +
                cliente.getInetAddress().getHostAddress()
        );

        Scanner s = new Scanner(cliente.getInputStream());
        while (s.hasNextLine()) {
            System.out.println(s.nextLine());
        }

        s.close();
        servidor.close();
        cliente.close();
    }


}