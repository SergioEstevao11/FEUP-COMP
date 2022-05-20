package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class CallToUndeclaredMethodCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final List<Report> reports;

    public CallToUndeclaredMethodCheck() {
        this.reports = new ArrayList<>();
        addVisit("ExpressionStatement", this::visitExpressionStatement);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitExpressionStatement(JmmNode dotNode, SymbolTableBuilder symbolTable){
        JmmNode childNode = dotNode.getChildren().get(0);

        for(int i = 0; i < symbolTable.getMethods().size(); i++){
            if(symbolTable.getMethods().get(i).equals(childNode.getChildren().get(1).get("name"))){
                return 1;
            }
        }
        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Method \"" + childNode.getChildren().get(1).get("name") + "\" is missing.", null));
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
