import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try(Socket socket = new Socket("10.3.48.156", 5556);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)) {


            System.out.println("Connected");
            while(true) {
                String input = scanner.nextLine();
                out.println(input);
            }
        } catch (Exception e) {
            System.err.println("Client error:" + e.getMessage());
        }
    }
}
