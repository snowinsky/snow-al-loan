package cn.snow.loan.dao.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private LocalDateTime repayDate;

    /**
     * 宽限日
     */
    @Column
    private LocalDateTime graceDate;

    /**
     * 代偿日
     */
    @Column
    private LocalDateTime compensationDate;

    /**
     * 违约金日利率，百分号前面的部分
     */
    private BigDecimal breachFeeRate;

    /**
     * 期款滞纳金日利率，百分号前面的部分
     */
    private BigDecimal termLateFeeRate;

    /**
     * 整笔贷款滞纳金日利率，百分号前面的部分
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
    private LocalDateTime lastRepayDate;

    /**
     * n=normal, o=overdue, c=close, t=term compensation, l=loan compensation
     */
    private String loanTermStatus;

    private static final long serialVersionUID = 1L;
}