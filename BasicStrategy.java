//perfers number cards, saves wilds

import java.util.List;

public class BasicStrategy implements Strategy {
    @Override
    public Card chooseCard(CPUPlayer cpu) {
        List<Card> hand = cpu.getHand();
        if (hand.isEmpty()) return null;

        for (Card c : hand) {
            if (c instanceof NumberCard) return c;
        }
        return hand.get(0);
    }
}
