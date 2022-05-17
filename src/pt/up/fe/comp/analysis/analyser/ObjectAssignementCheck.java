package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class ObjectAssignementCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {

    private final List<Report> reports;

    public ObjectAssignementCheck() {
        this.reports = new ArrayList<>();
        addVisit("Identifier", this::visitObjectAssignement);

        setDefaultVisit((node, oi) -> 0);
    }

    public Boolean isClassInstance(String typeStr) {
        return !typeStr.equals("int") && !typeStr.equals("int[]") && !typeStr.equals("String") && !typeStr.equals("boolean");
    }

    public Integer visitObjectAssignement(JmmNode node, SymbolTableBuilder symbolTable) {
        JmmNode objectNode = node.getChildren().get(0);
        String objectName = objectNode.get("name");
        System.out.println("name: " + objectName);

        // Check if the object is an instance of the actual class
        if(objectName.equals(symbolTable.getClassName())) return 1;

        // Check if the object is is an instance of the extended class
        if(symbolTable.getSuper() != null &&
                objectName.equals(symbolTable.getSuper())) return 1;

        // Check if it is an import
        if (UtilsAnalyser.hasImport(objectName, symbolTable)) return 1;

        System.out.println("node : " + node.getAttributes().get(Integer.parseInt("line")));

        reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")), "\"" + objectName + "\" is not an import nor an object",null));
        return 0;
    }
    public List<Report> getReports(){
        return reports;
    }
}