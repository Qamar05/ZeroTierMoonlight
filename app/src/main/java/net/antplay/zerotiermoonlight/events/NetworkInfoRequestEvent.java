package net.antplay.zerotiermoonlight.events;

public class NetworkInfoRequestEvent {
    private final long networkId;

    public NetworkInfoRequestEvent(long j) {
        this.networkId = j;
    }

    public long getNetworkId() {
        return this.networkId;
    }
}
