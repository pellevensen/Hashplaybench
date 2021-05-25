package hashtables;

class SeparateChainingHashPrimeTest extends HashTableTest {

	@Override
	HashTableFactory getHashTableFactory() {
		return SeparateChainingHashPrime::new;
	}
}
