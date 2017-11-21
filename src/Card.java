import java.util.function.Predicate;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Card extends Label {

	public final int card;
	private final NumberBinding widthBinding, heightBinding;
	private final Property<ChangeListener<Point2D>> onMove = new SimpleObjectProperty<>();
	private final Property<EventHandler<MouseEvent>> onDrag = new SimpleObjectProperty<>();

	private double offsetX, offsetY;
	private Point2D start;
	public static final String[] COLORS = new String[] {"Red", "Green", "Black"};
	public static final String[] DRAGONS = new String[] {"\u4E2D", "\u767C", "\u000B"};// "\uD83D\uDDCC"};
	public static final String[] NUMERALS = new String[] {"\u4E00", "\u4E8C", "\u4E09", "\u56DB", "\u4E94", "\u516D", "\u4E03", "\u516B", "\u4E5D"};

	public Card(final int card, final NumberBinding widthBinding) {
		this(card, widthBinding, c -> true);
	}

	public Card(final int card, final NumberBinding widthBinding, final Predicate<Card> draggable) {
		super(" " + nameOfCard(card));
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

		setBackground(new Background(new BackgroundFill(new Color(.9, .9, .9, 1), new CornerRadii(10), Insets.EMPTY)));
		setAlignment(Pos.TOP_LEFT);
		setRotationAxis(new Point3D(0, 1, 0));

		setOnMousePressed(event -> {
			if (!draggable.test(this) || event.getButton() != MouseButton.PRIMARY) return;

			if (onDrag.getValue() != null) {
				onDrag.getValue().handle(event);
			}

			offsetX = getTranslateX() - event.getSceneX();
			offsetY = getTranslateY() - event.getSceneY();
			start = new Point2D(getTranslateX(), getTranslateY());
		});

		setOnMouseDragged(event -> {
			if (start == null || event.getButton() != MouseButton.PRIMARY) return;

			translateXProperty().unbind();
			translateYProperty().unbind();
			setTranslateX(event.getSceneX() + offsetX);
			setTranslateY(event.getSceneY() + offsetY);
		});

		setOnMouseReleased(event -> {
			if (start == null || event.getButton() != MouseButton.PRIMARY) return;

			if (onMove.getValue() != null) {
				onMove.getValue().changed(null, start, new Point2D(getTranslateX(), getTranslateY()));
			}

			start = null;
		});
	}

	public void enableShadow(final boolean enabled) {
		if (!enabled) {
			setEffect(null);
		} else if (getEffect() == null && (card == Game.ROSE || (card & 0b1001111) != 0b1001111)) {
			setEffect(new DropShadow(3.25, 0, 1, Color.BLACK));
		}

	}

	public boolean isDragging() {
		return start != null;
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
				return "\uD83C\uDF39";
			} else if ((card & 0b1001111) == Game.DRAGON_MOD) {
				return DRAGONS[card >> 4 & 0b11];
			} else {
				return "";
			}
		} else {
			final int value = card & 0b1111;
			return String.format("%d\n %s", 1 + value, NUMERALS[value]);
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

}
