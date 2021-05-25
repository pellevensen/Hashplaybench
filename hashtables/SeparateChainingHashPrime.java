package hashtables;

import java.util.LinkedList;
import java.util.List;

public class SeparateChainingHashPrime<K, V> implements HashTable<K,V>{
	private static final double MAX_LOAD_FACTOR = 2;
	private static final int INITIAL_CAPACITY = 8;
	private List<Entry<K,V>>[] entries;
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
	public SeparateChainingHashPrime() {
		this.entries = new LinkedList[INITIAL_CAPACITY];
		for(int i = 0; i < INITIAL_CAPACITY; i++) {
			this.entries[i] = new LinkedList<>();
		}
		this.size = 0;
	}

	@SuppressWarnings("unchecked")
	private SeparateChainingHashPrime(int oldCapacity) {
		this.entries = new LinkedList[oldCapacity * 2];
		for(int i = 0; i < this.entries.length; i++) {
			this.entries[i] = new LinkedList<>();
		}
		this.size = 0;
	}

	@Override
	public void put(K key, V value) {
		// Must mask the most significant bit to avoid negative table indices.
		int hash = (key.hashCode() & 0x7FFFFFFF) % this.entries.length;
		for(Entry<K, V> e : this.entries[hash]) {
			if(e.key.equals(key)) {
				e.value = value;
				return;
			}
		}
		this.entries[hash].add(new Entry<>(key, value));
		this.size++;
		if(loadFactorTooHigh()) {
			growTable();
		}
	}

	private void growTable() {
		// Let n denote the new *capacity* and m the *size* of the current table.
		// Create a new table, twice the size of the current table (in O(n)).
		SeparateChainingHashPrime<K,V> newTable = new SeparateChainingHashPrime<>(this.entries.length);

		// Add all entries from the current table to the new table. (In O(max(n, m)).
		for(List<Entry<K,V>> e : this.entries) {
			if(e != null) {
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
		// Must mask the most significant bit to avoid negative table indices.
		int hash = (key.hashCode() & 0x7FFFFFFF) % this.entries.length;
		for(Entry<K, V> e : this.entries[hash]) {
			if(e.key.equals(key)) {
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
