import java.awt.Point;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Window extends Application {

	private static final int TRANSLATE_DURATION = 100;

	private Game game;

	private StackPane stackPane;
	private NumberBinding cardWidth;

	private final BidirectionalHashMap<Point, Card> cards = new BidirectionalHashMap<>();
	// private final Map<Card, Boolean> isAnimating = new HashMap<>();
	private boolean animating = false;

	public static void main(final String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		game = new Game();

		stackPane = new StackPane();

		stackPane.setAlignment(Pos.TOP_LEFT);
		// stackPane.getChildren().add(makeCard(stackPane, 0));

		cardWidth = Bindings.min(stackPane.widthProperty().divide(8), stackPane.heightProperty().multiply(25D / (7 * 22)));

		for (int s = 0; s < 8; s++) {
			final Label topPlaceholder = makePlaceholder(cardWidth);
			topPlaceholder.translateXProperty().bind(cardWidth.multiply(s));
			stackPane.getChildren().add(topPlaceholder);

			final Label mainPlaceholder = makePlaceholder(cardWidth);
			mainPlaceholder.translateXProperty().bind(cardWidth.multiply(s));
			mainPlaceholder.translateYProperty().bind(cardWidth.multiply(7D / 5).add(16));
			stackPane.getChildren().add(mainPlaceholder);
		}

		for (int i = 0; i < 40; i++) {
			final int x = i % 8;
			final int y = i / 8;

			final Card card = new Card(game.cardAt(x, y), cardWidth, this::isDraggable);
			if (y == 0) {
				card.translateXProperty().bind(cardWidth.multiply(x));
				card.translateYProperty().bind(cardWidth.multiply(7D / 5).add(16));
			} else {
				card.translateXProperty().bind(cards.get(new Point(x, y - 1)).translateXProperty());
				card.translateYProperty().bind(cards.get(new Point(x, y - 1)).translateYProperty().add(cardWidth.multiply(7D / 25)));
			}

			card.setOnMove((observableNull, oldValue, newValue) -> {
				animating = true;

				final Point oldPosition = cards.getKey(card);

				final int newX = (int) (newValue.getX() / cardWidth.doubleValue() + 0.5);
				final boolean sideboard = newValue.getY() < cardWidth.doubleValue() * (game.cardsIn(newX) + 5) * 7 / 25 + 16;

				final int srcSlotCards = oldPosition.y < 0 ? 1 : game.cardsIn(oldPosition.x);
				int destSlotCards = game.cardsIn(newX);

				final int index = sideboard ? newX > 3 ? -2 : -1 : 0;
				boolean moved;
				try {
					moved = game.move(oldPosition.x, oldPosition.y, newX, index);
				} catch (final Exception e) {
					moved = false;
				}

				final TranslateTransition anim = new TranslateTransition(Duration.millis(TRANSLATE_DURATION), card);
				NumberBinding xBinding, yBinding;
				if (moved) {
					// Update the positions in #cards
					final int slot = index > -2 ? newX : newX - 5;
					if (oldPosition.y > 0) {
						for (int n = oldPosition.y; n < srcSlotCards; n++) {
							cards.put(new Point(slot, destSlotCards++), cards.get(new Point(oldPosition.x, n)));
						}
					} else {
						cards.put(new Point(slot, destSlotCards), card);
					}

					xBinding = cardWidth.multiply(newX);
					yBinding = sideboard ? new SimpleDoubleProperty(0).negate() : cardWidth.multiply((destSlotCards + 5) * 7D / 25);
				} else {
					xBinding = cardWidth.multiply(oldPosition.x);
					yBinding = sideboard ? new SimpleDoubleProperty(0).negate() : cardWidth.multiply((oldPosition.y + 5) * 7D / 25);
				}
				anim.toXProperty().bind(xBinding);
				anim.toYProperty().bind(yBinding);
				card.translateXProperty().unbind();
				card.translateYProperty().unbind();

				final boolean finalMoved = moved;
				anim.setOnFinished(event -> {
					card.translateXProperty().bind(xBinding);
					card.translateYProperty().bind(yBinding);

					if (finalMoved) {
						autocomplete(true);
					} else {
						animating = false;
					}
				});
				anim.play();
			});

			stackPane.getChildren().add(card);
			cards.put(new Point(x, y), card);
		}

		stage.setScene(new Scene(stackPane, 640, 480));
		stage.show();
	}

	private NumberBinding xBinding(final int slot, final int index) {
		return this.xBinding(slot, index, true);
	}

	private NumberBinding xBinding(final int slot, final int index, final boolean allowRelative) {
		// TODO
		return null;
	}

	private NumberBinding yBinding(final int slot, final int index) {
		return this.yBinding(slot, index, true);
	}

	private NumberBinding yBinding(final int slot, final int index, final boolean allowRelative) {
		if (index < 0) return new SimpleDoubleProperty(0).negate();

		final int trueIndex = Math.min(game.cardsIn(slot), index);
		if (allowRelative || trueIndex == 0) return cardWidth.multiply((trueIndex + 5) * 7D / 25).add(16);

		return cards.get(new Point(slot, index)).translateYProperty().add(cardWidth.multiply(7D / 25));
	}

	private void autocomplete(final boolean changeAnimating) {
		final int move = game.autoFill();
		if (move == -1) {
			animating = false;
		} else {
			System.out.println(move);
			System.out.println(game.asString());
			Card card;
			if (move < 8) { // Main board
				card = cards.get(new Point(move, game.cardsIn(move) - 1));
			} else { // Sideboard
				card = cards.get(new Point(move, -1));
			}
			game.move(move % 8, move >= 8 ? -1 : game.cardsIn(move) - 1, 0, -2);
			final int xTarget = card.card == Game.ROSE ? 3 : 4 + (card.card >> 4 & 0b11);
			cards.put(new Point(xTarget, -2), card);

			final TranslateTransition tt = new TranslateTransition(Duration.millis(TRANSLATE_DURATION), card);
			tt.toXProperty().bind(cardWidth.multiply(xTarget));
			tt.setToY(0);
			card.translateXProperty().unbind();
			card.translateYProperty().unbind();

			tt.setOnFinished(event -> {
				card.translateXProperty().bind(cardWidth.multiply(xTarget));
				card.translateYProperty().set(0);
				autocomplete(changeAnimating);
			});
			tt.play();
		}
	}

	private boolean isDraggable(final Card card) {
		final Point position = cards.getKey(card);
		// return !isAnimating.getOrDefault(card, false) && game.canDrag(position.x, position.y);
		return !animating && game.canDrag(position.x, position.y);
	}

	private static Label makePlaceholder(final NumberBinding widthBinding) {
		final Label label = new Label();

		final NumberBinding wb = widthBinding.subtract(10);
		final NumberBinding hb = widthBinding.multiply(7D / 5).subtract(5);

		label.minWidthProperty().bind(wb);
		label.maxWidthProperty().bind(wb);
		label.minHeightProperty().bind(hb);
		label.maxHeightProperty().bind(hb);

		label.setBackground(Background.EMPTY);
		label.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));

		return label;
	}

}
