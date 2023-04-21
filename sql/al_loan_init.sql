-- -----------------------------------------------------------------------------------
-- 资方贷款合同表
-- ___________________________________________________________________________________
drop table if exists t_funding_loan_contract;
CREATE TABLE t_funding_loan_contract
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    contract_no      VARCHAR(50)    NOT NULL COMMENT '合同号',
    year_rate        DECIMAL(20, 6) NOT NULL COMMENT '年利率',
    overdue_fee_rate DECIMAL(20, 6) NOT NULL COMMENT '罚息日利率',
    repay_day        TINYINT        NOT NULL COMMENT '到期还款日',
    grace_day        TINYINT        NOT NULL COMMENT '宽限天数',
    loan_term        TINYINT        NOT NULL COMMENT '贷款期数',
    first_repay_date DATETIME       NOT NULL COMMENT '首期还款日',
    PRIMARY KEY pk_funding_loan_contract (id),
    UNIQUE INDEX uk_funding_loan_contract (contract_no)
) COMMENT = '资方的合同'
;

-- -----------------------------------------------------------------------------------
-- 资方贷款还款计划表
-- ___________________________________________________________________________________
drop table if exists t_funding_Loan_repay_plan;
CREATE TABLE t_funding_Loan_repay_plan
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    contract_no      VARCHAR(50)    NOT NULL COMMENT '合同号',
    loan_term        TINYINT        NOT NULL COMMENT '贷款期数',
    repay_date       DATETIME       NOT NULL COMMENT '还款日',
    grace_date       DATETIME       NOT NULL COMMENT '宽限日',
    overdue_fee_rate DECIMAL(20, 6) NOT NULL COMMENT '罚息日利率',
    principal        DECIMAL(20, 2) NOT NULL COMMENT '本金',
    interest         DECIMAL(20, 2) NOT NULL COMMENT '利息',
    overdue_fee      DECIMAL(20, 2) NOT NULL COMMENT '罚息',
    last_repay_date  DATETIME       NOT NULL COMMENT '上一次的还款时间',
    PRIMARY KEY pk_funding_Loan_repay_plan (id),
    UNIQUE INDEX uk_funding_Loan_repay_plan (contract_no, loan_term)
) COMMENT '资方的合同中的还款计划中每一期的还款额';

-- -----------------------------------------------------------------------------------
-- 助贷合同表
-- ___________________________________________________________________________________
drop table if exists t_al_loan_contract;
CREATE TABLE t_al_loan_contract
(
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    contract_no         VARCHAR(50)    NOT NULL COMMENT '合同号',
    funding_contract_no VARCHAR(50)    NOT NULL COMMENT '资方合同号',
    year_rate           DECIMAL(20, 6) NOT NULL COMMENT '年利率',
    breach_fee_rate     DECIMAL(20, 6) NOT NULL COMMENT '违约金日利率',
    term_late_fee_rate  DECIMAL(20, 6) NOT NULL COMMENT '期款滞纳金日利率',
    loan_late_fee_rate  DECIMAL(20, 6) NOT NULL COMMENT '整笔贷款滞纳金日利率',
    repay_day           TINYINT        NOT NULL COMMENT '到期还款日',
    grace_day           TINYINT        NOT NULL COMMENT '宽限天数',
    compensation_day    TINYINT        NOT NULL COMMENT '代偿天数',
    loan_term           TINYINT        NOT NULL COMMENT '贷款期数',
    first_repay_date    DATETIME       NOT NULL COMMENT '首期还款日',
    compensation_date   DATETIME       NOT NULL COMMENT '整笔代偿日',
    loan_late_fee       DECIMAL(20, 2) NOT NULL COMMENT '整笔贷款滞纳金',
    last_repay_date     DATETIME       NOT NULL COMMENT '上一次还款日',
    PRIMARY KEY pk_al_loan_contract (id),
    UNIQUE INDEX uk_al_loan_contract (contract_no)
) COMMENT = '助贷方合同'
;

-- -----------------------------------------------------------------------------------
-- 助贷还款计划表
-- ___________________________________________________________________________________
drop table if exists t_al_Loan_repay_plan;
CREATE TABLE t_al_Loan_repay_plan
(
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    contract_no        VARCHAR(50)    NOT NULL COMMENT '合同号',
    loan_term          TINYINT        NOT NULL COMMENT '贷款期数',
    repay_date         DATETIME       NOT NULL COMMENT '还款日',
    grace_date         DATETIME       NOT NULL COMMENT '宽限日',
    compensation_date  DATETIME       NOT NULL COMMENT '代偿日',
    breach_fee_rate    DECIMAL(20, 6) NOT NULL COMMENT '违约金日利率',
    term_late_fee_rate DECIMAL(20, 6) NOT NULL COMMENT '期款滞纳金日利率',
    loan_late_fee_rate DECIMAL(20, 6) NOT NULL COMMENT '整笔贷款滞纳金日利率',
    principal          DECIMAL(20, 2) NOT NULL DEFAULT '0' COMMENT '本金',
    interest           DECIMAL(20, 2) NOT NULL DEFAULT '0' COMMENT '利息',
    overdue_fee        DECIMAL(20, 2) NOT NULL DEFAULT '0' COMMENT '罚息',
    guarantee_fee      DECIMAL(20, 2) NOT NULL DEFAULT '0' COMMENT '担保费',
    breach_fee         DECIMAL(20, 2) NOT NULL DEFAULT '0' COMMENT '违约金',
    term_late_fee      DECIMAL(20, 2) NOT NULL DEFAULT '0' COMMENT '期款滞纳金',
    last_repay_date    DATETIME       NOT NULL COMMENT '上一次的还款时间',
    loan_term_status   VARCHAR(5)     NOT NULL DEFAULT 'n' COMMENT 'n=normal, o=overdue, c=close, t=term compensation, l=loan compensation',
    PRIMARY KEY pk_al_Loan_repay_plan (id),
    UNIQUE INDEX uk_al_Loan_repay_plan (contract_no, loan_term)
) COMMENT '助贷的合同中的还款计划中每一期的还款额';

-- -----------------------------------------------------------------------------------
-- 客户还款历史表
-- ___________________________________________________________________________________
drop table if exists t_repay_history;
CREATE TABLE t_repay_history
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    al_contract_no VARCHAR(50)    NOT NULL,
    repay_type     TINYINT(4)     NOT NULL COMMENT '1.repay 2.term compensation 3 loan compensation',
    amount         DECIMAL(20, 2) NOT NULL,
    repay_date     DATETIME       NOT NULL,
    pair_detail    JSON           NOT NULL,
    comments       VARCHAR(50)    NOT NULL DEFAULT '',
    PRIMARY KEY pk_repay_history (id),
    FULLTEXT INDEX idx_repay_history_al_contract_no (al_contract_no)
);

