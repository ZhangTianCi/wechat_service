package priv.asura.wechat_service.service;

import lombok.extern.slf4j.Slf4j;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.Account;
import priv.asura.wechat_service.model.wechat.AccessToken;
import priv.asura.wechat_service.utils.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.ServerException;

@Slf4j
public class AccessTokenService extends DataService {

    private Account account;

    public AccessTokenService(Account account) {
        this.account = account;
    }

    public String get() throws ServiceException {
        // 需要单次失败检测
        AccessToken accessToken = null;
        try {
            accessToken = getFromData();
        } catch (Exception e) {
            refresh();
            log.warn("首次获取出错。" + e);
        }
        if (accessToken == null) {
            throw new ServiceException("经过首次容错后，依然没有获取到AccessToken");
        } else if (!accessToken.isValid()) {
            return getFromData().getAccessToken();
        } else {
            return accessToken.getAccessToken();
        }
    }

    private AccessToken getFromData() throws ServiceException {
        return this.readData(AccessToken.class);
    }

    public void refresh() throws ServiceException {
        String uri = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";
        uri += "&appid=" + account.getWeChatAppId();
        uri += "&secret=" + account.getWeChatAppSecret();
        try {
            AccessToken accessToken = new HttpUtil(HttpUtil.Method.GET).setUri(uri).send(AccessToken.class);
            if (accessToken.isValid()) {
                writeData(accessToken);
            } else {
                throw new ServiceException(accessToken.getErrorMessage());
            }
        } catch (Exception ex) {
            log.error("通过接口请求AccessToken失败。", ex);
            throw new ServiceException("通过接口请求AccessToken失败。", ex);
        }
    }

    @Override
    public String getFilePath() {
        return "access_token" + File.separator + account + ".data";
    }
}
