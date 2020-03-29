package priv.asura.wechat_service.global;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;

/**
 * visual error messages
 * source is map (AbstractErrorController.getErrorAttributes)
 *
 * @see org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController
 */

public class VisualErrorInfo {
    String path;
    String error;
    Integer status;
    Date timestamp;
    String message;

    /**
     * Visualize error info from map.
     *
     * @param map error info
     * @return result
     */
    public static VisualErrorInfo Visualize(Map<String, Object> map) {
        return new VisualErrorInfo() {{
            path = map.get("path").toString();
            error = map.get("error").toString();
            message = map.get("message").toString();
            timestamp = (Date) map.getOrDefault("timestamp", new Date());
            status = (Integer) map.getOrDefault("status", 500);
        }};
    }

    /**
     * Revert error info to map.
     *
     * @return original
     */
    public Map<String, ?> Revert() {
        return new HashMap<String, Object>() {{
            put("path", path);
            put("error", error);
            put("status", status);
            put("message", message);
            put("timestamp", timestamp);
        }};
    }
}