package com.plantronics.oda.sample.websockets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by mramakrishnan on 8/15/17.
 */
public enum MessageType {
    CONNECT_SUCCESS("connectionSuccess"),PING_MESSAGE("ping"),LQ_CALLEND_NOTIFICATION("linkQualityCallEndNotification");

    private String value;

    private MessageType(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MessageType fromValue(final String value) {
        for (MessageType current : MessageType.values()) {
            if (current.getValue().equals(value)) {
                return current;
            }
        }

        throw new IllegalArgumentException("Invalid enumeration value=" + value);
    }
}
