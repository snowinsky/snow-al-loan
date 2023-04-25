package cn.snow.loan.repayment.aviator.plugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class ScaleAmountFunction extends AbstractFunction {

    private static final long serialVersionUID = 3482479362647348727L;

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Number n1 = FunctionUtils.getNumberValue(arg1, env);
        BigDecimal b1 = BigDecimal.valueOf(n1.doubleValue());
        return AviatorDecimal.valueOf(b1.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public String getName() {
        return "scale";
    }
}
