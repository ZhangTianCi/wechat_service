package priv.asura.wechat_service.controller;

import org.springframework.util.DigestUtils;
import priv.asura.wechat_service.global.ServiceException;
import priv.asura.wechat_service.model.Client;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class AccountController extends BaseController {

    public AccountController(HttpServletRequest request) {
        super(request);
    }

    abstract String getAccountType();


    /**
     * 获取账号类型的数据存储地址
     *
     * @return 文件夹路径
     */
    String getAccountTypePath() {
        try {
            // 使用者
            Client client = this.getClient();
            // MD5使用者id
            String clientKey = DigestUtils.md5DigestAsHex(client.getId().getBytes());
            // 使用者目录
            Map<String, String> clientList = getClients();
            // 使用者存在校验
            if (clientList != null && clientList.containsKey(clientKey)) {
                // 账号目录
                String clientPath = clientList.get(clientKey);
                return Paths.get(resourcesDirectory, clientPath, getAccountType()).toString();
            } else {
                throw new ServiceException("未找到使用者");
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取账号的数据存储地址
     *
     * @param accountId 账号id
     * @return 路径
     */
    public String getAccountDirectoryPath(String accountId) {
        // MD5 id
        String accountKey = DigestUtils.md5DigestAsHex(accountId.getBytes());
        // 使用者目录
        Map<String, String> accounts = getAccounts();
        // 使用者存在校验
        if (accounts != null && accounts.containsKey(accountKey)) {
            String accountPath = accounts.get(accountKey);
            return Paths.get(getAccountTypePath(), accountPath).toString();
        } else {
            throw new ServiceException("账号不存在");
        }
    }

    /**
     * 获取账号目录
     *
     * @return 账号目录
     */
    HashMap<String, String> getAccounts() {
        try {
            String filePath = Paths.get(getAccountTypePath(), "list.txt").toString();
            if (!new File(filePath).exists()) {
                fileUtil.write(filePath, getDESUtil().encrypt("{}".getBytes()));
            }
            HashMap<String, String> accounts = new HashMap<>(5);
            HashMap tempData = objectMapper.readValue(getDESUtil().decrypt(fileUtil.getByte(filePath)), HashMap.class);
            for (Object key : tempData.keySet()) {
                accounts.put(key.toString(), tempData.get(key).toString());
            }
            return accounts;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保存账号目录
     *
     * @param accountKey           账号id
     * @param accountDirectoryPath 账号资源目录
     */
    void saveAccount(String accountKey, String accountDirectoryPath) {
        try {
            Client client = this.getClient();
            // MD5使用者id
            String clientKey = DigestUtils.md5DigestAsHex(client.getId().getBytes());
            // 获取使用者目录
            Map<String, String> clientList = getClients();
            HashMap<String, String> accountList = getAccounts();
            if (accountList != null) {
                accountList.put(accountKey, accountDirectoryPath);
                String filePath = Paths.get(resourcesDirectory, clientList.get(clientKey), "we_chat", "list.txt").toString();
                fileUtil.write(filePath, getDESUtil().encrypt(objectMapper.writeValueAsString(accountList).getBytes()));
            } else {
                throw new ServiceException("账号集合为空");
            }
        } catch (Exception e) {
            throw new ServiceException("账号目录失败", e);
        }
    }
}
