package priv.asura.wechat_service.controller;

import javax.servlet.http.HttpServletRequest;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.util.DigestUtils;
import priv.asura.wechat_service.model.Client;
import priv.asura.wechat_service.utils.DESUtil;
import priv.asura.wechat_service.global.ApiResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/client")
public class ClientController extends BaseController {

    public ClientController(HttpServletRequest request) {
        super(request);
    }

    /**
     * 创建/校验 使用者
     *
     * @return 操作结果
     */
    @PostMapping
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
            verifyClient();
            return ApiResult.success();
        }
        // 使用者不存在即创建
        else {
            String tempPath = generateDirectoryName();
            String clientDirectoryPath = Paths.get(resourcesDirectory, tempPath).toString();
            boolean directoryCreated = new File(clientDirectoryPath).mkdir();
            if (!directoryCreated) {
                return ApiResult.fail(501, "资源目录创建失败");
            } else {
                String secretCheckContent = BCrypt.withDefaults().hashToString(10, client.getSecret().toCharArray());
                String secretCheckContentPath = Paths.get(clientDirectoryPath, "password.txt").toString();
                fileUtil.write(secretCheckContentPath, getDESUtil().encrypt(secretCheckContent.getBytes()));
                saveClients(clientKey, tempPath);
                return ApiResult.success();
            }
        }
    }
}
