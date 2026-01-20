import java.util.ArrayList;

public class Player {

    private final String name;
    private final ArrayList<Card> hand;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getHandSize() {
        return hand.size();
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void draw(Card card) {
        if (card != null) {
            hand.add(card);
        }
    }

    public boolean removeFromHand(Card card) {
        return hand.remove(card);
    }


    public void showHand() {
        System.out.println(name + "'s hand:");
        for (Card c : hand) {
            System.out.println("  " + c);
        }
    }
}
