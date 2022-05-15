package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SemanticAnalyser;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public abstract class SingleMainMethodCheck extends PreorderJmmVisitor<Integer,Integer> implements SemanticAnalyser {

    private final List<Report> reports;

    public SingleMainMethodCheck(){
        reports = new ArrayList<>();
    }


    @Override
    public List<Report> getReports() {
        return null;
    }
}
