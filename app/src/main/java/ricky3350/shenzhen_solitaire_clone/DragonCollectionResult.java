package ricky3350.shenzhen_solitaire_clone;

import java.util.Collection;
import java.util.HashSet;

public class DragonCollectionResult {
	/**
	 * The slots of the moved dragons. 0-7 are main board slots. 8-10 are sideboard slots.
	 */
	public final Collection<Integer> slots = new HashSet<>();

	/**
	 * The slot in the sideboard where the dragons have been moved to.
	 */
	public final int destinationSlot;

	public final int color;

	public DragonCollectionResult(final Collection<Integer> slots, final int destinationSlot, final int color) {
		this.slots.addAll(slots);
		this.destinationSlot = destinationSlot;
		this.color = color;
	}
}
