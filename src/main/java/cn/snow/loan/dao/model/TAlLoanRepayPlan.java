package cn.snow.loan.dao.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.braisdom.objsql.annotations.DomainModel;
import com.github.braisdom.objsql.annotations.PrimaryKey;

@DomainModel
public class TAlLoanRepayPlan {
    @PrimaryKey
    private Long id;

    private String contractNo;
    private int loanTerm;
    private LocalDateTime repayDate;
    private LocalDateTime graceDate;
    private LocalDateTime compensationDate;
    private BigDecimal overdueFeeRate;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal overdueFee;
    private BigDecimal guaranteeFee;
    private BigDecimal breachFee;
    private BigDecimal termLateFee;
}
