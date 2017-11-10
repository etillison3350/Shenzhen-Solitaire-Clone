import java.util.Arrays;

public class DragonCollectionResult {

	/**
	 * The slots of the moved dragons. 0-7 are main board slots. 8-10 are sideboard slots.
	 */
	public final int[] slots;

	/**
	 * The slot in the sideboard where the dragons have been moved to.
	 */
	public final int destinationSlot;

	public DragonCollectionResult(final int[] slots, final int destinationSlot) {
		this.slots = slots;
		this.destinationSlot = destinationSlot;
	}

	@Override
	public String toString() {
		return String.format("%s [slots=%s, destination=%d]", this.getClass().getName(), Arrays.toString(slots), destinationSlot);
	}

}
