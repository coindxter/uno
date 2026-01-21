import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 5555;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("UNO Server running on port " + PORT + "... Waiting for client.");

            try (Socket client = server.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                System.out.println("Client connected: " + client.getInetAddress());

                UnoGameEngine game = new UnoGameEngine();
                sendWelcome(out);

                String msg;
                while ((msg = in.readLine()) != null) {
                    msg = msg.trim();
                    if (msg.isEmpty()) continue;

                    sendLines(out, handleMessage(game, msg));

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

    private static void sendWelcome(PrintWriter out) {
        out.println("WELCOME UNO");
        out.println("First please join. Type: JOIN <yourName>");
        out.println("Commands: HAND | STATE | PLAY <index> | DRAW | COLOR <RED|YELLOW|GREEN|BLUE> | QUIT");
    }

    private static void sendLines(PrintWriter out, String response) {
        for (String line : response.split("\n")) {
            out.println(line);
        }
    }

    private static String handleMessage(UnoGameEngine game, String msg) {
        String[] parts = msg.split("\\s+");
        String cmd = parts[0].toUpperCase();

        try {
            return switch (cmd) {
                case "JOIN" -> join(game, msg, parts);
                case "STATE" -> requireStarted(game, game.renderState());
                case "HAND"  -> requireStarted(game, game.renderHand());
                case "PLAY"  -> requireStarted(game, play(game, parts));
                case "DRAW"  -> requireStarted(game, game.draw().toWireString());
                case "COLOR" -> requireStarted(game, chooseColor(game, parts));
                case "QUIT"  -> "INFO Quitting...";
                default      -> "ERROR Unknown command: " + cmd;
            };
        } catch (IllegalArgumentException e) {
            return "ERROR " + e.getMessage();
        } catch (Exception e) {
            return "ERROR Server exception: " + e.getMessage();
        }
    }

    private static String requireStarted(UnoGameEngine game, String ok) {
        if (game.isStarted()) {
            return ok;
        } else {
            return "ERROR You must JOIN first";
        }
    }

    private static String join(UnoGameEngine game, String msg, String[] parts) {
        if (parts.length < 2) {
            return "ERROR Usage: JOIN <name>";
        }

        if (game.isStarted()) {
            return "ERROR Game already started";
        }

        String name = "";

        int spaceIndex = msg.indexOf(" ");
        if (spaceIndex != -1) {
            name = msg.substring(spaceIndex + 1);
            name = name.trim();
        }

        if (name.length() == 0) {
            return "ERROR Name cannot be empty";
        }

        game.startSinglePlayer(name);

        return game.renderStateAndHand();
    }

    private static String play(UnoGameEngine game, String[] parts) {
        if (parts.length != 2) {
            return "ERROR Usage: PLAY <index>";
        }

        int index;

        try {
            index = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return "ERROR That is not a number";
        }

        return game.playIndex(index).toWireString();
    }

    private static String chooseColor(UnoGameEngine game, String[] parts) {
        if (parts.length != 2) {
            return "ERROR Usage: COLOR <RED|YELLOW|GREEN|BLUE>";
        }

        String colorStr = parts[1].toUpperCase();
        Color color;

        try {
            color = Color.valueOf(colorStr);
        } catch (Exception e) {
            return "ERROR Not a valid color";
        }

        return game.chooseColor(color).toWireString();
    }

}
