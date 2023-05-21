package net.antplay.zerotiermoonlight.events;

import lombok.Data;

/**
 * 应答服务是否运行事件
 */
@Data
public class IsServiceRunningReplyEvent {
    private final boolean isRunning;

    public IsServiceRunningReplyEvent(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
