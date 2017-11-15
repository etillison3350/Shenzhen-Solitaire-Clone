package ricky3350.shenzhen_solitaire_clone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {
	private final List<List<Integer>> board = new ArrayList<>();
	private final List<Integer> topBoard = new ArrayList<>();

	// Cards:
	// Numerical: 0b0xxyyyy, xx = color, yyyy = value
	// Dragons: 0b1xx0000, xx = color
	// Solved Dragons: 0b1xx1111, xx = color
	// Rose: 0b1111111
	// Empty: -1

	public static final int ROSE = 0b1111111;
	public static final int DRAGON_MOD = 0b1000000;

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

		for (int s = 0; s < 8; s++) {
			List<Integer> slot = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				slot.add(cards.get(s * 5 + i));
			}
			board.add(slot);
		}

		for (int i = 0; i < 7; i++) {
			topBoard.add(-1);
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

		final int card = topBoard.get(color + 4);
		return card < 0 ? -1 : card & 0b1111;
	}

	public boolean rose() {
		return topBoard.get(3) == ROSE;
	}

	public int maxAutoFill() {
		int minValue = Integer.MAX_VALUE;
		for (int color = 0; color < 3; color++) {
			final int n = highestComplete(color) + 1;
			if (n < minValue) {
				minValue = n;
			}
		}

		if (minValue < 1) return 1;
		return minValue;
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
	 * @param destIndex - <i>Note: all positive values of destIndex have the same function.</i>
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

			// Attempting to move no card (empty space)
			if (bottomCard < 0) {
				return false;
			}

			// Attempting to move a solved dragon
			if ((bottomCard & 0b1001111) == 0b1001111) {
				return false;
			}
		} else {
			bottomCard = cardAt(srcSlot, srcIndex);
			if (!canDrag(srcSlot, srcIndex)) return false;

			numCards = cardsIn(srcSlot) - srcIndex; // Calculate the number of cards to be moved.
		}

		// Cannot move more than one card to the top bar
		if (destIndex < 0 && numCards > 1) return false;

		if (destIndex == -2) {
			if (bottomCard != ROSE) { // The rose can always be completed if it has not yet
				// been
				if ((bottomCard & 0b1001111) == DRAGON_MOD) return false; // Cannot complete dragons

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

		final List<Integer> moving = new ArrayList<>();
		if (srcIndex == -1) {
			// Clear the slot and add the cleared value to the moving list
			moving.add(topBoard.set(srcSlot, -1));
		} else {
			final List<Integer> slot = board.get(srcSlot);
			while (slot.size() > srcIndex) {
				moving.add(slot.remove(srcIndex));
			}
		}

		if (destIndex == -2) {
			if (bottomCard == ROSE) {
				topBoard.set(3, ROSE);
			} else {
				topBoard.set((bottomCard >> 4 & 0b11) + 4, bottomCard);
			}
		} else if (destIndex == -1) {
			topBoard.set(destSlot, moving.get(0));
		} else {
			board.get(destSlot).addAll(moving);
		}

		return true;
	}

	public DragonCollectionResult canCollectDragons(final int color) {
		// The value of the dragons of the given color
		final int dragon = (color | 0b100) << 4;

		// Check open top board slots
		int openIndex = -1;
		for (int i = 0; i < 3; i++) {
			// Can use an empty space or a space with the correct dragon in it already
			if (sideboardCard(i) < 0 || sideboardCard(i) == dragon) {
				openIndex = i;
				break;
			}
		}

		// If there are no open top board slots
		if (openIndex < 0) return null;

		final Set<Integer> matches = new HashSet<>();

		// Count the number of dragons at the bottom of the main board
		for (int n = 0; n < board.size(); n++) {
			if (cardsIn(n) > 0 && cardAt(n, cardsIn(n) - 1) == dragon) {
				matches.add(n);
				if (matches.size() >= 4) break;
			}
		}

		if (matches.size() < 4) { // If there are less than four open dragons in the main board
			// Check the sideboard
			for (int n = 0; n < 3; n++) {
				if (sideboardCard(n) == dragon) {
					matches.add(n + 8); // Top bar values range from 8 to 10
					if (matches.size() >= 4) break;
				}
			}

			// Less than four dragons open
			if (matches.size() < 4) return null;
		}

		return new DragonCollectionResult(matches, openIndex, color);
	}

	public void collectDragons(final DragonCollectionResult dcr) {
		// Remove dragons from board
		for (final Integer slot : dcr.slots) {
			if (slot < 8) { // Main board
				final List<Integer> slotList = board.get(slot);
				slotList.remove(slotList.size() - 1);
			} else { // Sideboard
				topBoard.set(slot - 8, -1);
			}
		}

		topBoard.set(dcr.destinationSlot, 0b1001111 | dcr.color << 4);
	}

	/**
	 * Gives the slot index of a card to autofill. Does not move any cards.
	 * @return the slot index the card is in (0 to 7, 8 to 10 for sideboard slots), or -1 if nothing
	 *         was moved.
	 */
	public int autoFill() {
		// Iterate through the main board slots
		for (int i = 0; i < board.size(); i++) {
			if (cardsIn(i) <= 0) continue; // Cannot autofill from an empty slot

			final int card = cardAt(i, cardsIn(i) - 1);

			if (card == ROSE) { // Rose can always be autofilled
				return i;
			} else if ((card & 0b1001111) != DRAGON_MOD) { // Do not autofill dragons
				// The number value of the card
				final int value = card & 0b1111;
				if (value <= maxAutoFill()) {
					// if (value != 1) return i;

					final int color = card >> 4 & 0b11;
					if (highestComplete(color) == value - 1) return i;
				}
			}
		}

		// Autofill from sideboard
		for (int i = 0; i < 3; i++) {
			final int card = sideboardCard(i);

			if (card == ROSE) { // Rose can always be autofilled
				return i | 0b1000;
			} else if ((card & DRAGON_MOD) != DRAGON_MOD) { // Do not autofill dragons
				final int value = card & 0b1111;
				if (value <= maxAutoFill()) {
					if (value != 1) return i | 0b1000;

					final int color = card >> 4 & 0b11;
					if (highestComplete(color) >= 0) return i | 0b1000;
				}
			}
		}

		return -1;
	}

	/**
	 * Evaluates whether or not the card at the given position is able to be dragged, i.e. are all
	 * cards below it in a pattern such that each card is one less that the card above it, and that
	 * neighbouring cards are of different colors?
	 * @param slot - The slot that the card is in, as in {@link #move(int, int, int, int)}
	 * @param index - The index of the card, as in {@link #move(int, int, int, int)}
	 * @return Whether or not the given card can be dragged.
	 */
	public boolean canDrag(final int slot, final int index) {
		if (index == -2) {
			return false;
		} else if (index == -1) {
			return (topBoard.get(slot) & 0b1001111) != 0b1001111;
		} else {
			int value = cardAt(slot, index);
			for (int i = index + 1; i < cardsIn(slot); i++) {
				final int n = cardAt(slot, i);

				// Check that every card is one more than the card below it and that no two
				// consecutive cards are the same color.
				if ((value & DRAGON_MOD) > 0 || (value & 0b1111) - 1 != (n & 0b1111) || (value & 0b110000) == (n & 0b110000)) return false;

				value = n;
			}
			return true;
		}
	}

	public boolean isWon() {
		// Check whether the top board has all the right cards (rose, complete dragons, row of 8s)
		if (!rose()) return false;

		for (int i = 0; i < 3; i++) {
			if ((sideboardCard(i) & 0b1001111) != 0b1001111) return false;
		}

		for (int color = 0; color < 3; color++) {
			if (highestComplete(color) < 8) return false;
		}

		return true;
	}
}
