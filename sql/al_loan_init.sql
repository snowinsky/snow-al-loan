-- -----------------------------------------------------------------------------------
-- table 01：【公共表】现在的批量渠道和wechat单条渠道，通过java高并发生成table id主键value(+数据库: 只是负责持久化新的申请id buffer区间)
-- ___________________________________________________________________________________
drop table if exists t_funding_loan_contract;
CREATE TABLE t_funding_loan_contract
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    contract_no      VARCHAR(50)    NOT NULL COMMENT '合同号',
    year_rate        DECIMAL(20, 6) NOT NULL COMMENT '年利率，百分号前面的部分',
    overdue_fee_rate DECIMAL(20, 6) NOT NULL COMMENT '罚息日利率，百分号前面的部分',
    repay_day        TINYINT        NOT NULL COMMENT '到期还款日',
    grace_day        TINYINT        NOT NULL COMMENT '宽限天数',
    loan_term        TINYINT        NOT NULL COMMENT '贷款期数',
    first_repay_date DATETIME       NOT NULL COMMENT '首期还款日',
    PRIMARY KEY pk_funding_loan_contract (id),
    UNIQUE INDEX uk_funding_loan_contract (contract_no)
) COMMENT = '资方的合同'
;

-- -----------------------------------------------------------------------------------
-- table 01：【公共表】现在的批量渠道和wechat单条渠道，通过java高并发生成table id主键value(+数据库: 只是负责持久化新的申请id buffer区间)
-- ___________________________________________________________________________________
drop table if exists t_funding_Loan_repay_plan;
CREATE TABLE t_funding_Loan_repay_plan
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    contract_no      VARCHAR(50)    NOT NULL COMMENT '合同号',
    loan_term        TINYINT        NOT NULL COMMENT '贷款期数',
    repay_date       DATETIME       NOT NULL COMMENT '还款日',
    grace_date       DATETIME       NOT NULL COMMENT '宽限日',
    overdue_fee_rate DECIMAL(20, 6) NOT NULL COMMENT '罚息日利率，百分号前面的部分',
    principal        DECIMAL(20, 6) NOT NULL COMMENT '本金',
    interest         DECIMAL(20, 6) NOT NULL COMMENT '利息',
    overdue_fee      DECIMAL(20, 6) NOT NULL COMMENT '罚息',
    last_repay_date  DATETIME       NOT NULL COMMENT '上一次的还款时间',
    PRIMARY KEY pk_funding_Loan_repay_plan (id),
    UNIQUE INDEX uk_funding_Loan_repay_plan (contract_no, loan_term)
) COMMENT '资方的合同中的还款计划中每一期的还款额';

