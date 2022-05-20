package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class AssignIntToBoolCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final List<Report> reports;

    public AssignIntToBoolCheck() {
        this.reports = new ArrayList<>();
        addVisit("Assignment", this::visitAssignment);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitAssignment(JmmNode plusNode, SymbolTableBuilder symbolTable){
        JmmNode leftNode = plusNode.getChildren().get(0);
        JmmNode rightNode = plusNode.getChildren().get(1);

        if (symbolTable.getType(leftNode,"type").equals("boolean") && symbolTable.getType(rightNode,"type").equals("int")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + symbolTable.getType(rightNode,"type").equals("int") + "\" invalid type: expecting a boolean.", null));
        }
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
