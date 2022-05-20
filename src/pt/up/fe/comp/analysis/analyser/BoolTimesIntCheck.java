package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.Identifier;
import pt.up.fe.comp.MethodBody;
import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;


public class BoolTimesIntCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;
    public BoolTimesIntCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("Times", this::visitTimes);
        setDefaultVisit((node, oi) -> 0);
    }

    public Integer visitTimes(JmmNode timesNode,Integer ret){
        System.out.println("im inside visit times");

        String method_name = timesNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = timesNode.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
        String right_node_name = timesNode.getJmmChild(1).get("name");
        String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();

        String type_deles_left = symbolTable.getType(timesNode.getJmmChild(0), "name").getName();
        String type_deles_right = symbolTable.getType(timesNode.getJmmChild(1), "name").getName();
        System.out.println("type deles left: " + type_deles_left);
        System.out.println("type deles right: " + type_deles_right);


        if (left_node_type.equals("boolean") && right_node_type.equals("int")){
            System.out.println("1");
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + timesNode.getJmmChild(1) + "\" invalid type: expecting an boolean.", null));
        }
        else if (left_node_type.equals("int") && right_node_type.equals("boolean")){
            System.out.println("2");

            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + timesNode.getJmmChild(1) + "\" invalid type: expecting an boolean.", null));
        }
        else if (timesNode.getJmmChild(0).getKind().equals("Number") && right_node_type.equals("boolean")){
            System.out.println("3");

            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + timesNode.getJmmChild(1) + "\" invalid type: expecting an boolean.", null));
        }
        else if (left_node_type.equals("int") && right_node_type.equals("boolean")){
            System.out.println("4");

            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + timesNode.getJmmChild(1) + "\" invalid type: expecting an boolean.", null));
        }
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }

}
