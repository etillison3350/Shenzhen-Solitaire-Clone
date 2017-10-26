import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Game {

	private final List<List<Integer>> board = new ArrayList<>();
	private final List<Integer> topBoard = new ArrayList<>();

	// Cards:
	// Numerical: 0b0xxyyyy, xx = color, yyyy = value
	// Dragons: 0b1xx0000, xx = color
	// Rose: 0b1111111
	// Empty: -1

	private static final int ROSE = 0b1111111;
	private static final int DRAGON_MOD = 0b1000000;

	public Game() {
		final List<Integer> cards = new ArrayList<>();
		for (int c = 0; c < 3; c++) {
			for (int i = 0; i < 9; i++) {
				cards.add(i | c << 4);
			}
			for (int i = 0; i < 4; i++) {
				cards.add(c << 4 | DRAGON_MOD);
			}
		}
		cards.add(ROSE);
		Collections.shuffle(cards);

		for (int i = 0; i < 8; i++) {
			board.add(cards.stream().skip(i * 5).limit(5).collect(Collectors.toCollection(ArrayList::new)));
		}

		for (int i = 0; i < 3; i++) {
			topBoard.add(-1);
		}
		for (int i = 0; i < 4; i++) {
			topBoard.add(0);
		}
	}

	public int cardsIn(final int slot) {
		return board.get(slot).size();
	}

	public int cardAt(final int slot, final int index) {
		return board.get(slot).get(index);
	}

	public int sideboardCard(final int slot) {
		if (slot >= 3) throw new IndexOutOfBoundsException();

		return topBoard.get(slot);
	}

	public int highestComplete(final int color) {
		if (color < 0) throw new IndexOutOfBoundsException();

		return topBoard.get(color + 4) & 0b1111;
	}

	public boolean rose() {
		return topBoard.get(3) == ROSE;
	}

	/**
	 * Attempts to move the cards from the source position to the destination.<br>
	 * <i>slot</i> parameters range from 0 - 7 for main board slots, or 0 - 2 for sideboard slots.
	 * The value for the completed slots is inferred from the identity of the card.<br>
	 * <i>index</i> parameters refer to the index as used in {@link #cardAt(int, int)}. An index of
	 * -1 refers to the sideboard, and an index of -2 refers to the completed slots.
	 * @param srcSlot
	 * @param srcIndex - <i>Note: a value of -2 will always return <code>false</code></i>
	 * @param destSlot
	 * @param destIndex - <i>Note: all positive values of destIndex have the same function. Also,
	 *            moving a dragon to an index of -2 attempts to collect all dragons to the sideboard
	 *            slots.</i>
	 * @return whether or not the movement was successful (i.e. if it was allowed)
	 */
	public boolean move(final int srcSlot, final int srcIndex, final int destSlot, final int destIndex) {
		if (srcSlot > 7 || srcSlot < 0) throw new IllegalArgumentException("Source slot (" + srcSlot + ") out of range");
		if (destSlot > 7 || destSlot < 0) throw new IllegalArgumentException("Destination slot (" + destSlot + ") out of range");

		if (srcIndex < -2) throw new IllegalArgumentException("Source index (" + srcIndex + ") out of range");
		if (destIndex < -2) throw new IllegalArgumentException("Destination index (" + destIndex + ") out of range");

		int numCards = 1; // The number of cards to be moved.
		int bottomCard = -1;

		if (srcIndex == -2) {
			return false; // Cannot move card out of completed zone
		} else if (srcIndex == -1) {
			bottomCard = sideboardCard(srcSlot);
			if (bottomCard < 0) { // Attempting to move no card (empty space)
				return false;
			}

		} else {
			int value = bottomCard = cardAt(srcSlot, srcIndex);
			for (int i = srcIndex + 1; i < cardsIn(srcSlot); i++) {
				final int n = cardAt(srcSlot, i);

				// Check that every card is one more than the card below it and that no two
				// consecutive cards are the same color.
				if ((value & 0b1111) - 1 != (n & 0b1111) || (value & 0b110000) == (n & 0b110000)) return false;

				value = n;
			}

			numCards = cardsIn(srcSlot) - srcIndex; // Calculate the number of cards to be moved.
		}

		// Cannot move more than one card to the top bar
		if (destIndex < 0 && numCards > 1) return false;

		if (destIndex == -2) {
			// moving a dragon to an index of -2 attempts to collect all dragons to the sideboard
			// slots.
			if ((bottomCard & 0b1001111) == DRAGON_MOD) {

				// No slots available
				if (!topBoard.stream().limit(3).anyMatch(i -> i == 0)) return false;

				int ct = 0;
				for (int i = 0; i < board.size(); i++) {
					if (cardAt(i, cardsIn(i) - 1) == bottomCard) {
						if (++ct >= 4) break;
					}
				}

				if (ct != 4) {
					for (int i = 0; i < 3; i++) {
						if (sideboardCard(i) == bottomCard) {
							if (++ct >= 4) break;
						}
					}

					if (ct != 4) return false;
				}
			} else if (bottomCard != ROSE) { // The rose can always be completed if it has not yet
												// been
				final int color = bottomCard >> 4 & 0b11;

				// Completed cards must be one higher than the current complete card.
				if (highestComplete(color) != (bottomCard & 0b1111) - 1) return false;
			}
		} else if (destIndex == -1) {
			if (sideboardCard(destSlot) >= 0) return false;
		} else {
			if (cardsIn(destSlot) != 0) {
				final int below = cardAt(destSlot, cardsIn(destSlot) - 1);

				// Cannot place dragons on any card
				if ((bottomCard & 0b1001111) == DRAGON_MOD) return false;

				// Cannot place if numbers do not match
				if ((below & 0b1111) - 1 != (bottomCard & 0b1111)) return false;

				// Cannot place matching colors on top of each other.
				if ((below & 0b110000) == (bottomCard & 0b110000)) return false;
			}
		}

		// TODO perform movement

		return true;
	}

}
