package serverTB;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


class ClientHandler extends Thread{
    Socket client;
    BufferedReader br;
    PrintWriter pw;
    String  name;
    Dispatcherr dispatcherr;
    BlockingQueue<String> allMessageQueue;

    public BlockingQueue<String> getAllMessageQueue() {
        return allMessageQueue;
    }

    @Override
    public String toString() {
        return "ClientHandler{" +
                "client=" + client +
                ", br=" + br +
                ", pw=" + pw +
                ", name='" + name + '\'' +
                ", dispatcherr=" + dispatcherr +
                ", allMessageQueue=" + allMessageQueue +
                '}';
    }

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

    public ClientHandler (PrintWriter pw, BufferedReader br, BlockingQueue allMsgQ, String name){
this.allMessageQueue = allMsgQ;
    this.pw = pw;
    this.br = br;
    this.name = name;
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
                case "ONE" : talkToOnePerson();break;
                default: pw.println("prøv igen");
            }
        }
        client.close(); //lukker connection
    }

    private void talkToOnePerson() throws IOException {
        pw.println("Hvem vil du sende en besked til?");
        String msgToOne = br.readLine();


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

class Dispatcherr extends Thread {
    List<Socket> clients;
    BlockingQueue<PrintWriter> allWriters;
    BlockingQueue<String> allMsg;

    public Dispatcherr(BlockingQueue<String> allMsg) {
        this.allMsg = allMsg;
        this.allWriters = new ArrayBlockingQueue<>(200);
    }

    public void addWriterToList (PrintWriter pw){
        allWriters.add(pw);
    }

    @Override
    public void run() {
        //tjek køen og send besked til alle
        while (true){
            try {
                String msg = allMsg.take();
                sendMessageToAll(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageToAll(String msg) {
        for (PrintWriter pw : allWriters) {
            pw.println(msg);
        }
    }
}

public class EchoServer {
    public static final int DEFAULT_PORT = 2345;
    public static List<ClientHandler> clientHandlers;
    public static BlockingQueue<String> allMsg = new ArrayBlockingQueue<>(250);
    public static String name;

    public static List<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public static void main(String[] args) throws IOException {
        int port = 8188;

        clientHandlers = new ArrayList<>();
        ServerSocket ss = new ServerSocket(8189);
        Dispatcherr dispatcherr = new Dispatcherr(allMsg);
        dispatcherr.start();
        if (args.length==1) {
            port=Integer.parseInt(args[0]);
        }
        try {
            while (true) {
                System.out.println("Waiting for client ");

                Socket client = ss.accept();

                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter pw = new PrintWriter(client.getOutputStream(),true);
                dispatcherr.addWriterToList(pw);

                //DataInputStream ds = new DataInputStream(client.getInputStream());
                //InputStreamReader ir = new InputStreamReader(client.getInputStream());
                //Scanner sc = new Scanner(ir);
                ClientHandler cl = new ClientHandler(pw,br,allMsg,name);
                clientHandlers.add(cl);
                pw.println(clientHandlers.get(0));
                pw.println(cl.getAllMessageQueue());
                cl.start();
                //System.out.println(cl.getName());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}