package cn.snow.loan.repayment.aviator;

import java.math.BigDecimal;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;

import cn.snow.loan.repayment.aviator.plugin.RateMultiplyFunction;
import cn.snow.loan.repayment.aviator.plugin.ScaleAmountFunction;

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
