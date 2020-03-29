package priv.asura.wechat_service.model;

import lombok.Data;

@Data
public class Account {
    private Client client;
    private String secret;
    private String weChatAppId;
    private String weChatAppSecret;
}
