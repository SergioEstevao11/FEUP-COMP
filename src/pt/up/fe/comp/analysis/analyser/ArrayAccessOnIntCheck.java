package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class ArrayAccessOnIntCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final List<Report> reports;

    public ArrayAccessOnIntCheck() {
        this.reports = new ArrayList<>();
        addVisit("ArrayAccess", this::visitArrayAccess);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitArrayAccess(JmmNode plusNode, SymbolTableBuilder symbolTable){
        JmmNode leftNode = plusNode.getChildren().get(0);

        Type leftNodeType = symbolTable.getType(leftNode,"type");

        if (!leftNodeType.equals("array")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNodeType + "\" invalid type: expecting an array.", null));
        }

        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
