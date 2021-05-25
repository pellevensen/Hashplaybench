package hashtables;

public class OpenAddressingHash2K<K,V> implements HashTable<K,V> {
	private static class Entry<K, V> {
		public K key;
		public V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
	private Entry<K,V>[] entries;
	private static final int INITIAL_CAPACITY = 4; // Must be a power of two.
	private static final double MAX_LOAD_FACTOR = 0.75;
	private int size;

	@SuppressWarnings("unchecked")
	public OpenAddressingHash2K() {
		// No way to instantiate an array with proper generics.
		// This is safe as long as we keep Entry private.
		// Non-solution:
		// If we were to make the Entry class non-static, the consequence
		// would be that each Entry object will carry a reference to the
		// enclosing OpenAddressingHash-instance.
		this.entries = new Entry[INITIAL_CAPACITY];
		this.size = 0;
	}

	@SuppressWarnings("unchecked")
	private OpenAddressingHash2K(int oldCapacity) {
		// See note for OpenAddressingHash above.
		this.entries = new Entry[oldCapacity * 2];
		this.size = 0;
	}

	private void growTable() {
		OpenAddressingHash2K<K,V> newTable = new OpenAddressingHash2K<>(this.entries.length);
		for(Entry<K,V> e : this.entries) {
			if(e != null) {
				newTable.put(e.key, e.value);
			}
		}
		this.entries = newTable.entries;
	}

	@Override
	public void put(K key, V value) {
		int hash = key.hashCode() & this.entries.length - 1;
		while(this.entries[hash] != null) {
			if(this.entries[hash].key.equals(key)) {
				this.entries[hash].value = value;
				return;
			}
			hash = hash + 1 & this.entries.length - 1;
		}
		this.entries[hash] = new Entry<>(key, value);
		this.size++;
		if(loadFactorTooHigh()) {
			growTable();
		}
	}

	@Override
	public V get(K key) {
		int hash = key.hashCode() & this.entries.length - 1;
		while(this.entries[hash] != null) {
			if(this.entries[hash].key.equals(key)) {
				return this.entries[hash].value;
			}
			hash = hash + 1 & this.entries.length - 1;
		}
		return null;
	}

	private boolean loadFactorTooHigh() {
		return this.entries.length * MAX_LOAD_FACTOR < this.size;
	}

	@Override
	public int size() {
		return this.size;
	}

}
