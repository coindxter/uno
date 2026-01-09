public abstract class Card {

    protected Color color;
    protected Value value;

    public Card(Color color, Value value) {
        this.color = color;
        this.value = value;
    }

    public Color getColor() {
        return color;
    }

    public Value getValue() {
        return value;
    }

    public abstract void play();

    @Override
    public String toString() {
        return color + " " + value;
    }
}