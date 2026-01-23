public class ReverseCard extends Card {
    public ReverseCard(Color color) {
        super(color, Value.REVERSE);
    }

    @Override
    public CardEffect effect() {
        return new CardEffect(false, true, 0, false);
    }
}
