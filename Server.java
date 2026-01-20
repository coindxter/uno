import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(5555)) {
            System.out.println("UNO Server running on port 5555... Waiting for client.");

            try (Socket client = server.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                System.out.println("Client connected: " + client.getInetAddress());

                UnoGameEngine game = new UnoGameEngine();

                out.println("WELCOME UNO");
                out.println("Type: JOIN <yourName>");
                out.println("Commands: HAND | STATE | PLAY <index> | DRAW | COLOR <RED|YELLOW|GREEN|BLUE> | QUIT");

                String msg;
                while ((msg = in.readLine()) != null) {
                    msg = msg.trim();
                    if (msg.isEmpty()) continue;

                    String response = handleMessage(game, msg);
                    for (String line : response.split("\n")) {
                        out.println(line);
                    }

                    if (msg.equalsIgnoreCase("QUIT")) {
                        out.println("BYE");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String handleMessage(UnoGameEngine game, String msg) {
        String[] parts = msg.split("\\s+");
        String cmd = parts[0].toUpperCase();

        try {
            return switch (cmd) {
                case "JOIN" -> {
                    if (parts.length < 2) yield "ERROR Usage: JOIN <name>";
                    if (game.isStarted()) yield "ERROR Game already started";
                    String name = msg.substring(msg.indexOf(' ') + 1).trim();
                    game.startSinglePlayer(name);
                    yield game.renderStateAndHand();
                }
                case "STATE" -> {
                    if (!game.isStarted()) yield "ERROR You must JOIN first";
                    yield game.renderState();
                }
                case "HAND" -> {
                    if (!game.isStarted()) yield "ERROR You must JOIN first";
                    yield game.renderHand();
                }
                case "PLAY" -> {
                    if (!game.isStarted()) yield "ERROR You must JOIN first";
                    if (parts.length != 2) yield "ERROR Usage: PLAY <index>";
                    int idx = Integer.parseInt(parts[1]);
                    UnoGameEngine.Result r = game.playIndex(idx);
                    yield r.toWireString();
                }
                case "DRAW" -> {
                    if (!game.isStarted()) yield "ERROR You must JOIN first";
                    UnoGameEngine.Result r = game.draw();
                    yield r.toWireString();
                }
                case "COLOR" -> {
                    if (!game.isStarted()) yield "ERROR You must JOIN first";
                    if (parts.length != 2) yield "ERROR Usage: COLOR <RED|YELLOW|GREEN|BLUE>";
                    Color c = Color.valueOf(parts[1].toUpperCase());
                    UnoGameEngine.Result r = game.chooseColor(c);
                    yield r.toWireString();
                }
                case "QUIT" -> "INFO Quitting...";
                default -> "ERROR Unknown command: " + cmd;
            };
        } catch (IllegalArgumentException e) {
            return "ERROR " + e.getMessage();
        } catch (Exception e) {
            return "ERROR Server exception: " + e.getMessage();
        }
    }
}
