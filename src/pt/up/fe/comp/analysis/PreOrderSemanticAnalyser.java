package pt.up.fe.comp.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

    public abstract class PreOrderSemanticAnalyser extends PreorderJmmVisitor<SymbolTable,Integer> implements SemanticAnalyser {
    private final List<Report> reports;

    public PreOrderSemanticAnalyser(){
        reports = new ArrayList<>();
    }

    @Override
    public List<Report> getReports() {
        return null;
    }

    protected void addReport(Report report){
        reports.add(report);
    }
}
