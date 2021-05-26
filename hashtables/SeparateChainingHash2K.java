package hashtables;

import java.util.LinkedList;
import java.util.List;

public class SeparateChainingHash2K<K, V> implements HashTable<K, V> {
	private static final double MAX_LOAD_FACTOR = 2;
	private static final int INITIAL_CAPACITY = 8; // Must be a power of two.
	private List<Entry<K, V>>[] entries;
	private int size;

	private static class Entry<K, V> {
		public K key;
		public V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	@SuppressWarnings("unchecked")
	public SeparateChainingHash2K() {
		this.entries = new LinkedList[INITIAL_CAPACITY];
		for (int i = 0; i < INITIAL_CAPACITY; i++) {
			this.entries[i] = new LinkedList<>();
		}
		this.size = 0;
	}

	@SuppressWarnings("unchecked")
	private SeparateChainingHash2K(int oldCapacity) {
		this.entries = new LinkedList[oldCapacity * 2];
		for (int i = 0; i < this.entries.length; i++) {
			this.entries[i] = new LinkedList<>();
		}
		this.size = 0;
	}

	@Override
	public void put(K key, V value) {
		// In general:
		// int hash = key.hashCode() % arr.length;
		//
		// The identity below holds iff arr.length is a power of two.
		// m = 2^k => a % m == a & (m - 1) [which is *much* faster to compute.]
		//
		// Short reasoning focusing mainly on disadvantages of m = 2^k:
		// https://qr.ae/pGKIG4
		// If the hash function is good, 2^k will work well.
		// If the hash function is not so good (how do we know?),
		// a prime (or at least an odd number) is a better choice for m.
		int hash = HashUtils.mix(key.hashCode()) & this.entries.length - 1;
		for (Entry<K, V> e : this.entries[hash]) {
			if (e.key.equals(key)) {
				e.value = value;
				return;
			}
		}
		this.entries[hash].add(new Entry<>(key, value));
		this.size++;
		if (loadFactorTooHigh()) {
			growTable();
		}
	}

	private void growTable() {
		// Let n denote the new *capacity* and m the *size* of the current table.
		// Create a new table, twice the size of the current table (in O(n)).
		SeparateChainingHash2K<K, V> newTable = new SeparateChainingHash2K<>(this.entries.length);

		// Add all entries from the current table to the new table. (In O(max(n, m)).
		for (List<Entry<K, V>> e : this.entries) {
			if (e != null) {
				e.forEach(x -> newTable.put(x.key, x.value));
			}
		}
		// Finally, assign the new set of entries to the current set of entries.
		// In O(1).
		this.entries = newTable.entries;
	}

	private boolean loadFactorTooHigh() {
		return this.entries.length * MAX_LOAD_FACTOR < this.size;
	}

	@Override
	public V get(K key) {
		int hash = HashUtils.mix(key.hashCode()) & this.entries.length - 1;
		for (Entry<K, V> e : this.entries[hash]) {
			if (e.key.equals(key)) {
				return e.value;
			}
		}
		return null;
	}

	@Override
	public int size() {
		return this.size;
	}

}
