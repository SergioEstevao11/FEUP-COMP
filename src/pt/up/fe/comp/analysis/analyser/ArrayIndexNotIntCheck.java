package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class ArrayIndexNotIntCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;


    public ArrayIndexNotIntCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("ArrayAccess", this::visitArrayAccess);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitArrayAccess(JmmNode arrayAccessNode, Integer ret){
        String method_name;

        if( arrayAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = arrayAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(0).get("name");
        else method_name = arrayAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        boolean isMathExpression = symbolTable.isMathExpression(arrayAccessNode.getJmmChild(1).getKind());
        boolean isBooleanExpression = symbolTable.isBooleanExpression(arrayAccessNode.getJmmChild(1).getKind());

        if(isBooleanExpression || arrayAccessNode.getJmmChild(1).getKind().equals("True") || arrayAccessNode.getJmmChild(1).getKind().equals("False")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Cant access an array with a boolean", null));
        }
        else if(isMathExpression || arrayAccessNode.getJmmChild(1).getKind().equals("Number")) return 1;
        else if(arrayAccessNode.getJmmChild(1).getKind().equals("Identifier")){

            System.out.println(arrayAccessNode.getJmmChild(1).get);
            System.out.println("TIPO:" + symbolTable.getVariableType(method_name,arrayAccessNode.getJmmChild(1).get("name")));
            String variableType = symbolTable.getVariableType(method_name,arrayAccessNode.getJmmChild(1).get("name")).getName();
            if(!variableType.equals("int")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Variable isn't of type Int", null));
        }
        else{
            String call_method_name = arrayAccessNode.getJmmChild(1).getJmmChild(0).get("name");
            String returnMethodType = symbolTable.getReturnType(call_method_name).getName();
            if(!returnMethodType.equals("int")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Invalid type, was expecting an int", null));
        }

        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
