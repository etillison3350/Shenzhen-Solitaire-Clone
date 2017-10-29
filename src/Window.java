import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Window extends Application {

	private Game game;

	private StackPane stackPane;
	private NumberBinding cardWidth;

	public static void main(final String[] args) {
		// final Game game = new Game();
		//
		// final Scanner scanner = new Scanner(System.in);
		// while (Math.sqrt(2) > 1) {
		// int auto;
		// while ((auto = game.autoFill()) >= 0) {
		// game.move(auto & 0b111, auto < 8 ? game.cardsIn(auto) - 1 : -1, 0, -2);
		// }
		//
		// System.out.println(game.asString());
		// final String line = scanner.nextLine();
		//
		// try {
		// try {
		// final int[] values =
		// Arrays.stream(line.split("\\s+")).mapToInt(Integer::parseInt).toArray();
		// System.out.println(game.move(values[0], values[1], values[2], values[3]));
		// } catch (final Exception e) {
		// System.out.println(game.collectDragons(Integer.parseInt(line)));
		// }
		// } catch (final Exception e) {
		// System.out.println(false);
		// }
		// }

		Application.launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		stackPane = new StackPane();

		stackPane.setAlignment(Pos.TOP_LEFT);
		// stackPane.getChildren().add(makeCard(stackPane, 0));

		cardWidth = Bindings.min(stackPane.widthProperty().divide(8), stackPane.heightProperty().multiply(25D / (7 * 22)));

		for (int i = 0; i < 40; i++) {
			final Node card = makeCard(0);
			card.translateXProperty().bind(cardWidth.multiply(i % 8));
			card.translateYProperty().bind(cardWidth.multiply(i / 8 * 7D / 25));
			stackPane.getChildren().add(card);
		}

		stage.setScene(new Scene(stackPane, 640, 480));
		stage.show();
	}

	/*
	 * @Override public void start(final Stage stage) throws Exception { final GridPane gp = new
	 * GridPane();
	 *
	 * final ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(12.5);
	 * cc.setFillWidth(true); for (int i = 0; i < 8; i++) { gp.getColumnConstraints().add(cc); }
	 *
	 * final RowConstraints first = new RowConstraints(); gp.getRowConstraints().add(first);
	 *
	 * final RowConstraints rc = new RowConstraints(); for (int i = 1; i < 18; i++) {
	 * gp.getRowConstraints().add(rc); }
	 *
	 * // final Button b = new Button("Hello, World!"); // b.setMaxSize(Double.POSITIVE_INFINITY,
	 * Double.POSITIVE_INFINITY); // // gp.add(b, 3, 0); // gp.getChildren().remove(b); // gp.add(b,
	 * 7, 0);
	 *
	 * for (int x = 0; x < 8; x++) { final double n = Math.random() * 10; for (int y = 0; y < n;
	 * y++) { final Button card = makeCard(x + " " + y); gp.add(card, x, y, 1, y == 0 ? 1 : 5);
	 *
	 * if (x == 0 && y == 0) { first.minHeightProperty().bind(card.heightProperty());
	 * first.maxHeightProperty().bind(first.minHeightProperty()); } else if (x == 0 && y == 1) {
	 * rc.minHeightProperty().bind(card.heightProperty().divide(5));
	 * rc.maxHeightProperty().bind(rc.minHeightProperty()); } } }
	 *
	 * stage.setScene(new Scene(gp, 640, 480)); stage.show();
	 *
	 * gp.requestLayout();
	 *
	 * stage.widthProperty().addListener((observable, oldValue, newValue) -> { gp.requestLayout();
	 * }); }
	 */

	private Node makeCard(final int card) {
		final Label ret = new Label(Long.toString(Double.doubleToLongBits(Math.random())));

		ret.minWidthProperty().bind(cardWidth.subtract(10));
		ret.maxWidthProperty().bind(ret.minWidthProperty());
		ret.minHeightProperty().bind(cardWidth.multiply(7D / 5).subtract(5));
		ret.maxHeightProperty().bind(ret.minHeightProperty());

		ret.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(10), Insets.EMPTY)));

		ret.setEffect(new DropShadow(3, 0, 1, Color.BLACK));

		ret.setAlignment(Pos.TOP_LEFT);

		ret.setOnMouseEntered(event -> {
			ret.setUnderline(true);
		});
		ret.setOnMouseExited(event -> {
			ret.setUnderline(false);
		});

		return ret;
	}

	private static Button makeCard(final String title) {
		final Button ret = new Button(title);
		ret.minHeightProperty().bind(ret.widthProperty().multiply(7D / 5));
		ret.maxHeightProperty().bind(ret.widthProperty().multiply(7D / 5));
		ret.setMaxWidth(Double.POSITIVE_INFINITY);
		ret.setAlignment(Pos.TOP_CENTER);
		return ret;
	}
}
