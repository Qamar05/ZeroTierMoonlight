package net.antplay.zerotiermoonlight.events;

import com.zerotier.sdk.VirtualNetworkConfig;


import net.antplay.zerotiermoonlight.model.Network;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *Result event for updating network configuration
 */
@Data
@AllArgsConstructor
public class NetworkReconfigureEvent {
    private final boolean changed;
    private final Network network;
    private final VirtualNetworkConfig virtualNetworkConfig;


    public boolean isChanged() {
        return changed;
    }

    public Network getNetwork() {
        return network;
    }

    public VirtualNetworkConfig getVirtualNetworkConfig() {
        return virtualNetworkConfig;
    }
}
