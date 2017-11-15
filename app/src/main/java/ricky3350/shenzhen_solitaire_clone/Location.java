package ricky3350.shenzhen_solitaire_clone;

public class Location {

	public final int slot, index;

	public Location(int slot, int index) {
		this.slot = slot;
		this.index = index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + slot;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Location other = (Location) obj;
		if (index != other.index) return false;
		if (slot != other.slot) return false;
		return true;
	}
}
