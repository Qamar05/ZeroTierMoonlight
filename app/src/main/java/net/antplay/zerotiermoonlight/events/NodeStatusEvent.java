package net.antplay.zerotiermoonlight.events;

import com.zerotier.sdk.NodeStatus;
import com.zerotier.sdk.Version;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Node Status Events
 *
 * Fired when a {@link NodeStatusRequestEvent} or Zerotier event is encountered
 */
@Data
@AllArgsConstructor
public class NodeStatusEvent {

    /**
     * Node's own state
     */
    private final NodeStatus status;

    /**
     * Client Zerotier program version
     */
    private final Version clientVersion;


    public NodeStatus getStatus() {
        return status;
    }

    public Version getClientVersion() {
        return clientVersion;
    }
}
