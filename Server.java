import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(5556)) {
            System.out.println("Server running on port 5555... Waiting for client.");
           
            try (Socket client = server.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
               
                System.out.println("Client connected: " + client.getInetAddress());


                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("Client says: " + msg);
                    out.println("Echo: " + msg);
                }
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
