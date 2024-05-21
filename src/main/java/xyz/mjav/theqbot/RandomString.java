package xyz.mjav.theqbot;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Class to generate random strings
 * @author https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 *
 * = Usage examples =
 *  o Create an insecure generator for 8-character identifiers:
 *
 *     RandomString gen = new RandomString(8, ThreadLocalRandom.current());
 *
 *  o Create a secure generator for session identifiers:
 *
 *     RandomString session = new RandomString();
 *
 *  o Create a generator with easy-to-read codes for printing. The strings are longer than full alphanumeric strings to compensate for using fewer symbols:
 *
 * String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
 * RandomString tickets = new RandomString(23, new SecureRandom(), easy);
 *
 * = Use as session identifiers =
 *
 * Generating session identifiers that are likely to be unique is not good enough, or you could just use a simple counter.
 * Attackers hijack sessions when predictable identifiers are used.
 *
 * There is tension between length and security. Shorter identifiers are easier to guess, because there are fewer possibilities.
 * But longer identifiers consume more storage and bandwidth. A larger set of symbols helps, but might cause encoding problems if
 * identifiers are included in URLs or re-entered by hand.
 *
 * The underlying source of randomness, or entropy, for session identifiers should come from a random number generator designed
 * for cryptography. However, initializing these generators can sometimes be computationally expensive or slow, so effort should
 * be made to re-use them when possible.
 *
 * = Use as object identifiers =
 *
 * Not every application requires security. Random assignment can be an efficient way for multiple entities to generate identifiers
 * in a shared space without any coordination or partitioning. Coordination can be slow, especially in a clustered or distributed
 * environment, and splitting up a space causes problems when entities end up with shares that are too small or too big.
 *
 * Identifiers generated without taking measures to make them unpredictable should be protected by other means if an attacker might
 * be able to view and manipulate them, as happens in most web applications. There should be a separate authorization system that protects
 * objects whose identifier can be guessed by an attacker without access permission.
 *
 * Care must be also be taken to use identifiers that are long enough to make collisions unlikely given the anticipated total number of identifiers.
 * This is referred to as "the birthday paradox." The probability of a collision, p, is approximately n2/(2qx), where n is the number of identifiers
 * actually generated, q is the number of distinct symbols in the alphabet, and x is the length of the identifiers. This should be a very small
 * number, like 2‑50 or less.
 *
 * Working this out shows that the chance of collision among 500k 15-character identifiers is about 2‑52, which is probably less likely than
 * undetected errors from cosmic rays, etc.
 *
 * = Comparison with UUIDs =
 * According to their specification, UUIDs are not designed to be unpredictable, and should not be used as session identifiers.
 *
 * UUIDs in their standard format take a lot of space: 36 characters for only 122 bits of entropy. (Not all bits of a "random" UUID are selected randomly.)
 * A randomly chosen alphanumeric string packs more entropy in just 21 characters.
 *
 * UUIDs are not flexible; they have a standardized structure and layout. This is their chief virtue as well as their main weakness.
 * When collaborating with an outside party, the standardization offered by UUIDs may be helpful. For purely internal use, they can be inefficient.
 *
 */
public class RandomString {

    /**
     * Generate a random string.
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String lower = upper.toLowerCase(Locale.ROOT);

    public static final String digits = "0123456789";

    public static final String alphanum = upper + lower + digits;

    private final Random random;

    private final char[] symbols;

    private final char[] buf;

    public RandomString(int length, Random random, String symbols) {
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an alphanumeric string generator.
     */
    public RandomString(int length, Random random) {
        this(length, random, alphanum);
    }

    /**
     * Create an alphanumeric strings from a secure generator.
     */
    public RandomString(int length) {
        this(length, new SecureRandom());
    }

    /**
     * Create session identifiers.
     */
    public RandomString() {
        this(21);
    }

}