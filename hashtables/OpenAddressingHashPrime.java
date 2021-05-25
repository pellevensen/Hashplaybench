package hashtables;

import java.math.BigInteger;

public class OpenAddressingHashPrime<K,V> implements HashTable<K,V> {
	private static class Entry<K, V> {
		public K key;
		public V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
	private Entry<K,V>[] entries;
	private static final int INITIAL_CAPACITY = 5; // Must be prime.
	private static final double MAX_LOAD_FACTOR = 0.75;
	private static final double GROWTH_FACTOR = 1.6; // Must be > 1.

	private int size;

	@SuppressWarnings("unchecked")
	public OpenAddressingHashPrime() {
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
	private OpenAddressingHashPrime(int oldCapacity) {
		int smallestPossibleNewSize = (int) Math.max(oldCapacity * GROWTH_FACTOR, oldCapacity + 1);
		BigInteger newSize = BigInteger.valueOf(smallestPossibleNewSize).nextProbablePrime();
		// See note for OpenAddressingPrimeHash above.
		this.entries = new Entry[(int) newSize.longValue()];
		this.size = 0;
	}

	private void growTable() {
		OpenAddressingHashPrime<K,V> newTable = new OpenAddressingHashPrime<>(this.entries.length);
		for(Entry<K,V> e : this.entries) {
			if(e != null) {
				newTable.put(e.key, e.value);
			}
		}
		this.entries = newTable.entries;
	}

	@Override
	public void put(K key, V value) {
		// Must mask the most significant bit to avoid negative table indices.
		int hash = (key.hashCode() & 0x7FFFFFFF) % this.entries.length;
		while(this.entries[hash] != null) {
			if(this.entries[hash].key.equals(key)) {
				this.entries[hash].value = value;
				return;
			}
			hash = (hash + 1) % this.entries.length;
		}
		this.entries[hash] = new Entry<>(key, value);
		this.size++;
		if(loadFactorTooHigh()) {
			growTable();
		}
	}

	@Override
	public V get(K key) {
		// Must mask the most significant bit to avoid negative table indices.
		int hash = (key.hashCode() & 0x7FFFFFFF) % this.entries.length;
		while(this.entries[hash] != null) {
			if(this.entries[hash].key.equals(key)) {
				return this.entries[hash].value;
			}
			hash = (hash + 1) % this.entries.length;
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
