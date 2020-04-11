/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tanni
 */
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyServer {

    private static final int DefaultPort = 5000;//if not port is provided then it will be used as server port
    private Socket s = null;
    private ServerSocket ss = null;
    private Scanner in = null;//used for receiving client input via server socket
    private PrintWriter out = null;//output stream of the client for broadcasting purpose 

    private ArrayList<String> clients = new ArrayList<>();//store active client username in a list
    private HashSet<PrintWriter> writers = new HashSet<>();//set for broadcast msg to all user 

    public MyServer(int port) throws IOException {

        ss = new ServerSocket(port);
        System.out.println("The server is running...");

        while (true) {

            try {

                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                in = new Scanner(s.getInputStream());
                out = new PrintWriter(s.getOutputStream(), true);

                new ProcesClient(s, in, out).start();

            } catch (Exception e) {
                s.close();
                System.out.println(e.getMessage());
            }
        }

    }

    private String getCurrentTime() {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String now = sdf.format(date);
        return now;
    }

    public static void main(String args[]) throws IOException {

        int port = args.length > 0 ? Integer.parseInt(args[0]) : DefaultPort;
        MyServer server = new MyServer(port);

    }

    public class ProcesClient extends Thread {

        private final Scanner in;
        private final PrintWriter out;
        private final Socket s;
        private String username;
        private String ServerMsgPrefix;
        private String ClientMsgPrefix;

        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        public ProcesClient(Socket s, Scanner in, PrintWriter out) {
            this.s = s;
            this.in = in;
            this.out = out;

        }

        @Override
        public void run() {
            try {

                //get username until a unique/not null name is found
                while (true) {

                    out.println("Please Enter UserName:");

                    username = in.nextLine();
                    if (!username.trim().equals("")) {
                        synchronized (clients) {
                            if (!clients.contains(username)) {

                                clients.add(username);
                                break;
                            }
                        }
                    }

                }
                //unique name found so add this in the writers set so that it can receive broadcast msg from server 
                writers.add(out);
                ServerMsgPrefix = getCurrentTime() + " ";
                ClientMsgPrefix = "> " + getCurrentTime() + " ";
                System.out.println(ServerMsgPrefix + "Welcome " + username);
                out.println("Welcome " + username);
                for (PrintWriter writer : writers) {
                    if (!writer.equals(out)) {
                        writer.println(ClientMsgPrefix + "Server: Welcome " + username);
                    }

                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            while (true) {
                try {

                    //broadcast all clients msg other than "AllUsers",it will be only sent to the sender
                    String msg = in.nextLine();

                    ServerMsgPrefix = getCurrentTime() + " ";
                    ClientMsgPrefix = "> " + getCurrentTime() + " ";

                    if (!msg.equals("AllUsers")) {
                        System.out.println(ServerMsgPrefix + username + ": " + msg);
                        for (PrintWriter writer : writers) {
                            if (!writer.equals(out)) {
                                writer.println(ClientMsgPrefix + username + ": " + msg);
                            }
                        }
                    }
                    //user types Bye so close this connection 
                    if (msg.equals("Bye")) {
                        System.out.println(ServerMsgPrefix + "Server: Goodbye " + username);
                        for (PrintWriter writer : writers) {

                            writer.println(ClientMsgPrefix + "Server: Goodbye " + username);
                        }
                        clients.remove(username);
                        writers.remove(out);

                        this.s.close();
                        System.out.println(ServerMsgPrefix + "Client " + this.username + " disconnected with a bye message");
                        break;
                    } else if (msg.equals("AllUsers")) {

                        out.println("> List of Connected Users:");
                        //getting active user list  
                        for (int i = 0; i < clients.size(); i++) {

                            out.println(clients.get(i));
                        }

                    }

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }

        }

    }
}
