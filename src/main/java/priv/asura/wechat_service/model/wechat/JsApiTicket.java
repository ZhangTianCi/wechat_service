package priv.asura.wechat_service.model.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * 用于调用微信JS接口的临时票据
 */
@Data
public class JsApiTicket {
    /**
     * 错误码
     */
    @JsonProperty("errcode")
    private Integer errorCode;
    /**
     * 错误信息
     */
    @JsonProperty("errmsg")
    private String errorMessage;
    /**
     * 获取到的票据
     */
    @JsonProperty("ticket")
    private String ticket;
    /**
     * 票据有效时间，单位：秒
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;
    /**
     * 生成时间
     */
    private Long generateTime = System.currentTimeMillis();

    /**
     * 是否有效
     *
     * @return 有效性
     */
    public boolean isValid() {
        return (getErrorCode() == null || getErrorCode().equals(0)) && new Date(generateTime + (getExpiresIn() * 1000)).after(new Date());
    }
}
