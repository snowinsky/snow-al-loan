package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.dao.DbHandler;
import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class FundingLoanContractRepayTest {

    FundingLoanContractRepay tested = new FundingLoanContractRepay();

    @Test
    public void initRepayPlan() {

        DbHandler.INIT.initMySqlConnectionFactory();

        FundingLoanContract contract = new FundingLoanContract(LoanAmount.valueOf(new BigDecimal("12000")),
                LoanTerm.monthTerm(12),
                LoanRate.yearRate(8.2));
        contract.setRepayDay(4);
        contract.setDayOfGrace(3);
        contract.setDayOfCompensation(9);
        contract.setFirstRepayDate(LocalDate.of(2022,1,1));
        tested.initRepayPlan(contract);
    }

    @Test
    public void testRepayTrail() throws SQLException {
        DbHandler.INIT.initMySqlConnectionFactory();
        List l = tested.preRepayTrail("F633664211301700", LocalDateTime.of(2022,3,8, 11,12));
        l.stream().forEach(new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println(o.toString());
            }
        });
    }

    @Test
    public void testRepay() throws SQLException {
        DbHandler.INIT.initMySqlConnectionFactory();
        List l = tested.preRepayTrail("F633664211301700", LocalDateTime.of(2022,3,8, 11,12));
        l.stream().forEach(System.out::println);
        tested.repay("F633664211301700", LocalDateTime.of(2022,3,8, 11,12), new BigDecimal("1100"));
        l = tested.preRepayTrail("F633664211301700", LocalDateTime.of(2022,3,8, 11,12));
        l.stream().forEach(System.out::println);
    }

}