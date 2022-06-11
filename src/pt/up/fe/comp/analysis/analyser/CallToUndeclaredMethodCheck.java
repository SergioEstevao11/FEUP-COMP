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
        addVisit("DotAccess", this::visitDotAccess);
        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitDotAccess(JmmNode dotAccessNode,Integer ret){
        JmmNode right_node = dotAccessNode.getChildren().get(0);
        String method_node_name = right_node.get("name");
        if(symbolTable.getMethods().contains(method_node_name)) return 1;

        if(symbolTable.getSuper() != null) return 1;

        if(!symbolTable.getImports().isEmpty()) {
            return 1;
        }

        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Method \"" + method_node_name + "\" is missing.", null));
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
