package cn.snow.loan.plan.funding.prepare;

public class LoanTerm {

    private final int term;
    private final int termType;

    private LoanTerm(){
        throw new UnsupportedOperationException("");
    }

    private LoanTerm(int term) {
        this.term = term;
        this.termType = TERM_TYPE_MONTH;
    }

    public static LoanTerm monthTerm(int term) {
        return new LoanTerm(term);
    }

    public int getTerm() {
        return this.term;
    }

    public int getTermType() {
        return this.termType;
    }

    public static final int TERM_TYPE_MONTH = 100;
    public static final int TERM_TYPE_YEAR = 101;
    public static final int TERM_TYPE_WEEK = 102;
}
