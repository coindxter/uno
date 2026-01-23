public class CardEffect {
    public final boolean skip;
    public final boolean reverse;
    public final int drawCount;       
    public final boolean needsColor;   

    public CardEffect(boolean skip, boolean reverse, int drawCount, boolean needsColor) {
        this.skip = skip;
        this.reverse = reverse;
        this.drawCount = drawCount;
        this.needsColor = needsColor;
    }

    public static CardEffect none() {
        return new CardEffect(false, false, 0, false);
    }
}
