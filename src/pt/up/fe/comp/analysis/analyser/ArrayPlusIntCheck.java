package pt.up.fe.comp.analysis.analyser;

import jdk.swing.interop.SwingInterOpUtils;
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
        addVisit("Plus", this::visitArrayPlusInt);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitArrayPlusInt(JmmNode plusNode, Integer ret){
//        JmmNode left_node = plusNode.getJmmChild(0);
//        JmmNode right_node  = plusNode.getJmmChild(1);
//        String method_name = null;
//
//        if( plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(0).get("name");
//        else method_name = plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");        String left_node_name = left_node.get("name");
//
//        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
//        String right_node_name = right_node.get("name");
//        String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();
//
//        if (symbolTable.isArray(method_name, left_node_name) && (right_node_type.equals("int") || right_node.getKind().equals("Number"))){
//            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node_type + "\" invalid type: can't add an int to an array", null));
//        }
//        else if (symbolTable.isArray(method_name, right_node_name) && (left_node_type.equals("int") || left_node.getKind().equals("Number"))){
//            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + right_node_name + "\" invalid type: can't add an int to an array", null));
//        }
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
