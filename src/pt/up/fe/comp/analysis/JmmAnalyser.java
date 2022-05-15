package pt.up.fe.comp.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.comp.analysis.analyser.SingleMainMethodCheck;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

public class JmmAnalyser implements JmmAnalysis{

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        List<Report> reports = new ArrayList<>();

        var symbolTable = new SymbolTableBuilder();

        var SymbolTableFiller = new SymbolTableFiller();
        SymbolTableFiller.visit(parserResult.getRootNode(), symbolTable);
        reports.addAll(SymbolTableFiller.getReports());

        /*
        Semantic Analysis
        List<SemanticAnalyser> analysers = Arrays.asList(new SingleMainMethodCheck(symbolTable), new SingleMainMethodCheck() )

        for(var analyser: analysers){
            reports.addAll(analyser.getReports());
        }*/
        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
