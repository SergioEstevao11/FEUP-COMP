package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.Identifier;
import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
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
        String method_name = null;
        String left_node_type = null;
        Boolean found = false;
        if( assignmentNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")){
            method_name = "main";
            List<Symbol> mainMethodVariables = symbolTable.getLocalVariables(method_name);
            for (Symbol mainMethodVariable : mainMethodVariables) {
                if(assignmentNode.getJmmChild(0).getKind().equals("Identifier")){
                    if (assignmentNode.getJmmChild(0).get("name").equals(mainMethodVariable.getName())){
                        found = true;
                    }
                }
            }
            if(!found) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, " Variable not declared on main", null));
        }
        else method_name = assignmentNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        if(assignmentNode.getJmmChild(0).getKind().equals("Identifier")){
            String identifierType = symbolTable.getVariableType(method_name,assignmentNode.getJmmChild(0).get("name")).getName();

            if(assignmentNode.getJmmChild(1).getKind().equals("Identifier")){
                if(!symbolTable.getVariableType(method_name,assignmentNode.getJmmChild(1).get("name")).getName().equals(identifierType)) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Cannot assign a int to a boolean", null));
            }
            else{
                String call_method_name = assignmentNode.getJmmChild(1).getJmmChild(0).get("name");
                String returnMethodType = symbolTable.getReturnType(call_method_name).getName();
                if(!returnMethodType.equals(identifierType)) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + assignmentNode.getJmmChild(0).getKind().equals("Number") + "\" invalid type: expecting a boolean.", null));
            }

            if(identifierType.equals("boolean")){
                boolean isMathExpression = symbolTable.isMathExpression(assignmentNode.getJmmChild(1).getKind());
                    boolean isBooleanExpression = symbolTable.isBooleanExpression(assignmentNode.getJmmChild(1).getKind());
                    if(assignmentNode.getJmmChild(1).getKind().equals("True") || assignmentNode.getJmmChild(1).getKind().equals("False") || isBooleanExpression) return 1;
                    else if(assignmentNode.getJmmChild(1).getKind().equals("Number") || isMathExpression) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + assignmentNode.getJmmChild(0).getKind().equals("Number") + "\" invalid type: expecting a boolean.", null));
                    else if(assignmentNode.getJmmChild(1).getKind().equals("NewDeclaration")){
                        return 1;
                    }

            }
            else if(identifierType.equals("int")){}
        }

//        else if(assignmentNode.getJmmChild(1) != null) {
//            String left_node_name = assignmentNode.getJmmChild(1).getJmmChild(0).get("name");
//            left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
//        }
//        else{
//            String call_method_name = assignmentNode.getJmmChild(1).getJmmChild(0).get("name");
//            String returnMethodType = symbolTable.getReturnType(call_method_name).getName();
//            if(!returnMethodType.equals("boolean")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + assignmentNode.getJmmChild(0).getKind().equals("Number") + "\" invalid type: expecting a boolean.", null));
//        }

        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
