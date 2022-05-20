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
        JmmNode left_node = plusNode.getJmmChild(0);
        JmmNode right_node  = plusNode.getJmmChild(1);
        String method_name = plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = left_node.get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
        String right_node_name = right_node.get("name");
        String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();
        System.out.println("method name: " + method_name);
        System.out.println("left node kind : " + left_node.getKind());
        System.out.println("right node kind : " + right_node.getKind());
        System.out.println("left node type : " + left_node_type);
        System.out.println("right node type : " + right_node_type);
        System.out.println("left node children : " + left_node.getAttributes());
        System.out.println("right node children : " + right_node.getAttributes());
        System.out.println("node left is array: " + symbolTable.isArray(method_name, left_node_name));
        System.out.println("node right is array: " + symbolTable.isArray(method_name, right_node_name));

        /*
        if (left_node_type.equals("boolean") && right_node_type.equals("int")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + left_node_type + "\" invalid type: expecting an boolean.", null));
        }
        else if (UtilsAnalyser.getVariableType(rightNode, symbolTable).equals("object") && leftNode.getKind().equals("Number")){
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
