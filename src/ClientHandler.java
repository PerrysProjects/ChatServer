import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class ClientHandler implements Runnable {
    private final Socket socket;
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

            while(true) {
                out.println("Enter your username:");
                username = in.readLine().trim();
                synchronized(Main.getUsernames()) {
                    if(!Main.getUsernames().contains(username)) {
                        Main.getUsernames().add(username);
                        break;
                    }
                }
            }

            out.println("Welcome to the chat room, " + username + "!");
            Main.println("User " + username + " connected");

            synchronized(Main.getUsers()) {
                Main.getUsers().put(username, out);
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
            Main.println("Error handling client: " + e);
        } finally {
            if(username != null) {
                Main.getUsernames().remove(username);
            }
            if(out != null) {
                Main.getUsers().remove(username);
            }
            try {
                socket.close();
            } catch(IOException e) {
                Main.println("Error closing socket: " + e);
            }
        }
    }

    private void sendPrivateMessage(String sender, String recipient, String message) {
        PrintWriter pw = Main.getUsers().get(recipient);
        PrintWriter pwSender = Main.getUsers().get(sender);
        if(pw != null) {
            String msg = "@" + sender + ": " + message;
            pw.println(msg);
            if(!Objects.equals(sender, recipient)) {
                pwSender.println(msg);
            }
            Main.println(msg);
        }
    }

    private void sendGroupMessage(String sender, String message) {
        synchronized(Main.getUsers()) {
            for(PrintWriter pw : Main.getUsers().values()) {
                String msg = sender + ": " + message;
                pw.println(msg);
                Main.println(msg);
            }
        }
    }
}
