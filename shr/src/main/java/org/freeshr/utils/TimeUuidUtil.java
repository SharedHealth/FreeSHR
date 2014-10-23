package org.freeshr.utils;

import java.util.Date;
import java.util.UUID;

/**
 * @see <a href="https://wiki.apache.org/cassandra/FAQ#working_with_timeuuid_in_java">https://wiki.apache.org/cassandra/FAQ</a>
 */
public class TimeUuidUtil {


    public static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

    public static java.util.UUID getTimeUUID()
    {
        return java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
    }

    /**
     * Returns an instance of uuid.
     *
     * @param uuid the uuid
     * @return the java.util. uuid
     */
    public static java.util.UUID toUUID( byte[] uuid )
    {
        long msb = 0;
        long lsb = 0;
        assert uuid.length == 16;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (uuid[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (uuid[i] & 0xff);
        long mostSigBits = msb;
        long leastSigBits = lsb;

        com.eaio.uuid.UUID u = new com.eaio.uuid.UUID(msb,lsb);
        return java.util.UUID.fromString(u.toString());
    }

    /**
     * As byte array.
     *
     * @param uuid the uuid
     *
     * @return the byte[]
     */
    public static byte[] asByteArray(java.util.UUID uuid)
    {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }

        return buffer;
    }

    public static java.util.UUID uuidForDate(Date d)
    {
        /*
          Magic number obtained from #cassandra's thobbs, who
          claims to have stolen it from a Python library.
        */

        long origTime = d.getTime();
        long time = origTime * 10000 + NUM_100NS_INTERVALS_SINCE_UUID_EPOCH;
        long timeLow = time &       0xffffffffL;
        long timeMid = time &   0xffff00000000L;
        long timeHi = time & 0xfff000000000000L;
        long upperLong = (timeLow << 32) | (timeMid >> 16) | (1 << 12) | (timeHi >> 48) ;
        return new java.util.UUID(upperLong, 0xC000000000000000L);
    }

    public static long getTimeFromUUID(UUID uuid) {
        return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
    }
}
