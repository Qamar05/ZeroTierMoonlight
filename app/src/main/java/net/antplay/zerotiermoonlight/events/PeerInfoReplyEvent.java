package net.antplay.zerotiermoonlight.events;

import com.zerotier.sdk.Peer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * Respond to node information events
 *
 * @author kaaass
 */
@Data
@AllArgsConstructor
public class PeerInfoReplyEvent {

    @Getter
    private final Peer[] peers;


}
