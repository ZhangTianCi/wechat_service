package priv.asura.wechat_service.controller;

import javax.servlet.http.HttpServletRequest;

import priv.asura.wechat_service.utils.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

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

}
