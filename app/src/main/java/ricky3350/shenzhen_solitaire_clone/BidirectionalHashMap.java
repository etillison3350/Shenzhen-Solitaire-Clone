package ricky3350.shenzhen_solitaire_clone;

import java.util.HashMap;
import java.util.Map;

public class BidirectionalHashMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 5635109078750987210L;

	 private final HashMap<V, K> reverseMap;

	public BidirectionalHashMap() {
		reverseMap = new HashMap<>();
	}

	public BidirectionalHashMap(final int initialCapacity) {
		super(initialCapacity);
		reverseMap = new HashMap<>(initialCapacity);
	}

	public BidirectionalHashMap(final Map<? extends K, ? extends V> m) {
		this();

		for (K k : m.keySet()) {
			this.put(k, m.get(k));
		}
	}

	public BidirectionalHashMap(final int initialCapacity, final float loadFactor) {
		super(initialCapacity, loadFactor);
		reverseMap = new HashMap<>(initialCapacity, loadFactor);
	}

	@Override
	public V remove(Object key) {
		final V ret = super.remove(key);
		reverseMap.remove(ret);
		return ret;
	}

	public K removeValue(Object value) {
		final K ret = reverseMap.remove(value);
		super.remove(ret);
		return ret;
	}

	@Override
	public void clear() {
		super.clear();
		reverseMap.clear();
	}

	@Override
	public V put(K key, V value) {
		final V ret = super.put(key, value);
		final K old = reverseMap.put(value, key);
		if (ret != null) reverseMap.remove(ret);
		if (old != null) super.remove(old);
		return ret;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K k : m.keySet()) {
			this.put(k, m.get(k));
		}
	}

	public K getKey(Object value) {
		return reverseMap.get(value);
	}
}
