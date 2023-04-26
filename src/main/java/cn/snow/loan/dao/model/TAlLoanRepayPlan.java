package cn.snow.loan.dao.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.braisdom.objsql.annotations.Column;
import com.github.braisdom.objsql.annotations.DomainModel;
import com.github.braisdom.objsql.annotations.PrimaryKey;

/**
 * t_al_loan_repay_plan
 * @author 
 */
@DomainModel(tableName = "t_al_loan_repay_plan")
public class TAlLoanRepayPlan implements Serializable {
    @PrimaryKey
    private Long id;

    /**
     * 合同号
     */
    private String contractNo;

    /**
     * 贷款期数
     */
    private Integer loanTerm;

    /**
     * 还款日
     */
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm",timezone = "Asia/Shanghai")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime repayDate;

    /**
     * 宽限日
     */
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm",timezone = "Asia/Shanghai")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime graceDate;

    /**
     * 代偿日
     */
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm",timezone = "Asia/Shanghai")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime compensationDate;

    /**
     * 罚息日利率
     */
    private BigDecimal overdueFeeRate;

    /**
     * 违约金日利率
     */
    private BigDecimal breachFeeRate;

    /**
     * 期款滞纳金日利率
     */
    private BigDecimal termLateFeeRate;

    /**
     * 整笔贷款滞纳金日利率
     */
    private BigDecimal loanLateFeeRate;

    /**
     * 本金
     */
    private BigDecimal principal;

    /**
     * 利息
     */
    private BigDecimal interest;

    /**
     * 罚息
     */
    private BigDecimal overdueFee;

    /**
     * 担保费
     */
    private BigDecimal guaranteeFee;

    /**
     * 违约金
     */
    private BigDecimal breachFee;

    /**
     * 期款滞纳金
     */
    private BigDecimal termLateFee;

    /**
     * 上一次的还款时间
     */
    @Column

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm",timezone = "Asia/Shanghai")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastRepayDate;

    /**
     * 是否逾期 1.逾期 0.不逾期
     */
    private Integer overdueFlag;

    /**
     * n=normal, o=overdue, c=close, t=term compensation, l=loan compensation
     */
    private String loanTermStatus;

    /**
     * 本金
     */
    private BigDecimal compPrincipal;

    /**
     * 利息
     */
    private BigDecimal compInterest;

    /**
     * 罚息
     */
    private BigDecimal compOverdueFee;

    /**
     * 整笔代偿具体时间
     */
    @Column
    private LocalDateTime compLoanDate;

    /**
     * 当期代偿具体时间
     */
    @Column
    private LocalDateTime compTermDate;

    /**
     * 代偿期款滞纳金
     */
    private BigDecimal compTermLateFee;

    /**
     * 代偿担保费
     */
    private BigDecimal compGuaranteeFee;

    /**
     * 代偿违约金
     */
    private BigDecimal compBreachFee;

    /**
     * 代偿整笔金额
     */
    private BigDecimal compAmt;

    /**
     * 整笔贷款滞纳金
     */
    @Column
    private BigDecimal loanLateFee;

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return new StringJoiner(", ", TAlLoanRepayPlan.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("contractNo='" + contractNo + "'")
                .add("loanTerm=" + loanTerm)
                .add("repayDate=" + repayDate)
                .add("graceDate=" + graceDate)
                .add("compensationDate=" + compensationDate)
                .add("overdueFeeRate=" + overdueFeeRate)
                .add("breachFeeRate=" + breachFeeRate)
                .add("termLateFeeRate=" + termLateFeeRate)
                .add("loanLateFeeRate=" + loanLateFeeRate)
                .add("principal=" + principal)
                .add("interest=" + interest)
                .add("overdueFee=" + overdueFee)
                .add("guaranteeFee=" + guaranteeFee)
                .add("breachFee=" + breachFee)
                .add("termLateFee=" + termLateFee)
                .add("lastRepayDate=" + lastRepayDate)
                .add("overdueFlag=" + overdueFlag)
                .add("loanTermStatus='" + loanTermStatus + "'")
                .add("compPrincipal=" + compPrincipal)
                .add("compInterest=" + compInterest)
                .add("compOverdueFee=" + compOverdueFee)
                .add("compLoanDate=" + compLoanDate)
                .add("compTermDate=" + compTermDate)
                .add("compTermLateFee=" + compTermLateFee)
                .add("compGuaranteeFee=" + compGuaranteeFee)
                .add("compBreachFee=" + compBreachFee)
                .add("compAmt=" + compAmt)
                .add("loanLateFee=" + loanLateFee)
                .toString();
    }
}