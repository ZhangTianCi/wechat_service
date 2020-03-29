package priv.asura.wechat_service.model.wechat;

import lombok.Data;

@Data
public class AccessToken {
    /**
     * 错误码
     */
    private Integer errcode;
    /**
     * 错误信息
     */
    private String errmsg;
    /**
     * 获取到的凭证
     */
    private String access_token;
    /**
     * 凭证有效时间，单位：秒
     */
    private Integer expires_in;
}
