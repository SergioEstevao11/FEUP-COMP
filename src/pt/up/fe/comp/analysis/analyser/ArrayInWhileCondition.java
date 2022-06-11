package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class ArrayInWhileCondition extends PreorderJmmVisitor<Integer, Integer> {

    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public ArrayInWhileCondition(SymbolTableBuilder symbolTable, List<Report> reports) {

        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("WhileStatement", this::visitWhileCondition);
        addVisit("IfStatement", this::visitWhileCondition);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitWhileCondition(JmmNode whileStatementNode, Integer ret) {
        JmmNode left_node = whileStatementNode.getJmmChild(0);
        String method_name = null;
        if( whileStatementNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = "main";
        else method_name = whileStatementNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        boolean isMathExpression = symbolTable.isMathExpression(left_node.getKind());
        boolean isBooleanExpression = symbolTable.isBooleanExpression(left_node.getKind());

        if(isMathExpression || left_node.getKind().equals("Number"))  reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node + "\" invalid type: can't have an int on a If statement", null));
        else if(isBooleanExpression) return 1;
        else if(whileStatementNode.getJmmChild(1).getKind().equals("DotAccess")){
            String call_method_name = whileStatementNode.getJmmChild(1).getJmmChild(0).get("name");
            String returnMethodType = symbolTable.getReturnType(call_method_name).getName();
            if(!returnMethodType.equals("boolean")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node + "\" Function did not return a boolean", null));
        }
        else if(!symbolTable.getVariableType(method_name,left_node.get("name")).getName().equals("boolean")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node + "\" invalid type: has to be a boolean", null));
        return 1;
    }
    public List<Report> getReports(){
        return reports;
    }
}
