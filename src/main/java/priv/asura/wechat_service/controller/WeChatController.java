package priv.asura.wechat_service.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import priv.asura.wechat_service.global.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import priv.asura.wechat_service.service.MicroMessengerService;
import priv.asura.wechat_service.service.MicroMessengerProxyService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/tencent")
public class WeChatController extends BaseController {
    @Autowired
    MicroMessengerProxyService microMessengerProxyService;

    public WeChatController(HttpServletRequest request) {
        super(request);
    }


    @PostMapping("/micro_messenger/{id}")
    public ApiResult create(@PathVariable(value = "id", required = false) String appId, @RequestBody(required = false) String secret) {
        try {
            if (StrUtil.isBlank(appId)) {
                throw new RuntimeException("appId不能为空");
            }
            if (StrUtil.isBlank(secret)) {
                throw new RuntimeException("secret不能为空");
            }
            MicroMessengerService microMessengerService = microMessengerProxyService.getInstance(appId);
            if (microMessengerService.create()) {
                microMessengerService.writeSecret(secret);
            } else {
                return ApiResult.fail(500, "资源目录创建失败");
            }
            return ApiResult.success();
        } catch (Exception e) {
            log.warn("微信信息创建失败", e);
            return ApiResult.fail(e);
        }
    }

    @GetMapping("micro_messenger/access_token/{id}")
    public ApiResult accessToken(@PathVariable(value = "id", required = false) String appId) {
        MicroMessengerService microMessengerService = microMessengerProxyService.getInstance(appId);
        return ApiResult.success(microMessengerService.getAccessToken());
    }

    @GetMapping("micro_messenger/js_api_ticket/{id}")
    public ApiResult jsTicket(@PathVariable(value = "id", required = false) String appId) {
        MicroMessengerService microMessengerService = microMessengerProxyService.getInstance(appId);
        return ApiResult.success(microMessengerService.getJsApiTicket());
    }

    @GetMapping("micro_messenger/web_code/{id}")
    public ApiResult webCode(@PathVariable(value = "id", required = false) String appId, @RequestParam(value = "code", required = false) String code) {

        try {
            if (StrUtil.isBlank(appId)) {
                throw new RuntimeException("appId不能为空");
            }
            if (StrUtil.isBlank(code)) {
                throw new RuntimeException("code不能为空");
            }
            MicroMessengerService microMessengerService = microMessengerProxyService.getInstance(appId);
            String url = StrUtil.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid={}&secret={}&code={}&grant_type=authorization_code", appId, microMessengerService.getSecret(), code);
            String apiResultString = HttpUtil.get(url);
            Map<String, Object> apiResult = objectMapper.readValue(apiResultString, Map.class);
            String accessTokenTemp = apiResult.getOrDefault("access_token", "").toString();
            String openId = apiResult.getOrDefault("openid", "").toString();
            if (StrUtil.isBlank(accessTokenTemp) || StrUtil.isBlank(openId)) {
                throw new RuntimeException("中间步骤出错");
            }
            url = StrUtil.format("https://api.weixin.qq.com/sns/userinfo?access_token={}&openid={}&lang=zh_CN", accessTokenTemp, code);
            String infoString = HttpUtil.get(url);
            HashMap<String, Object> info = objectMapper.readValue(infoString, HashMap.class);
            return ApiResult.success(info);
        } catch (Exception e) {
            return ApiResult.fail(e);
        }
    }
}
