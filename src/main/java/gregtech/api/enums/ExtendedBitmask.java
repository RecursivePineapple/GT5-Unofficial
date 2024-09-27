package gregtech.api.enums;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bootleg {@link java.util.BitSet} that uses an enum as the bit index.
 */
public class ExtendedBitmask<T extends Enum<T>> {

    private static final Map<Class<?>, Object[]> ENUM_VALUES = new ConcurrentHashMap<>();

    private final int bitCount;
    private final int[] bitChunks;

    public ExtendedBitmask(Class<T> enumClass) {
        bitCount = ENUM_VALUES.computeIfAbsent(enumClass, ExtendedBitmask::getEnumValues).length;

        int round = bitCount % 32 > 0 ? 1 : 0;

        bitChunks = new int[bitCount / 32 + round];
    }

    private static Object[] getEnumValues(Object enumClass) {
        try {
            return (Object[]) ((Class<?>) enumClass).getMethod("values").invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new RuntimeException("Could not determine bit count for enum " + enumClass, e);
        }
    }

    public boolean has(T first) {
        int bit = first.ordinal();

        int chunk = bit >> 5;

        return (bitChunks[chunk] & (0b1 << (bit & 0b11111))) != 0;
    }

    public boolean has(T first, T second) {
        int firstBit = first.ordinal(), secondBit = second.ordinal();
        int firstChunk = firstBit >> 5, secondChunk = secondBit >> 5;

        if (firstChunk == secondChunk) {
            int chunk = bitChunks[firstChunk];

            return (chunk & (0b1 << (firstBit & 0b11111))) != 0 &&
                (chunk & (0b1 << (secondBit & 0b11111))) != 0;
        } else {
            return (bitChunks[firstChunk] & (0b1 << (firstBit & 0b11111))) != 0 &&
                (bitChunks[secondChunk] & (0b1 << (secondBit & 0b11111))) != 0;
        }
    }

    public boolean has(T first, T second, T third) {
        int firstBit = first.ordinal(), secondBit = second.ordinal(), thirdBit = second.ordinal();
        int firstChunk = firstBit >> 5, secondChunk = secondBit >> 5, thirdChunk = thirdBit >> 5;

        if (firstChunk == secondChunk && firstChunk == thirdChunk) {
            int chunk = bitChunks[firstChunk];

            return (chunk & (0b1 << (firstBit & 0b11111))) != 0 &&
                (chunk & (0b1 << (secondBit & 0b11111))) != 0 &&
                (chunk & (0b1 << (thirdBit & 0b11111))) != 0;
        } else {
            return (bitChunks[firstChunk] & (0b1 << (firstBit & 0b11111))) != 0 &&
                (bitChunks[secondChunk] & (0b1 << (secondBit & 0b11111))) != 0 &&
                (bitChunks[thirdChunk] & (0b1 << (thirdBit & 0b11111))) != 0;
        }
    }

    public boolean hasMany(@SuppressWarnings("unchecked") T... rest) {
        int lastChunk = 0, lastChunkIndex = -1;

        for(int i = 0; i < rest.length; i++) {
            T flag = rest[i];
            int bit = flag.ordinal();

            int chunk = bit / 32;

            if (chunk != lastChunkIndex) {
                lastChunk = bitChunks[chunk];
                lastChunkIndex = chunk;
            }

            if ((lastChunk & (0b1 << (bit % 32))) == 0) {
                return false;
            }
        }

        return true;
    }

    public boolean hasAny(@SuppressWarnings("unchecked") T... flags) {
        for (T flag : flags) {
            if (has(flag)) {
                return true;
            }
        }
        
        return false;
    }

    public void add(T first) {
        int bit = first.ordinal();

        int chunk = bit >> 5;

        bitChunks[chunk] |= 0b1 << (bit & 0b11111);
    }

    public void add(T first, T second) {
        int firstBit = first.ordinal(), secondBit = second.ordinal();
        int firstChunk = firstBit >> 5, secondChunk = secondBit >> 5;

        if (firstChunk == secondChunk) {
            bitChunks[firstChunk] |= 0b1 << (firstBit & 0b11111) | 0b1 << (secondBit & 0b11111);
        } else {
            bitChunks[firstChunk] |= 0b1 << (firstBit & 0b11111);
            bitChunks[secondChunk] |= 0b1 << (secondBit & 0b11111);
        }
    }

    public void add(T first, T second, T third) {
        int firstBit = first.ordinal(), secondBit = second.ordinal(), thirdBit = second.ordinal();
        int firstChunk = firstBit >> 5, secondChunk = secondBit >> 5, thirdChunk = thirdBit >> 5;

        if (firstChunk == secondChunk && firstChunk == thirdChunk) {
            bitChunks[firstChunk] |= 0b1 << (firstBit & 0b11111) |
                0b1 << (secondBit & 0b11111) |
                0b1 << (thirdBit & 0b11111);
        } else {
            bitChunks[firstChunk] |= 0b1 << (firstBit & 0b11111);
            bitChunks[secondChunk] |= 0b1 << (secondBit & 0b11111);
            bitChunks[thirdChunk] |= 0b1 << (thirdBit & 0b11111);
        }
    }

    public void addMany(@SuppressWarnings("unchecked") T... flags) {
        for (int i = 0; i < flags.length; i++) {
            T flag = flags[i];

            int bit = flag.ordinal();
            int chunk = bit >> 5;

            bitChunks[chunk] |= 0b1 << (bit & 0b11111);
        }
    }

    public void remove(T first) {
        int bit = first.ordinal();

        int chunk = bit >> 5;

        bitChunks[chunk] &= ~(0b1 << (bit & 0b11111));
    }

    public void remove(T first, T second) {
        int firstBit = first.ordinal(), secondBit = second.ordinal();
        int firstChunk = firstBit >> 5, secondChunk = secondBit >> 5;

        if (firstChunk == secondChunk) {
            bitChunks[firstChunk] &= ~(0b1 << (firstBit & 0b11111) | 0b1 << (secondBit & 0b11111));
        } else {
            bitChunks[firstChunk] &= ~(0b1 << (firstBit & 0b11111));
            bitChunks[secondChunk] &= ~(0b1 << (secondBit & 0b11111));
        }
    }

    public void remove(T first, T second, T third) {
        int firstBit = first.ordinal(), secondBit = second.ordinal(), thirdBit = second.ordinal();
        int firstChunk = firstBit >> 5, secondChunk = secondBit >> 5, thirdChunk = thirdBit >> 5;

        if (firstChunk == secondChunk && firstChunk == thirdChunk) {
            bitChunks[firstChunk] &= ~(
                0b1 << (firstBit & 0b11111) |
                0b1 << (secondBit & 0b11111) |
                0b1 << (thirdBit & 0b11111));
        } else {
            bitChunks[firstChunk] &= ~(0b1 << (firstBit & 0b11111));
            bitChunks[secondChunk] &= ~(0b1 << (secondBit & 0b11111));
            bitChunks[thirdChunk] &= ~(0b1 << (thirdBit & 0b11111));
        }
    }

    public void removeMany(@SuppressWarnings("unchecked") T... flags) {
        for (int i = 0; i < flags.length; i++) {
            T flag = flags[i];

            int bit = flag.ordinal();
            int chunk = bit >> 5;

            bitChunks[chunk] &= ~(0b1 << (bit & 0b11111));
        }
    }
}
