package pt.up.fe.comp.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.javacc.output.Translator.SymbolTable;

import pt.up.fe.comp.ast.ASTNode;
import pt.up.fe.comp.ast.ASTUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

public class SymbolTableFiller extends PreorderJmmVisitor<SymbolTableBuilder,Integer> {

    private final List<Report> reports;

    public SymbolTableFiller(){
        this.reports = new ArrayList<>();

        addVisit(ASTNode.IMPORT_DECL, this::importDeclVisit);
        addVisit(ASTNode.CLASS_DECL, this::classDeclVisit);
        addVisit(ASTNode.METHOD_DECL, this::methodDeclVisit);
    }

    public List<Report> getReports(){
        return reports;
    }

    private Integer importDeclVisit(JmmNode importDecl, SymbolTableBuilder symbolTable){
        var importString = importDecl.getChildren().stream().map(id->id.get("name")).collect(Collectors.joining("."));

        symbolTable.addImport(importString);
        return 0;
    }
    
    private Integer classDeclVisit(JmmNode classDecl, SymbolTableBuilder symbolTable){
        symbolTable.setClassName(classDecl.get("name"));
        classDecl.getOptional("extends").ifPresent(superClass -> symbolTable.setSuperClass(superClass));

        return 0;
    }

    private Integer methodDeclVisit(JmmNode methodDecl, SymbolTableBuilder symbolTable){

        var methodName = methodDecl.getJmmChild(1).get("name");
        
        if(symbolTable.hasMethod(methodName)){
            reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(methodDecl.get("line")), Integer.valueOf(methodDecl.get("col")), "Found duplicated method with signature '" + "methodName" + "'", null));

            return -1;
        }

        var returnTypeNode = methodDecl.getJmmChild(0);

        var returnType = ASTUtils.buildType(returnTypeNode);

        // var params = methodDecl.getChildren().subList(2, methodDecl.getNumChildren()-1);
        var params = methodDecl.getChildren().subList(2, methodDecl.getNumChildren()).stream().filter(node -> node.getKind().equals("Param")).collect(Collectors.toList());

        var paramSymbols = params.stream().map(param -> new Symbol(ASTUtils.buildType(param.getJmmChild(0)), param.getJmmChild(1).get("name"))).collect(Collectors.toList());

        symbolTable.addMethod(methodName,returnType, paramSymbols);
        
        return 0;
    }
}
