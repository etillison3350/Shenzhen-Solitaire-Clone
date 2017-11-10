import java.util.function.Predicate;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Card extends Label {

	public final int card;
	private final NumberBinding widthBinding, heightBinding;
	private final IntegerProperty slot = new SimpleIntegerProperty(0);
	private final IntegerProperty index = new SimpleIntegerProperty(0);
	private final Property<ChangeListener<Point2D>> onMove = new SimpleObjectProperty<>();
	private final Property<EventHandler<MouseEvent>> onDrag = new SimpleObjectProperty<>();

	private double offsetX, offsetY;
	private Point2D start;
	public static final String[] COLORS = new String[] {"Red", "Green", "Black"};

	public Card(final int card, final NumberBinding widthBinding) {
		this(card, widthBinding, c -> true);
	}

	public Card(final int card, final NumberBinding widthBinding, final Predicate<Card> draggable) {
		super(nameOfCard(card));
		setTextFill(cardColor(card));

		this.card = card;
		this.widthBinding = widthBinding.subtract(10);
		heightBinding = widthBinding.multiply(7D / 5).subtract(5);

		minWidthProperty().bind(this.widthBinding);
		maxWidthProperty().bind(this.widthBinding);
		minHeightProperty().bind(heightBinding);
		maxHeightProperty().bind(heightBinding);

		setFont(new Font(heightBinding.getValue().doubleValue() / 8));
		heightBinding.addListener((observable, oldValue, newValue) -> {
			setFont(new Font(newValue.doubleValue() / 8));
		});

		setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(10), Insets.EMPTY)));

		setEffect(new DropShadow(3, 0, 1, Color.BLACK));

		setAlignment(Pos.TOP_LEFT);

		setOnMousePressed(event -> {
			if (!draggable.test(this)) return;

			if (onDrag.getValue() != null) {
				onDrag.getValue().handle(event);
			}

			offsetX = getTranslateX() - event.getSceneX();
			offsetY = getTranslateY() - event.getSceneY();
			start = new Point2D(getTranslateX(), getTranslateY());
		});

		setOnMouseDragged(event -> {
			if (start == null) return;

			translateXProperty().unbind();
			translateYProperty().unbind();
			setTranslateX(event.getSceneX() + offsetX);
			setTranslateY(event.getSceneY() + offsetY);
		});

		setOnMouseReleased(event -> {
			if (start == null) return;

			if (onMove.getValue() != null) {
				onMove.getValue().changed(null, start, new Point2D(getTranslateX(), getTranslateY()));
			}

			start = null;
		});
	}

	// Cards:
	// Numerical: 0b0xxyyyy, xx = color, yyyy = value
	// Dragons: 0b1xx0000, xx = color
	// Solved Dragons: 0b1xx1111, xx = color
	// Rose: 0b1111111
	// Empty: -1
	public static String nameOfCard(final int card) {
		if ((card & 0b1000000) > 0) {
			if (card == Game.ROSE) {
				return "Rose";
			} else if ((card & 0b1001111) == Game.DRAGON_MOD) {
				return "Dragon";
			} else {
				return "X";
			}
		} else {
			return Integer.toString(1 + (card & 0b1111));
		}
	}

	public static Color cardColor(final int card) {
		switch (card >> 4 & 0b11) {
			case 0b00:
				return Color.RED;
			case 0b01:
				return Color.GREEN;
			default:
				return Color.BLACK;
		}
	}

	public int getSlot() {
		return slot.get();
	}

	public void setSlot(final int slot) {
		if (slot < 0 || slot >= 8) throw new IllegalArgumentException("Slot value (" + slot + ") out of bounds");

		this.slot.set(slot);
	}

	public IntegerProperty slotProperty() {
		return slot;
	}

	public int getIndex() {
		return index.get();
	}

	public void setIndex(final int index) {
		this.index.set(index);
	}

	public IntegerProperty indexProperty() {
		return index;
	}

	public ChangeListener<Point2D> getOnMove() {
		return onMove.getValue();
	}

	public void setOnMove(final ChangeListener<Point2D> onMove) {
		this.onMove.setValue(onMove);
	}

	public Property<ChangeListener<Point2D>> onMoveProperty() {
		return onMove;
	}

	public EventHandler<MouseEvent> getOnDrag() {
		return onDrag.getValue();
	}

	public void setOnDrag(final EventHandler<MouseEvent> onDrag) {
		this.onDrag.setValue(onDrag);
	}

	public Property<EventHandler<MouseEvent>> onDragProperty() {
		return onDrag;
	}

	@Override
	public String toString() {
		if (card == Game.ROSE) return "Card [Rose]";
		return String.format("Card [%s %s]", COLORS[card >> 4 & 0b11], nameOfCard(card));
	}
}
