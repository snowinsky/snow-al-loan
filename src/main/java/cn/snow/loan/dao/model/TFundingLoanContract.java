package cn.snow.loan.dao.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.github.braisdom.objsql.annotations.Column;
import com.github.braisdom.objsql.annotations.DomainModel;
import com.github.braisdom.objsql.annotations.PrimaryKey;
import com.github.braisdom.objsql.annotations.Relation;
import com.github.braisdom.objsql.relation.RelationType;

@SuppressWarnings("all")
@DomainModel(tableName = "t_funding_loan_contract")
public class TFundingLoanContract {
    @PrimaryKey
    private Long id;
    private String contractNo;
    private BigDecimal yearRate;
    private BigDecimal overdueFeeRate;
    private int repayDay;
    private int graceDay;
    private int loanTerm;
    @Column
    private LocalDateTime firstRepayDate;

    @Relation(relationType = RelationType.HAS_MANY)
    private List<TFundingLoanRepayPlan> fundingLoanRepayPlans;

}
