package list;


public class MyOperator {
    private String operatorSymbol;
    private String operatorName;

    public MyOperator(String operatorSymbol, String operatorName) {
        this.operatorSymbol = operatorSymbol;
        this.operatorName = operatorName;
    }

    public String getOperatorSymbol() {
        return operatorSymbol;
    }

    public String getOperatorName() {
        return operatorName;
    }
}
