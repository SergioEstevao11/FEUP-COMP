package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class ArrayPlusIntCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final List<Report> reports;

    public ArrayPlusIntCheck() {
        this.reports = new ArrayList<>();
        addVisit("Plus", this::visitPlus);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitPlus(JmmNode plusNode, SymbolTableBuilder symbolTable){
        JmmNode leftNode = plusNode.getChildren().get(0);
        JmmNode rightNode = plusNode.getChildren().get(1);

        if (UtilsAnalyser.getVariableType(leftNode, symbolTable).equals("") && rightNode.getKind().equals("Number")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("object") && leftNode.getKind().equals("Number")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(leftNode, symbolTable).equals("int") && UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("object")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("int") && UtilsAnalyser.getVariableType(leftNode, symbolTable).equals("object")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
