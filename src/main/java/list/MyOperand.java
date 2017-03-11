package list;


public class MyOperand {
    private String operandSymbol;
    private String operandName;

    public MyOperand(String operandSymbol, String operandName) {
        this.operandSymbol = operandSymbol;
        this.operandName = operandName;
    }

    public String getOperandSymbol() {
        return operandSymbol;
    }

    public String getOperandName() {
        return operandName;
    }

    public void setOperandSymbol(String operandSymbol) {
        this.operandSymbol = operandSymbol;
    }

    public void setOperandName(String operandName) {
        this.operandName = operandName;
    }
}
