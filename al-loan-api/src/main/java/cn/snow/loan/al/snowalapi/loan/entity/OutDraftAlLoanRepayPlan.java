package cn.snow.loan.al.snowalapi.loan.entity;

import java.util.List;

import lombok.Data;

@Data
public class OutDraftAlLoanRepayPlan {
    String[] titles = new String[]{
            "期数",
            "到期日",
            "剩余本金",
            "期供",
            "本金",
            "利息",
            "担保费"
    };
    List<TermAlLoanRepayPlan> termAlLoanRepayPlanList;
}
