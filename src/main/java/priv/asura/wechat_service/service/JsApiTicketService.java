package priv.asura.wechat_service.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.wechat.JsApiTicket;

import java.nio.file.Paths;

@Slf4j
public class JsApiTicketService {

    private String appId;
    String resourcesDirectory;
    private AccessTokenService accessTokenService;

    /**
     * 构造函数
     *
     * @param appId
     * @param appSecret
     */
    public JsApiTicketService(String resourcesDirectory, String appId, String appSecret) {
        this.appId = appId;
        this.resourcesDirectory = resourcesDirectory;
        accessTokenService = new AccessTokenService(resourcesDirectory, appId, appSecret);
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
     * 获取JsApiTicket存储路径
     *
     * @return -
     */
    public String getFilePath() {
        return Paths.get(this.getResourcePath(), ".js_api_ticket").toString();
    }

    /**
     * 通过调用微信接口刷新
     */
    private void refresh() {
        try {
            String accessToken = accessTokenService.get();
            String url = StrUtil.format("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={}&type=jsapi", accessToken);
            String result = HttpUtil.get(url);
            JsApiTicket apiResult = new ObjectMapper().readValue(result, JsApiTicket.class);
            if (apiResult.getErrorCode() != null && apiResult.getErrorCode() != 0) {
                throw new ServiceException(apiResult.getErrorMessage());
            } else {
                FileUtil.writeUtf8String(result, getFilePath());
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
            JsApiTicket jsApiTicket = new ObjectMapper().readValue(result, JsApiTicket.class);
            if (jsApiTicket.isValid()) {
                return jsApiTicket.getTicket();
            } else {
                throw new ServiceException("缓存已失效");
            }
        } catch (Exception e) {
            throw new ServiceException("从硬盘获取失败", e);
        }
    }

    /**
     * 获取JsApiTicket
     *
     * @return JsApiTicket
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
