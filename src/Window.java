import java.awt.Point;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
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

public class Window extends Application {

	private Game game;

	private StackPane stackPane;
	private NumberBinding cardWidth;

	private final BidirectionalHashMap<Point, Card> cards = new BidirectionalHashMap<>();

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
				card.translateYProperty().bind(cardWidth.multiply((y + 5) * 7D / 25).add(16));
			} else {
				card.translateXProperty().bind(cards.get(new Point(x, y - 1)).translateXProperty());
				card.translateYProperty().bind(cards.get(new Point(x, y - 1)).translateYProperty().add(cardWidth.multiply(7D / 25)));
			}
			stackPane.getChildren().add(card);
			cards.put(new Point(x, y), card);
		}

		stage.setScene(new Scene(stackPane, 640, 480));
		stage.show();
	}

	private boolean isDraggable(final Card card) {
		// TODO
		return cards.getKey(card).x % 2 == 0;
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
