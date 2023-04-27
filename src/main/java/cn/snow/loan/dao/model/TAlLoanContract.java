package cn.snow.loan.dao.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.braisdom.objsql.annotations.Column;
import com.github.braisdom.objsql.annotations.DomainModel;
import com.github.braisdom.objsql.annotations.PrimaryKey;

/**
 * t_al_loan_contract
 * @author 
 */
@SuppressWarnings("all")
@DomainModel(tableName = "t_al_loan_contract")
public class TAlLoanContract implements Serializable {
    @PrimaryKey
    private Long id;

    /**
     * 合同号
     */
    private String contractNo;

    /**
     * 资方合同号
     */
    private String fundingContractNo;

    /**
     * 年利率
     */
    private BigDecimal yearRate;

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
     * 到期还款日
     */
    private Integer repayDay;

    /**
     * 宽限天数
     */
    private Integer graceDay;

    /**
     * 代偿天数
     */
    private Integer compensationDay;

    /**
     * 贷款期数
     */
    private Integer loanTerm;

    /**
     * 首期还款日
     */
    @Column
    private LocalDateTime firstRepayDate;

    private static final long serialVersionUID = 1L;
}