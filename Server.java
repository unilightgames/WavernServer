import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        // Start a separate thread to listen for admin commands
        new Thread(this::listenForCommands).start();

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New login: " + socket.getRemoteSocketAddress());
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to handle admin commands
    private void listenForCommands() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (command.startsWith("kick ")) {
                String username = command.split(" ")[1];
                kickUser(username);
            } else if (command.startsWith("ban ")) {
                String username = command.split(" ")[1];
                banUser(username);
            } else if (command.equals("shutdown")) {
                shutdownServer();
                break;
            } else {
                System.out.println("Unknown command. Try 'kick <username>', 'ban <username>', or 'shutdown'.");
            }
        }
    }

    // Kick a user by their username
    public void kickUser(String username) {
        for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
            clientHandler.kickUser(username);
        }
    }

    // Ban a user by their username
    public void banUser(String username) {
        for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
            clientHandler.banUser(username);
        }
    }

    // Shutdown the server
    public void shutdownServer() {
        try {
            System.out.println("Shutting down the server...");
            closeServerSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(27546);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
