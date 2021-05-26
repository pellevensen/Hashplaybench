package hashtables;

public enum HashUtils {
	;

	public static long mix(long v) {
		long x = v;
		// Moremur:
		// http://mostlymangling.blogspot.com/2019/12/stronger-better-morer-moremur-better.html
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		x ^= x >>> 27;

		return x;
	}

	// A utility to somewhat alleviate the consequences of (some)
	// bad hash functions.
	// Has the property that a one bit change in input will yield
	// (approximately) 16 bit changes in the output.
	public static int mix(int v) {
		long x = mix((long) v);
		return (int) ((x ^ x >>> 32) & 0xFFFFFFFFL);
	}
}
