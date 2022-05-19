package pt.up.fe.comp.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolTableFiller extends PreorderJmmVisitor<String, String> {
    private final SymbolTableBuilder table;
    private String scope;
    private final List<Report> reports;

    public SymbolTableFiller(SymbolTableBuilder symbolTable, List<Report> reports) {
        // super(SymbolTableFiller::reduce);
        this.table = symbolTable;
        this.reports = reports;

        addVisit("ImportDeclaration", this::visitImports);
        addVisit("ClassDeclaration", this::visitClassDelcaration);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
        addVisit("MainMethodHeader", this::visitMainMethodDeclaration);
        addVisit("VarDeclaration", this::visitLocalVariables);
        addVisit("MethodDeclaration", this::visitParameter);

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

    private String visitMethodDeclaration(JmmNode node, String space) {
       // System.out.println("inisde method delcaration");
        scope = "METHOD";

       // table.addMethod(node.getJmmChild(0).get("name"), SymbolTableBuilder.getType(node, "return"), Collections.emptyList());

        /*
        System.out.println("Method Attributes: " + node.getAttributes());
        System.out.println("type: " + node.getKind());
        System.out.println("children's  0 type: " + node.getJmmChild(0).getKind());
        System.out.println("children's  1 type: " + node.getJmmChild(1).getKind());
        System.out.println("all children: " + node.getChildren());*/
        //System.out.println("inside method delcaration, name: " + node.getJmmChild(0).get("name") );

        node.put("params", "");

        return node.toString();
    }

    private String visitMainMethodDeclaration(JmmNode node, String space) {
     //   System.out.println("inside main method declaration");
        scope = "MAIN";

        table.addMethod("main", new Type("void", false), Collections.emptyList());

        node.put("params", "");

        return node.toString();
    }


    /*
    PARA PASSSAR NO TESTE: VarNotDeclared
     */
    private String visitLocalVariables(JmmNode node, String space) {
        System.out.println("inside visit local variables");
        System.out.println("scope: " + scope);

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

    private String visitParameter(JmmNode node, String space) {
        System.out.println("VISIT MainMethodHeader");


        //Quando passo methodBody
      /*  System.out.println("node kind: " + node.getKind());
        System.out.println("children's 0 kind: " + node.getJmmChild(0).getKind());*/

        //Quando passo MethodDeclaration para o visit
        /*System.out.println("node kind: " + node.getKind());
        System.out.println("children's 0 kind: " + node.getJmmChild(0).getKind());
        System.out.println("children's 0 attributes: " + node.getJmmChild(0).getJmmChild(0).getAttributes());
        //System.out.println("children's 0 name: " + node.getJmmChild(0).get("name"));
        System.out.println("children's 1 kind: " + node.getJmmChild(1).getKind());
        System.out.println("children's 1 , children 0 , attributes: " + node.getJmmChild(1).getJmmChild(0).getKind());*/

        /*
        QUANDO PASSO PARA O VISIT :

        System.out.println("node kind: " + node.getKind());
        System.out.println("children's 0 kind: " + node.getJmmChild(0).getKind());
        System.out.println("children's 1 kind: " + node.getJmmChild(1).getKind());
        System.out.println("arguments do node" + node.getAttributes());
        System.out.println("children's 0 attributes: " + node.getJmmChild(0).getAttributes());
        System.out.println("children's 1 attributes: " + node.getJmmChild(1).getAttributes());
        System.out.println("children's name: " + node.getJmmChild(0).get("name"));*/
        //System.out.println("node type: " + SymbolTableBuilder.getType(node,"t"));
       // System.out.println("scope : " + scope);
        if (scope.equals("METHOD")) {
            System.out.println("dentro da scope method");
            Symbol field = new Symbol(SymbolTableBuilder.getType(node, "type"), node.get("value"));
            table.addField(field);

            String paramType = field.getType().getName() + ((field.getType().isArray()) ? " []" : "");
            node.getJmmParent().put("params", node.getJmmParent().get("params") + paramType + ",");
        } else if (scope.equals("MAIN")) {
            System.out.println("dentro da scope main");;
            Symbol field = new Symbol(new Type("String", true), node.get("value"));
            table.addField(field);

            String paramType = field.getType().getName() + ((field.getType().isArray()) ? " []" : "");
            node.getJmmParent().put("params", node.getJmmParent().get("params") + paramType + ",");
        }

        return space + "PARAM";
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
