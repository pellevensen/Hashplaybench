package hashtables;

class SeparateChainingHash2KTest extends HashTableTest {

	@Override
	HashTableFactory getHashTableFactory() {
		return SeparateChainingHash2K::new;
	}
}
