package org.freeshr.utils;

import me.prettyprint.cassandra.utils.TimeUUIDUtils;

import java.util.Date;
import java.util.UUID;

public class TimeUuidUtil {

    public static java.util.UUID uuidForDate(Date d) {
        long origTime = d.getTime();
        return uuidForDate(origTime);
    }

    public static UUID uuidForDate(long timeInMillis) {
        return new java.util.UUID(com.eaio.uuid.UUIDGen.createTime(timeInMillis), com.eaio.uuid.UUIDGen.getClockSeqAndNode());
    }


    public static long getTimeFromUUID(UUID uuid) {
        return TimeUUIDUtils.getTimeFromUUID(uuid);
    }

    public static Date getDateFromUUID(UUID uuid) {
        return new Date(getTimeFromUUID(uuid));
    }

    public static boolean isValidTimeUUID(String uuid) {
        try {
            final UUID parsedUUID = UUID.fromString(uuid);
            if (1 != parsedUUID.version()) return false;
            TimeUUIDUtils.getTimeFromUUID(parsedUUID);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}