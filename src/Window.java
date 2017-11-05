import java.awt.Point;
import java.io.FileNotFoundException;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

	private static final int TRANSLATE_DURATION = 250;

	private Game game;

	private StackPane stackPane;
	private NumberBinding cardWidth;

	private final BidirectionalHashMap<Point, Card> cards = new BidirectionalHashMap<>();
	// private final Map<Card, Boolean> isAnimating = new HashMap<>();
	private boolean animating = false;

	public static void main(final String[] args) throws FileNotFoundException {
		Application.launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		game = new Game();

		stackPane = new StackPane();

		stackPane.setAlignment(Pos.TOP_LEFT);

		cardWidth = Bindings.min(stackPane.widthProperty().divide(8), stackPane.heightProperty().multiply(25D / (7 * 22)));

		for (int s = 0; s < 8; s++) {
			if (s != 3) {
				final Label topPlaceholder = makePlaceholder(cardWidth);
				topPlaceholder.translateXProperty().bind(xBinding(s, -1));
				stackPane.getChildren().add(topPlaceholder);
			}

			final Label mainPlaceholder = makePlaceholder(cardWidth);
			mainPlaceholder.translateXProperty().bind(xBinding(s, 0));
			mainPlaceholder.translateYProperty().bind(yBinding(s, 0));
			stackPane.getChildren().add(mainPlaceholder);
		}

		for (int i = 0; i < 40; i++) {
			final int x = i % 8;
			final int y = i / 8;

			final Card card = new Card(game.cardAt(x, y), cardWidth, this::isDraggable);
			card.translateXProperty().bind(xBinding(x, y));
			card.translateYProperty().bind(yBinding(x, y));

			card.setOnMove((observableNull, oldValue, newValue) -> {
				animating = true;

				final Point oldPosition = cards.getKey(card);

				final int newX = (int) (newValue.getX() / cardWidth.doubleValue() + 0.5);
				final boolean sideboard = 2 * newValue.getY() < cardWidth.doubleValue() * (game.cardsIn(newX) + 5) * 7 / 25 + 16;

				final int srcSlotCards = oldPosition.y < 0 ? 1 : game.cardsIn(oldPosition.x);
				final int destSlotCards = sideboard ? -1 : game.cardsIn(newX);

				final int index = sideboard ? newX > 3 ? -2 : -1 : 0;
				boolean moved;
				try {
					moved = game.move(oldPosition.x, oldPosition.y, newX, index);
				} catch (final Exception e) {
					moved = false;
				}

				final TranslateTransition anim = new TranslateTransition(Duration.millis(TRANSLATE_DURATION), card);
				ObservableValue<Number> xBinding, yBinding;

				if (moved) {
					// Update the positions in #cards
					final int slot = index > -2 ? newX : newX - 5;
					if (oldPosition.y > 0) {
						int destIndex = destSlotCards;
						for (int n = oldPosition.y; n < srcSlotCards; n++) {
							cards.put(new Point(slot, destIndex++), cards.get(new Point(oldPosition.x, n)));
						}
					} else {
						cards.put(new Point(slot, destSlotCards), card);
					}

					xBinding = xBinding(newX, sideboard ? -1 : destSlotCards);
					yBinding = yBinding(newX, sideboard ? -1 : destSlotCards);
				} else {
					xBinding = xBinding(oldPosition.x, oldPosition.y);
					yBinding = yBinding(oldPosition.x, oldPosition.y);
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

			card.setOnDrag(event -> {
				// Bring all cards being moved to the front, in order
				final Point position = cards.getKey(card);
				final int n = game.cardsIn(position.x);
				for (int ix = position.y; ix < n; ix++) {
					cards.get(new Point(position.x, ix)).toFront();
				}
			});

			stackPane.getChildren().add(card);
			cards.put(new Point(x, y), card);
		}

		for (int c = 0; c < 3; c++) {
			final int color = c;

			final Button button = new Button(Card.COLORS[c]);
			button.setOnAction(event -> {
				if (animating) return;

				final DragonCollectionResult res = game.collectDragons(color);
				if (res != null) {
					animating = true;
					final ParallelTransition move = new ParallelTransition();

					System.out.println(game.asString());
					for (final int slot : res.slots) {
						final int index = slot < 0 ? -1 : game.cardsIn(slot);
						System.out.println(index);
						final Card card = cards.get(new Point(slot, index));
						card.translateXProperty().unbind();
						card.translateYProperty().unbind();

						final TranslateTransition anim = new TranslateTransition(new Duration(TRANSLATE_DURATION), card);
						anim.toXProperty().bind(xBinding(res.destinationSlot, -1));
						anim.toYProperty().set(0);
						move.getChildren().add(anim);
					}

					move.setOnFinished(e -> {
						for (final int slot : res.slots) {
							final int index = slot < 0 ? -1 : game.cardsIn(slot);
							final Card card = cards.get(new Point(slot, index));

							stackPane.getChildren().remove(card);
							cards.removeValue(card);
						}

						final Card newCard = new Card(game.sideboardCard(res.destinationSlot), cardWidth, this::isDraggable);
						stackPane.getChildren().add(newCard);
						cards.put(new Point(res.destinationSlot, -1), newCard);

						newCard.translateXProperty().bind(xBinding(res.destinationSlot, -1));

						autocomplete(true);
					});
					move.play();
				}
			});

			button.translateXProperty().bind(cardWidth.multiply(3));
			button.translateYProperty().bind(cardWidth.multiply(c * 7D / 15));
			button.minWidthProperty().bind(cardWidth.subtract(10));
			button.maxWidthProperty().bind(button.minWidthProperty());
			button.minHeightProperty().bind(cardWidth.multiply(7D / 15).subtract(5));
			button.maxHeightProperty().bind(button.minHeightProperty());
			stackPane.getChildren().add(button);
		}

		stage.setScene(new Scene(stackPane, 640, 480));
		stage.show();
	}

	private ObservableValue<Number> xBinding(final int slot, final int index) {
		return this.xBinding(slot, index, true);
	}

	private ObservableValue<Number> xBinding(final int slot, final int index, final boolean allowRelative) {
		if (!allowRelative || index < 1) return cardWidth.multiply(slot);

		return cards.get(new Point(slot, index - 1)).translateXProperty();
	}

	private ObservableValue<Number> yBinding(final int slot, final int index) {
		return this.yBinding(slot, index, true);
	}

	private ObservableValue<Number> yBinding(final int slot, final int index, final boolean allowRelative) {
		if (index < 0) return new SimpleDoubleProperty(0);

		final int trueIndex = Math.min(game.cardsIn(slot), index);
		if (!allowRelative || trueIndex == 0) return cardWidth.multiply((trueIndex + 5) * 7D / 25).add(16);

		return cards.get(new Point(slot, index - 1)).translateYProperty().add(cardWidth.multiply(7D / 25));
	}

	private void autocomplete(final boolean changeAnimating) {
		final int move = game.autoFill();
		if (move == -1) {
			animating = false;
		} else {
			System.out.println(move);
			System.out.println(game.asString());
			Card card;
			System.out.println(cards);
			if (move < 8) { // Main board
				card = cards.get(new Point(move, game.cardsIn(move) - 1));
			} else { // Sideboard
				card = cards.get(new Point(move, -1));
			}
			game.move(move % 8, move >= 8 ? -1 : game.cardsIn(move) - 1, 0, -2);
			final int xTarget = card.card == Game.ROSE ? 4 : 5 + (card.card >> 4 & 0b11);
			cards.put(new Point(xTarget, -2), card);

			final TranslateTransition tt = new TranslateTransition(Duration.millis(TRANSLATE_DURATION), card);
			tt.toXProperty().bind(xBinding(xTarget, 0));
			tt.setToY(0);
			card.translateXProperty().unbind();
			card.translateYProperty().unbind();

			tt.setOnFinished(event -> {
				card.translateXProperty().bind(xBinding(xTarget, 0));
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
