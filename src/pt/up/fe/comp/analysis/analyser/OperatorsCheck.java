package pt.up.fe.comp.analysis.analyser;

import jdk.swing.interop.SwingInterOpUtils;
import pt.up.fe.comp.Identifier;
import pt.up.fe.comp.MethodBody;
import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;


public class OperatorsCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;
    public OperatorsCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("Times", this::visitTimes);
        addVisit("Plus", this::visitPlus);
        addVisit("And", this::visitAnd);
        setDefaultVisit((node, oi) -> 0);
    }

    public Integer visitTimes(JmmNode timesNode,Integer ret){


        String method_name = timesNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = timesNode.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
        String right_node_name = timesNode.getJmmChild(1).get("name");
        String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();


        if (left_node_type.equals("boolean") && (right_node_type.equals("int") || timesNode.getJmmChild(1).getKind().equals("Number"))){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + timesNode.getJmmChild(1) + "\" invalid type: expecting an boolean.", null));
        }
        else if ((left_node_type.equals("int") || timesNode.getJmmChild(0).getKind().equals("Number")) && right_node_type.equals("boolean")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + timesNode.getJmmChild(1) + "\" invalid type: expecting an boolean.", null));
        }

        return 1;
    }

    public Integer visitAnd(JmmNode andNode,Integer ret){
        String method_name = null;

        if( andNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = "main";
        else method_name = andNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        if(andNode.getJmmChild(0).getKind().equals("Identifier") ) {
            String left_side = symbolTable.getVariableType(method_name, andNode.getJmmChild(0).get("name")).getName();
            if (andNode.getJmmChild(1).getKind().equals("Identifier")) {
                if (symbolTable.isArray(method_name, andNode.getJmmChild(1).get("name")) && symbolTable.isArray(method_name, andNode.getJmmChild(0).get("name")))
                    reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add two arrays", null));
                String right_side = symbolTable.getVariableType(method_name, andNode.getJmmChild(1).get("name")).getName();
                if (!left_side.equals(right_side))
                    reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "variables of different type", null));
            }
        }
        else if(andNode.getJmmChild(1).getKind().equals("Identifier")){
            String right_side = symbolTable.getVariableType(method_name,andNode.getJmmChild(0).get("name")).getName();

        }
        return 0;
    }

    public Integer visitPlus(JmmNode plusNode,Integer ret){
        String method_name = null;

        if( plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = "main";
        else method_name = plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        if(plusNode.getJmmChild(0).getKind().equals("Identifier") ) {
            String left_side = symbolTable.getVariableType(method_name, plusNode.getJmmChild(0).get("name")).getName();
            if (plusNode.getJmmChild(1).getKind().equals("Identifier")) {
                if (symbolTable.isArray(method_name, plusNode.getJmmChild(1).get("name"))){
                    if(symbolTable.isArray(method_name, plusNode.getJmmChild(0).get("name"))){
                        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add two arrays", null));
                    }
                    else reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add int/boolean to an array", null));
                }
                else if(symbolTable.isArray(method_name, plusNode.getJmmChild(0).get("name"))){
                    if(symbolTable.isArray(method_name, plusNode.getJmmChild(1).get("name"))){
                        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add two arrays", null));
                    }
                    else reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add int/boolean to an array", null));
                }
                String right_side = symbolTable.getVariableType(method_name, plusNode.getJmmChild(1).get("name")).getName();
                if (!left_side.equals(right_side))
                    reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "variables of different type", null));
            }
            else if(!plusNode.getJmmChild(1).getKind().equals("Number")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add variable which aren't of type int", null));

        }
        else if(plusNode.getJmmChild(0).getKind().equals("Number") ){
            if(!plusNode.getJmmChild(1).getKind().equals("Number")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add variable which aren't of type int", null));
        }

        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }

}
