package priv.asura.wechat_service.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import priv.asura.wechat_service.global.ApiResult;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.Client;
import priv.asura.wechat_service.model.wechat.AccessToken;
import priv.asura.wechat_service.model.wechat.Account;
import priv.asura.wechat_service.model.wechat.JsApiTicket;
import priv.asura.wechat_service.utils.DESUtil;
import priv.asura.wechat_service.utils.FileUtil;
import priv.asura.wechat_service.utils.HttpUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/we_chat")
public class WeChatController extends AccountController {

    public WeChatController(HttpServletRequest request) {
        super(request);
    }

    @Override
    String getAccountType() {
        return "we_chat";
    }

    @PostMapping("")
    public ApiResult create(@RequestBody(required = false) Account account) {
        if (account == null) {
            return ApiResult.fail(500, "账号信息不能为空");
        }
        Map<String, String> accounts = getAccounts();
        // MD5账号id
        String accountKey = DigestUtils.md5DigestAsHex(account.getId().getBytes());
        // 账号已存在
        if (accounts != null && accounts.containsKey(accountKey)) {
            // 账号信息目录
            String accountDirectoryPath = Paths.get(resourcesDirectory, getClientPath()).toString();
            String appIdPath = Paths.get(accountDirectoryPath, "app_id.txt").toString();
            String appSecretPath = Paths.get(accountDirectoryPath, "app_secret.txt").toString();
            if (!new File(appIdPath).exists()) {
                return ApiResult.fail(501, "appId信息丢失");
            }
            if (!new File(appSecretPath).exists()) {
                return ApiResult.fail(501, "appSecret信息丢失");
            }
            return ApiResult.success();
        }
        // 账号不存在
        else {
            String tempPath = generateDirectoryName();
            // 账号信息目录
            String clientDirectoryPath = Paths.get(resourcesDirectory, getClientPath(), "we_chat", tempPath).toString();
            String appIdPath = Paths.get(clientDirectoryPath, "app_id.txt").toString();
            String appSecretPath = Paths.get(clientDirectoryPath, "app_secret.txt").toString();
            boolean directoryCreated = new File(clientDirectoryPath).mkdir();
            if (!directoryCreated) {
                return ApiResult.fail(501, "资源目录创建失败");
            } else {
                fileUtil.write(appIdPath, getDESUtil().encrypt(account.getAppId().getBytes()));
                fileUtil.write(appSecretPath, getDESUtil().encrypt(account.getAppSecret().getBytes()));
                saveAccount(accountKey, tempPath);
                return ApiResult.success();
            }
        }
    }

    @GetMapping("access_token")
    public ApiResult accessToken(String accountId) {
        return ApiResult.success(getAccessTokenValue(accountId));
    }

    @GetMapping("js_api_ticket")
    public ApiResult jsTicket(String accountId) {
        return ApiResult.success(getJsApiTicketValue(accountId));
    }

    @GetMapping("web_code")
    public ApiResult webCode(String accountId, String code) {
        return ApiResult.success();
    }


    /**
     * 获取AccessToken
     *
     * @param accountId 账号id
     * @return access_token's value.
     */
    private String getAccessTokenValue(String accountId) {
        try {
            return getAccessTokenValueFromCache(accountId);
        } catch (Exception e) {
            refreshAccessToken(accountId);
        }
        return getAccessTokenValueFromCache(accountId);
    }

    /**
     * 通过调用微信接口刷新 access_token.
     *
     * @param accountId 账号id
     */
    private void refreshAccessToken(String accountId) {
        String appId = getAppId(accountId);
        String appSecret = getAppSecret(accountId);
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, appSecret);
        try {
            String result = new HttpUtil(HttpUtil.Method.GET, url).send("UTF-8");
            AccessToken apiResult = new ObjectMapper().readValue(result, AccessToken.class);
            if (apiResult.getErrorCode() != null && apiResult.getErrorCode() != 0) {
                throw new ServiceException(apiResult.getErrorMessage());
            } else {
                String accessTokenPath = Paths.get(getAccountDirectoryPath(accountId), "access_token.txt").toString();
                fileUtil.write(accessTokenPath, getDESUtil().encrypt(result.getBytes()));
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("刷新失败", e);
        }
    }

    /**
     * 从硬盘获取access_token's value.
     *
     * @param accountId 账号id
     * @return AccessToken
     */
    private String getAccessTokenValueFromCache(String accountId) {
        String accessTokenPath = Paths.get(getAccountDirectoryPath(accountId), "access_token.txt").toString();
        try {
            byte[] result = getDESUtil().decrypt(fileUtil.getByte(accessTokenPath));
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
     * @param accountId 账号id
     * @return access_token's value.
     */
    private String getJsApiTicketValue(String accountId) {
        try {
            return getJsApiTicketValueFromCache(accountId);
        } catch (Exception e) {
            refreshJsApiTicket(accountId);
        }
        return getJsApiTicketValueFromCache(accountId);
    }

    /**
     * 通过调用微信接口刷新 js_api_ticket.
     *
     * @param accountId 账号id
     */
    private void refreshJsApiTicket(String accountId) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi", getAccessTokenValue(accountId));
        try {
            String result = new HttpUtil(HttpUtil.Method.GET, url).send("UTF-8");
            JsApiTicket apiResult = new ObjectMapper().readValue(result, JsApiTicket.class);
            if (apiResult.getErrorCode() != null && apiResult.getErrorCode() != 0) {
                throw new ServiceException(apiResult.getErrorMessage());
            } else {
                String accessTokenPath = Paths.get(getAccountDirectoryPath(accountId), "js_api_ticket.txt").toString();
                fileUtil.write(accessTokenPath, getDESUtil().encrypt(result.getBytes()));
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("刷新失败", e);
        }
    }

    /**
     * 从硬盘获取js_api_ticket's value.
     *
     * @param accountId 账号id
     * @return AccessToken
     */
    private String getJsApiTicketValueFromCache(String accountId) {
        String accessTokenPath = Paths.get(getAccountDirectoryPath(accountId), "js_api_ticket.txt").toString();
        try {
            byte[] result = getDESUtil().decrypt(fileUtil.getByte(accessTokenPath));
            JsApiTicket accessToken = new ObjectMapper().readValue(result, JsApiTicket.class);
            if (accessToken.isValid()) {
                return accessToken.getTicket();
            } else {
                throw new ServiceException("缓存已失效");
            }
        } catch (Exception e) {
            throw new ServiceException("从硬盘获取失败", e);
        }
    }

    /**
     * 获取appId
     *
     * @param accountId 账号id
     * @return appId
     */
    private String getAppId(String accountId) {
        String filePath = Paths.get(getAccountDirectoryPath(accountId), "app_id.txt").toString();
        return new String(getDESUtil().decrypt(fileUtil.getByte(filePath)));
    }

    /**
     * 获取appSecret
     *
     * @param accountId 账号id
     * @return appSecret
     */
    private String getAppSecret(String accountId) {
        String filePath = Paths.get(getAccountDirectoryPath(accountId), "app_secret.txt").toString();
        return new String(getDESUtil().decrypt(fileUtil.getByte(filePath)));
    }
}
