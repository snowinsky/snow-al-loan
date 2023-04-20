package cn.snow.loan.plan.funding;


import java.math.BigDecimal;

import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

/**
 * Design it on 1/14/16.
 */
public class LoanCalculatorTest {

    private int totalMonth;
    private BigDecimal totalMoney;
    private double rate;


    protected void setUp() {
        totalMonth = 12;//十二期，一个月一期
        totalMoney = new BigDecimal("120000");//总房款
        rate = 4;//利率，比如8.2的意思就是8.2%的意思
    }

    public static void main(String[] args) {
        LoanCalculatorTest t = new LoanCalculatorTest();
        t.setUp();
        t.testACMCalculate();
        t.testACPIMCalculate1();
    }

    public void testACPIMCalculate1() {
        ILoanCalculator calculator = new ACPIMLoanCalculator();
        Loan loan = calculator.repaymentPlan(
                LoanAmount.valueOf(totalMoney),
                LoanTerm.monthTerm(totalMonth),
                LoanRate.yearRate(rate));
        System.out.println(loan);
    }

    public void testACMCalculate() {
        ILoanCalculator calculator = new ACMLoanCalculator();
        Loan loan = calculator.repaymentPlan(
                LoanAmount.valueOf(totalMoney),
                LoanTerm.monthTerm(totalMonth),
                LoanRate.yearRate(rate));
        System.out.println(loan);
    }

}
