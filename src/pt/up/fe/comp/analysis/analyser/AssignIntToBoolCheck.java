package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class AssignIntToBoolCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public AssignIntToBoolCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("Assignment", this::visitAssignment);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitAssignment(JmmNode assignmentNode,Integer ret){

        String method_name = assignmentNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = assignmentNode.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
        JmmNode right_node = assignmentNode.getJmmChild(1);
/*
        System.out.println(right_node);
        System.out.println(assignmentNode.getJmmChild(0));
        System.out.println(left_node_type);
        System.out.println(symbolTable.getVariableType(method_name,right_node.get("name")));
*/
        if(left_node_type.equals("boolean")) {
            if (right_node.getKind().equals("DotAccess")) {
                String dot_right_node_name = right_node.getJmmChild(0).get("name");
                System.out.println(dot_right_node_name);
                Type methodReturnType = symbolTable.getReturnType(dot_right_node_name);
                if (methodReturnType.equals("int")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + methodReturnType + "\" invalid type: expecting a boolean.", null));
            }
            else if(!right_node.getKind().equals("True") && !right_node.getKind().equals("False") ){
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + "int" + "\" invalid type: expecting a boolean.", null));
            }
        }
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
