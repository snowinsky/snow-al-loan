package cn.snow.loan.al.snowalapi.config.exception;

public class InvalidParameterException extends RuntimeException{

    private static final long serialVersionUID = 8984839761152712504L;

    public InvalidParameterException() {
        super();
    }

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidParameterException(Throwable cause) {
        super(cause);
    }
}
