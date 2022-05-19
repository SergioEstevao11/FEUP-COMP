package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class IntPlusObjectCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final List<Report> reports;

    public IntPlusObjectCheck() {
        this.reports = new ArrayList<>();
        addVisit("Plus", this::visitPlus);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitPlus(JmmNode timesNode, SymbolTableBuilder symbolTable){
        JmmNode leftNode = timesNode.getChildren().get(0);
        JmmNode rightNode = timesNode.getChildren().get(1);

        if (UtilsAnalyser.getVariableType(leftNode, symbolTable).equals("object") && UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("int")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(leftNode, symbolTable).equals("int") && UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("object.")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        return 0;
    }

}
