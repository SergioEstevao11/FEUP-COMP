package pt.up.fe.comp.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.analysis.analyser.*;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class JmmAnalyser implements JmmAnalysis{

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
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
        symbolTableFiller.visit(rootNode, "");
        reports.addAll(reports);

        System.out.println("Semantic Analysis");

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_02_ClassNotImported
        /*var classNotImported = new ClassNotImportedCheck(symbolTable,reports);
        classNotImported.visit(rootNode,0);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_03_IntPlusObject  //falta dot access
        /*var intPlusObject = new IntPlusObjectCheck(symbolTable,reports);
        intPlusObject.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_04_BoolTimesInt  //falta dot access
        /*var boolTimesIntCheck = new BoolTimesIntCheck(symbolTable,reports);
        boolTimesIntCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_05_ArrayPlusInt //falta dot access
        /*var arrayPlusIntCheck = new ArrayPlusIntCheck(symbolTable,reports);
        arrayPlusIntCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_06_ArrayAccessOnInt
        /*var arrayAccessOnInt = new ArrayAccessOnIntCheck(symbolTable,reports);
        arrayAccessOnInt.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_07_ArrayIndexNotInt  //falta dot access
        /*var arrayIndexNotIntCheck = new ArrayIndexNotIntCheck(symbolTable,reports);
        arrayIndexNotIntCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_08_AssignIntToBool   //AST PARTIDA NO CASO DE DOT ACCESS
        /*var assignIntToBoolCheck = new AssignIntToBoolCheck(symbolTable,reports);
        assignIntToBoolCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_09_ObjectAssignmentFail
        /*var objectAssignementCheck = new ObjectAssignmentCheck(symbolTable,reports);
        objectAssignementCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_10_ObjectAssignmentPassExtends
        /*var objectAssignementCheck = new ObjectAssignmentCheck(symbolTable,reports);
        objectAssignementCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_11_ObjectAssignmentPassImports
        /*var objectAssignementCheck = new ObjectAssignmentCheck(symbolTable,reports);
        objectAssignementCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_12_ObjectAssignmentPassImports
        /*var intInIfConditionCheck = new IntInIfConditionCheck(symbolTable,reports);
        intInIfConditionCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_13_ArrayInWhileCondition
        /*var arrayInWhileCondition = new ArrayInWhileCondition(symbolTable,reports);
        arrayInWhileCondition.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_14_CallToUndeclaredMethod
        /*var callToUndeclaredMethodCheck = new CallToUndeclaredMethodCheck(symbolTable,reports);
        callToUndeclaredMethodCheck.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_15_CallToMethodAssumedInExtends
        /*var callToMethodAssumedInExtends = new CallToMethodAssumedInExtends(symbolTable,reports);   /VERIFICAR AGAIN
        callToMethodAssumedInExtends.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_16_CallToMethodAssumedInImport
        /*var callToMethodAssumedInExtends = new CallToMethodAssumedInExtends(symbolTable,reports);    /VERIFICAR AGAIN
        callToMethodAssumedInExtends.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_17_IncompatibleArguments
        /*var incompatibleArguments = new IncompatibleArgumentsCheck(symbolTable,reports);
        incompatibleArguments.visit(rootNode,null);*/

        //CHECK ESTÁ A CORRER BEM, FALTA SÓ ACRRESCENTAR CASOS EXTRA
        // test_1_18_IncompatibleReturn
        var incompatibleReturnCheck = new IncompatibleReturnCheck(symbolTable,reports);
        incompatibleReturnCheck.visit(rootNode,null);



        System.out.println(reports);

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
