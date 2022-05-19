package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

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

        if(leftNode.getKind().equals("Identifier")) {
            //Ver getReturnType e ver se é  igual ao tipo do Identifier
        }

        return 0;
    }

}
