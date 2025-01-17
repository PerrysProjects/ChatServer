import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    private static int port;
    private static final HashSet<String> usernames = new HashSet<>();
    private static final HashMap<String, PrintWriter> users = new HashMap<>();

    public static void main(String[] args) {
        if(!checkArgs(args)) {
            println("Arguments incorrect!");
            System.exit(0);
        }
        port = Integer.parseInt(getArg(args, "port"));

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException e) {
            println(e.getMessage());
        }
        println("Server started on port " + port);

        while(true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch(IOException e) {
                println(e.getMessage());
            }
            println("New client connected: " + socket);

            Thread thread = new Thread(new ClientHandler(socket));
            thread.start();
        }
    }

    private static boolean checkArgs(String[] args) {
        boolean exists = false;
        for(String arg : args) {
            if(arg.indexOf("-") == 0 && arg.contains(":")) {
                exists = true;
            } else {
                exists = false;
                break;
            }
        }
        return exists;
    }

    private static String getArg(String[] args, String arg) {
        String argument = "";
        for(String s : args) {
            String[] splitArg = s.split(":");
            if(splitArg[0].equalsIgnoreCase("-" + arg)) {
                argument = splitArg[1];
            } else {
                argument = null;
                break;
            }
        }
        return argument;
    }

    public static void println(String text) {
        System.out.println(text);
        Log.log(text);
    }

    public static int getPort() {
        return port;
    }

    public static HashMap<String, PrintWriter> getUsers() {
        return users;
    }

    public static HashSet<String> getUsernames() {
        return usernames;
    }
}