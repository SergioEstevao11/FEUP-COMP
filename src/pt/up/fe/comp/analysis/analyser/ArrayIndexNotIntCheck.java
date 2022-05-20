package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class ArrayIndexNotIntCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final List<Report> reports;

    public ArrayIndexNotIntCheck() {
        this.reports = new ArrayList<>();
        addVisit("ArrayAccess", this::visitArrayAccess);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitArrayAccess(JmmNode plusNode, SymbolTableBuilder symbolTable){
        JmmNode leftNode = plusNode.getChildren().get(0);
        JmmNode rightNode = plusNode.getChildren().get(1);


        Type rightNodeType = symbolTable.getType(rightNode,"type");

        if (!rightNodeType.equals("int")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNodeType + "\" invalid type: expecting an int.", null));
        }
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
