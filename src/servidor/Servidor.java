/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

public class Servidor extends Thread {
    // Parte que controla as conex�es por meio de threads.
    private static Vector CLIENTES;
    private static Vector ADDRESS;

    private static Map<String, Socket> CliAdre;
    private static Map<String, PrintStream> CliStream;

    // socket deste cliente
    private Socket conexao;
    // nome deste cliente
    private String nomeCliente;
    // lista que armazena nome de CLIENTES
    private static List LISTA_DE_NOMES = new ArrayList();
    // construtor que recebe o socket deste cliente
    public Servidor(Socket socket) {
        this.conexao = socket;
    }
    //testa se nomes s�o iguais, se for retorna true
    public boolean armazena(String newName){
        //   System.out.println(LISTA_DE_NOMES);
        for (int i=0; i < LISTA_DE_NOMES.size(); i++){
            if(LISTA_DE_NOMES.get(i).equals(newName))
                return true;
        }
        //adiciona na lista apenas se n�o existir
        LISTA_DE_NOMES.add(newName);
        return false;
    }
    //remove da lista os CLIENTES que j� deixaram o chat
    public void remove(String oldName) {
        for (int i=0; i< LISTA_DE_NOMES.size(); i++){
            if(LISTA_DE_NOMES.get(i).equals(oldName))
                LISTA_DE_NOMES.remove(oldName);
        }
    }
    public static void main(String args[]) {
        // instancia o vetor de CLIENTES conectados
        CLIENTES = new Vector();
        ADDRESS = new Vector();
        CliAdre = new HashMap<String, Socket>();
        CliStream = new HashMap<String, PrintStream>();

        try {
            // cria um socket que fica escutando a porta 5555.
            ServerSocket server = new ServerSocket(5555);
            System.out.println("ServidorSocket rodando na porta 5555");
            // Loop principal.
            while (true) {
                // aguarda algum cliente se conectar.
                // A execu��o do servidor fica bloqueada na chamada do m�todo accept da
                // classe ServerSocket at� que algum cliente se conecte ao servidor.
                // O pr�prio m�todo desbloqueia e retorna com um objeto da classe Socket
                Socket conexao = server.accept();
                // cria uma nova thread para tratar essa conex�o
                Thread t = new Servidor(conexao);
                t.start();
                // voltando ao loop, esperando mais algu�m se conectar.
            }
        } catch (IOException e) {
            // caso ocorra alguma excess�o de E/S, mostre qual foi.
            System.out.println("IOException: " + e);
        }
    }
    // execu��o da thread
    public void run()
    {
        try {
            // objetos que permitem controlar fluxo de comunica��o que vem do cliente
            BufferedReader entrada =
                    new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));

            PrintStream saida = new PrintStream(this.conexao.getOutputStream());
            // recebe o nome do cliente
            this.nomeCliente = entrada.readLine();

            System.out.println (this.conexao.getRemoteSocketAddress().toString());

            //chamada ao metodo que testa nomes iguais
            if (armazena(this.nomeCliente)){
                saida.println("Este nome ja existe! Conecte novamente com outro Nome.");
                CLIENTES.add(saida);

                //fecha a conexao com este cliente
                this.conexao.close();
                return;
            } else {
                //mostra o nome do cliente conectado ao servidor
                System.out.println(this.nomeCliente + " : Conectado ao Servidor!");
            }
//            //igual a null encerra a execu��o
            if (this.nomeCliente == null) {
                return;
            }
            //adiciona os dados de saida do cliente no objeto CLIENTES
            CliAdre.put(this.nomeCliente, this.conexao);
            CliStream.put(this.nomeCliente, saida);

            CLIENTES.add(saida);
            ADDRESS.add(this.conexao.getRemoteSocketAddress().toString());
            //recebe a mensagem do cliente
            String msg = entrada.readLine();
            // Verificar se linha � null (conex�o encerrada)
            // Se n�o for nula, mostra a troca de mensagens entre os CLIENTES
            while (msg != null && !(msg.trim().equals("")))
            {

                /*
                * Make the system recognize users inputs as defined commands
                * */

                clientCommandsHandler(saida, msg);

                // reenvia a linha para todos os CLIENTES conectados
                //sendToAll(saida, " escreveu: ", msg);

                //sendToAll(saida, "asdfg#",this.conexao.getRemoteSocketAddress().toString());


                // espera por uma nova linha.
                msg = entrada.readLine();
            }
            //se cliente enviar linha em branco, mostra a saida no servidor
            System.out.println(this.nomeCliente + " saiu do bate-papo!");
            //se cliente enviar linha em branco, servidor envia
            // mensagem de saida do chat aos CLIENTES conectados
            sendToAll(saida, " saiu", " do bate-papo!");
            //remove nome da lista
            remove(this.nomeCliente);
            //exclui atributos setados ao cliente
            CLIENTES.remove(saida);
            //fecha a conexao com este cliente
            this.conexao.close();
        } catch (IOException e) {
            // Caso ocorra alguma excess�o de E/S, mostre qual foi.
            System.out.println("Falha na Conexao... .. ."+" IOException: " + e);
        }
    }
    // enviar uma mensagem para todos, menos para o pr�prio
    public void sendToAll(PrintStream saida, String acao, String msg) throws IOException {
        Enumeration e = CLIENTES.elements();
        while (e.hasMoreElements()) {
            // obt�m o fluxo de sa�da de um dos CLIENTES
            PrintStream chat = (PrintStream) e.nextElement();
            // envia para todos, menos para o pr�prio usu�rio
            if (chat != saida) {
                chat.println(this.nomeCliente + acao + msg);
            }
        }
    }

    public  void clientCommandsHandler(PrintStream saida, String msg) throws IOException {

        for ( Map.Entry<String, Socket> entry : CliAdre.entrySet() ) {
                System.out.println("NOME" + entry.getKey() +" IP: "+ entry.getValue().getRemoteSocketAddress().toString());
        }

        // Commands
        switch (msg) {
            case "PLAY":
                BufferedReader entrada =
                        new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));

                // when a client wants to play, send all the users online and wait for answer
                saida.println(LISTA_DE_NOMES.toString());

                // Start a new game
                String player2;
                Boolean newGame = true;
                while (newGame) {
                    // choose player2
                    player2 = entrada.readLine();
                    System.out.println("Escolheu Player 2: " + player2);

                    // Procura pelo socket do Player 2
                    Socket player2Connection = CliAdre.get(player2);
                    System.out.println("Player 2 IP: " + player2Connection.getRemoteSocketAddress().toString());

                    //Envia para o player 2 uma pergunta
                    CliStream.get(player2).println(this.nomeCliente + " gostaria de jogar com voce. Digite SIM ou NAO: ");
                    BufferedReader player2Input = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));

                    String player2Res = null;
                    while (player2Res == null) {
                        System.out.println("Aguardando resposta");
                        String res = player2Input.readLine();
                        System.out.println("Resposta: " + res);
                        if (res.equals("SIM")) {
                            // Aqui ele remove os clientes da lista de usuários disponíveis.
                            // Para eles criarem um P2P entre si e jogarem
                            System.out.println(this.nomeCliente + " e "+ player2 + " jogando...");
                            player2Res = res;
                        }
                    }

                    System.out.println("Final do comando PLAY...");
                }
                break;

            default:
                System.out.println("Invalid: " + msg);
        }
    }






}