import java.util.Arrays;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Window extends Application {

	public static void main(final String[] args) {
		final Game game = new Game();

		final Scanner scanner = new Scanner(System.in);
		while (Math.sqrt(2) > 1) {
			System.out.println(game.asString());
			final String line = scanner.nextLine();

			final int[] values = Arrays.stream(line.split("\\s+")).mapToInt(Integer::parseInt).toArray();

			System.out.println(game.move(values[0], values[1], values[2], values[3]));
		}

		Application.launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		final GridPane gp = new GridPane();

		final ColumnConstraints cc = new ColumnConstraints();
		cc.setPercentWidth(12.5);
		cc.setFillWidth(true);
		for (int i = 0; i < 8; i++) {
			gp.getColumnConstraints().add(cc);
		}

		final Button b = new Button("Hello, World!");
		b.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

		gp.add(b, 3, 0);
		gp.getChildren().remove(b);
		gp.add(b, 7, 0);

		stage.setScene(new Scene(gp, 640, 480));
		stage.show();
	}
}
