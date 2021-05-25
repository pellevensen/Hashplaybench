package hashtables;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.util.BitSet;
import java.util.SplittableRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract class HashTableTest {
	private SplittableRandom rng;
	private final int MAX_OPS = 100000;
	private final double SIZE_GROWTH_RATE = 1.5;

	abstract HashTableFactory getHashTableFactory();

	@BeforeEach
	void setUp() throws Exception {
		this.rng = new SplittableRandom(1);
	}

	private int[] getUniqueInts(int size) {
		int[] values = new int[size];
		BitSet used = new BitSet();
		int r;
		for (int i = 0; i < size; i++) {
			do {
				r = this.rng.nextInt(size * 4) - size * 2;
			} while (used.get(Math.abs(r)));
			values[i] = r;
			used.set(Math.abs(r));
		}
		return values;
	}

	@Test
	/*
	 * Inserts i unique keys into the set and after all insertions,
	 * checks that the size matches.
	 *
	 * Terminates as failure if it takes too long (which is an indication
	 * of quadratic performance).
	 */
	void testPut1() {
		assertTimeoutPreemptively(ofSeconds(1), () -> {
			for (int i = 10; i <= this.MAX_OPS; i *= this.SIZE_GROWTH_RATE) {
				int[] keys = getUniqueInts(i);
				HashTable<Integer, Integer> h = getHashTableFactory().newInstance();
				for (int j = 0; j < i; j++) {
					// For our current purposes, all values can be the same.
					h.put(Integer.valueOf(keys[j]), Integer.valueOf(0));
				}
				assertEquals(i, h.size());
			}
		});

	}

	@Test
	/*
	 * Inserts i keys, some guaranteed to be duplicates, and checks that the
	 * size matches the number of unique keys.
	 *
	 * Terminates as failure if it takes too long (which is an indication
	 * of quadratic performance).
	 */
	void testPut2() {
		assertTimeoutPreemptively(ofSeconds(1), () -> {
			for (int i = 10; i <= this.MAX_OPS; i *= this.SIZE_GROWTH_RATE) {
				BitSet seenKeys = new BitSet();
				// Ensure that there are duplicates in keys since each key
				// is on [0, i / 2).
				int[] keys = this.rng.ints(0, i / 2).limit(i).toArray();
				// Not important that the values are unique.
				HashTable<Integer, Integer> h = getHashTableFactory().newInstance();
				for (int j = 0; j < i; j++) {
					seenKeys.set(keys[j]);
					h.put(Integer.valueOf(keys[j]), Integer.valueOf(0));
				}
				assertEquals(seenKeys.cardinality(), h.size());
			}
		});

	}

	@Test
	/*
	 * Inserts i unique keys into the set and after all insertions,
	 * checks that the values match the keys.
	 *
	 * Terminates as failure if it takes too long (which is an indication
	 * of quadratic performance).
	 */
	void testGet1() {
		assertTimeoutPreemptively(ofSeconds(1), () -> {
			for (int i = 10; i <= this.MAX_OPS; i *= this.SIZE_GROWTH_RATE) {
				int[] keys = getUniqueInts(i);
				SplittableRandom valueRNG = new SplittableRandom(2);
				// Not important that the values are unique.
				int[] values = valueRNG.ints().limit(i).toArray();
				HashTable<Integer, Integer> h = getHashTableFactory().newInstance();
				for (int j = 0; j < i; j++) {
					h.put(Integer.valueOf(keys[j]), Integer.valueOf(values[j]));
				}
				for(int j = 0; j < i; j++) {
					assertEquals(Integer.valueOf(values[j]), Integer.valueOf(h.get(Integer.valueOf(keys[j]))));
				}
			}
		});

	}

	@Test
	/*
	 * Inserts i keys, some guaranteed to be duplicates, and checks that the
	 * final keys match the expected values.
	 *
	 * Terminates as failure if it takes too long (which is an indication
	 * of quadratic performance).
	 */
	void testGet2() {
		assertTimeoutPreemptively(ofSeconds(1), () -> {
			for (int i = 10; i <= this.MAX_OPS; i *= this.SIZE_GROWTH_RATE) {
				SplittableRandom valueRNG = new SplittableRandom(3);
				int[] values = valueRNG.ints().limit(i).toArray();
				int[] keys = valueRNG.ints(0, i / 2).limit(i).toArray();
				HashTable<Integer, Integer> h = getHashTableFactory().newInstance();
				for (int j = 0; j < i; j++) {
					h.put(Integer.valueOf(keys[j]), Integer.valueOf(values[j]));
				}

				// We now traverse the keys in reverse order, skipping keys we have
				// already seen. This way we can verify that it is the last added
				// key's value that is in the map.
				BitSet seenKeys = new BitSet();
				for(int j = i - 1; j > 0; j--) {
					if(!seenKeys.get(keys[j])) {
						seenKeys.set(keys[j]);
						assertEquals(Integer.valueOf(values[j]), h.get(Integer.valueOf(keys[j])));
					}
				}
			}
		});

	}

}
