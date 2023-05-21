package net.antplay.zerotiermoonlight.events;

import com.zerotier.sdk.VirtualNetworkConfig;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ZT network configuration successfully changed event
 */
@Data
@AllArgsConstructor
public class VirtualNetworkConfigChangedEvent {
    private final VirtualNetworkConfig virtualNetworkConfig;



    public VirtualNetworkConfig getVirtualNetworkConfig() {
        return virtualNetworkConfig;
    }
}
