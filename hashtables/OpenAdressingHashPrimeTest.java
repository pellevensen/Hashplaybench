package hashtables;

class OpenAdressingHashPrimeTest extends HashTableTest {

	@Override
	HashTableFactory getHashTableFactory() {
		return OpenAddressingHashPrime::new;
	}
}
