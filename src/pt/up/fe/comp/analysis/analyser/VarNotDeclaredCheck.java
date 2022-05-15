package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.AJmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class VarNotDeclaredCheck extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {

    private final List<Report> reports;

    public VarNotDeclaredCheck() {
        System.out.println("DENTRO DO CONSTRUTOR");
        this.reports = new ArrayList<>();
        addVisit("Identifier", this::visitVarDeclaration);

        setDefaultVisit((node, oi) -> 0);
    }

    public Boolean isClassInstance(String typeStr) {
        return !typeStr.equals("int") && !typeStr.equals("int[]") && !typeStr.equals("String") && !typeStr.equals("boolean");
    }

    public Integer visitVarDeclaration(JmmNode node, SymbolTableBuilder symbolTable) {

        if (!node.getAncestor("MethodBody").isEmpty()) {
            return 1;
        }

        String typeStr = node.getKind();

        if (!isClassInstance(typeStr)) return 1;

        if (UtilsAnalyser.hasImport(typeStr, symbolTable) || symbolTable.getClassName().equals(typeStr)) {
            return 1;
        }

        reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Type \"" + typeStr + "\" is missing.", null));
        return 0;
    }
    public List<Report> getReports(){
        return reports;
    }
}


