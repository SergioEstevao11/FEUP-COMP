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
        addVisit("Identifier", this::visitClassNotImported);
        setDefaultVisit((node, oi) -> 0);
    }

    public Integer visitClassNotImported(JmmNode node, Integer ret) {

        /*System.out.println(node.getKind());
        String method_name = node.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = node.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
        String right_node_name = node.getJmmChild(1).get("name");
        String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();

        node.getKind();

        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Type \"" + node + "\" is missing.", null));*/
        return 0;
    }
    public List<Report> getReports(){
        return reports;
    }
}
