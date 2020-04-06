package priv.asura.wechat_service.controller;

import org.springframework.web.bind.annotation.*;
import priv.asura.wechat_service.global.ApiResult;
import priv.asura.wechat_service.model.Client;
import priv.asura.wechat_service.service.AccessTokenProxyService;
import priv.asura.wechat_service.service.AccessTokenService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @PutMapping("")
    public ApiResult create(@RequestBody(required = false) Map<String, String> accounts) {
        if (accounts == null || accounts.size() == 0) {
            return ApiResult.fail(500, "账号信息不能为空");
        }
        if (accounts.size() == 0) {
            return ApiResult.fail(500, "账号信息不能为空");
        }
        ArrayList<ApiResult> result = new ArrayList<ApiResult>(accounts.size());
        for (Map.Entry<String, String> account : accounts.entrySet()) {
            try {
                service.getInstance(getClient(), account.getKey(), account.getValue());
                result.add(ApiResult.success());
            } catch (Exception e) {
                result.add(ApiResult.fail(500, e.getMessage()));
            }
        }
        return ApiResult.success(result);
    }

    @DeleteMapping("")
    public ApiResult delete() {

        return ApiResult.success();
    }

    @GetMapping("{accountId}")
    public ApiResult get(@PathVariable("accountId") String accountId) {

        return ApiResult.success(service.getInstance(getClient(), accountId).get());
    }

    public Client getClient() {
        return new Client() {{
            setId(request.getHeader("client-id"));
            setSecret(request.getHeader("client-secret"));
        }};
    }
}
