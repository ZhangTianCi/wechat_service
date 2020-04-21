package priv.asura.wechat_service.controller;

import java.io.File;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletRequest;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.util.DigestUtils;
import priv.asura.wechat_service.model.Client;
import priv.asura.wechat_service.utils.DESUtil;
import priv.asura.wechat_service.utils.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import priv.asura.wechat_service.global.ServiceException;

public class BaseController {
    @Value("${application.data.directory}")
    String resourcesDirectory;
    final HttpServletRequest request;
    final FileUtil fileUtil = new FileUtil();
    final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数
     *
     * @param request http请求对象
     */
    public BaseController(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 获取加/解密工具实例
     *
     * @return DESUtil
     */
    DESUtil getDESUtil() {
        return new DESUtil(this.getClient().getSecret());
    }

    /**
     * 从请求头中拿到使用者信息
     *
     * @return 使用者信息
     */
    Client getClient() {
        return new Client() {{
            setId(request.getHeader("client-id"));
            setSecret(request.getHeader("client-secret"));
        }};
    }

    /**
     * 获取使用者目录
     *
     * @return clientList
     */
    HashMap<String, String> getClients() {
        HashMap<String, String> clients = new HashMap<>(5);
        try {
            String filePath = Paths.get(resourcesDirectory, "list.txt").toString();
            HashMap tempData = objectMapper.readValue(fileUtil.getByte(filePath), HashMap.class);
            for (Object key : tempData.keySet()) {
                clients.put(key.toString(), tempData.get(key).toString());
            }
        } catch (Exception e) {
            throw new ServiceException("获取使用者目录失败", e);
        }
        return clients;
    }

    /**
     * 保存使用者目录
     *
     * @param clientKey           MD5使用者id
     * @param clientDirectoryPath 使用者资源目录
     */
    void saveClients(String clientKey, String clientDirectoryPath) {
        try {
            HashMap<String, String> clients = getClients();
            clients.put(clientKey, clientDirectoryPath);
            String filePath = Paths.get(resourcesDirectory, "list.txt").toString();
            fileUtil.write(filePath, objectMapper.writeValueAsString(clients).getBytes());
        } catch (Exception e) {
            throw new ServiceException("保存使用者目录失败", e);
        }
    }

    /**
     * 验证使用者
     */
    void verifyClient() {
        Client client = this.getClient();
        String clientDirectoryPath = Paths.get(resourcesDirectory, getClientPath()).toString();
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

    }

    /**
     * 获取使用者的数据路径
     *
     * @return 相对路径
     */
    String getClientPath() {
        Client client = this.getClient();
        // MD5使用者id
        String clientKey = DigestUtils.md5DigestAsHex(client.getId().getBytes());
        // 获取使用者目录
        Map<String, String> clients = getClients();
        // 使用者存在即校验
        if (clients != null && clients.containsKey(clientKey)) {
            return clients.get(clientKey);
        } else {
            throw new ServiceException("使用者不存在");
        }
    }

    /**
     * 生成文件夹名
     *
     * @return directory name
     */
    String generateDirectoryName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd-hh_mm_ss_SSS");
        return simpleDateFormat.format(new Date());
    }
}
