import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 5555;
    private static final UnoGameEngine game = new UnoGameEngine(); 

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server running on port " + PORT);

            while (true) {
                Socket client = server.accept();
                new Thread(() -> handleClient(client)).start();
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket client) {
        try (
            Socket c = client;
            BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
            PrintWriter out = new PrintWriter(c.getOutputStream(), true)
        ) {
            out.println("JOIN <name> | START (start the game) | HAND | STATE | PLAY <i> | DRAW | COLOR <c> | QUIT");
            out.println("Welcome");

            UnoGameEngine.PlayerSession session = null;

            String msg;
            while ((msg = in.readLine()) != null) {
                msg = msg.trim();
                if (msg.isEmpty()) continue;

                String response;
                synchronized (game) {
                    response = handle(msg, out, session);
                }

                if (response.startsWith("SESSION ")) {
                    String[] lines = response.split("\n", 2);
                    String sessionId = lines[0].substring("SESSION ".length()).trim();
                    session = game.getSession(sessionId);
                    response = (lines.length > 1) ? lines[1] : "";
                }

                out.println("BEGIN");
                for (String line : response.split("\n")) out.println(line);
                out.println("END");

                if (msg.equalsIgnoreCase("QUIT")) break;
            }
        } catch (Exception ignored) {}
    }

    private static String handle(String msg, PrintWriter out, UnoGameEngine.PlayerSession session) {
        String[] parts = msg.split("\\s+");
        String cmd = parts[0].toUpperCase();

        return switch (cmd) {
            case "JOIN"  -> join(msg, out);
            case "START" -> (session == null) ? "ERROR JOIN first" : game.startGame().toWireString();
            case "HAND"  -> (session == null) ? "ERROR JOIN first" : game.renderHand(session);
            case "STATE" -> (session == null) ? "ERROR JOIN first" : game.renderState(session);
            case "PLAY"  -> (session == null) ? "ERROR JOIN first" : play(session, parts).toWireString();
            case "DRAW"  -> (session == null) ? "ERROR JOIN first" : game.draw(session).toWireString();
            case "COLOR" -> (session == null) ? "ERROR JOIN first" : color(session, parts).toWireString();
            case "QUIT"  -> "OK Bye";
            default      -> "ERROR Unknown command";
        };
    }

    private static String join(String msg, PrintWriter out) {
        int sp = msg.indexOf(' ');
        if (sp < 0) return "ERROR Usage: JOIN <name>";

        String name = msg.substring(sp + 1).trim();
        if (name.isEmpty()) return "ERROR Name cannot be empty";

        UnoGameEngine.JoinResult jr = game.join(name, out);
        if (!jr.result.ok) return jr.result.toWireString();

        return "SESSION " + jr.sessionId + "\n" + jr.result.toWireString();
    }

    private static UnoGameEngine.Result play(UnoGameEngine.PlayerSession session, String[] parts) {
        if (parts.length != 2) {
            return new UnoGameEngine.Result(false, "Usage: PLAY <index>", game.renderState(session), game.renderHand(session));
        }

        int idx;
        try {
            idx = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return new UnoGameEngine.Result(false, "Not a number", game.renderState(session), game.renderHand(session));
        }

        return game.playIndex(session, idx);
    }

    private static UnoGameEngine.Result color(UnoGameEngine.PlayerSession session, String[] parts) {
        if (parts.length != 2) {
            return new UnoGameEngine.Result(false, "Usage: COLOR <RED|YELLOW|GREEN|BLUE>", game.renderState(session), game.renderHand(session));
        }

        try {
            Color c = Color.valueOf(parts[1].toUpperCase());
            return game.chooseColor(session, c);
        } catch (Exception e) {
            return new UnoGameEngine.Result(false, "Not a valid color", game.renderState(session), game.renderHand(session));
        }
    }
}
