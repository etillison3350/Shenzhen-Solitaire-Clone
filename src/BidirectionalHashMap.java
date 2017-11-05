import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

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

		m.forEach(this::put);
	}

	public BidirectionalHashMap(final int initialCapacity, final float loadFactor) {
		super(initialCapacity, loadFactor);
		reverseMap = new HashMap<>(initialCapacity, loadFactor);
	}

	@Override
	public V remove(final Object key) {
		final V ret = super.remove(key);
		reverseMap.remove(ret);
		return ret;
	}

	public K removeValue(final Object value) {
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
	public boolean remove(final Object key, final Object value) {
		if (super.remove(key, value)) {
			reverseMap.remove(value);
			return true;
		}
		return false;
	}

	@Override
	public boolean replace(final K key, final V oldValue, final V newValue) {
		if (super.replace(key, oldValue, newValue)) {
			reverseMap.remove(oldValue);
			final K old = reverseMap.put(newValue, key);
			if (old != null) {
				super.remove(old);
			}
			return true;
		}
		return false;
	}

	@Override
	public V replace(final K key, final V value) {
		final V ret = super.replace(key, value);
		if (ret != null) {
			reverseMap.remove(ret);
			final K old = reverseMap.put(value, key);
			if (old != null) {
				super.remove(old);
			}
		}

		return ret;
	}

	@Override
	public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
		throw new UnsupportedOperationException("I haven't made this yet!");
	}

	@Override
	public V put(final K key, final V value) {
		final V ret = super.put(key, value);
		final K old = reverseMap.put(value, key);
		if (ret != null) reverseMap.remove(ret);
		if (old != null) super.remove(old);
		return ret;
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		m.forEach(this::put);
	}

	@Override
	public V putIfAbsent(final K key, final V value) {
		final V ret = super.putIfAbsent(key, value);
		final K old = reverseMap.put(value, key);
		if (old != null) super.remove(old);
		return ret;
	}

	public K getKey(final Object value) {
		return reverseMap.get(value);
	}

	public K getKeyOrDefault(final Object value, final K defaultKey) {
		return reverseMap.getOrDefault(value, defaultKey);
	}

}
