package priv.asura.wechat_service.service;

import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {

    public String get(String clientId, String clientSecret) {
        return clientId + clientSecret;
    }
}
