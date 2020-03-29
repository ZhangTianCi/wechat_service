package priv.asura.wechat_service.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import priv.asura.wechat_service.global.ApiResult;
import priv.asura.wechat_service.model.Client;
import priv.asura.wechat_service.service.ClientDataService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

@RestController
@RequestMapping("/client")
public class ClientController {

    final ClientDataService clientDataService;

    final HttpServletRequest request;

    public ClientController(HttpServletRequest request, ClientDataService clientDataService) {
        this.request = request;
        this.clientDataService = clientDataService;
    }

    @RequestMapping("init")
    public ApiResult init() throws Exception {
        clientDataService.init();
        return ApiResult.success();
    }

    @RequestMapping({"/", "list"})
    public ApiResult list() throws Exception {

        return ApiResult.success(clientDataService.getClients());
    }

    @PostMapping("add")
    public ApiResult add(@RequestBody Client client) throws Exception {
        clientDataService.add(client);
        return ApiResult.success();
    }
}
