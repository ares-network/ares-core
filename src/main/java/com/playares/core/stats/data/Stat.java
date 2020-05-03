package com.playares.core.stats.data;

import com.playares.commons.util.general.Time;
import org.bson.types.ObjectId;

public interface Stat {
    /**
     * Returns the MongoDB Object ID
     * @return MongoDB Object ID
     */
    ObjectId getObjectId();

    /**
     * Returns the map number connected to this event
     * @return Map Number
     */
    int getMap();

    /**
     * Returns the description of this event
     * @return Description
     */
    String getDescription();

    /**
     * Returns the time in milliseconds this event occured at
     * @return
     */
    long getCreateTime();

    /**
     * Returns a string containing the time since this event occured
     * @return Time since event occured
     */
    default String getTimeSince() {
        return Time.convertToElapsed(getCreateTime() - Time.now());
    }
}