package priv.asura.wechat_service.service;

import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

/**
 * 代理服务
 */
@Service
public class MicroMessengerProxyService {
    @Value("${application.data.directory}")
    String resourcesDirectory;
    private HashMap<String, MicroMessengerService> microMessengerCache = new HashMap<>();

    /**
     * 获取一个服务实例
     *
     * @return 服务实例
     */
    public synchronized MicroMessengerService getInstance(String appId) {
        if (!microMessengerCache.containsKey(appId)) {
            microMessengerCache.put(appId, new MicroMessengerService(resourcesDirectory, appId));
        }
        return microMessengerCache.get(appId);
    }
}
