package cn.snow.loan.dao.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.StringJoiner;

import com.github.braisdom.objsql.annotations.Column;
import com.github.braisdom.objsql.annotations.DomainModel;
import com.github.braisdom.objsql.annotations.PrimaryKey;

@DomainModel(tableName = "t_funding_Loan_repay_plan")
public class TFundingLoanRepayPlan {
    @PrimaryKey
    private Long id;

    private String contractNo;
    private int loanTerm;

    @Column
    private LocalDateTime repayDate;
    @Column
    private LocalDateTime graceDate;
    private BigDecimal overdueFeeRate;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal overdueFee;
    @Column
    private LocalDateTime lastRepayDate;

    @Override
    public String toString() {
        return new StringJoiner(", ", TFundingLoanRepayPlan.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("contractNo='" + contractNo + "'")
                .add("loanTerm=" + loanTerm)
                .add("repayDate=" + repayDate)
                .add("graceDate=" + graceDate)
                .add("overdueFeeRate=" + overdueFeeRate)
                .add("principal=" + principal)
                .add("interest=" + interest)
                .add("overdueFee=" + overdueFee)
                .add("lastRepayDate=" + lastRepayDate)
                .toString();
    }
}
