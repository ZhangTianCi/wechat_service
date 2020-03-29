package priv.asura.wechat_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import priv.asura.wechat_service.model.Client;

@Service
public class ClientDataService extends DataService {

    String filePath = "client.data";

    @Override
    public String getFilePath() {
        return filePath;
    }

    public ArrayList<Client> getClients() throws Exception {
        return this.readDataWhitList(Client.class);
    }

    public void init() throws Exception {
        this.writeData(new ArrayList<Client>());
    }

    public void add(Client client) throws Exception {
        List<Client> clients = getClients();
        int oldIndex = clients.indexOf(client);
        if (oldIndex >= 0) {
            throw new Exception("client已存在。");
        } else {
            clients.add(client);
            this.writeData(clients);
        }
    }

    public void remove(Client client) throws Exception {
        List<Client> clients = getClients();
        List<String> clientsId = clients.stream().map(Client::getId).collect(Collectors.toList());
        int oldIndex = clientsId.indexOf(client.getId());
        if (oldIndex < 0) {
            throw new Exception("client不存在。");
        } else {
            clients.remove(oldIndex);
            this.writeData(clients);
        }
    }
}
