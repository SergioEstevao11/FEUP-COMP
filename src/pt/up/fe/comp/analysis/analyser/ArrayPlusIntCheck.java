package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class ArrayPlusIntCheck  extends PreorderJmmVisitor<Integer, Integer> {

    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public ArrayPlusIntCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("Plus", this::visitPlus);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitPlus(JmmNode plusNode, Integer ret){
        String method_name = plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = plusNode.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
        String right_node_name = plusNode.getJmmChild(1).get("name");
        String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();
        System.out.println("Type left: " + left_node_type);
        System.out.println("Name left: " + left_node_name);
        System.out.println("Type right: " + left_node_type);


        if (left_node_type.equals("boolean") && right_node_type.equals("int")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node_type + "\" invalid type: expecting an boolean.", null));
        }
        /*else if (UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("object") && leftNode.getKind().equals("Number")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(leftNode, symbolTable).equals("int") && UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("object")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("int") && UtilsAnalyser.getVariableType(leftNode, symbolTable).equals("object")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }*/
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
