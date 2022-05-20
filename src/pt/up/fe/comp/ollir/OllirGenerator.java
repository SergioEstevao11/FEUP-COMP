package pt.up.fe.comp.ollir;
import pt.up.fe.comp.IDENTIFIER;
import pt.up.fe.comp.INTEGERLITERAL;
import pt.up.fe.comp.ast.ASTNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.lang.reflect.Method;
import java.text.AttributedString;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, String>{

    static int varCounter;
    private final StringBuilder code;
    private final SymbolTable symbolTable;

    public OllirGenerator(SymbolTable symbolTable){
            this.varCounter = 0;
            this.code = new StringBuilder();
            this.symbolTable = symbolTable;

            addVisit(ASTNode.START, this::programVisit);
            addVisit(ASTNode.IMPORT_DECLARATION, this::importDeclVisit);
            addVisit(ASTNode.CLASS_DECLARATION, this::classDeclVisit);
            addVisit(ASTNode.METHOD_DECLARATION, this::methodDeclVisit);
            addVisit(ASTNode.DOT_ACCESS, this::memberCallVisit);
            addVisit(ASTNode.ARGUMENTS, this::argumentsVisit);
            addVisit(ASTNode.IDENTIFIER, this::idVisit);
            addVisit(ASTNode.INT, this::intVisit);
            addVisit(ASTNode.TRUE, this::boolVisit);
            addVisit(ASTNode.FALSE, this::boolVisit);
            addVisit(ASTNode.EXPRESSION_STATEMENT, this::operationVisit);
            addVisit(ASTNode.EXPRESSION, this::operationVisit);
            addVisit(ASTNode.LESS_DECLARATION, this::operationVisit);
            addVisit(ASTNode.ADD_SUB_DECLARATION, this::operationVisit);
            addVisit(ASTNode.MULT_DIV_DECLARATION, this::operationVisit);
            addVisit(ASTNode.NOT_DECLARATION, this::operationVisit);
            addVisit(ASTNode.NEW, this::newVisit);
            addVisit(ASTNode.IF, this::ifVisit);
            //addVisit(ASTNode.ELSE, this::elseVisit);
            addVisit(ASTNode.WHILE, this::whileVisit);
            addVisit(ASTNode.ASSIGNMENT, this::assignmentVisit);
            addVisit(ASTNode.RETURN, this::returnVisit);
            addVisit(ASTNode.ARRAY_ACCESS, this::arrayAccessVisit);
            setDefaultVisit(this::defaultVisit);


        //addVisit("VAR_DECLARATION", this::dealWithVar);

        //addVisit("ASSIGNMENT", this::dealWithAssignment);
        //addVisit("IF", this::dealWithIf);
        //addVisit("ELSE", this::dealWithElse);
        //addVisit("WHILE", this::dealWithWhile);
        //addVisit("ARRAY_ACCESS", this::dealWithArrayAccess);
        //setDefaultVisit(this::defaultVisit);
    }

    public String getCode(){
        return code.toString();
    }

    private String programVisit(JmmNode program, Integer dummy){

        //System.out.println(code);

        for (var child: program.getChildren())
            code.append(visit(child));
        
        return code.toString();
    }
    private String importDeclVisit(JmmNode importDecl, Integer dummy) {
        StringBuilder importString = new StringBuilder();
        for (var importStmt : symbolTable.getImports())
            importString.append("import ").append(importStmt).append(";\n");

        return importString.toString();
    }

    private String classDeclVisit(JmmNode classDecl, Integer dummy){
        StringBuilder classString = new StringBuilder();

        classString.append(symbolTable.getClassName());

        var superClass = symbolTable.getSuper();
        if (superClass != null)
            classString.append(" extends ").append(superClass);

        classString.append(" {\n");

        classString.append(".construct ").append(symbolTable.getClassName()).append("().V{\ninvokespecial(this, \"<init>\").V;\n}\n\n");

        for (var child: classDecl.getChildren())
            if (!child.getKind().equals("Identifier")) classString.append(visit(child));


        classString.append("}\n");
        
        return classString.toString();
    }

    private String methodDeclVisit(JmmNode methodDecl, Integer dummy){
        StringBuilder methodString = new StringBuilder();
        var methodType = methodDecl.getJmmChild(0);

        boolean isMain = methodType.getKind().equals("MainMethodHeader");

        methodString.append(" .method public ");
        if (isMain)
            methodString.append("static main(");

        else methodString.append(methodDecl.getJmmChild(0).get("name")); //TODO CHECKAR COMMON METHOD


        var methods = symbolTable.getMethods();
        var params = symbolTable.getParameters(methods.get(methods.size() - 1));

        var paramCode = params.stream().map(symbol -> OllirUtils.getCode(symbol)).collect(Collectors.joining(", "));
        methodString.append(paramCode);

        methodString.append(").");

        methodString.append(OllirUtils.getOllirType(symbolTable.getReturnType(methods.get(methods.size() - 1))));
        methodString.append(" {\n");


         for (var stmt: methodDecl.getJmmChild(1).getChildren())
             methodString.append(visit(stmt));

        methodString.append("}\n");

        return methodString.toString();
    }


    private String ifVisit(JmmNode ifStmt, Integer dummy){
        JmmNode conditionStmt = ifStmt.getChildren().get(0);
        JmmNode ifBlock = ifStmt.getChildren().get(1);
        JmmNode elseBlock = ifStmt.getChildren().get(2);

        code.append("if (");
        visit(conditionStmt); // TRATAR INVERTER CONDIÇÃO
        code.append("goto then;\n").append("else:\n");
        
        var elseStmts = elseBlock.getChildren();
        for (var elseStmt: elseStmts)
            visit(elseStmt);

        code.append("goto endif;\n").append("then:\n");
        var thenStmts = ifBlock.getChildren();
        for (var thenStmt: thenStmts)
            visit(thenStmt);
      
        code.append("endif:\n");
            
        return "";
    }

    private String whileVisit(JmmNode whileStmt, Integer dummy){
        JmmNode conditionStmt = whileStmt.getChildren().get(0);
        JmmNode whileBlock =  whileStmt.getChildren().get(1);

        code.append("Loop:\n");

        visit(conditionStmt); // TODO INVERTER CONDIÇÃO
        code.append("goto Endloop;\n");

        var whileStmts = whileBlock.getChildren();
        for (var whileBlockStmt: whileStmts)
            visit(whileBlockStmt);

        code.append("goto Loop;\n").append("EndLoop:");

            
        return "";
    }

     private String assignmentVisit(JmmNode assignmentStmt, Integer dummy){
        JmmNode leftStmt = assignmentStmt.getChildren().get(0); 
        JmmNode rightStmt = assignmentStmt.getChildren().get(1); 

        return "";
     }

     public String arrayAccessVisit(JmmNode arrayAccess, Integer dummy){
        //if (arrayAccess.getJmmChild(0).getKind("Identifier"), )

        return "";
     }

    private String memberCallVisit(JmmNode memberCall, Integer dummy){
        StringBuilder methodString = new StringBuilder();
        JmmNode id= memberCall.getJmmChild(0);
        JmmNode func = memberCall.getJmmChild(1);
        String type = OllirUtils.getOllirOperator(memberCall, code);

        if (id.getKind().equals("Identifier")) {
            methodString.append("invokestatic(").append(visit(id));

            methodString.append(", \"").append(visit(func)).append("\"");

            //visit(memberCall.getJmmChild(2)); // TODO ARGS
            methodString.append(")").append(type).append(";\n");

            return methodString.toString();

        } else if (id.getKind().equals("NEW")) {

            String var = newAuxiliarVar(symbolTable.getReturnType(memberCall.get("name")).getName());
            methodString.append("invokespecial(").append("t" + var).append(", \"<init>\").V;\n");
            methodString.append(visit(id));
        } else {
            String varName = this.getVarName(id);
            String callName = !func.getKind().equals("LENGTH") ? func.get("name") : "length";
            String kind = !varName.equalsIgnoreCase("this") ? varName + "." : "";

            if (callName.equals("length")) {
                methodString.append("t").append(varCounter++).append(".i32 :=.i32 arraylength(").append(kind).append("array.i32).i32;\n");

            } else {
                Type tp = symbolTable.getReturnType(func.get("name"));


                methodString.append("invokevirtual(");
                methodString.append(kind);
                if (tp.isArray()) methodString.append("array.");
                methodString.append(tp.getName());

                methodString.append(", \"");
                visit(memberCall.getJmmChild(1));
                methodString.append("\"");
                //visit(memberCall.getJmmChild(2)); // TODO ARGS
                methodString.append(")");

                methodString.append(type).append(";\n");
            }
        }

        return methodString.toString();
    }


    private String getVarName(JmmNode identifier) {
        if (identifier.getKind().equals("THIS"))
            return "this";
        if (identifier.getKind().equals("NEW"))
            return identifier.getChildren().get(0).get("name");
        return identifier.get("name");
    }


    private String argumentsVisit(JmmNode arguments, Integer dummy){
        StringBuilder argsString = new StringBuilder();
        for (var child: arguments.getChildren())
            argsString.append(", ").append(visit(child));

        return argsString.toString();
    }

    private String returnVisit(JmmNode returnNode, Integer dummy){
        StringBuilder returnString = new StringBuilder();
        JmmNode expression = returnNode.getChildren().get(0);

        Optional<JmmNode> methodNode = returnNode.getAncestor("Method_Decl");
        String exp = visit(expression);
        returnString.append("ret").append(OllirUtils.getOllirType(symbolTable.getReturnType(methodNode.get().get("name"))));

        if (exp.contains(";")) returnString.append(" t").append(varCounter).append(OllirUtils.getOllirType(symbolTable.getReturnType(methodNode.get().get("name"))));
        else {
            returnString.append("ret").append(OllirUtils.getOllirType(symbolTable.getReturnType(methodNode.get().get("name"))));
            returnString.append(" ").append(exp);
        }
        returnString.append(";\n");
        return returnString.toString();
    }


    private String idVisit(JmmNode id, Integer dummy){
        return id.get("name");
    }

    private String intVisit(JmmNode value, Integer dummy){
        return value.get("value") + ".i32";
    }

    private String boolVisit(JmmNode value, Integer dummy){
        return value.get("value") + ".bool";
    }

    private String newVisit(JmmNode newNode, Integer dummy){

        StringBuilder newString = new StringBuilder();
        JmmNode child = newNode.getChildren().get(0);
        if (child.getKind().equals("ARRAY")) {
            JmmNode grandChild = child.getChildren().get(0);
            newString.append("new(array, ");
            visit(grandChild);
            newString.append(").array.i32");
           // if (grandchild.getKind().equals("OBJECT_METHOD")) {
           //     str += grandchildVisit + "\n";
           //     grandchildVisit = grandchildVisit.substring(0, grandchildVisit.indexOf(" "));
           // }
        } else newString.append("new(").append(child.get("name")).append(").").append(child.get("name"));

        return newString.toString();
    }

    private String parseOp(String op){
        if (op.equals("AND") || op.equals("LESS") || op.equals("NOT")) return "bool";
        return "int";
    }

    private String defaultVisit(JmmNode defaultNode, Integer dummy) {
        StringBuilder visitStr = new StringBuilder();
        for (JmmNode child : defaultNode.getChildren()) {
            visitStr.append(visit(child, dummy));
        }
        return visitStr.toString();
    }

    private String operationVisit(JmmNode operation, Integer dummy) {
        StringBuilder operationString = new StringBuilder();
        boolean isNot = operation.getKind().equals("NOT");

        operation.get("value");

        JmmNode lhs = operation.getChildren().get(0);
        JmmNode rhs = operation.getChildren().get(1);

        if (!lhs.getKind().equals("IDENTIFIER"))
            visit(lhs);

        else if (!rhs.getKind().equals("IDENTIFIER") && !isNot)
            visit(rhs);




        String var = newAuxiliarVar(parseOp(lhs.getKind()));
        code.append(var).append(" :=").append(" ");

        //TODO NOT CORRECT
        visit(lhs);
        code.append(" ");
        if (isNot) {
            code.append(OllirUtils.getOllirOperator(operation, code));
            visit(lhs);
            code.append(";\n");
        }
        else {
            visit(lhs);
            code.append(" ").append(OllirUtils.getOllirOperator(operation, code));
            visit(rhs);
            code.append(";\n");
        }

        return operationString.toString();
    }
  // visitBinOp(JmmNode node){

  //     Code lhs = visit(node.getChild(0));
  //     Code rhs = visit(node.getChild(1));
  //     String op = node.getAttribute("op");

  //     Code thisCode = new Code();
  //     thisCode.prefix = lhs.prefix;
  //     thisCode.prefix += rhs.prefix;

  //     //here you can decide if the temporary variable is necessary or not

  //     //I am considering that I always need a new temp

  //     String temp = createTemp();
  //     thisCode.prefix += temp + "=" + lhs.code + op + rhs.code;
  //     thisCode.code = temp;

  //     return thisCode;

  // }

     
    private Type getFieldType(String name){
        for (Symbol symbol: symbolTable.getFields()){
            if (name.equals(symbol.getName()))
                return symbol.getType();
        }
        return null;
    }

    private int getCondition(String methodName, JmmNode node){
        //if(isField(node) || node.getKind().equals("Dot")){
            //String type = ".bool";
            //code.append(newAuxiliarVar(type, methodName, node));
            //code.append("t").append(varCounter).append(type).append(" &&.bool 1.bool");
        //}

        visit(node);
        //String condition = ollirExpression(methodName, node);
        //if(!Utils.isBooleanExpression(node.getKind())){
            //condition += " &&.bool 1.bool";
        //}
        return 0;
    }

    // private String newAuxiliarVar(String type, String methodName, JmmNode node){
    //     String value;
    //     if(node.getKind().equals("Dot"))
    //         value = ollirDotMethod(methodName, node, type);
    //     else value = ollirExpression(methodName, node);
    //     varCounter++;
    //     return "t" + varCounter + type + " :=" + type +" " + value + ";\n";
    // }
    private String newAuxiliarVar(String type) {
        varCounter++;
        return "t" + varCounter + "." + type;
    }

     //compound
     //Var decl
};


