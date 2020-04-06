package priv.asura.wechat_service.model.wechat;

import java.util.Date;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class AccessToken {
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
     * 获取到的凭证
     */
    @JsonProperty("access_token")
    private String accessToken;
    /**
     * 凭证有效时间，单位：秒
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
