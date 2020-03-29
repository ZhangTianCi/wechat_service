package priv.asura.wechat_service.service;

import java.util.HashMap;

import org.springframework.stereotype.Service;
import priv.asura.wechat_service.model.Account;

/**
 * 代理服务
 */
@Service
public class AccessTokenProxyService {
    /**
     * 实例池
     */
    private HashMap<Account, AccessTokenService> map = new HashMap<Account, AccessTokenService>();

    /**
     * 获取一个服务实例
     *
     * @param account 服务实例的账号
     * @return 服务实例
     */
    public synchronized AccessTokenService getInstance(Account account) {
        if (!map.containsKey(account)) {
            map.put(account, new AccessTokenService(account));
        }
        return map.get(account);
    }
}
