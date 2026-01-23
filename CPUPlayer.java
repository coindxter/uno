import java.util.EnumMap;
import java.util.Map;

public class CPUPlayer extends Player {

    private final Strategy strategy;
    private final Difficulty difficulty;

    private static final Map<Difficulty, Strategy> STRATEGY_MAP =
            new EnumMap<>(Difficulty.class);

    static {
        STRATEGY_MAP.put(Difficulty.EASY, (CPUPlayer p) -> {
            for (Card c : p.getHand()) return c;
            return null;
        });

        STRATEGY_MAP.put(Difficulty.MEDIUM, (CPUPlayer p) -> {
            for (Card c : p.getHand()) {
                if (c.getColor() != Color.WILD) return c;
            }
            for (Card c : p.getHand()) return c;
            return null;
        });

        STRATEGY_MAP.put(Difficulty.HARD, (CPUPlayer p) -> {
            for (Card c : p.getHand()) {
                if (c instanceof IsAttackCard) return c;
            }
            for (Card c : p.getHand()) return c;
            return null;
        });
    }

    public CPUPlayer(String name, Difficulty difficulty) {
        super(name);
        this.difficulty = difficulty;
        this.strategy = STRATEGY_MAP.get(difficulty);
    }

    public CPUPlayer(String name, Strategy strategy) {
        super(name);
        this.strategy = strategy;
        this.difficulty = null;
    }

    public Card playCard() {
        Card chosen = strategy.chooseCard(this);
        if (chosen != null) {
            getHand().remove(chosen);
        }
        return chosen;
    }
}
