package priv.asura.wechat_service.controller;

import priv.asura.wechat_service.global.ApiResult;
import priv.asura.wechat_service.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/access_token")
public class AccessTokenController {

    final AccessTokenService service;

    public AccessTokenController(AccessTokenService service) {
        this.service = service;
    }

    @RequestMapping(value = {"/", "get"})
    public ApiResult get() {

        return ApiResult.success();
    }

    @RequestMapping("refresh")
    public ApiResult refresh() {

        return ApiResult.success();
    }
}
