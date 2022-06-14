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
        addVisit("Minus", this::visitPlus);
        addVisit("And", this::visitAnd);
        setDefaultVisit((node, oi) -> 0);
    }

    public Integer visitTimes(JmmNode timesNode,Integer ret){


        String method_name = null;

        if( timesNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = "main";
        else method_name = timesNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        if(timesNode.getJmmChild(0).getKind().equals("Identifier") ) {
            String left_side = symbolTable.getVariableType(method_name, timesNode.getJmmChild(0).get("name")).getName();
            if (timesNode.getJmmChild(1).getKind().equals("Identifier")) {
                if (symbolTable.isArray(method_name, timesNode.getJmmChild(1).get("name"))){
                    if(symbolTable.isArray(method_name, timesNode.getJmmChild(0).get("name"))){
                        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't multiply two arrays", null));
                    }
                    else reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't multiplyint/boolean to an array", null));
                }
                else if(symbolTable.isArray(method_name, timesNode.getJmmChild(0).get("name"))){
                    if(symbolTable.isArray(method_name, timesNode.getJmmChild(1).get("name"))){
                        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't multiply two arrays", null));
                    }
                    else reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't multiply int/boolean to an array", null));
                }
                String right_side = symbolTable.getVariableType(method_name, timesNode.getJmmChild(1).get("name")).getName();
                if (!left_side.equals(right_side))
                    reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "variables of different type", null));
            }
            else if(timesNode.getJmmChild(1).getKind().equals("DotAccess")){
                String call_method_name = timesNode.getJmmChild(1).getJmmChild(1).getJmmChild(0).get("name");
                String returnMethodType = symbolTable.getReturnType(call_method_name).getName();
                if (!left_side.equals(returnMethodType))
                    reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "variables of different type5", null));
            }
            else if(!timesNode.getJmmChild(1).getKind().equals("Number")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't multiply variable which aren't of type int4", null));

        }
        else if(timesNode.getJmmChild(0).getKind().equals("Number") ){
            if(!timesNode.getJmmChild(1).getKind().equals("Number")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't multiply variable which aren't of type int3", null));
        }

        return 0;
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
        String left_side_type = null;
        if( plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = "main";
        else method_name = plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        boolean isMathExpression = symbolTable.isMathExpression(plusNode.getJmmChild(0).getKind());
        boolean isBooleanExpression = symbolTable.isBooleanExpression(plusNode.getJmmChild(0).getKind());

        if(plusNode.getJmmChild(0).getKind().equals("Identifier")) {
            if(symbolTable.isArray(method_name, plusNode.getJmmChild(0).get("name"))) {
                if(plusNode.getJmmChild(1).getKind().equals("Identifier")) {
                    if (symbolTable.isArray(method_name, plusNode.getJmmChild(1).get("name"))) {
                        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add two arrays", null));
                    } else
                        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add int/boolean to an array", null));
                }
            }
            left_side_type = symbolTable.getVariableType(method_name, plusNode.getJmmChild(0).get("name")).getName();
        }
        else if(plusNode.getJmmChild(0).getKind().equals("DotAccess")) {
            if(plusNode.getJmmChild(0).getJmmChild(1).getJmmChild(0).getKind().equals("Length")){
                left_side_type = "int";
            }
            else{
                System.out.println("OLAA");
                String call_method_name = plusNode.getJmmChild(1).getJmmChild(0).get("name");
                left_side_type = symbolTable.getReturnType(call_method_name).getName();
            }
        }
        else if(plusNode.getJmmChild(0).getKind().equals("ArrayAccess")){

            left_side_type = symbolTable.getVariableType(method_name,plusNode.getJmmChild(0).getJmmChild(0).get("name")).getName();
        }
        else if(plusNode.getJmmChild(0).getKind().equals("True") || plusNode.getJmmChild(0).getKind().equals("False") || isBooleanExpression){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add booleans", null));
        }
        else{
            left_side_type = "int";
        }

        if(!left_side_type.equals("int")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add not ints", null));

        if (plusNode.getJmmChild(1).getKind().equals("Identifier")) {
//            System.out.println(plusNode.getJmmChild(1));
//            System.out.println(symbolTable.isArray(method_name, plusNode.getJmmChild(1).get("name")));
            System.out.println(plusNode);
            System.out.println(plusNode.getJmmChild(0));
           //System.out.println(symbolTable.isArray(method_name, plusNode.getJmmChild(0).get("name")));
            if (symbolTable.isArray(method_name, plusNode.getJmmChild(1).get("name"))){
                if(symbolTable.isArray(method_name, plusNode.getJmmChild(0).get("name"))){
                    reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add two arrays", null));
                }
                else reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add int/boolean to an array", null));
            }
//            else if(symbolTable.isArray(method_name, plusNode.getJmmChild(0).get("name"))){
//                if(symbolTable.isArray(method_name, plusNode.getJmmChild(1).get("name"))){
//                    reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add two arrays", null));
//                }
//                else reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add int/boolean to an array", null));
//            }
            String right_side = symbolTable.getVariableType(method_name, plusNode.getJmmChild(1).get("name")).getName();
            if (!left_side_type.equals(right_side))
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "variables of different type", null));
        }
        else if(plusNode.getJmmChild(1).getKind().equals("Identifie")){

        }
        else if(!plusNode.getJmmChild(1).getKind().equals("Number")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Can't add variable which aren't of type int5", null));


        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }

}
