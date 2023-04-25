package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.dao.DbHandler;
import cn.snow.loan.dao.model.TAlLoanRepayPlan;
import cn.snow.loan.dao.model.TRepayHistory;
import cn.snow.loan.plan.al.prepare.AlLoanRate;
import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class AlLoanContractRepayTest {

    Logger log = LoggerFactory.getLogger(AlLoanContractRepayTest.class);

    AlLoanContractRepay tested;

    @Before
    public void setUp() throws Exception {
        DbHandler.INIT.initMySqlConnectionFactory();

        tested = new AlLoanContractRepay();
    }

    @Test
    public void testJsonInMySql() throws SQLException {
        log.info("sdfsdfsdf{}", "asdfa");
        TRepayHistory h = new TRepayHistory();
        h.setAlContractNo("qwrqrqwrwq");
        h.setRepayType(1);
        h.setAmount(new BigDecimal("0"));
        h.setRepayDate(LocalDateTime.now());
        h.setPairDetail("{\"sdf\":\"123\"}");
        h.setComments("");
        TRepayHistory.create(h, true);
    }

    @Test
    public void initRepayPlan() {
        createNewContract();
    }

    private String createNewContract() {
        FundingLoanContract contract = new FundingLoanContract(LoanAmount.valueOf(new BigDecimal("12000")),
                LoanTerm.monthTerm(12),
                LoanRate.yearRateBeforePercent(8.2),
                LoanRate.yearRateBeforePercent(8.2));
        contract.contractNo("F" + System.nanoTime());
        contract.setRepayDay(12);
        contract.setDayOfGrace(6);
        contract.setFirstRepayDate(LocalDate.of(2022, 9, 12));

        AlLoanRate alLoanRate = new AlLoanRate(LoanRate.yearRateBeforePercent(23.9));
        alLoanRate.setBreachFeeRate(LoanRate.dayRateBeforePercent(0.062));
        alLoanRate.setTermLateFeeRate(LoanRate.dayRateBeforePercent(0.097));
        alLoanRate.setLoanLateFeeRate(LoanRate.dayRateBeforePercent(0.097));

        AlLoanContract alContract = new AlLoanContract(contract, alLoanRate);
        alContract.dayOfCompensation(10);
        alContract.contractNo("AL" + contract.contractNo());

        tested.initRepayPlan(alContract);
        return alContract.contractNo();
    }

    @Test
    public void preRepayTrail() {
        List l = tested.preRepayTrail("ALF964435947359300", LocalDateTime.of(2022, 9, 12, 11, 12));
        l.stream().forEach(System.out::println);
    }

    @Test
    public void repay() {
        /*tested.repay("ALF964435947359300",
                LocalDateTime.of(2022, 9, 12, 11, 12),
                new BigDecimal("1134.13"));*/
        /*tested.repay("ALF964435947359300",
                LocalDateTime.of(2022, 10, 17, 11, 12),
                new BigDecimal("1134.13"));*/
        /*tested.repay("ALF964435947359300",
                LocalDateTime.of(2022, 11, 18, 11, 12),
                new BigDecimal("1139.11"));*/
        /*tested.repay("ALF964435947359300",
                LocalDateTime.of(2022, 12, 12, 11, 12),
                new BigDecimal("1134.13"));*/
        /*tested.repay("ALF964435947359300",
                LocalDateTime.of(2023, 1, 14, 11, 12),
                new BigDecimal("300"));*/
        /*tested.repay("ALF964435947359300",
                LocalDateTime.of(2023, 1, 19, 11, 12),
                new BigDecimal("200"));*/
        /*tested.repay("ALF964435947359300",
                LocalDateTime.of(2023, 2, 18, 11, 12),
                new BigDecimal("200.01"));*/
        tested.repay("ALF964435947359300",
                LocalDateTime.of(2023, 3, 10, 11, 12),
                new BigDecimal("1000"));
    }

    @Test
    public void termCompensation() {
        /*tested.termCompensation("ALF964435947359300",
                LoanTerm.monthTerm(5),
                LocalDateTime.of(2023, 1, 22, 11, 12));*/
        tested.termCompensation("ALF964435947359300",
                LoanTerm.monthTerm(6),
                LocalDateTime.of(2023, 2, 22, 11, 12));
    }

    @Test
    public void loanCompensation() {
        tested.loanCompensation("ALF964435947359300", LocalDateTime.of(2023, 3, 22, 11, 12));
    }

    @Test
    public void testGoThroughAllLoan() {
        String contractNo = createNewContract();
        //到期还款结清
        tested.repay(contractNo,
                LocalDateTime.of(2022, 9, 12, 11, 12),
                new BigDecimal("1134.13"));
        //宽限期内还款结清
        tested.repay(contractNo,
                LocalDateTime.of(2022, 10, 17, 11, 12),
                new BigDecimal("1134.13"));
        //逾期还款结清
        tested.repay(contractNo,
                LocalDateTime.of(2022, 11, 18, 11, 12),
                new BigDecimal("1139.11"));
        //到期还款结清
        tested.repay(contractNo,
                LocalDateTime.of(2022, 12, 12, 11, 12),
                new BigDecimal("1134.13"));

        //宽限期内还款未结清
        tested.repay(contractNo,
                LocalDateTime.of(2023, 1, 14, 11, 12),
                new BigDecimal("300"));
        //逾期还款未结清
        tested.repay(contractNo,
                LocalDateTime.of(2023, 1, 19, 11, 12),
                new BigDecimal("200"));
        //当期代偿
        tested.termCompensation(contractNo,
                LoanTerm.monthTerm(5),
                LocalDateTime.of(2023, 1, 22, 11, 12));

        //逾期还款未结清
        tested.repay(contractNo,
                LocalDateTime.of(2023, 2, 18, 11, 12),
                new BigDecimal("200.00"));
        //当期代偿
        tested.termCompensation(contractNo,
                LoanTerm.monthTerm(6),
                LocalDateTime.of(2023, 2, 22, 11, 12));
        //未到期还款未结清
        tested.repay(contractNo,
                LocalDateTime.of(2023, 3, 10, 11, 12),
                new BigDecimal("1000"));
        //整笔代偿
        tested.loanCompensation(contractNo, LocalDateTime.of(2023, 3, 22, 11, 12));

        List<TAlLoanRepayPlan>  l = tested.preRepayTrail(contractNo, LocalDateTime.of(2023, 4, 22, 11, 12));
        l.forEach(System.out::println);
    }
}