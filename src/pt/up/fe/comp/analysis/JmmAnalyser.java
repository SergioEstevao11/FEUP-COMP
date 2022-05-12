package pt.up.fe.comp.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

public class JmmAnalyser implements JmmAnalysis{

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        List<Report> reports = new ArrayList<>();

        SymbolTable symbolTable = null;

        var SymbolTableFiller = new SymbolTableFiller();
        // SymbolTableFiller.visit(parserResult.getRootNode(), symbolTable)

        reports.addAll(SymbolTableFiller.getReports());

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
