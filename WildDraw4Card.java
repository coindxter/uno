public class WildDraw4Card extends Card implements IsWildCard, IsAttackCard {

    public WildDraw4Card() {
        super(Color.WILD, Value.WILD_DRAW_FOUR);
    }

    @Override
    public void chooseColor(Color color) {
        this.color = color;
    }

    @Override
    public int getAttackAmount() {
        return 4;
    }

    @Override
    public void play() {
    }
}
