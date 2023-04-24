import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    private static int port = 8000;
    private static HashSet<String> usernames = new HashSet<>();
    private static HashMap<String, PrintWriter> users = new HashMap<>();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while(true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket);

            Thread thread = new Thread(new ClientHandler(socket));
            thread.start();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String username;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("Enter your username:");
                    username = in.readLine().trim();
                    if(username == null) {
                        return;
                    }
                    synchronized(usernames) {
                        if(!usernames.contains(username)) {
                            usernames.add(username);
                            break;
                        }
                    }
                }

                out.println("Welcome to the chat room, " + username + "!");
                System.out.println("User " + username + " connected");

                synchronized(users) {
                    users.put(username, out);
                }

                while(true) {
                    String input = in.readLine();
                    if(input == null) {
                        return;
                    }

                    if(input.startsWith("@")) { // Private message
                        int spaceIndex = input.indexOf(" ");
                        if(spaceIndex != -1) {
                            String recipient = input.substring(1, spaceIndex);
                            String message = input.substring(spaceIndex + 1);
                            sendPrivateMessage(username, recipient, message);
                        }
                    } else { // Group message
                        sendGroupMessage(username, input);
                    }
                }
            } catch(IOException e) {
                System.out.println("Error handling client: " + e);
            } finally {
                if(username != null) {
                    usernames.remove(username);
                }
                if(out != null) {
                    users.remove(username);
                }
                try {
                    socket.close();
                } catch(IOException e) {
                    System.out.println("Error closing socket: " + e);
                }
            }
        }

        private void sendPrivateMessage(String sender, String recipient, String message) {
            PrintWriter pw = users.get(recipient);
            if(pw != null) {
                pw.println("@" + sender + ": " + message);
            }
        }

        private void sendGroupMessage(String sender, String message) {
            synchronized(users) {
                for(PrintWriter pw : users.values()) {
                    pw.println(sender + ": " + message);
                }
            }
        }
    }
}