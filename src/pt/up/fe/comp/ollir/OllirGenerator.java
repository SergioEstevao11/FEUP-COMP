package pt.up.fe.comp.ollir;
import pt.up.fe.comp.IDENTIFIER;
import pt.up.fe.comp.INTEGERLITERAL;
import pt.up.fe.comp.ast.ASTNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Optional;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, Integer>{

    static int varCounter;
    private final StringBuilder code;
    private final SymbolTable symbolTable;

    public OllirGenerator(SymbolTable symbolTable){
            this.varCounter = 0;
            this.code = new StringBuilder();
            this.symbolTable = symbolTable;

            addVisit(ASTNode.START, this::programVisit);
            addVisit(ASTNode.IMPORT_DECLARATION, this::programVisit);
            addVisit(ASTNode.CLASS_DECLARATION, this::classDeclVisit);
            addVisit(ASTNode.METHOD_DECLARATION, this::methodDeclVisit);
            addVisit(ASTNode.EXPR, this::exprStmtVisit);
            addVisit(ASTNode.MEMBER_CALL, this::memberCallVisit);
            addVisit(ASTNode.ARGUMENTS, this::argumentsVisit);
            addVisit(ASTNode.IDENTIFIER, this::idVisit);
            addVisit(ASTNode.INT, this::intVisit);
            addVisit(ASTNode.TRUE, this::boolVisit);
            addVisit(ASTNode.FALSE, this::boolVisit);
            addVisit(ASTNode.OPERATION, this::operationVisit);
            addVisit(ASTNode.LESS, this::operationVisit);
            addVisit(ASTNode.AND, this::operationVisit);
            addVisit(ASTNode.NOT, this::operationVisit);
            addVisit(ASTNode.NEW, this::newVisit);
            addVisit(ASTNode.IF, this::ifVisit);
            //addVisit(ASTNode.ELSE, this::elseVisit);
            addVisit(ASTNode.WHILE, this::whileVisit);
            addVisit(ASTNode.ASSIGNMENT, this::assignmentVisit);
            addVisit(ASTNode.RETURN, this::returnVisit);
            setDefaultVisit(this::defaultVisit);

        //addVisit("CLASS_DECLARATION", this::dealWithClass);
        //addVisit("MAIN", this::dealWithMain);
        //addVisit("METHOD_DECLARATION", this::dealWithMethodDeclaration);
        //addVisit("IDENTIFIER", this::dealWithIdentifier);
        //addVisit("INT", this::dealWithInt);
        //addVisit("TRUE", this::dealWithBoolean);
        //addVisit("FALSE", this::dealWithBoolean);
        //addVisit("NEW", this::dealWithNew);
        //addVisit("OPERATION", this::dealWithOperation);
        //addVisit("LESS", this::dealWithOperation);
        //addVisit("AND", this::dealWithOperation);
        //addVisit("EXCLAMATION", this::dealWithOperation);
        //addVisit("OBJECT_METHOD", this::dealWithObjectMethod);



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

    private Integer programVisit(JmmNode program, Integer dummy){
        for (var importString : symbolTable.getImports())
            code.append("import ").append(importString).append(";\n");

        System.out.println(code);

        for (var child: program.getChildren())
            visit(child);
        
        return 0;
    }

    private Integer classDeclVisit(JmmNode classDecl, Integer dummy){
        code.append("public ").append(symbolTable.getClassName());
        var superClass = symbolTable.getSuper();
        if (superClass != null)
            code.append(" extends ").append(superClass);

        code.append(" {\n");

        System.out.println(code);

        for (var child: classDecl.getChildren())
            visit(child);
        

        code.append("}\n");
        
        return 0;
    }

    private Integer methodDeclVisit(JmmNode methodDecl, Integer dummy){
        var methodSignature = methodDecl.getJmmChild(1).get("name");
        boolean isStatic = Boolean.valueOf(methodDecl.get("isStatic"));
        boolean isMain = Boolean.valueOf(methodDecl.getKind().equals("MainMethod"));

        code.append(" .method public "); // TODO VALE PRIVATES?
        if (isStatic)
            code.append("static ");

        if (isMain)
            code.append("main(");
        else code.append(methodDecl.getChildren().get(1).get("name"));

        

        var params = symbolTable.getParameters(methodSignature);

        var paramCode = params.stream().map(symbol -> OllirUtils.getCode(symbol)).collect(Collectors.joining(", "));
        code.append(paramCode);

        code.append(")");

        code.append(OllirUtils.getOllirType(symbolTable.getReturnType(methodSignature)));
        code.append(" {\n");

        int lastParamIndex = -1;
        for (int i = 0; i < methodDecl.getNumChildren();i++)
            if (methodDecl.getJmmChild(i).getKind().equals("Param"))
                lastParamIndex = i;
        
        var stmts = methodDecl.getChildren().subList(lastParamIndex + 1, methodDecl.getNumChildren());

         for (var stmt: stmts)
            visit(stmt);



        code.append("}\n");

        return 0;
    }

    private Integer exprStmtVisit(JmmNode exprStmt, Integer dummy){

        visit(exprStmt.getJmmChild(0));
        code.append(";\n");
        
        
        return 0;
    }

    private Integer ifVisit(JmmNode ifStmt, Integer dummy){
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
            
        return 0;
    }

    private Integer whileVisit(JmmNode whileStmt, Integer dummy){
        JmmNode conditionStmt = whileStmt.getChildren().get(0);
        JmmNode whileBlock =  whileStmt.getChildren().get(1);

        code.append("Loop:\n");

        visit(conditionStmt); // TODO INVERTER CONDIÇÃO
        code.append("goto Endloop;\n");

        var whileStmts = whileBlock.getChildren();
        for (var whileBlockStmt: whileStmts)
            visit(whileBlockStmt);

        code.append("goto Loop;\n").append("EndLoop:");

            
        return 0;
    }

     private Integer assignmentVisit(JmmNode assignmentStmt, Integer dummy){
        JmmNode leftStmt = assignmentStmt.getChildren().get(0); 
        JmmNode rightStmt = assignmentStmt.getChildren().get(1); 

        return 0;
     }


    private Integer memberCallVisit(JmmNode memberCall, Integer dummy){

        JmmNode identifier = memberCall.getChildren().get(0);
        JmmNode call = memberCall.getChildren().get(1);

        if (identifier.getKind().equals("IDENTIFIER")) {
            code.append("invokestatic(");
            code.append(", \"");
            visit(memberCall.getJmmChild(1));
            code.append("\"");
            visit(memberCall.getJmmChild(2));
            code.append(").").append(OllirUtils.getOllirOperator(memberCall)); // opearator i think


        } else if (identifier.getKind().equals("NEW")) {

            String var = newAuxiliarVar(symbolTable.getReturnType(memberCall.get("name")).getName());
            code.append("invokespecial(").append("t" + var).append(", \"<init>\").V;\n");
            visit(identifier);
        } else {
            String varName = this.getVarName(identifier);
            String callName = !call.getKind().equals("LENGTH") ? call.get("name") : "length";
            String type = !varName.equalsIgnoreCase("this") ? varName + "." : "";

            if (callName.equals("length")) {
                code.append("t").append(varCounter++).append(".i32 :=.i32 arraylength(").append(type).append("array.i32).i32");

            } else {
                code.append("invokevirtual(");
                code.append(", \"");
                visit(memberCall.getJmmChild(1));
                code.append("\"");
                visit(memberCall.getJmmChild(2));
                code.append(").").append(OllirUtils.getOllirOperator(memberCall)); // opearator i think

            }
        }
        //visit(memberCall.getJmmChild(0)); //TODO MUDAR PARA + COMPLEXAS
        //code.append(", \"");
        //visit(memberCall.getJmmChild(1));
        //code.append("\"");
        //visit(memberCall.getJmmChild(2));
        //code.append(").").append(OllirUtils.getOllirOperator(memberCall)); // opearator i think

        return 0;
    }

    private String getVarName(JmmNode identifier) {
        if (identifier.getKind().equals("THIS"))
            return "this";
        if (identifier.getKind().equals("NEW"))
            return identifier.getChildren().get(0).get("name");
        return identifier.get("name");
    }


    private Integer argumentsVisit(JmmNode arguments, Integer dummy){
        for (var child: arguments.getChildren()){
            code.append(", ");
            visit(child);
        }

        return 0;
    }

    private Integer returnVisit(JmmNode returnNode, Integer dummy){
        //here you can decide if the temporary variable is necessary or not
        //I am considering that I always need a new temp
       // String temp = createTemp();
       // finalCode = temp + "=" + finalCode;
       // Code thisCode = new Code();
       // thisCode.code = temp;
       // thisCode.prefix = prefixCode;
       // return thisCode;

        JmmNode expression = returnNode.getChildren().get(0);

        Optional<JmmNode> methodNode = returnNode.getAncestor("Method_Decl");
        if (!expression.getKind().equals("Identifier")) {
            visit(expression);
            code.append("ret").append(OllirUtils.getOllirType(symbolTable.getReturnType(methodNode.get().get("name"))));
            code.append(" t").append(varCounter).append(OllirUtils.getOllirType(symbolTable.getReturnType(methodNode.get().get("name"))));
        }
        else {
            code.append("ret").append(OllirUtils.getOllirType(symbolTable.getReturnType(methodNode.get().get("name"))));
            visit(expression);
        }
        code.append(";\n");
        return 0;
    }


    private Integer idVisit(JmmNode id, Integer dummy){
        code.append(id.get("name"));

        return 0;
    }

    private Integer intVisit(JmmNode value, Integer dummy){
        code.append(value.get("value")).append(".i32");

        return 0;
    }

    private Integer boolVisit(JmmNode value, Integer dummy){
        code.append(value.get("value")).append(".bool");

        return 0;
    }

    private Integer newVisit(JmmNode newNode, Integer dummy){String str = "";
        JmmNode child = newNode.getChildren().get(0);
        if (child.getKind().equals("ARRAY")) {
            JmmNode grandChild = child.getChildren().get(0);
            code.append("new(array, ");
            visit(grandChild);
            code.append(").array.i32");
           // if (grandchild.getKind().equals("OBJECT_METHOD")) {
           //     str += grandchildVisit + "\n";
           //     grandchildVisit = grandchildVisit.substring(0, grandchildVisit.indexOf(" "));
           // }
        } else code.append("new(").append(child.get("name")).append(").").append(child.get("name"));

        return 0;
    }

    private String parseOp(String op){
        if (op.equals("AND") || op.equals("LESS") || op.equals("NOT")) return "bool";
        return "int";
    }

    private Integer defaultVisit(JmmNode defaultNode, Integer dummy) {
        StringBuilder visitStr = new StringBuilder();
        for (JmmNode child : defaultNode.getChildren()) {
            visit(child, dummy);
        }
        return 0;
    }

    private Integer operationVisit(JmmNode operation, Integer dummy) {
        String str = "";
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
            code.append(OllirUtils.getOllirOperator(operation));
            visit(lhs);
            code.append(";\n");
        }
        else {
            visit(lhs);
            code.append(" ").append(OllirUtils.getOllirOperator(operation));
            visit(rhs);
            code.append(";\n");
        }

        return 0;
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


