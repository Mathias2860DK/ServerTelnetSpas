package serverTB;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        String outToServer = "ALL) send message to all \n";
        String outToServer1 = "ONE) send private message to one \n";
        String outToServer2 = "Error try 'ALL' or 'ONE' \n";

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String msg = "Enter username for login (your name) \n";
        outputStream.write(msg.getBytes());
        handleLogin(outputStream);
        while(!line.equalsIgnoreCase("logoff")) {
          outputStream.write(outToServer.getBytes());
          outputStream.write(outToServer1.getBytes());

            line = reader.readLine();
            switch (line) {
                case "ALL" : talkToAll();break;
                case "ONE" : talkToOnePerson();break;
                default: outputStream.write(outToServer2.getBytes());
            }
        }
        clientSocket.close(); //lukker connection
    }

    private void talkToOnePerson() throws IOException {
        List<ServerWorker> serverWorkerList = server.getWorkerList();

        InputStream inputStream = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String whoToMsg = "Who do you want to send a message to? \n"
                + "1) for pm someone \n" +
                "2) send message to all \n";
        outputStream.write(whoToMsg.getBytes());
        String choice = reader.readLine();
        while (choice != null) {
            switch (choice) {
                case "1":
                    String outputWrite = "Who would you like to send the text to? \n";
                    outputStream.write(outputWrite.getBytes());
                    String sendTo = reader.readLine();
                    if (sendTo.equals("back")) {
                        choice = null;
                        break;
                    }
                    String text = "";
                    while (!text.equals("back")) {
                        text = reader.readLine();
                        for (ServerWorker worker : serverWorkerList) {

                            if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                                String outMsg = login + " " + text + "\n";
                                worker.send(outMsg);
                            }
                        }
                    }


            }
        }
    }

    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        } else {
            String error = "You need to login before sending messages \n";
            outputStream.write(error.getBytes());
        }
    }

    private void talkToAll() {
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream) throws IOException {
        Scanner scanner = new Scanner(clientSocket.getInputStream()); //could mabye be optimized
        String login = scanner.nextLine();
        if (login.length() < 25) {
            this.login = login;
            String message = "User logged in succesfully: " + login + "\n";
            System.out.print(message);
            outputStream.write(message.getBytes());
        }

        //List<ServerWorker> workerList = server.getWorkerList();
    }
}
