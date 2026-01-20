import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class UnoGame {

    private Card current;
    private Color activeColor = null;

    private final Set<Card> validPlays = new HashSet<>();
    private final List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;

    private final Scanner scanner = new Scanner(System.in);

    public void addPlayer(Player player) {
        players.add(player);
    }

    private Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private void recalcValidPlays(Player player) {
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

    public void start() {
        Deck deck = new Deck();

        current = deck.draw();
        activeColor = null;

        System.out.println("Starting card: " + current);

        addPlayer(new Player("Owen"));

        for (Player p : players) {
            for (int i = 0; i < 7; i++) {
                p.draw(deck.draw());
            }
        }

        while (true) {
            Player player = getCurrentPlayer();

            System.out.println("\n--- Your Turn ---");
            System.out.println("Current card: " + current);
            if (activeColor != null) {
                System.out.println("Active color: " + activeColor);
            }

            player.showHand();
            recalcValidPlays(player);

            if (validPlays.isEmpty()) {
                System.out.println("No valid plays. Drawing a card...");
                player.draw(deck.draw());
                recalcValidPlays(player);
            }

            playHumanTurn(player, deck);

            if (player.getHandSize() == 0) {
                System.out.println("You Won");
                break;
            }

            nextPlayer();
        }
    }

    private void playHumanTurn(Player player, Deck deck) {
        while (true) {
            System.out.println("\nChoose a card index to play (-1 to draw):");

            List<Card> hand = player.getHand();
            for (int i = 0; i < hand.size(); i++) {
                System.out.println(i + ": " + hand.get(i));
            }

            int choice = scanner.nextInt();

            if (choice == -1) {
                player.draw(deck.draw());
                return;
            }

            if (choice < 0 || choice >= hand.size()) {
                System.out.println("Invalid index.");
                continue;
            }

            Card chosen = hand.get(choice);

            if (!validPlays.contains(chosen)) {
                System.out.println("That card can't be played.");
                continue;
            }

            player.removeFromHand(chosen);
            current = chosen;

            if (chosen.getColor() == Color.WILD) {
                activeColor = chooseColor();
            } else {
                activeColor = null;
            }

            System.out.println("You played: " + chosen);
            return;
        }
    }

    private Color chooseColor() {
        System.out.println("Choose a color:");
        System.out.println("0: RED");
        System.out.println("1: YELLOW");
        System.out.println("2: GREEN");
        System.out.println("3: BLUE");

        int choice = scanner.nextInt();

        return switch (choice) {
            case 0 -> Color.RED;
            case 1 -> Color.YELLOW;
            case 2 -> Color.GREEN;
            case 3 -> Color.BLUE;
            default -> Color.RED;
        };
    }
}
