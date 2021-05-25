package hashtables;

class OpenAdressingHash2KTest extends HashTableTest {

	@Override
	HashTableFactory getHashTableFactory() {
		return OpenAddressingHash2K::new;
	}
}
