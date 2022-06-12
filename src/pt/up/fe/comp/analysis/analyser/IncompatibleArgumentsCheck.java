package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
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
        addVisit("DotAccess", this::visitDotAccess);
        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitDotAccess(JmmNode dotAccessNode, Integer ret) {
        String method_name = null;
        if( dotAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = dotAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(0).get("name");
        else method_name = dotAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");

        List<Symbol> parameters = symbolTable.getParameters(dotAccessNode.getJmmChild(1).getJmmChild(0).get("name"));
        if(dotAccessNode.getJmmChild(1).getKind().equals("DotDeclarationArgs")){
            System.out.println("PARRAMETRROS: " + parameters);
            System.out.println("Numero filhos" + dotAccessNode.getJmmChild(1).getNumChildren() );
            if(parameters == null && dotAccessNode.getJmmChild(1).getNumChildren() != 0) {
                System.out.println("DIFERENT NUMERRO DE ARGUMENTOS");
                if(!symbolTable.getImports().isEmpty()) {
                    return 1;
                }
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Incompatible number of arguments \"" , null));
                return 1;
            }
            else if(parameters == null && dotAccessNode.getJmmChild(1).getNumChildren() == 0) {
                System.out.println("NUMERO IGUAL DE ARGUMENTOS");
                return 1;
            }
            else {
                System.out.println("NORMAL NUMERO DE ARGUMENTOS");
                for (var j = 0; j < dotAccessNode.getJmmChild(1).getNumChildren(); j++) {
                    if (parameters != null) {
                        String argumentType;
                        for (Symbol parameter : parameters) {
                            String parameterType = parameter.getType().getName();
                            System.out.println("PARAMETERTYPE:" + parameterType);
                            if(dotAccessNode.getJmmChild(1).getJmmChild(j).getKind().equals("This")){
                                System.out.println("ABACATE");
                                argumentType = symbolTable.getClassName();
                            }
                            else{
                                System.out.println("ABACATE2");
                                System.out.println(dotAccessNode.getJmmChild(1).getJmmChild(j).getKind());
                                if(dotAccessNode.getJmmChild(1).getJmmChild(j).getKind().equals("Number") || dotAccessNode.getJmmChild(1).getJmmChild(j).getKind().equals("True") || dotAccessNode.getJmmChild(1).getJmmChild(j).getKind().equals("False")){
                                    argumentType = dotAccessNode.getJmmChild(1).getJmmChild(j).getKind();
                                }
                                else {
                                    argumentType = symbolTable.getVariableType(method_name, dotAccessNode.getJmmChild(1).getJmmChild(j).get("name")).getName();
                                }
                            }
                            System.out.println("ARGUMENTTYPE:" + argumentType);
                            System.out.println("ola:" + !argumentType.equals(parameterType));
                            if (!argumentType.equals(parameterType)) {
                                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Incompatible arguments \"", null));
                                return 0;
                            }
                        }
                    }
                }
            }
        }
        return 1;
    }
    public List<Report> getReports(){
        return reports;
    }
}
