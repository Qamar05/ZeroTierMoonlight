package net.antplay.zerotiermoonlight.events;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * VPN error event
 */
@Data
@AllArgsConstructor
public class VPNErrorEvent {
    private final String message;

    public String getMessage() {
        return message;
    }
}
