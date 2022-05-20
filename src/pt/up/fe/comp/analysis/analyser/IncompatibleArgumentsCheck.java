package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class IncompatibleArgumentsCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public IncompatibleArgumentsCheck(SymbolTableBuilder symbolTable, List<Report> reports) {

        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("Return", this::visitReturn);
        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitReturn(JmmNode returnStatementNode, Integer ret) {
        String method_name = returnStatementNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = returnStatementNode.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();


        String methodReturnType = symbolTable.getReturnType(method_name).getName();

        if(!left_node_type.equals(methodReturnType)){}
        return 1;
    }
    public List<Report> getReports(){
        return reports;
    }
}
