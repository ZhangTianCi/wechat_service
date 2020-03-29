package priv.asura.wechat_service.global;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;


@Slf4j
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrorController extends AbstractErrorController {

    /**
     * httpRequest of each call
     */
    private final HttpServletRequest request;
    /**
     * errorProperties of each call
     */
    private final ErrorProperties errorProperties;


    /**
     * Creates an instance.
     *
     * @param request          HttpRequest of each call
     * @param errorAttributes  ErrorAttributes of each call
     * @param serverProperties ServerProperties of each call
     */
    @Autowired
    public ErrorController(HttpServletRequest request, ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        super(errorAttributes);
        this.request = request;
        this.errorProperties = serverProperties.getError();
    }

    @Override
    public String getErrorPath() {
        return errorProperties.getPath();
    }

    /**
     * Handle error of following
     * 1. Request.Header[Accept]=text/html
     * 2. Response.Header[Content-Type]=text/html
     *
     * @return object/document of html.
     */
    @RequestMapping(produces = "text/html")
    public ModelAndView errorHtml() {
        // Get visual error info.
        VisualErrorInfo errorInfo = VisualErrorInfo.Visualize(getErrorAttributes(request, isIncludeStackTrace()));
        // Plan 1. redirect
        if (true) {
            // waring : redirect spin
            String redirectUrl = "http://zhangtiancinb.cn";
            if (redirectUrl.equals(errorInfo.path)) {
                return new ModelAndView("error", errorInfo.Revert());
            } else {
                return new ModelAndView("redirect:" + redirectUrl);
            }
        }
        // Plan 2. ModelAndView
        {
            /*
            返回ModelAndView时，如果失败（视图不存在或异常），则会返回body为empty的response
             */
            return new ModelAndView("error", errorInfo.Revert());
        }
    }

    /**
     * Handle error of all request
     * <p>
     * emm~ 如果没有像上面的方法一样，写其他的Mapping的话
     *
     * @return api call result entity of global
     */
    @ResponseBody
    @RequestMapping
    public ApiResult error() {
        // Get visual error info.
        VisualErrorInfo errorInfo = VisualErrorInfo.Visualize(getErrorAttributes(request, isIncludeStackTrace()));
        // Output api call result entity of global.
        return ApiResult.fail(errorInfo.status, errorInfo.message);
    }

    /**
     * Copy BasicErrorController.isIncludeStackTrace.
     *
     * @return if the stacktrace attribute should be included
     * @see org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
     * Determine if the stacktrace attribute should be included.
     */
    private boolean isIncludeStackTrace() {
        ErrorProperties.IncludeStacktrace include = this.errorProperties.getIncludeStacktrace();
        if (include == ErrorProperties.IncludeStacktrace.ALWAYS) {
            return true;
        } else if (include == ErrorProperties.IncludeStacktrace.ON_TRACE_PARAM) {
            return getTraceParameter(request);
        } else {
            return false;
        }
    }
}