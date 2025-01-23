import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    // Static list of all connected clients
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();  // Read username
            clientHandlers.add(this);  // Add this client handler to the list
            broadcastMessage("SERVER: " + clientUsername + " has joined the server.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    // Broadcast a message to all clients
    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // Removes the client from the clientHandlers list when they disconnect
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the server.");
    }

    // Closes everything related to the connection
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Kick a user from the server
    public void kickUser(String username) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equals(username)) {
                try {
                    clientHandler.bufferedWriter.write("SERVER: You have been kicked by an admin.");
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                    clientHandler.closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Ban a user from the server
    public void banUser(String username) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equals(username)) {
                try {
                    clientHandler.bufferedWriter.write("SERVER: You have been banned by an admin.");
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                    clientHandler.closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
