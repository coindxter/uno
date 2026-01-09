public class UnoGame {

    private Card current;

    // ---------- STRATEGIES AS LAMBDAS ----------

    private final Strategy matchAny = (CPUPlayer p) -> {
        for (Card c : p.getHand()) {
            if (isPlayable(c)) {
                return c;
            }
        }
        return null;
    };

    private final Strategy matchColor = (CPUPlayer p) -> {
        for (Card c : p.getHand()) {
            if (c.getColor() == current.getColor() || c.getColor() == Color.WILD) {
                return c;
            }
        }
        return null;
    };

    private final Strategy matchValue = (CPUPlayer p) -> {
        for (Card c : p.getHand()) {
            if (c.getValue() == current.getValue() || c.getColor() == Color.WILD) {
                return c;
            }
        }
        return null;
    };

    // ---------- HELPER ----------

    private boolean isPlayable(Card c) {
        if (c.getColor() == Color.WILD) return true;
        return c.getColor() == current.getColor()
                || c.getValue() == current.getValue();
    }

    // ---------- TEST METHOD ----------

    public void testCPUStrategies() {

        Deck deck = new Deck();
        current = deck.draw();

        System.out.println("Starting card: " + current);

        CPUPlayer cpu1 = new CPUPlayer("CPU 1", matchAny);
        CPUPlayer cpu2 = new CPUPlayer("CPU 2", matchColor);
        CPUPlayer cpu3 = new CPUPlayer("CPU 3", matchValue);

        for (int i = 0; i < 7; i++) {
            cpu1.draw(deck.draw());
            cpu2.draw(deck.draw());
            cpu3.draw(deck.draw());
        }

        testCPU(cpu1);
        testCPU(cpu2);
        testCPU(cpu3);
    }

    private void testCPU(CPUPlayer cpu) {
        System.out.println("\n" + cpu.getName());
        cpu.showHand();
        Card played = cpu.playCard();
        System.out.println("Played: " + played);
    }
}
