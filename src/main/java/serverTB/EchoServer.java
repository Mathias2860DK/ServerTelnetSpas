package serverTB;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;


class ClientHandler extends Thread{
    Socket client;
    BufferedReader br;
    PrintWriter pw;
    String  name;
    Dispatcherr dispatcherr;
    BlockingQueue<String> allMessageQueue;

    public ClientHandler(Socket client, Dispatcherr dispatcherr) {
        this.dispatcherr = dispatcherr;
        this.client = client;
        try {
            this.br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.pw = new PrintWriter(client.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientHandler (Socket socket, PrintWriter pr, BufferedReader br, BlockingQueue allMsgQ){
this.allMessageQueue = allMsgQ;
        try {
            this.br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.pw = new PrintWriter(client.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientHandler(Socket client) {
        this.client = client;
        try {
            this.br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.pw = new PrintWriter(client.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void greeting() throws IOException {
        pw.println("Hej Hvad hedder du?");
        name = br.readLine();
        //pw.printf("Hej %s \n",name);
        //int ans = ir.read();
        //pw.close();
        //br.close();
        //client.close();
    }

    public void protocol() throws IOException {
        pw.printf("Hej %s, Du har nu følgende valgmuligheder: ",name);
        pw.printf("Indtast valg\n");
        String valg = "123";
        while(!valg.equalsIgnoreCase("bye")) {
            pw.println("UPPER) All chars to upper case");
            pw.println("LOWER) All chars to lower case");
            pw.println("REVERSED) All chars reversed");
            pw.println("TRANSLATE) Translate (dog, house)");
            valg = br.readLine();
            switch (valg) {
                case "GEO": geoHandler();break;
                case "ADD": pw.println(valg + "ADD");break;
                case "UPPER" : toUpperCase();break;
                case "LOWER" : toLowerCase();break;
                case "REVERSED" : reversed();break;
                case "TRANSLATE" : translate();break;
                case "ALL" : talkToAll();break;
                default: pw.println("prøv igen");
            }
        }
        client.close(); //lukker connection
    }

    private void talkToAll() throws IOException {
        pw.println("Hvad vil du sige til alle?");
        String msgToAll = br.readLine();
        allMessageQueue.add(msgToAll);

        //dispatcherr.sendThisToAll(msgToAll);
    }

    private void translate() throws IOException {
        pw.println("Translate dog eller house");
        String ans = br.readLine();
        if (ans.equals("dog")){
            ans = "hund";
        }
        if (ans.equals("house")){
            ans = "hus";
        }
        pw.println(ans);
    }

    private void reversed() throws IOException {
        pw.println("Alt bliver reversed: ");
        String ans = br.readLine();
        ans = new StringBuilder(ans).reverse().toString();
        pw.println(ans);

    }

    private void toLowerCase() throws IOException {
        pw.println("Alt bliver til lower case: ");
        String ans = br.readLine();
        ans = ans.toLowerCase();
        pw.println(ans);
    }

    private void toUpperCase() throws IOException {
        pw.println("Alt bliver til Upper case: ");
        String ans = br.readLine();
        ans = ans.toUpperCase();
        pw.println(ans);
    }

    private void geoHandler() throws IOException {
        List<String> questions = new ArrayList<>();
        questions.add("Hvad er hovedstaden i Norge?");
        questions.add("Hvad er hovedstaden i Sverige?");
        questions.add("Hvad er hovedstaden i Tyrkiet?");
        pw.println("Kan du svare på flg:?");
        Random random = new Random();
        int randomQuestion = random.nextInt(3);
        pw.println(randomQuestion);
        pw.println(questions.get(randomQuestion));

        String ans = br.readLine();

        if (randomQuestion == 0) {
            if (ans.equals("Oslo")) {
                pw.println("Korrekt!");
            } else {
                pw.println("Forkert");
            }
        }
        if (randomQuestion == 1) {
            if (ans.equals("Stockholm")) {
                pw.println("Korrekt!");
            } else {
                pw.println("Forkert");
            }
        }
        if (randomQuestion == 2) {
            if (ans.equals("Ankara")) {
                pw.println("Korrekt!");
            } else {
                pw.println("Forkert!!!!");
            }
        }

     /*   try {

            String ans = br.readLine();
            if (ans.equals("Oslo")) {
                pw.println("Fint. Hvad nu?");
            } else {
                pw.println("Dumkopff ..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    }

    @Override
    public void run() {
        try {
            greeting();
            protocol();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

class Dispatcherr {
    //List<Socket> clients;
    List<PrintWriter> clients;

    public Dispatcherr() {
        clients = new ArrayList<>();
    }

    public void addClientToList (PrintWriter pw){
        clients.add(pw);
    }

    public void sendThisToAll(String msgToAll){
        //TODO: loop through all clients and send message
        EchoServer.getClientHandlers();

    }

}

public class EchoServer {
    public static final int DEFAULT_PORT = 2345;
    public static List<ClientHandler> clientHandlers;

    public static List<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public static void main(String[] args) throws IOException {
        int port = 8188;
        clientHandlers = new ArrayList<>();
        ServerSocket ss = new ServerSocket(8189);
        if (args.length==1) {
            port=Integer.parseInt(args[0]);
        }
        try {
            while (true) {

                Socket client = ss.accept();

                //DataInputStream ds = new DataInputStream(client.getInputStream());
                //InputStreamReader ir = new InputStreamReader(client.getInputStream());
                //Scanner sc = new Scanner(ir);
                ClientHandler cl = new ClientHandler(client);
                clientHandlers.add(cl);
                cl.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}