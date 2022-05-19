package pt.up.fe.comp.analysis;

import pt.up.fe.comp.ImportDeclaration;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolTableFiller extends PreorderJmmVisitor<String, String> {
    private final SymbolTableBuilder table;
    private String scope;
    private final List<Report> reports;

    public SymbolTableFiller(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.table = symbolTable;
        this.reports = reports;

        addVisit("ImportDeclaration", this::visitImports);
        addVisit("ClassDeclaration", this::visitClassDelcaration);
        addVisit("CommonMethodHeader", this::visitCommonMethodDeclaration);
        addVisit("MainMethodHeader", this::visitMainMethodDeclaration);
        //addVisit("MethodDeclaration", this::visitParameter);
    //    addVisit("VarDeclaration", this::visitLocalVariables);
      //  addVisit("MethodBody", this::visitParameter); children -> são as variáveis locais

        setDefaultVisit(this::defaultVisit);
    }

    private String visitImports(JmmNode node, String space) {
        List<String> imports = table.getImports();

        var counter = 0;
        for (JmmNode ignored : node.getChildren()){
            String newImport = node.getJmmChild(counter).get("name");
            imports.add(newImport);
            counter++;
        }

        return space + "IMPORT";
    }

    private String visitClassDelcaration(JmmNode node, String space) {

        table.setClassName(node.getJmmChild(0).get("name"));
        try {
            table.setSuperClass(node.getJmmChild(1).get("name"));
        } catch (NullPointerException ignored) {

        }

        scope = "CLASS";
        return space + "CLASS";
    }

    private String visitCommonMethodDeclaration(JmmNode node, String space) {
        scope = "METHOD";

        List<Symbol> parameters = new ArrayList<>();

        for(int counter = 2 ; counter < node.getNumChildren() ; counter++){
            Symbol parameter = new Symbol(SymbolTableBuilder.getType( node.getJmmChild(counter), "type"), node.getJmmChild(counter+1).get("name"));
            parameters.add(parameter);
            counter++;
        }


        table.addMethod(node.getJmmChild(1).get("name"), SymbolTableBuilder.getType(node.getJmmChild(0), "type"), parameters);

        return node.toString();
    }

    private String visitMainMethodDeclaration(JmmNode node, String space) {
        scope = "MAIN";

        table.addMethod("main", new Type("void", false), Collections.emptyList());

        //node.put("params", "");

        return node.toString();
    }

    /*
    PARA PASSSAR NO TESTE: VarNotDeclared
     */
    private String visitLocalVariables(JmmNode node, String space) {
        Symbol field = new Symbol(SymbolTableBuilder.getType(node, "type"), node.get("identifier"));

        if (scope.equals("CLASS")) {
            if (table.hasField(field.getName())) {
                this.reports.add(new Report(
                        ReportType.ERROR, Stage.SEMANTIC,
                        Integer.parseInt(node.get("line")),
                        Integer.parseInt(node.get("col")),
                        "Variable already declared: " + field.getName()));
                return space + "ERROR";
            }
            table.addField(field);
        } else {
            if (table.hasField(field.getName())) {
                this.reports.add(new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(node.get("line")),
                        Integer.parseInt(node.get("col")),
                        "Variable already declared: " + field.getName()));
                return space + "ERROR";
            }
            table.addField(field);
        }

        return space + "VARDECLARATION";
    }

    /*

    private String dealWithParameter(JmmNode node, String space) {
        if (scope.equals("METHOD")) {
            Symbol field = new Symbol(SymbolTableBuilder.getType(node, "type"), node.get("value"));
            table.getMe.addParameter(field);

            String paramType = field.getType().getName() + ((field.getType().isArray()) ? " []" : "");
            node.getJmmParent().put("params", node.getJmmParent().get("params") + paramType + ",");
        } else if (scope.equals("MAIN")) {
            Symbol field = new Symbol(new Type("String", true), node.get("value"));
            table.getCurrentMethod().addParameter(field);

            String paramType = field.getType().getName() + ((field.getType().isArray()) ? " []" : "");
            node.getJmmParent().put("params", node.getJmmParent().get("params") + paramType + ",");
        }

        return space + "PARAM";
    }

    private String dealWithMainDeclaration(JmmNode node, String space) {
        scope = "MAIN";

        table.addMethod("main", new Type("void", false));

        node.put("params", "");

        return node.toString();
    }*/

    /**
     * Prints node information and appends space
     *
     * @param node  Node to be visited
     * @param space Info passed down from other nodes
     * @return New info to be returned
     */
    private String defaultVisit(JmmNode node, String space) {
        String content = space + node.getKind();
        String attrs = node.getAttributes()
                .stream()
                .filter(a -> !a.equals("line"))
                .map(a -> a + "=" + node.get(a))
                .collect(Collectors.joining(", ", "[", "]"));

        content += ((attrs.length() > 2) ? attrs : "");

        return content;
    }


}
