import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Game {

	private final List<List<Integer>> board = new ArrayList<>();
	private final List<Integer> topBoard = new ArrayList<>();

	public Game() {
		final List<Integer> cards = new ArrayList<>();
		for (int c = 0; c < 3; c++) {
			for (int i = 0; i < 9; i++) {
				cards.add(i | c << 4);
			}
			for (int i = 0; i < 4; i++) {
				cards.add(-c);
			}
		}
		cards.add(Integer.MIN_VALUE);
		Collections.shuffle(cards);

		for (int i = 0; i < 8; i++) {
			board.add(cards.stream().skip(i * 5).limit(5).collect(Collectors.toCollection(ArrayList::new)));
		}
	}

	public int cardsIn(final int slot) {
		return board.get(slot).size();
	}

	public int cardAt(final int slot, final int index) {
		return board.get(slot).get(index);
	}

}
