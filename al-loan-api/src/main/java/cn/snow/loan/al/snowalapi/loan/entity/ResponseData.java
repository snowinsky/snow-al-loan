package cn.snow.loan.al.snowalapi.loan.entity;

import java.io.Serializable;

/**
 * @param <T>
 * @author dddt dev
 */
public class ResponseData<T> implements Serializable {

    public static final String CODE_SUCCESS = "0000";
    public static final String CODE_ARG_FAIL = "1000";
    public static final String CODE_ERROR = "2000";
    public static final String MESSAGE_SUCCESS = "交易成功";
    public static final String MESSAGE_FAIL = "请求参数异常";
    public static final String MESSAGE_ERROR = "业务异常";
    private static final long serialVersionUID = 8152881040409266797L;

    private String code;
    private String message;
    private transient T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ResponseData<T> build(String code, String info) {
        ResponseData<T> r = new ResponseData<>();
        r.setCode(code);
        r.setMessage(info);
        return r;
    }

    public static <T> ResponseData<T> build(String code, String info, T data) {
        ResponseData<T> r = new ResponseData<>();
        r.setCode(code);
        r.setMessage(info);
        r.setData(data);
        return r;
    }

    public static <T> ResponseData<T> success() {
        return build(CODE_SUCCESS, MESSAGE_SUCCESS);
    }

    public static <T> ResponseData<T> success(T data) {
        return build(CODE_SUCCESS, MESSAGE_SUCCESS, data);
    }

    public static <T> ResponseData<T> argumentfail(String message) {
        return build(CODE_ARG_FAIL, message);
    }

    public static <T> ResponseData<T> argumentfail() {
        return build(CODE_ARG_FAIL, MESSAGE_FAIL);
    }

    public static <T> ResponseData<T> argumentfail(T data) {
        return build(CODE_ARG_FAIL, MESSAGE_FAIL, data);
    }

    public static <T> ResponseData<T> bizfail() {
        return build(CODE_ERROR, MESSAGE_ERROR);
    }

    public static <T> ResponseData<T> bizfail(T data) {
        return build(CODE_ERROR, MESSAGE_ERROR, data);
    }

    @Override
    public String toString() {
        return "ResponseData{code='" + code + '\'' + ", message='" + message + '\'' + ", data=" + data + '}';
    }

}
