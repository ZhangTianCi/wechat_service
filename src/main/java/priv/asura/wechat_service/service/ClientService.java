package priv.asura.wechat_service.service;

import java.io.File;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import at.favre.lib.crypto.bcrypt.BCrypt;
import priv.asura.wechat_service.model.Client;
import priv.asura.wechat_service.utils.DESUtil;
import priv.asura.wechat_service.utils.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import priv.asura.wechat_service.global.ServiceException;


@Slf4j
public class ClientService {
    private Client client;
    private String resourcesPath;
    final FileUtil fileUtil = new FileUtil();

    public ClientService(String resourcesPath, Client client) {
        this.client = client;
        this.resourcesPath = resourcesPath;
    }

    /**
     * 获取使用者编号
     *
     * @return -
     */
    String getId() {
        String id = client.getId();
        if (id == null) {
            throw new ServiceException("使用者编码为空");
        }
        id = id.trim();
        if (id.length() < 16 || id.length() > 64) {
            throw new ServiceException("使用者编码为空长度超出限制[32,64]");
        }
        return id;
    }

    /**
     * 获取用户资源目录路径
     *
     * @return -
     */
    String getResourcePath() {
        return Paths.get(this.resourcesPath, this.getId()).toString();
    }

    /**
     * 获取用户资源目录
     *
     * @return -
     */
    File getResource() {
        return new File(getResourcePath());
    }

    /**
     * 获取用户密码校验内容存储目录
     *
     * @return -
     */
    String getSecretCipherPath() {
        return Paths.get(getResourcePath(), ".secret").toString();
    }

    /**
     * 获取使用者密码
     *
     * @return -
     */
    String getSecret() {
        String secret = client.getSecret();
        if (secret == null) {
            throw new ServiceException("密码为空");
        }
        secret = secret.trim();
        if (secret.length() < 8 || secret.length() > 56) {
            throw new ServiceException("密码长度超出限制[8,56]");
        }
        return secret;
    }

    /**
     * 创建使用者
     */
    public void create() {
        try {
            File resource = getResource();
            if (resource.exists()) {
                throw new ServiceException("用户文件已存在");
            } else {
                boolean createResourceResult = resource.mkdir();
                if (!createResourceResult) {
                    throw new ServiceException("用户文件创建失败");
                } else {
                    String infoString;
                    try {
                        infoString = new ObjectMapper().writeValueAsString(client.getInfos());
                    } catch (Exception e) {
                        throw new ServiceException("序列化用户信息失败", e);
                    }
                    String secretCipher = BCrypt.withDefaults().hashToString(10, getSecret().toCharArray());
                    fileUtil.write(getSecretCipherPath(), secretCipher.getBytes());
                    fileUtil.write(getClientInfoPath(), new DESUtil(getSecret()).encrypt(infoString.getBytes()));
                    // 创建其它目录
                    new File(Paths.get(getResourcePath(), "wechat_access_token").toString()).mkdir();
                }
            }
        } catch (ServiceException e) {
            deleteClient();
            throw e;
        }
    }

    /**
     * 校验使用者密码
     *
     * @return -
     */
    public boolean verifySecret() {
        try {
            byte[] secretCipher = fileUtil.getByte(getSecretCipherPath());
            return BCrypt.verifyer().verify(getSecret().getBytes(), secretCipher).verified;
        } catch (Exception e) {
            throw new ServiceException("使用者密码校验失败。", e);
        }
    }

    /**
     * 删除使用者资源目录
     */
    public void deleteClient() {
        try {
            File resource = getResource();
            if (resource.exists()) {
                resource.delete();
            }
        } catch (Exception e) {
            log.error("删除用户文件失败。", e);
        }
    }

    /**
     * 获取使用者附加信息存储目录
     *
     * @return -
     */
    public String getClientInfoPath() {
        return Paths.get(getResourcePath(), ".info").toString();
    }

}
