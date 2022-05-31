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
        String method_name = dotAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        List<Symbol> parameters = symbolTable.getParameters(dotAccessNode.getJmmChild(0).get("name"));
        System.out.println(parameters);
        if(dotAccessNode.getJmmChild(1).getKind().equals("DotDeclarationArgs")){
            System.out.println("entrei");
            System.out.println("numero flhos" + dotAccessNode.getJmmChild(1).getNumChildren() );
            if(parameters == null && dotAccessNode.getJmmChild(1).getNumChildren() != 0) {
                System.out.println("PERA");
                System.out.println(symbolTable.getImports().isEmpty());
                if(!symbolTable.getImports().isEmpty()) {
                    System.out.println("trueeeeeeeeeee");
                    return 1;
                }
                System.out.println("COUE");

                System.out.println("maçâ");
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Incompatible number of arguments \"" , null));
                return 1;
            }
            else if(parameters == null && dotAccessNode.getJmmChild(1).getNumChildren() == 0) return 1;
            else {
                for (var j = 0; j < dotAccessNode.getJmmChild(1).getNumChildren(); j++) {
                    if (parameters != null) {
                        for (Symbol parameter : parameters) {
                            String argumentType = symbolTable.getVariableType(method_name, dotAccessNode.getJmmChild(1).getJmmChild(0).get("name")).getName();
                            String parameterType = parameter.getType().getName();

                            if (!argumentType.equals(parameterType)) {
                                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Incompatible number of arguments \"", null));
                                return 0;
                            }
                        }
                    }
                }
            }
        }
        /*String left_node_name = dotAccessNode.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_type,left_node_name).getName();

        String methodReturnType = symbolTable.getReturnType(method_type).getName();

        if(!left_node_type.equals(methodReturnType)){}*/
        return 1;
    }
    public List<Report> getReports(){
        return reports;
    }
}
