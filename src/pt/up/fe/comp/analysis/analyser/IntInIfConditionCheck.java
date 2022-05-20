package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class IntInIfConditionCheck extends PreorderJmmVisitor<Integer, Integer> {

    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;
    public IntInIfConditionCheck(SymbolTableBuilder symbolTable, List<Report> reports) {

        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("IfStatement", this::visitIfCondition);
        setDefaultVisit((node, oi) -> 0);
    }

    public Integer visitIfCondition(JmmNode ifStatementNode, Integer ret) {
        JmmNode left_node = ifStatementNode.getJmmChild(0);
        JmmNode right_node  = ifStatementNode.getJmmChild(1);
        String method_name = ifStatementNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        //String left_node_name = left_node.get("name");
       // String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
       // String right_node_name = right_node.get("name");
     //   String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();

        System.out.println("method name : " + method_name);
        System.out.println("left node kind:" + left_node.getKind());
        System.out.println("right node kind: " + right_node.getKind());

        boolean isMathExpression = symbolTable.isMathExpression(left_node.getKind());
        System.out.println("is math expression: " + isMathExpression);

        boolean isBooleanExpression = symbolTable.isBooleanExpression(left_node.getKind());
        System.out.println("is boolean expression: " + isBooleanExpression);

     //   System.out.println("left node name: " + left_node_name);
     //   System.out.println("left node type : " + left_node_type);
     //   System.out.println("right node name: " + right_node_name);
     //   System.out.println("right node type : " + right_node_type);


        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
