public class CPUPlayer extends Player {

    private Strategy strategy;

    public CPUPlayer(String name, Strategy s) {
        super(name);
        this.strategy = s;
    }

    public void setStrategy(Strategy s) {
        this.strategy = s;
    }

    public Card playCard() {
        Card chosen = strategy.chooseCard(this);
        if (chosen != null) {
            getHand().remove(chosen);
        }
        return chosen;
    }
}
