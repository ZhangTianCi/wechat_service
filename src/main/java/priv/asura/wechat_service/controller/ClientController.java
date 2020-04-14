package priv.asura.wechat_service.controller;

import javax.servlet.http.HttpServletRequest;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import priv.asura.wechat_service.global.ApiResult;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.Client;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import priv.asura.wechat_service.utils.DESUtil;
import priv.asura.wechat_service.utils.FileUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Value("${application.data.directory}")
    String resourcesDirectory;
    final HttpServletRequest request;
    private final FileUtil fileUtil = new FileUtil();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClientController(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 获取使用者目录
     *
     * @return clientList
     */
    private HashMap<String, String> getClients() {
        try {
            String filePath = Paths.get(resourcesDirectory, "list.txt").toString();
            return objectMapper.readValue(fileUtil.getByte(filePath), HashMap.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保存使用者目录
     *
     * @param clientKey           MD5使用者id
     * @param clientDirectoryPath 使用者资源目录
     */
    private void saveClients(String clientKey, String clientDirectoryPath) {
        try {
            HashMap<String, String> clientList = getClients();
            if (clientList != null) {
                clientList.put(clientKey, clientDirectoryPath);
                String filePath = Paths.get(resourcesDirectory, "list.txt").toString();
                fileUtil.write(filePath, objectMapper.writeValueAsString(clientList).getBytes());
            } else {
                throw new ServiceException("使用者集合为空");
            }
        } catch (Exception e) {
            throw new ServiceException("保存使用者目录失败", e);
        }
    }

    /**
     * 验证使用者
     *
     * @param clientId     使用者id
     * @param clientSecret 使用者密钥
     */
    public void verifyClient(String clientId, String clientSecret) {
        Map clientList = getClients();
        String clientKey = DigestUtils.md5DigestAsHex(clientId.getBytes());
        if (clientList != null && clientList.containsKey(clientKey)) {
            String clientDirectoryPath = Paths.get(resourcesDirectory, clientList.get(clientKey).toString()).toString();
            File clientDirectory = new File(clientDirectoryPath);
            if (clientDirectory.exists()) {
                String secretCheckContentPath = Paths.get(clientDirectoryPath, "password.txt").toString();
                File secretCheckContent = new File(secretCheckContentPath);
                if (secretCheckContent.exists()) {
                    String checkContent = new String(new DESUtil(clientSecret).decrypt(fileUtil.getByte(secretCheckContentPath)));
                    BCrypt.Result verifyResult = BCrypt.verifyer().verify(clientSecret.getBytes(), checkContent.getBytes());
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

    /**
     * 创建/校验 使用者
     *
     * @return 操作结果
     */
    @PostMapping("")
    public ApiResult create() {
        Client client = this.getClient();
        // 数据校验(非空校验等)
        // ......

        // MD5使用者id
        String clientKey = DigestUtils.md5DigestAsHex(client.getId().getBytes());
        // 获取使用者目录
        Map clientList = getClients();
        // 使用者存在即校验
        if (clientList != null && clientList.containsKey(clientKey)) {
            verifyClient(client.getId(), client.getSecret());
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

    /**
     * 从请求头中拿到使用者信息
     *
     * @return 使用者信息
     */
    public Client getClient() {
        return new Client() {{
            setId(request.getHeader("client-id"));
            setSecret(request.getHeader("client-secret"));
        }};
    }
}
