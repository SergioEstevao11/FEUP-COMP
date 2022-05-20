package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public class IncompatibleReturn extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final List<Report> reports;

    public IncompatibleReturn() {
        this.reports = new ArrayList<>();
        addVisit("Return", this::visitReturn);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitReturn(JmmNode returnNode, SymbolTableBuilder symbolTable){
        JmmNode leftNode = returnNode.getChildren().get(0);
        JmmNode rightNode = returnNode.getChildren().get(1);

        System.out.println("ESTOU AQUI DENTRO");
        String parentMethodName = UtilsAnalyser.getParentMethodName(returnNode);
        System.out.println(symbolTable.getType(leftNode,"type"));

        if(leftNode.getKind().equals("Identifier") ) {
            if(symbolTable.getReturnType(parentMethodName) != symbolTable.getType(leftNode,"type")){

            }//Ver getReturnType e ver se é  igual ao tipo do Identifier
        }
        else if (rightNode.getKind().equals("Dot")){
            String returnValueMethod = UtilsAnalyser.getReturnValueMethod(rightNode, symbolTable);
            //if (!returnValueMethod.equals("undefined") && returnValueMethod == symbolTable.getReturnType(parentMethodName) ){}
                //symbolTable.addReport(leftNode, "\"" + leftNode + "\" invalid type: expecting an int or an int[].");
        }

        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }

}
