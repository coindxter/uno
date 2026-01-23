public class SkipCard extends Card {
    public SkipCard(Color color) {
        super(color, Value.SKIP);
    }

    @Override
    public CardEffect effect() {
        return new CardEffect(true, false, 0, false);
    }
}
