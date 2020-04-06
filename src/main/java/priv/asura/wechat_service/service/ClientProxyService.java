package priv.asura.wechat_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import priv.asura.wechat_service.model.Client;

@Service
public class ClientProxyService {
    @Value("${application.data.directory}")
    String resourcesDirectory;

    public ClientService getInstance(Client client) {
        return new ClientService(resourcesDirectory, client);
    }
}
