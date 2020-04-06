package priv.asura.wechat_service.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import priv.asura.wechat_service.model.Account;
import priv.asura.wechat_service.model.Client;

/**
 * 代理服务
 */
@Service
public class AccessTokenProxyService {
    @Value("${application.data.directory}")
    String resourcesDirectory;

    /**
     * 获取一个服务实例
     *
     * @param client    使用者
     * @param accountId 账号编号
     * @return 服务实例
     */
    public synchronized AccessTokenService getInstance(Client client, String accountId) {
        return new AccessTokenService(resourcesDirectory, client, accountId);
    }

    /**
     * 获取一个服务实例
     *
     * @param client    使用者
     * @param appId     AppID
     * @param appSecret AppSecret
     * @return 服务实例
     */
    public synchronized AccessTokenService getInstance(Client client, String appId, String appSecret) {
        return new AccessTokenService(resourcesDirectory, client, appId, appSecret);
    }
}
