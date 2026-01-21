import java.util.HashSet;
import java.util.List;
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

    private boolean started = false;

    private Deck deck;
    private Player player;

    private Card current;
    private Color activeColor = null;

    private final Set<Card> validPlays = new HashSet<>();
    private boolean awaitingWildColor = false;

    public boolean isStarted() {
        return started;
    }

    public void startSinglePlayer(String playerName) {
        this.deck = new Deck(); 
        this.current = deck.drawStartingCard();
        this.player = new Player(playerName);

        this.current = deck.draw();
        this.activeColor = null;
        this.awaitingWildColor = false;

        for (int i = 0; i < 7; i++) {
            player.draw(deck.draw());
        }

        started = true;
        recalcValidPlays();
    }

    public Result playIndex(int index) {
        if (!started) return err("Not started. JOIN first.");
        if (awaitingWildColor) return err("You must choose a color first: COLOR RED (etc).");

        List<Card> hand = player.getHand();
        if (index < 0 || index >= hand.size()) return err("Invalid index.");

        Card chosen = hand.get(index);

        recalcValidPlays();
        if (!validPlays.contains(chosen)) return err("That card can't be played.");

        player.removeFromHand(chosen);
        current = chosen;

        if (chosen.getColor() == Color.WILD) {
            awaitingWildColor = true;
            activeColor = null;
            recalcValidPlays(); 
            return ok("Choose a color: COLOR RED/YELLOW/GREEN/BLUE");
        } else {
            activeColor = null;
            recalcValidPlays();
        }

        if (player.getHandSize() == 0) {
            return ok("You played " + chosen + " and WON! ");
        }

        return ok("Next players turn");
    }

    public Result draw() {
        if (!started) return err("Not started. JOIN first.");
        if (awaitingWildColor) return err("You must choose a color first: COLOR RED (etc).");

        Card c = deck.draw();
        player.draw(c);
        recalcValidPlays();
        return ok("Drew " + c);
    }

    public Result chooseColor(Color color) {
        if (!started) return err("Not started. JOIN first.");
        if (!awaitingWildColor) return err("You can only choose a color after playing a WILD.");

        if (color == Color.WILD) return err("Choose RED/YELLOW/GREEN/BLUE, not WILD.");

        activeColor = color;
        awaitingWildColor = false;
        recalcValidPlays();
        return ok("Active color set to " + activeColor);
    }

    private void recalcValidPlays() {
        validPlays.clear();

        Color colorToMatch = (activeColor != null) ? activeColor : current.getColor();

        for (Card c : player.getHand()) {
            if (isPlayable(c, colorToMatch)) {
                validPlays.add(c);
            }
        }
    }

    private boolean isPlayable(Card c, Color colorToMatch) {
        if (c.getColor() == Color.WILD) return true;
        if (c.getColor() == colorToMatch) return true;
        return c.getValue() == current.getValue();
    }

    public String renderState() {
        if (!started) return "STATE Not started";
        StringBuilder sb = new StringBuilder();
        sb.append("Top Card: ").append(current);
        if (activeColor != null) sb.append(" activeColor=").append(activeColor);
        if (awaitingWildColor) sb.append(" awaitingColor=true");
        return sb.toString();
    }

    public String renderHand() {
        if (!started) return "HAND Not started";
        StringBuilder sb = new StringBuilder();
        sb.append("Your Hand").append("\n");/* .append(player.getName()).append(" cards=").append(player.getHandSize()).append("\n");*/

        List<Card> hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            sb.append(i).append(": ").append(c);
            if (validPlays.contains(c) && !awaitingWildColor) sb.append("  (playable)");
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    public String renderStateAndHand() {
        if (!started) return "ERROR Not started";
        recalcValidPlays();
        return renderState() + "\n" + renderHand();
    }

    private Result ok(String msg) {
        return new Result(true, msg, renderState(), renderHand());
    }

    private Result err(String msg) {
        return new Result(false, msg, renderState(), renderHand());
    }
}
