package priv.asura.wechat_service.global;

import lombok.Data;

import java.io.Serializable;

/**
 * api call result
 */
@Data
public class ApiResult {
    /**
     * Default code of api call result
     */
    private final static int SUCCESS_CODE = 0;
    /**
     * Default message of api call result
     */
    private final static String SUCCESS_MESSAGE = "success";
    /**
     * Default code of api call result  when fail
     */
    private final static int FAIL_CODE = 500;
    /**
     * Code of api call result
     */
    int code;
    /**
     * Timestamp of api call end
     */
    long timestamp;
    /**
     * Data of api call result
     */
    Serializable data;
    /**
     * Message of api call result
     */
    String message;

    /**
     * Creates an instance.
     */
    public ApiResult() {
        this.setTimestamp(System.currentTimeMillis());
    }

    /**
     * Creates an instance with code and message.It indicates api call fail.
     *
     * @param errCode    api code
     * @param errMessage api message
     * @return result
     */
    public static ApiResult fail(int errCode, String errMessage) {
        return new ApiResult() {{
            setCode(errCode);
            setMessage(errMessage);
        }};
    }

    public static ApiResult fail(Exception exception) {
        return new ApiResult() {{
            setCode(FAIL_CODE);
            setMessage(exception.getMessage());
        }};
    }

    /**
     * Creates an instance with default value.It indicates api call success.
     *
     * @return result
     */
    public static ApiResult success() {
        return new ApiResult() {{
            setCode(SUCCESS_CODE);
            setMessage(SUCCESS_MESSAGE);
        }};
    }

    /**
     * Creates an instance with data.It indicates api call success.
     *
     * @param object Serializable object
     * @return result
     */
    public static ApiResult success(Serializable object) {
        return new ApiResult() {{
            setData(object);
            setCode(SUCCESS_CODE);
            setMessage(SUCCESS_MESSAGE);
        }};
    }
}