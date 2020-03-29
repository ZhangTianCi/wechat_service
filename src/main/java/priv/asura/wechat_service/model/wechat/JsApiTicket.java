package priv.asura.wechat_service.model.wechat;

import lombok.Data;

/**
 * 用于调用微信JS接口的临时票据
 */
@Data
public class JsApiTicket {
    /**
     * 错误码
     */
    private Integer errcode;
    /**
     * 错误信息
     */
    private String errmsg;
    /**
     * 获取到的票据
     */
    private String ticket;
    /**
     * 票据有效时间，单位：秒
     */
    private Integer expires_in;
}
