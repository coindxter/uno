import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static String lastInfo = "";
    private static String lastState = "Waiting for STATE...";
    private static String lastHand  = "Waiting for HAND...";

    private static void fakeClear() {
        for (int i = 0; i < 40; i++) System.out.println();
    }

    private static void redraw() {
        fakeClear();

        if (!lastInfo.isEmpty()) {
            System.out.println(lastInfo);
            System.out.println();
        }

        System.out.println(lastState);
        System.out.println();
        System.out.println(lastHand);
    }

    public static void main(String[] args) {
        try (
            Socket socket = new Socket("localhost", 5555);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            Thread reader = new Thread(() -> {
                try {
                    String line;
                    boolean buffering = false;
                    String blockType = "";
                    StringBuilder block = new StringBuilder();

                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("BEGIN ")) {
                            buffering = true;
                            blockType = line.substring("BEGIN ".length()).trim().toUpperCase();
                            block.setLength(0);
                            continue;
                        }

                        if (line.equals("END")) {
                            String content = block.toString().trim();

                            if (blockType.equals("INFO"))  lastInfo  = content;
                            if (blockType.equals("STATE")) lastState = content;
                            if (blockType.equals("HAND"))  lastHand  = content;

                            buffering = false;
                            blockType = "";
                            redraw();
                            continue;
                        }

                        if (buffering) {
                            block.append(line).append("\n");
                        } else {
                            System.out.println(line);
                        }
                    }
                } catch (IOException ignored) {}
            });

            reader.setDaemon(true);
            reader.start();

            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                out.println(input);
                if (input.equalsIgnoreCase("QUIT")) break;
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
