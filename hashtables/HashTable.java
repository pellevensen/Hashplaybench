package hashtables;

public interface HashTable<K, V> {
	void put(K key, V value);

	V get(K key);

	int size();
}
