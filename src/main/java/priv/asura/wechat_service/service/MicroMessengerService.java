package priv.asura.wechat_service.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.wechat.AccessToken;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
public class MicroMessengerService {
    private String appId;
    String resourcesDirectory;

    public MicroMessengerService(String resourcesDirectory, String appId) {
        this.appId = appId;
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
     * 获取密钥文件
     *
     * @return -
     */
    public File getSecretFile() {
        return new File(Paths.get(this.getResourcePath(), ".secret").toString());
    }

    /**
     * 获取密钥内容
     *
     * @return secret
     */
    public String getSecret() {
        return FileUtil.readUtf8String(getSecretFile());
    }

    /**
     * 创建资源文件夹
     *
     * @return success/fail
     */
    public boolean create() {
        File dir = FileUtil.mkdir(getResourcePath());
        if (dir.exists()) {
            FileUtil.loopFiles(dir).forEach(FileUtil::del);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 更新账号信息
     *
     * @param secret 密钥
     */
    public void writeSecret(String secret) {
        try {
            FileUtil.writeUtf8String(secret, getSecretFile());
        } catch (Exception e) {
            log.error("账户信息写入失败", e);
            throw new RuntimeException("账户信息写入失败.");
        }
    }

    /**
     * 获取AccessToken
     *
     * @return AccessToken
     */
    public String getAccessToken() {
        return new AccessTokenService(resourcesDirectory, appId, getSecret()).get();
    }

    /**
     * 获取JsApiTicket
     *
     * @return JsApiTicket
     */
    public String getJsApiTicket() {
        return new JsApiTicketService(resourcesDirectory, appId, getSecret()).get();
    }
}
