package cn.snow.loan.repayment;

public class Result {
    private int returnCode;
    private String returnMsg;

    public Result(int returnCode, String returnMsg) {
        this.returnCode = returnCode;
        this.returnMsg = returnMsg;
    }

    public boolean success() {
        return returnCode == 0;
    }

    public static Result success(String returnMsg) {
        return new Result(0, returnMsg);
    }

    public static Result fail(String returnMsg) {
        return new Result(1000, returnMsg);
    }
}
