import java.util.ArrayList;
import java.util.Collections;

public final class Deck {
    private final ArrayList<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        buildDeck();
        shuffle();
    }

    private void buildDeck() {
        Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};
        Value[] numbers = {
                Value.ZERO, Value.ONE, Value.TWO, Value.THREE, Value.FOUR,
                Value.FIVE, Value.SIX, Value.SEVEN, Value.EIGHT, Value.NINE
        };

        for (Color c : colors) {
            for (Value v : numbers) {
                cards.add(new NumberCard(c, v));
            }

            cards.add(new SkipCard(c));
            cards.add(new ReverseCard(c));
            cards.add(new Draw2Card(c));
        }

        for (int i = 0; i < 4; i++) {
            cards.add(new WildCard());
            cards.add(new WildDraw4Card());
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public Card draw() {
        if (cards.isEmpty()) return null;
        return cards.remove(cards.size() - 1);
    }

    public int size() {
        return cards.size();
    }
}
