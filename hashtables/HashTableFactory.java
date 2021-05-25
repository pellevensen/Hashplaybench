package hashtables;

public interface HashTableFactory {
	<K, V> HashTable<K, V> newInstance();
}
