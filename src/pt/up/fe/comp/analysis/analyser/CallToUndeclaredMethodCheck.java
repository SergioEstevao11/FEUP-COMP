package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class CallToUndeclaredMethodCheck  extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public CallToUndeclaredMethodCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("ExpressionStatement", this::visitExpressionStatement);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitExpressionStatement(JmmNode expressionNode,Integer ret){

        if(expressionNode.getChildren().get(1) != null) {
            System.out.println(expressionNode.getChildren().get(1));
            JmmNode right_node = expressionNode.getChildren().get(1);
            if (right_node.getKind().equals("DotAccess")) {
                String method_node_name = right_node.getJmmChild(0).get("name");
                System.out.println(method_node_name);
                System.out.println(symbolTable.getMethods());
                if(symbolTable.getMethods().contains(method_node_name)) return 1;
            }
        }
        if(symbolTable.getSuper() != null) return 1;

        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Method \"" + "\" is missing.", null));
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
