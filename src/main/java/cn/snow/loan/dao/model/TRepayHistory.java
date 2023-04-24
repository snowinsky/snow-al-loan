package cn.snow.loan.dao.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.braisdom.objsql.annotations.Column;
import com.github.braisdom.objsql.annotations.DomainModel;
import com.github.braisdom.objsql.annotations.PrimaryKey;

/**
 * t_repay_history
 * @author 
 */
@DomainModel(tableName = "t_repay_history")
public class TRepayHistory implements Serializable {
    @PrimaryKey
    private Long id;

    private String alContractNo;

    /**
     * 1.repay 2.term compensation 3 loan compensation
     */
    private Integer repayType;

    private BigDecimal amount;

    @Column
    private LocalDateTime repayDate;

    private String pairDetail;

    private String comments;

    private static final long serialVersionUID = 1L;
}