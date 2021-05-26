package hashtables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.TreeMap;

public class HashShootout {
	private static final int TEST_RUNS = 10;
	private SplittableRandom rng;
	private final HashTableFactory factory;

	private HashShootout(HashTableFactory factory) {
		this.factory = factory;
		resetRNG();
	}

	private static class BadHashObject {
		private final long v;

		public BadHashObject(long v) {
			long x = v;
			// Moremur:
			// http://mostlymangling.blogspot.com/2019/12/stronger-better-morer-moremur-better.html
			x ^= x >>> 27;
			x *= 0x3C79AC492BA7B653L;
			x ^= x >>> 33;
			x *= 0x1C69B3F74AC4AE35L;
			x ^= x >>> 27;

			this.v = x;
		}

		@Override
		public int hashCode() {
			// Deliberate; limit this class to only yield 256 distinct hash values.
			return (int) (this.v & 255);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			BadHashObject other = (BadHashObject) obj;
			return this.v == other.v;
		}
	}

	private void resetRNG() {
		this.rng = new SplittableRandom(1);
	}

	private String randString(int length) {
		char[] s = new char[length];
		for (int i = 0; i < length; i++) {
			s[i] = (char) ('A' + this.rng.nextInt(26));
		}
		return String.valueOf(s);
	}

	private int runPutTestIntInt(int tests) {
		resetRNG();
		HashTable<Integer, Integer> h = this.factory.newInstance();
		for (int i = 0; i < tests; i++) {
			h.put(this.rng.nextInt(), this.rng.nextInt());
		}
		return h.size();
	}

	private int runPutTestBadInt(int tests) {
		resetRNG();
		HashTable<BadHashObject, Integer> h = this.factory.newInstance();
		for (int i = 0; i < tests; i++) {
			h.put(new BadHashObject(this.rng.nextInt()), this.rng.nextInt());
		}
		return h.size();
	}

	private int runPutTestStringInt(List<String> keys) {
		resetRNG();
		HashTable<String, Integer> h = this.factory.newInstance();
		for (String k : keys) {
			h.put(k, this.rng.nextInt());
		}
		return h.size();
	}

	private int runPutTestStringString(List<String> keys, List<String> values) {
		HashTable<String, String> h = buildStringString(keys, values);
		return h.size();
	}

	private HashTable<String, String> buildStringString(List<String> keys, List<String> values) {
		resetRNG();
		HashTable<String, String> h = this.factory.newInstance();
		Iterator<String> keyIt = keys.iterator();
		Iterator<String> valueIt = values.iterator();
		while (keyIt.hasNext()) {
			h.put(keyIt.next(), valueIt.next());
		}
		return h;
	}

	private static int runGetTestStringString(HashTable<String, String> ht, List<String> values) {
		int hits = 0;
		for (String k : values) {
			hits += ht.get(k) != null ? 1 : 0;
		}
		return hits;
	}

	private int runPutTestIntString(List<String> values) {
		resetRNG();
		HashTable<Integer, String> h = this.factory.newInstance();
		for (String v : values) {
			h.put(this.rng.nextInt(), v);
		}
		return h.size();
	}

	private List<String> getRandomStrings(int size, int length) {
		List<String> strings = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			strings.add(randString(length));
		}
		return strings;
	}

	private static void generateAndAddList(List<List<String>> lists, int size, int len, HashShootout s) {
		List<String> l = s.getRandomStrings(size, len);
		// Deliberately shuffle to not get overly positive results due to
		// very optimistic cache-hit ratio.
		Collections.shuffle(l);
		lists.add(l);
	}

	private static class HashMapAdapter<K, V> implements HashTable<K, V> {
		private final HashMap<K, V> map;

		public HashMapAdapter() {
			this.map = new HashMap<>();
		}

		@Override
		public void put(K key, V value) {
			this.map.put(key, value);
		}

		@Override
		public V get(K key) {
			return this.map.get(key);
		}

		@Override
		public int size() {
			return this.map.size();
		}
	}

	private static void incrementTime(Map<String, Double> accs, String id, double inc, int tests) {
		Double v = accs.get(id);
		double adjInc = inc / tests;
		if (v == null) {
			accs.put(id, adjInc);
		} else {
			accs.put(id, v + adjInc);
		}
	}

	public static void main(String[] args) {
		List<HashTableFactory> factories = new ArrayList<>();
		factories.add(SeparateChainingHash2K::new);
		factories.add(SeparateChainingHashPrime::new);
		factories.add(OpenAddressingHash2K::new);
		factories.add(OpenAddressingHashPrime::new);
		factories.add(HashMapAdapter::new);
		List<HashShootout> hs = new ArrayList<>();
		Map<String, Double> accumulatedTimes = new TreeMap<>();
		int testRuns = TEST_RUNS;
		int keys = 100000;

		if (args.length >= 1) {
			testRuns = Integer.parseInt(args[0]);
			if (args.length == 2) {
				keys = Integer.parseInt(args[1]);
			}
		}
		System.err.printf("Will do %d testruns with %d keys each.\n", testRuns, keys);
		// Set to true if you have an urge to wait for a very very long time.
		// Concrete example of the importance of not choosing a terrible
		// hash function.
		final boolean iAmReallyReallyPatient = false;

		for (HashTableFactory f : factories) {
			hs.add(new HashShootout(f));
		}
		HashShootout dummyShootout = new HashShootout(null);

		for (int testIdx = 0; testIdx < testRuns; testIdx++) {
			List<List<String>> keyLists1 = new ArrayList<>();
			List<List<String>> keyLists2 = new ArrayList<>();
			List<List<String>> valueLists = new ArrayList<>();
			for (int len = 1; len < 50; len = Math.max(len + 1, len * 2)) {
				generateAndAddList(keyLists1, keys, len, dummyShootout);
				generateAndAddList(keyLists2, keys, len, dummyShootout);
				generateAndAddList(valueLists, keys, len, dummyShootout);
			}

			// Run the implementations in different order each time.
			Collections.shuffle(hs);

			for (HashShootout s : hs) {
				for (int j = 0; j < keyLists1.size(); j++) {
					final List<String> keyList1 = keyLists1.get(j);
					final List<String> keyList2 = keyLists2.get(j);
					final List<String> valueList = valueLists.get(j);
					final int testSize = keys;
					int keyLength = keyList1.get(0).length();
					System.gc();
					incrementTime(accumulatedTimes, getTestId("PutII", s), timeCall(() -> s.runPutTestIntInt(testSize)),
							keys);
					incrementTime(accumulatedTimes, getTestId("PutSI" + String.format("%03d", keyLength), s),
							timeCall(() -> s.runPutTestStringInt(keyList1)), keys);
					incrementTime(accumulatedTimes, getTestId("PutSS" + String.format("%03d", keyLength), s),
							timeCall(() -> s.runPutTestStringString(keyList1, valueList)), keys);
					incrementTime(accumulatedTimes, getTestId("PutIS", s),
							timeCall(() -> s.runPutTestIntString(valueList)), keys);
					HashTable<String, String> preinsertedKeys = s.buildStringString(keyList1, valueList);
					incrementTime(accumulatedTimes, getTestId("GetHitsSS" + String.format("%03d", keyLength), s),
							timeCall(() -> HashShootout.runGetTestStringString(preinsertedKeys, keyList1)), keys);
					incrementTime(accumulatedTimes, getTestId("GetMissSS" + String.format("%03d", keyLength), s),
							timeCall(() -> HashShootout.runGetTestStringString(preinsertedKeys, keyList2)), keys);
					if (iAmReallyReallyPatient) {
						incrementTime(accumulatedTimes, getTestId("PutBI", s),
								timeCall(() -> s.runPutTestBadInt(testSize)), keys);
					}
				}
				System.out.print(testIdx % 10);
			}
		}
		System.out.println("");
		for (Map.Entry<String, Double> d : accumulatedTimes.entrySet()) {
			System.out.printf("%40s:\t%8.1f ns/operation\n", d.getKey(), d.getValue() / testRuns);
		}
	}

	private static long timeCall(Runnable test) {
		long now = System.nanoTime();
		test.run();
		return System.nanoTime() - now;
	}

	private static String getTestId(String testName, HashShootout s) {
		return String.format("%40s\t%10s", s.factory.newInstance().getClass().getName(), testName);
	}
}
