
//easy

import java.util.List;
import java.util.Random;

public class RandomStrategy implements Strategy {
    private final Random rand = new Random();

    @Override
    public Card chooseCard(CPUPlayer cpu) {
        List<Card> hand = cpu.getHand();
        if (hand.isEmpty()) return null;
        return hand.get(rand.nextInt(hand.size()));
    }
}
