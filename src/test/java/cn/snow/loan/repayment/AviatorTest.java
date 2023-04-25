package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;
import org.junit.Test;

import cn.snow.loan.repayment.aviator.plugin.RateMultiplyFunction;

public class AviatorTest {


    @Test
    public void testAa(){
        AviatorEvaluator.addFunction(new RateMultiplyFunction());
        Map<String, Object> m = new HashMap<>();
        m.put("a", new BigDecimal("3.21212411342353252"));
        m.put("b", new BigDecimal("4.23234244235"));
        m.put("c", new BigDecimal("5.55555555555555555"));

        System.out.println(AviatorEvaluator.execute("multiplyRate(a, b/100)", m));
    }
}
