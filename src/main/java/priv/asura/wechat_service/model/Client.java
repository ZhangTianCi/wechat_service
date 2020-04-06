package priv.asura.wechat_service.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

/**
 * 使用者
 */
@Data
public class Client implements Serializable {
    /**
     * 唯一编号
     */
    private String id;
    /**
     * 密码
     */
    private String secret;
    /**
     * 其它信息
     */
    private Map<String, Object> infos;

    public Map<String, Object> getInfos() {
        if (infos == null) {
            return new HashMap<>(0);
        } else {
            return infos;
        }
    }
}
