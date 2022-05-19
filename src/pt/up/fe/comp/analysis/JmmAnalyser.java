package pt.up.fe.comp.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.analysis.analyser.*;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.report.StyleReport;

public class JmmAnalyser implements JmmAnalysis{

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        if (TestUtils.getNumReports(parserResult.getReports(), ReportType.ERROR) > 0) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but there are errors from previous stage");
            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errorReport));
        }

        List<Report> reports = new ArrayList<>();
        var symbolTable = new SymbolTableBuilder();

        var rootNode = parserResult.getRootNode();

        if (rootNode == null) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but AST root node is null");
            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errorReport));
        }

        var symbolTableFiller = new SymbolTableFiller();
        symbolTableFiller.visit(rootNode, symbolTable);
        reports.addAll(symbolTableFiller.getReports());

        System.out.println("antes");

        var BoolTimesInt = new BoolTimesIntCheck();
        BoolTimesInt.visit(rootNode, symbolTable);
        reports.addAll(BoolTimesInt.getReports());

        System.out.println(reports);

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
