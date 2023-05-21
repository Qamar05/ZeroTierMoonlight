package net.antplay.zerotiermoonlight.events;

import androidx.appcompat.widget.SwitchCompat;


import net.antplay.zerotiermoonlight.model.Network;

import lombok.Data;

/**
 * 网络列表点击按钮事件。用于在后台进程控制 ZT 服务的启停
 */
@Data
public class NetworkListCheckedChangeEvent {
    private final SwitchCompat switchHandle;
    private final boolean checked;
    private final Network selectedNetwork;

    public NetworkListCheckedChangeEvent(SwitchCompat switchHandle, boolean checked, Network selectedNetwork) {
        this.switchHandle = switchHandle;
        this.checked = checked;
        this.selectedNetwork = selectedNetwork;
    }

    public SwitchCompat getSwitchHandle() {
        return switchHandle;
    }

    public boolean isChecked() {
        return checked;
    }

    public Network getSelectedNetwork() {
        return selectedNetwork;
    }
}
