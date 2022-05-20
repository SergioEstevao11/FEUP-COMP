package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class IncompatibleReturnCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public IncompatibleReturnCheck(SymbolTableBuilder symbolTable, List<Report> reports) {

        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("Return", this::visitReturn);
        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitReturn(JmmNode returnStatementNode, Integer ret) {

        JmmNode left_node = returnStatementNode.getJmmChild(0);
        String method_name = returnStatementNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String methodReturnType = symbolTable.getReturnType(method_name).getName();
        System.out.println(method_name);

        System.out.println(methodReturnType);

        boolean isMathExpression = symbolTable.isMathExpression(left_node.getJmmChild(0).getKind());
        System.out.println(left_node.getJmmChild(0).getKind());
        System.out.println(isMathExpression);
        boolean isBooleanExpression = symbolTable.isBooleanExpression(left_node.getJmmChild(0).getKind());

        if(isMathExpression){
            if(!methodReturnType.equals("int")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node + "\" invalid return type", null));
        }
        else if (isBooleanExpression){
            if(!methodReturnType.equals("boolean")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node + "\" invalid return type", null));
        }
       else if(left_node.getJmmChild(0).getKind().equals("Identifier")) {
            if (!symbolTable.getVariableType(method_name, left_node.getJmmChild(0).get("name")).getName().equals(methodReturnType))
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node + "\" invalid return type", null));
        }
        return 1;
    }
    public List<Report> getReports(){
        return reports;
    }

}
