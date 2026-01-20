import java.util.HashSet;
import java.util.Set;

public class UnoGame {

    private Card current;

    private final Set<Card> validPlays = new HashSet<>();

    private Color activeColor = null;


    private final Strategy matchAny = (CPUPlayer p) -> {
        for (Card c : validPlays) {
            return c; 
        }
        return null;
    };

    private final Strategy matchColor = (CPUPlayer p) -> {
        Color colorToMatch = (activeColor != null) ? activeColor : current.getColor();

        for (Card c : validPlays) {
            if (c.getColor() == colorToMatch || c.getColor() == Color.WILD) {
                return c;
            }
        }
        return null;
    };

    private final Strategy matchValue = (CPUPlayer p) -> {
        for (Card c : validPlays) {
            if (c.getValue() == current.getValue() || c.getColor() == Color.WILD) {
                return c;
            }
        }
        return null;
    };


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

    public void testCPUStrategies() {
        Deck deck = new Deck();
        current = deck.draw();
        activeColor = null; 

        System.out.println("Starting card: " + current);

        CPUPlayer cpu1 = new CPUPlayer("CPU 1", Difficulty.EASY);
        CPUPlayer cpu2 = new CPUPlayer("CPU 2", Difficulty.MEDIUM);
        CPUPlayer cpu3 = new CPUPlayer("CPU 3", Difficulty.HARD);
        CPUPlayer cpu4 = new CPUPlayer("CPU 4", Difficulty.EASY);


        for (int i = 0; i < 7; i++) {
            cpu1.draw(deck.draw());
            cpu2.draw(deck.draw());
            cpu3.draw(deck.draw());
            cpu4.draw(deck.draw());
        }

        testCPU(cpu1, deck);
        testCPU(cpu2, deck);
        testCPU(cpu3, deck);
        testCPU(cpu4, deck);
    }

    private void testCPU(CPUPlayer cpu, Deck deck) {
        System.out.println("\n" + cpu.getName());
        cpu.showHand();

        recalcValidPlays(cpu);

        System.out.println("Valid plays: " + validPlays);

        if (validPlays.isEmpty()) {
            System.out.println("No valid plays. Drawing a card...");
            cpu.draw(deck.draw());

            recalcValidPlays(cpu);
            System.out.println("Valid plays after draw: " + validPlays);
        }

        Card played = cpu.playCard();

        if (played == null) {
            System.out.println("Played: null (no move)");
            return;
        }

        System.out.println("Played: " + played);

        current = played;

        if (played.getColor() == Color.WILD) {
            activeColor = Color.RED;
            System.out.println("Wild played — activeColor set to " + activeColor);
        } else {
            activeColor = null;
        }
    }
}
