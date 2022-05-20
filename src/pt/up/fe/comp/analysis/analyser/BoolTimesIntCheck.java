package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.AJmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;


public class BoolTimesIntCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final SymbolTableBuilder table;
    private final List<Report> reports;
    public BoolTimesIntCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.table = symbolTable;
        addVisit("Times", this::visitTimes);
        setDefaultVisit((node, oi) -> 0);
    }

    public Integer visitTimes(JmmNode timesNode, SymbolTableBuilder symbolTable){
        System.out.println(timesNode.getChildren());
        JmmNode leftNode = timesNode.getChildren().get(0);
        System.out.println("leftNode: " + leftNode.getAttributes());
        JmmNode rightNode = timesNode.getChildren().get(1);
        System.out.println("leftNode");

        System.out.println("TOu aqui");
        System.out.println(table.print());
        if (UtilsAnalyser.getVariableType(leftNode, table).equals("boolean") && UtilsAnalyser.getVariableType(rightNode, table).equals("int")){
            System.out.println("Caso 1");

            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(leftNode, table).equals("int") && UtilsAnalyser.getVariableType(rightNode, table).equals("boolean")){
            System.out.println("Caso 2");

            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (leftNode.getKind().equals("Number") && UtilsAnalyser.getVariableType(rightNode, table).equals("boolean")){
            System.out.println("Caso 3");

            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(leftNode, table).equals("int") && UtilsAnalyser.getVariableType(rightNode, table).equals("boolean")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }

        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }

}
