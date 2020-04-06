package priv.asura.wechat_service.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import priv.asura.wechat_service.global.ApiResult;
import priv.asura.wechat_service.model.Client;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import priv.asura.wechat_service.service.ClientProxyService;
import priv.asura.wechat_service.service.ClientService;

import java.util.Map;

@RestController
@RequestMapping("/client")
public class ClientController {

    final ClientProxyService clientProxyService;

    final HttpServletRequest request;

    public ClientController(HttpServletRequest request, ClientProxyService clientProxyService) {
        this.request = request;
        this.clientProxyService = clientProxyService;
    }

    @PostMapping("create")
    public ApiResult create(@RequestBody Object infos) {
        Client client = getClient();
        client.setInfos(infos);
        ClientService clientService = clientProxyService.getInstance(client);
        clientService.create();
        return ApiResult.success();
    }

    public Client getClient() {
        return new Client() {{
            setId(request.getHeader("client-id"));
            setSecret(request.getHeader("client-secret"));
        }};
    }
}
