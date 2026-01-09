public class Draw2Card extends Card implements IsAttackCard {

    public Draw2Card(Color color) {
        super(color, Value.DRAW_TWO);
    }

    @Override
    public int getAttackAmount() {
        return 2;
    }

    @Override
    public void play() {
    }
}
