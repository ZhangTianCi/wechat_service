package priv.asura.wechat_service.controller;

import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.serial.SerialException;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import priv.asura.wechat_service.global.ApiResult;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.Client;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import priv.asura.wechat_service.service.ClientProxyService;
import priv.asura.wechat_service.service.ClientService;
import priv.asura.wechat_service.utils.DESUtil;
import priv.asura.wechat_service.utils.FileUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Value("${application.data.directory}")
    String resourcesDirectory;
    private final FileUtil fileUtil = new FileUtil();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取使用者目录
     *
     * @return
     */
    private Map<String, String> getClients() {
        try {
            String filePath = Paths.get(resourcesDirectory, "list.txt").toString();
            return objectMapper.readValue(fileUtil.getByte(filePath), Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保存使用者目录
     *
     * @param clientKey MD5使用者id
     * @param clientKey 使用者资源目录
     */
    private void saveClients(String clientKey, String clientDirectoryPath) {
        try {
            Map clientList = getClients();
            clientList.put(clientKey, clientDirectoryPath);
            String filePath = Paths.get(resourcesDirectory, "list.txt").toString();
            fileUtil.write(filePath, objectMapper.writeValueAsString(clientList).getBytes());
        } catch (Exception e) {
            throw new ServiceException("保存使用者目录失败", e);
        }
    }

    public void verifyClient(Client client) {
        Map clientList = getClients();
        String clientKey = DigestUtils.md5DigestAsHex(client.getId().getBytes());
        if (clientList.containsKey(clientKey)) {
            String clientDirectoryPath = Paths.get(resourcesDirectory, clientList.get(clientKey).toString()).toString();
            File clientDirectory = new File(clientDirectoryPath);
            if (clientDirectory.exists()) {
                String secretCheckContentPath = Paths.get(clientDirectoryPath, "password.txt").toString();
                File secretCheckContent = new File(secretCheckContentPath);
                if (secretCheckContent.exists()) {
                    String checkContent = new String(new DESUtil(client.getSecret()).decrypt(fileUtil.getByte(secretCheckContentPath)));
                    BCrypt.Result verifyResult = BCrypt.verifyer().verify(client.getSecret().getBytes(), checkContent.getBytes());
                    if (!verifyResult.verified) {
                        throw new ServiceException(verifyResult.formatErrorMessage);
                    }
                } else {
                    throw new ServiceException("用户密钥校验内容丢失");
                }
            } else {
                throw new ServiceException("用户资源目录丢失");
            }
        } else {
            throw new ServiceException("用户不存在");
        }
    }


    @PostMapping("create")

    public ApiResult create() {
        Client client = this.getClient();
        // 数据校验(非空校验等)
        // ......

        // MD5使用者id
        String clientKey = DigestUtils.md5DigestAsHex(client.getId().getBytes());
        // 获取使用者目录
        Map clientList = getClients();
        // 使用者存在即校验
        if (clientList.containsKey(clientKey)) {
            verifyClient(client);
            return ApiResult.success();
        }
        // 使用者不存在即创建
        else {
            String clientDirectoryPath = Paths.get(resourcesDirectory, String.valueOf(System.currentTimeMillis())).toString();
            boolean directoryCreated = new File(clientDirectoryPath).mkdir();
            if (!directoryCreated) {
                return ApiResult.fail(501, "资源目录创建失败");
            } else {
                String secretCheckContent = BCrypt.withDefaults().hashToString(10, client.getSecret().toCharArray());
                String secretCheckContentPath = Paths.get(clientDirectoryPath, "password.txt").toString();
                fileUtil.write(secretCheckContentPath, new DESUtil(client.getSecret()).encrypt(secretCheckContent.getBytes()));
                saveClients(clientKey, clientDirectoryPath.replace(resourcesDirectory, ""));
                return ApiResult.success();
            }
        }
    }


    final ClientProxyService clientProxyService;

    final HttpServletRequest request;

    public ClientController(HttpServletRequest request, ClientProxyService clientProxyService) {
        this.request = request;
        this.clientProxyService = clientProxyService;
    }

    public Client getClient() {
        return new Client() {{
            setId(request.getHeader("client-id"));
            setSecret(request.getHeader("client-secret"));
        }};
    }
}
