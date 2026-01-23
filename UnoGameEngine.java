import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnoGameEngine {

    public static class Result {
        public final boolean ok;
        public final String message;
        public final String state;
        public final String hand;

        public Result(boolean ok, String message, String state, String hand) {
            this.ok = ok;
            this.message = message;
            this.state = state;
            this.hand = hand;
        }

        public String toWireString() {
            StringBuilder sb = new StringBuilder();
            sb.append(ok ? "OK " : "ERROR ").append(message).append("\n");
            sb.append(state).append("\n");
            sb.append(hand);
            return sb.toString();
        }
    }

    public static class PlayerSession {
        public final String id;
        public final Player player;
        public final PrintWriter out;

        private PlayerSession(String id, Player player, PrintWriter out) {
            this.id = id;
            this.player = player;
            this.out = out;
        }
    }

    public static class JoinResult {
        public final String sessionId;
        public final Result result;

        public JoinResult(String sessionId, Result result) {
            this.sessionId = sessionId;
            this.result = result;
        }
    }

    private boolean started = false;

    private Deck deck;
    private final List<Player> players = new ArrayList<>();
    private int turnIndex = 0;

    private Card current;
    private Color activeColor = null;

    private final Set<Card> validPlays = new HashSet<>();

    private int nextSession = 1;
    private final Map<String, PlayerSession> sessions = new HashMap<>();

    private int direction = 1;     
    private int pendingDraw = 0;   
    private boolean skipExtra = false; 

    public boolean isStarted() {
        return started;
    }

    public PlayerSession getSession(String id) {
        return sessions.get(id);
    }

    public JoinResult join(String playerName, PrintWriter out) {
        if (started) return new JoinResult(null, err(null, "Game already started"));
        if (playerName == null || playerName.trim().isEmpty()) return new JoinResult(null, err(null, "Name cannot be empty"));

        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(playerName)) {
                return new JoinResult(null, err(null, "Name already taken"));
            }
        }

        Player p = new Player(playerName);
        players.add(p);

        String sessionId = "S" + (nextSession++);
        PlayerSession session = new PlayerSession(sessionId, p, out);
        sessions.put(sessionId, session);

        broadcastPublic("INFO " + playerName + " joined. Players=" + players.size());
        return new JoinResult(sessionId, ok(session, "Joined as " + playerName + ". Type START when ready."));
    }

    public Result startGame() {
        if (started) return err(null, "Already started");
        if (players.size() < 2) return err(null, "Need at least 2 players to start");

        this.deck = new Deck();

        this.current = deck.drawStartingCard();
        this.current = deck.draw();
        this.activeColor = null;

        for (Player p : players) {
            p.awaitingWildColor = false;
            p.pendingWild = null;
            p.getHand().clear();
            for (int i = 0; i < 7; i++) p.draw(deck.draw());
        }

        started = true;
        turnIndex = 0;
        direction = 1;
        pendingDraw = 0;
        skipExtra = false;

        broadcastPublic("INFO Game started. Turn: " + currentPlayer().getName());
        return ok(findSession(currentPlayer()), "Game started!");
    }

    public Result playIndex(PlayerSession session, int index) {
        Player p = session.player;

        if (!started) return err(session, "Not started. JOIN first.");
        if (!isPlayersTurn(p)) return err(session, "Not your turn. It's " + currentPlayer().getName() + "'s turn.");
        if (p.awaitingWildColor) return err(session, "You must choose a color first: COLOR RED (etc).");

        List<Card> hand = p.getHand();
        if (index < 0 || index >= hand.size()) return err(session, "Invalid index.");

        recalcValidPlays(p);
        Card chosen = hand.get(index);
        if (!validPlays.contains(chosen)) return err(session, "That card can't be played.");

        if (chosen.getColor() == Color.WILD) {
            p.awaitingWildColor = true;
            p.pendingWild = chosen;
            activeColor = null;
            recalcValidPlays(p);

            broadcastPublic("INFO " + p.getName() + " played a WILD (choosing color...)");
            return ok(session, "Choose a color: COLOR RED/YELLOW/GREEN/BLUE");
        }

        p.removeFromHand(chosen);
        current = chosen;
        activeColor = null;

        applyAction(chosen);

        if (p.getHandSize() == 0) {
            broadcastPublic("INFO " + p.getName() + " WON!");
            return ok(session, "You played " + chosen + " and WON!");
        }

        nextTurn();                
        applyPendingDrawIfAny();    
        if (skipExtra) nextTurn();  

        broadcastPublic("INFO " + p.getName() + " played " + chosen);
        return ok(session, "Played " + chosen);
    }

    public Result draw(PlayerSession session) {
        Player p = session.player;

        if (!started) return err(session, "Not started. JOIN first.");
        if (!isPlayersTurn(p)) return err(session, "Not your turn. It's " + currentPlayer().getName() + "'s turn.");
        if (p.awaitingWildColor) return err(session, "You must choose a color first: COLOR RED (etc).");

        Card c = deck.draw();
        p.draw(c);
        recalcValidPlays(p);

        nextTurn();
        broadcastPublic("INFO " + p.getName() + " drew a card");
        return ok(session, "Drew " + c);
    }

    public Result chooseColor(PlayerSession session, Color color) {
        Player p = session.player;

        if (!started) return err(session, "Not started. JOIN first.");
        if (!isPlayersTurn(p)) return err(session, "Not your turn. It's " + currentPlayer().getName() + "'s turn.");
        if (!p.awaitingWildColor) return err(session, "You can only choose a color after playing a WILD.");
        if (color == Color.WILD) return err(session, "Choose RED/YELLOW/GREEN/BLUE, not WILD.");
        if (p.pendingWild == null) return err(session, "No pending WILD to complete.");

        Card wild = p.pendingWild;
        p.removeFromHand(wild);
        current = wild;
        p.pendingWild = null;

        activeColor = color;
        p.awaitingWildColor = false;

        if (wild instanceof WildDraw4Card) {
            pendingDraw += 4;
        }

        if (p.getHandSize() == 0) {
            broadcastPublic("INFO " + p.getName() + " WON!");
            return ok(session, "Active color set to " + activeColor + ". You WON!");
        }

        nextTurn();
        applyPendingDrawIfAny();

        broadcastPublic("INFO " + p.getName() + " chose color " + color);
        return ok(session, "Active color set to " + activeColor);
    }

    public String renderState(PlayerSession viewer) {
        if (!started) return "STATE Not started";

        StringBuilder sb = new StringBuilder();
        sb.append("Top Card: ").append(current).append("\n");
        if (activeColor != null) sb.append("Active Color: ").append(activeColor).append("\n");
        sb.append("Turn: ").append(currentPlayer().getName()).append("\n");

        sb.append("Players: ");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            sb.append(p.getName()).append("(").append(p.getHandSize()).append(")");
            if (i < players.size() - 1) sb.append(", ");
        }

        return sb.toString().trim();
    }

    public String renderHand(PlayerSession session) {
        if (!started) return "HAND Not started";
        if (session == null) return "HAND Unknown player";

        Player p = session.player;
        recalcValidPlays(p);

        StringBuilder sb = new StringBuilder();
        sb.append("Your Hand").append("\n");

        List<Card> hand = p.getHand();
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            sb.append(i).append(": ").append(c);
            if (validPlays.contains(c) && !p.awaitingWildColor) sb.append("  (playable)");
            sb.append("\n");
        }

        if (p.awaitingWildColor) {
            sb.append("INFO Waiting for color: COLOR RED/YELLOW/GREEN/BLUE").append("\n");
        }

        return sb.toString().trim();
    }

    private Player currentPlayer() {
        return players.get(turnIndex);
    }

    private boolean isPlayersTurn(Player p) {
        return p == currentPlayer();
    }

    private void nextTurn() {
        int n = players.size();
        turnIndex = (turnIndex + direction) % n;
        if (turnIndex < 0) turnIndex += n;
    }

    private void applyAction(Card chosen) {
        skipExtra = false;

        if (chosen instanceof SkipCard) {
            skipExtra = true;
            return;
        }

        if (chosen instanceof ReverseCard) {
            direction *= -1;

            if (players.size() == 2) skipExtra = true;
            return;
        }

        if (chosen instanceof Draw2Card) {
            pendingDraw += 2;
        }
    }

    private void applyPendingDrawIfAny() {
        if (pendingDraw <= 0) return;

        Player victim = currentPlayer();
        for (int i = 0; i < pendingDraw; i++) {
            victim.draw(deck.draw());
        }
        pendingDraw = 0;

        nextTurn();
    }

    private PlayerSession findSession(Player p) {
        for (PlayerSession s : sessions.values()) {
            if (s.player == p) return s;
        }
        return null;
    }

    private void sendBlock(PlayerSession s, String type, String content) {
        s.out.println("BEGIN " + type);
        for (String line : content.split("\n")) s.out.println(line);
        s.out.println("END");
    }

    private void broadcastPublic(String infoMsg) {
        String publicState = renderState(null);

        for (PlayerSession s : sessions.values()) {
            sendBlock(s, "INFO", infoMsg);
            sendBlock(s, "STATE", publicState);
            sendBlock(s, "HAND", renderHand(s));
        }
    }

    private void recalcValidPlays(Player p) {
        validPlays.clear();
        Color colorToMatch = (activeColor != null) ? activeColor : current.getColor();

        for (Card c : p.getHand()) {
            if (isPlayable(c, colorToMatch)) validPlays.add(c);
        }
    }

    private boolean isPlayable(Card c, Color colorToMatch) {
        if (c.getColor() == Color.WILD) return true;
        if (c.getColor() == colorToMatch) return true;
        return c.getValue() == current.getValue();
    }

    private Result ok(PlayerSession session, String msg) {
        return new Result(true, msg, renderState(session), renderHand(session));
    }

    private Result err(PlayerSession session, String msg) {
        return new Result(false, msg, renderState(session), (session == null ? "HAND N/A" : renderHand(session)));
    }
}
