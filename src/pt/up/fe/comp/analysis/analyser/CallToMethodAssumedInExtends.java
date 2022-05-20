package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class CallToMethodAssumedInExtends extends PreorderJmmVisitor<Integer, Integer>  {

    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public CallToMethodAssumedInExtends(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("ExpressionStatement", this::visitExpressionStatement);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitExpressionStatement(JmmNode expressionNode, Integer ret){

        JmmNode callNode = expressionNode.getChildren().get(0);
        String method_name = expressionNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        String left_node_name = callNode.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();

        String method_node_name = callNode.getJmmChild(1).get("name");
        String method_node_type = symbolTable.getVariableType(method_name,method_node_name).getName();

        System.out.println("Left type: " + left_node_type);
        System.out.println("Left name: " + left_node_name);
        System.out.println("Right type: " + method_node_type);
        System.out.println("Right name: " + method_node_name);

        for(int i = 0; i < symbolTable.getMethods().size(); i++){
            System.out.println(symbolTable.getMethods().get(i));
            if(symbolTable.getMethods().get(i).equals(method_node_name)){
                return 1;
            }
        }
        System.out.println(!symbolTable.getSuper().isEmpty());
        if(!symbolTable.getSuper().isEmpty()) return 1;

        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Method \"" + method_node_name+ "\" is missing.", null));
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
