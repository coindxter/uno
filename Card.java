import java.util.Objects;

public abstract class Card {
    protected Color color;
    protected Value value;

    protected Card(Color color, Value value) {
        this.color = color;
        this.value = value;
    }

    public Color getColor() { return color; }
    public Value getValue() { return value; }

    public void play() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return color == card.color && value == card.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), color, value);
    }

    @Override
    public String toString() {
        return color + " " + value;
    }
}
