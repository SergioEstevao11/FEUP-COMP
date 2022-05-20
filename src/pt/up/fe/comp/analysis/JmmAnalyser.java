package pt.up.fe.comp.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.analysis.analyser.BoolTimesIntCheck;
import pt.up.fe.comp.analysis.analyser.ClassNotImportedCheck;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class JmmAnalyser implements JmmAnalysis{

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        System.out.println("DENTRO DO FUCKING SEMANTIC ANALYSER");
        if (TestUtils.getNumReports(parserResult.getReports(), ReportType.ERROR) > 0) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but there are errors from previous stage");
            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errorReport));
        }

        var rootNode = parserResult.getRootNode();

        if (rootNode == null  || rootNode.getJmmParent() != null ) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but AST root node is null or had a parent");
            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errorReport));
        }

        List<Report> reports = new ArrayList<>();
        var symbolTable = new SymbolTableBuilder();
        System.out.println("Symbol Table Created");

        System.out.println("Filling Symbol Table");
        var symbolTableFiller = new SymbolTableFiller(symbolTable, reports);
        symbolTableFiller.visit(rootNode, null);
        reports.addAll(reports);
        System.out.println("Symbol Table Filled");


        System.out.println("Reports antes :)");
        System.out.println(reports);
        var boolTimesIntCheck = new BoolTimesIntCheck(symbolTable,reports);
        boolTimesIntCheck.visit(rootNode,null);
        reports.addAll(reports);
        System.out.println("Reports depois :(");

        System.out.println(reports);


        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
