package priv.asura.wechat_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import priv.asura.wechat_service.global.ApiResult;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.Account;
import priv.asura.wechat_service.service.AccessTokenProxyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/access_token")
public class AccessTokenController {
    final
    HttpServletRequest request;
    final AccessTokenProxyService service;

    public AccessTokenController(HttpServletRequest request, AccessTokenProxyService service) {
        this.request = request;
        this.service = service;
    }

    @RequestMapping(value = {"/", "get"})
    public ApiResult get() throws ServiceException {
        // 此处需要校验
        Account account = new Account() {{
            setWeChatAppId(request.getHeader("Client-Id"));
            setWeChatAppSecret(request.getHeader("Client-Secret"));
        }};

        return ApiResult.success(service.getInstance(account).get());
    }

    @RequestMapping("refresh")
    public ApiResult refresh() {

        return ApiResult.success();
    }
}
