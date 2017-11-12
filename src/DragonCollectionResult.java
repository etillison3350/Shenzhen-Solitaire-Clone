public class DragonCollectionResult {

	/**
	 * The slots of the moved dragons. 0-7 are main board slots. 8-10 are sideboard slots.
	 */
	public final int[] slots;

	/**
	 * The slot in the sideboard where the dragons have been moved to.
	 */
	public final int destinationSlot;

	public final int color;

	public DragonCollectionResult(final int[] slots, final int destinationSlot, final int color) {
		this.slots = slots;
		this.destinationSlot = destinationSlot;
		this.color = color;
	}

}
