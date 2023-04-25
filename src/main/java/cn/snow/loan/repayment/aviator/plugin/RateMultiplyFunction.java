package cn.snow.loan.repayment.aviator.plugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class RateMultiplyFunction extends AbstractFunction {

    private static final long serialVersionUID = -6044872247714348373L;

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Number n1 = FunctionUtils.getNumberValue(arg1, env);
        Number n2 = FunctionUtils.getNumberValue(arg2, env);
        BigDecimal b1 = BigDecimal.valueOf(n1.doubleValue());
        BigDecimal b2 = BigDecimal.valueOf(n2.doubleValue());
        return AviatorDecimal.valueOf(b1.multiply(b2).setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public String getName() {
        return "multiplyRate";
    }
}
