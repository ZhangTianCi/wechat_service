package priv.asura.wechat_service.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.wechat.AccessToken;

import java.nio.file.Paths;

@Slf4j
public class AccessTokenService {

    private String appId;
    private String appSecret;
    private String resourcesDirectory;

    /**
     * 构造函数
     *
     * @param appId
     * @param appSecret
     */
    public AccessTokenService(String resourcesDirectory, String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.resourcesDirectory = resourcesDirectory;
    }

    /**
     * 获取资源路径
     *
     * @return -
     */
    public String getResourcePath() {
        return Paths.get(this.resourcesDirectory, "micro_messenger", appId).toString();
    }

    /**
     * 获取AccessToken存储路径
     *
     * @return -
     */
    public String getFilePath() {
        return Paths.get(this.getResourcePath(), ".access_token").toString();
    }

    /**
     * 通过调用微信接口刷新
     */
    private void refresh() {
        try {
            String url = StrUtil.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={}&secret={}", appId, appSecret);
            String result = HttpUtil.get(url);
            AccessToken apiResult = new ObjectMapper().readValue(result, AccessToken.class);
            if (apiResult.getErrorCode() != null && apiResult.getErrorCode() != 0) {
                throw new ServiceException(apiResult.getErrorMessage());
            } else {
                FileUtil.writeUtf8String(new ObjectMapper().writeValueAsString(apiResult), getFilePath());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("刷新失败", e);
        }
    }

    /**
     * 根据文件缓存获取
     *
     * @return -
     */
    private String getFromCache() {
        try {
            String result = FileUtil.readUtf8String(getFilePath());
            AccessToken accessToken = new ObjectMapper().readValue(result, AccessToken.class);
            if (accessToken.isValid()) {
                return accessToken.getAccessToken();
            } else {
                throw new ServiceException("缓存已失效");
            }
        } catch (Exception e) {
            throw new ServiceException("从硬盘获取失败", e);
        }
    }

    /**
     * 获取AccessToken
     *
     * @return AccessToken
     */
    public String get() {
        try {
            return getFromCache();
        } catch (Exception e) {
            log.warn("第一次获取失败", e);
            refresh();
        }
        return getFromCache();
    }
}
