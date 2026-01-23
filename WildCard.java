public class WildCard extends Card implements IsWildCard {

    public WildCard() {
        super(Color.WILD, Value.WILD);
    }

    @Override
    public void chooseColor(Color color) {
        this.color = color;
    }

    @Override
    public CardEffect effect() {
        return new CardEffect(false, false, 0, true); 
    }
}
