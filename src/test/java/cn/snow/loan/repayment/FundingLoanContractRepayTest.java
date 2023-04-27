package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;

import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.dao.DbHandler;
import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

@SuppressWarnings("all")
public class FundingLoanContractRepayTest {

    FundingLoanContractRepay tested = new FundingLoanContractRepay();

    @Test
    public void initRepayPlan() {

        DbHandler.INIT.initMySqlConnectionFactory();

        FundingLoanContract contract = new FundingLoanContract(LoanAmount.valueOf(new BigDecimal("12000")),
                LoanTerm.monthTerm(12),
                LoanRate.yearRateBeforePercent(8.2),
                LoanRate.yearRateBeforePercent(8.2));
        contract.contractNo("F" + System.nanoTime());
        contract.setRepayDay(4);
        contract.setDayOfGrace(3);
        contract.setFirstRepayDate(LocalDate.of(2022, 1, 1));
        tested.initRepayPlan(contract);
    }

    @Test
    public void testRepayTrail() throws SQLException {
        DbHandler.INIT.initMySqlConnectionFactory();
        List l = tested.preRepayTrail("F645978586742000", LocalDateTime.of(2022, 3, 9, 11, 12));
        l.stream().forEach(System.out::println);
    }

    @Test
    public void testRepay() throws SQLException {
        DbHandler.INIT.initMySqlConnectionFactory();

        String contractNo = "F645978586742000";
        LocalDateTime repayDateTime = LocalDateTime.of(2022, 3, 8, 11, 12);

        List l = tested.preRepayTrail(contractNo, repayDateTime);
        l.stream().forEach(System.out::println);
        tested.repay(contractNo, repayDateTime, new BigDecimal("10"));
        l = tested.preRepayTrail(contractNo, repayDateTime);
        l.stream().forEach(System.out::println);
    }

}