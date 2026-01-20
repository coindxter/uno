
//perfers attack cards first


import java.util.List;


public class AggressiveStrategy implements Strategy {
    @Override
    public Card chooseCard(CPUPlayer cpu) {
        List<Card> hand = cpu.getHand();
        if (hand.isEmpty()) return null;

        for (Card c : hand) {
            if (c instanceof IsAttackCard) return c; 
        }
        return hand.get(0);
    }
}
