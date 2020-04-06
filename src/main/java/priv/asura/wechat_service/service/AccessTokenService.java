package priv.asura.wechat_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.Account;
import priv.asura.wechat_service.model.Client;
import priv.asura.wechat_service.model.wechat.AccessToken;
import priv.asura.wechat_service.utils.DESUtil;
import priv.asura.wechat_service.utils.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.rmi.ServerException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AccessTokenService extends ClientService {
    private String accountId;

    public AccessTokenService(String resourcesPath, Client client, String accountId) {
        super(resourcesPath, client);
        this.verifySecret();
        this.accountId = accountId;
    }

    public AccessTokenService(String resourcesPath, Client client, String appId, String appSecret) {
        super(resourcesPath, client);
        this.verifySecret();
        this.createAccount(appId, appSecret);
    }

    public String getSelfResourcePath() {
        return Paths.get(this.getResourcePath(), "wechat_access_token", accountId).toString();
    }

    public File getSelfResource() {
        return new File(getSelfResourcePath());
    }

    public String getSelfUrlPath() {
        return Paths.get(this.getSelfResourcePath(), ".url").toString();
    }

    public File getSelfUrlFile() {
        return new File(getSelfUrlPath());
    }

    public String getSelfResultPath() {
        return Paths.get(this.getSelfResourcePath(), ".result").toString();
    }

    public File getSelfResultFile() {
        return new File(getSelfResultPath());
    }

    public String createAccount(String appId, String appSecret) {
        this.accountId = DigestUtils.md5DigestAsHex((appId + " " + appSecret).getBytes());
        File resource = getSelfResource();
        if (resource.exists()) {
            throw new ServiceException("已存在相同账户");
        } else {
            boolean createResourceResult = resource.mkdir();
            if (!createResourceResult) {
                throw new ServiceException("账号文件夹创建失败");
            } else {
                String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
                fileUtil.write(getSelfUrlPath(), new DESUtil(getSecret()).encrypt(url.getBytes()));
            }
        }
        return accountId;
    }

    /**
     * 通过调用微信接口刷新
     */
    private void refresh() {
        try {
            String url = new String(new DESUtil(getSecret()).decrypt(fileUtil.getByte(getSelfUrlPath())));
            String result = new HttpUtil(HttpUtil.Method.GET, url).send("UTF-8");
            AccessToken apiResult = new ObjectMapper().readValue(result, AccessToken.class);
            if (apiResult.getErrorCode() != null && apiResult.getErrorCode() != 0) {
                throw new ServiceException(apiResult.getErrorMessage());
            } else {
                fileUtil.write(getSelfResultPath(), new DESUtil(getSecret()).encrypt(result.getBytes()));
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("刷新失败", e);
        }
    }

    private String getFromCache() {
        try {
            byte[] result = new DESUtil(getSecret()).decrypt(fileUtil.getByte(getSelfResultPath()));
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
