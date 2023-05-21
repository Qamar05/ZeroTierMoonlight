package net.antplay.zerotiermoonlight.events;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 增加 Moon 入轨信息事件
 * <p>
 * 当需要入轨某一 Moon 时触发，如用户配置入轨信息。该事件处理应仅进行入轨信息的管理，实际请求 ZT 的事件为
 * {@link OrbitMoonEvent}
 *
 * @author kaaass
 */
@Data
@AllArgsConstructor
public class AddMoonOrbitEvent {

    /**
     * Moon 地址
     */
    private Long moonWorldId;

    /**
     * Moon 种子
     */
    private Long moonSeed;

    /**
     * 是否从文件添加
     */
    private boolean fromFile;


    public Long getMoonWorldId() {
        return moonWorldId;
    }

    public void setMoonWorldId(Long moonWorldId) {
        this.moonWorldId = moonWorldId;
    }

    public Long getMoonSeed() {
        return moonSeed;
    }

    public void setMoonSeed(Long moonSeed) {
        this.moonSeed = moonSeed;
    }

    public boolean isFromFile() {
        return fromFile;
    }

    public void setFromFile(boolean fromFile) {
        this.fromFile = fromFile;
    }
}
