public class Draw2Card extends Card implements IsAttackCard {

    public Draw2Card(Color color) {
        super(color, Value.DRAW_TWO);
    }

    @Override
    public int getAttackAmount() {
        return 2;
    }

    @Override
    public CardEffect effect() {
        return new CardEffect(false, false, 2, true); 
    }
}
