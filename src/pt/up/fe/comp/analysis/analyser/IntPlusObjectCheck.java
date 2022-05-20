package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class IntPlusObjectCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final SymbolTableBuilder table;

    private final List<Report> reports;

    public IntPlusObjectCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.table = symbolTable;

        addVisit("Plus", this::visitPlus);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitPlus(JmmNode plusNode, SymbolTableBuilder symbolTable){
        JmmNode leftNode = plusNode.getChildren().get(0);
        JmmNode rightNode = plusNode.getChildren().get(1);

        if (UtilsAnalyser.getVariableType(leftNode, table).equals("object") && rightNode.getKind().equals("Number")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(rightNode, table).equals("object") && leftNode.getKind().equals("Number")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(leftNode, table).equals("int") && UtilsAnalyser.getVariableType(rightNode, table).equals("object")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(rightNode, table).equals("int") && UtilsAnalyser.getVariableType(leftNode, table).equals("object")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        return 0;
    }

}
