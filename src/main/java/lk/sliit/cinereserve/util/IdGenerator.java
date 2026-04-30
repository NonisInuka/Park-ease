package lk.sliit.cinereserve.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * IdGenerator – Thread-safe ID generation utility.
 *
 * Generates unique booking IDs in the format "BK0001", "BK0002", etc.
 * Numeric IDs for movies, users, showtimes are generated via FileHandler.getLastId() + 1.
 */
public class IdGenerator {

    private static final AtomicInteger bookingCounter = new AtomicInteger(0);

    /**
     * Sets the starting counter (call once at startup after reading bookings.txt).
     */
    public static void initBookingCounter(int lastId) {
        bookingCounter.set(lastId);
    }

    /**
     * Returns the next booking ID string e.g. "BK0042".
     */
    public static String nextBookingId() {
        int next = bookingCounter.incrementAndGet();
        return String.format("BK%04d", next);
    }

    /**
     * Returns the next integer ID for movies, users, showtimes.
     */
    public static int nextIntId(FileHandler fh) {
        return fh.getLastId() + 1;
    }

    private IdGenerator() {} // Utility class – no instantiation
}
