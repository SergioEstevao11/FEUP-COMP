package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class ClassNotImportedCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public ClassNotImportedCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("ExpressionStatement", this::visitExpressionStatement);
        setDefaultVisit((node, oi) -> 0);
    }

    public Integer visitExpressionStatement(JmmNode node, Integer ret) {

        String left_node_name = node.getJmmChild(0).get("name");

        if(node.getChildren().get(1) != null){
            JmmNode right_node = node.getChildren().get(1);
            if(right_node.getKind().equals("DotAccess")){
                if(!symbolTable.getImports().contains(left_node_name)) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Method \"" + right_node + "\" is missing.", null));
            }
        }
        return 0;
    }
    public List<Report> getReports(){
        return reports;
    }
}
