package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.PreOrderSemanticAnalyser;
import pt.up.fe.comp.analysis.SemanticAnalyser;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SingleMainMethodCheck implements SemanticAnalyser {

    private final SymbolTable symbolTable;

    public SingleMainMethodCheck(SymbolTable symbolTable){
        this.symbolTable = symbolTable;

    }

    @Override
    public List<Report> getReports(){
        if(!symbolTable.getMethods().contains("main")){
            return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC , -1, -1, "Class " + symbolTable.getClassName() +  " does not contain main method"));
        }
        return Collections.emptyList();
    }
}
