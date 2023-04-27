package cn.snow.loan.repayment.aviator.plugin;

import java.math.BigDecimal;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;

public final class AviatorExecutor {

    private AviatorExecutor(){}

    static {
        AviatorEvaluator.addFunction(new RateMultiplyFunction());
        AviatorEvaluator.addFunction(new ScaleAmountFunction());
    }

    public static BigDecimal exec(Map<String, Object> env, String expression){
        return (BigDecimal) AviatorEvaluator.compile(expression, true).execute(env);
    }
}
