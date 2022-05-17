package pt.up.fe.comp.ollir;
import pt.up.fe.comp.ast.ASTNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, Integer>{

    static int varCounter;
    private final StringBuilder code;
    private final SymbolTable symbolTable;

    public OllirGenerator(SymbolTable symbolTable){
            this.varCounter = 0;
            this.code = new StringBuilder();
            this.symbolTable = symbolTable;

            addVisit(ASTNode.PROGRAM, this::programVisit);
            addVisit(ASTNode.CLASS_DECL, this::classDeclVisit);
            addVisit(ASTNode.VAR_DECL, this::varDeclVisit);
            addVisit(ASTNode.METHOD_DECL, this::methodDeclVisit);
            addVisit(ASTNode.EXPR_STMT, this::exprStmtVisit);
            addVisit(ASTNode.MEMBER_CALL, this::memberCallVisit);
            addVisit(ASTNode.ARGUMENTS, this::argumentsVisit);
            addVisit(ASTNode.ID, this::idVisit);
            addVisit(ASTNode.IF_STMT, this::ifStmtVisit);
            addVisit(ASTNode.WHILE_STMT, this::whileStmtVisit);
            addVisit(ASTNode.ASSIGNMENT, this::assignmentVisit);

        //     addVisit("CLASS_DECLARATION", this::dealWithClass);
        // addVisit("VAR_DECLARATION", this::dealWithVar);
        // addVisit("MAIN", this::dealWithMain);
        // addVisit("METHOD_DECLARATION", this::dealWithMethodDeclaration);
        // addVisit("OBJECT_METHOD", this::dealWithObjectMethod);
        // addVisit("ASSIGNMENT", this::dealWithAssignment);
        // addVisit("RETURN", this::dealWithReturn);
        // addVisit("IDENTIFIER", this::dealWithIdentifier);
        addVisit("INT", this::dealWithInt);
        // addVisit("TRUE", this::dealWithBoolean);
        // addVisit("FALSE", this::dealWithBoolean);
        addVisit("NEW", this::dealWithNew);
        // addVisit("OPERATION", this::dealWithOperation);
        // addVisit("LESS", this::dealWithOperation);
        // addVisit("AND", this::dealWithOperation);
        // addVisit("EXCLAMATION", this::dealWithOperation);
        // addVisit("IF", this::dealWithIf);
        // addVisit("ELSE", this::dealWithElse);
        // addVisit("WHILE", this::dealWithWhile);
        // addVisit("ARRAY_ACCESS", this::dealWithArrayAccess);
    }

    public String getCode(){
        return code.toString();
    }

    private Integer programVisit(JmmNode program, Integer dummy){
        for (var importString : symbolTable.getImports())
            code.append("import ").append(importString).append(";\n");
        

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

        for (var child: classDecl.getChildren())
            visit(child);
        

        code.append("}\n");
        
        return 0;
    }

    private Integer methodDeclVisit(JmmNode methodDecl, Integer dummy){
        var methodSignature = methodDecl.getJmmChild(1).get("name");
        var isStatic = Boolean.valueOf(methodDecl.get("isStatic"));
        var isMain = Boolean.valueOf(methodDecl.getKind().equals("MainMethod"));

        code.append(" .method public ");
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

        visitFields();
        classConstructor();

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

    private void visitField(){
        for(Symbol field : symbolTable.getFields()){
            sb.append(prefix()).append(".field private ");
            sb.append(field.getName());
            sb.append(MyOllirUtils.getOllirType(field.getType()));
            sb.append(";\n");
        }
    }

    private void classConstructor(){
        sb.append(prefix()).append(".construct ");
        sb.append(symbolTable.getClassName());
        sb.append("().V {\n");
        indent++;
        sb.append(prefix()).append("invokespecial(this, \"<init>\").V;\n\t}\n");
        indent--;
    }

    private Integer exprStmtVisit(JmmNode exprStmt, Integer dummy){

        visit(exprStmt.getJmmChild(0));
        code.append(";\n");
        
        
        return 0;
    }

    private Integer ifStmtVisit(JmmNode ifStmt, Integer dummy){
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

    private Integer whileStmtVisit(JmmNode whileStmt, Integer dummy){
        JmmNode conditionStmt = whileStmt.getChildren().get(0);
        JmmNode whileBlock =  whileStmt.getChildren().get(1);

        code.append("Loop:\n");

        visit(conditionStmt); // TODO INVERTER CONDIÇÃO + IF
        code.append("goto Endloop;\n");

        var whileStmts = whileBlock.getChildren();
        for (var whileBlockStmt: whileStmts)
            visit(whileBlockStmt);

        code.append("goto Loop;\n").append("EndLoop:");

            
        return 0;
    }

    //  private Integer arrayAssignmentVisit(JmmNode assignmentStmt, Integer dummy){
    //     String name = arrayIdentifier.get("name"); // Name of the array
    //     Type type = getIdentifierType(methodName,name); // Type of the array
    //     String assignmentType = MyOllirUtils.ollirType(type).split("\\.")[2];

    //     String leftSide = ollirArrayAccess(methodName,arrayIdentifier,indexNode);
    //     String rightSide;
    //     String kind = rightNode.getKind();
    //     if(kind.equals("Dot")){
    //         rightSide = ollirDotMethod(methodName, rightNode, "."+ assignmentType);
    //     }
    //     else {
    //         rightSide = ollirExpression(methodName, rightNode);
    //     }

    //     sb.append(prefix()).append(leftSide).append(" :=.").append(assignmentType).append(" ").append(rightSide).append(";");
    //  }

    //   private Integer identifierAssignmentVisit(JmmNode assignmentStmt, Integer dummy){
    //     JmmNode left = statement.getChildren().get(0); 
    //     JmmNode right = statement.getChildren().get(1); 

        
    //     if(left.getKind().equals("ArrayAssignment")){ // TODO CHANGE KIND??
    //         JmmNode arrayNode = left.getChildren().get(0); 
    //         JmmNode indexNode = left.getChildren().get(1).getChildren().get(0); 
    //         ollirArrayAssignment(methodName, arrayNode, indexNode, right);
    //         return;
    //     }

    //     // Identifier Assignment
    //     String name = left.get("name"); // Identifier Name
    //     ollirIdentifierAssignment(methodName, name, right);

    //     return 0;
    //  }

      private String dealWithAssignment(JmmNode assignment, List<Report> reports) {
        StringBuilder methodStr = new StringBuilder();
        JmmNode identifier = assignment.getChildren().get(0);
        JmmNode assignment = assignment.getChildren().get(1);
        Optional<JmmNode> ancestor = getAncestor(assignment, "MAIN", "METHOD_DECLARATION");
        Symbol var;
        boolean arrayAccess = false;

        if (identifier.getKind().equals("ARRAY_ACCESS")) {
            arrayAccess = true;
            var = symbolTable.getVariable(identifier.getChildren().get(0).get("name"), ancestor.get().get("name"));
            //var.setType(new Type("int", false));
        } else
            var = symbolTable.getVariable(identifier.get("name"), ancestor.get().get("name"));

        if (isOp(assignment)) {
            String operation = visit(assignment);
            String[] lines = operation.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (i == lines.length - 1) {
                    methodStr.append(varAssign(identifier, var, arrayAccess));

                    methodStr.append(lines[i]).append("\n");
                } else
                    methodStr.append("\t\t").append(lines[i]).append("\n");
            }
        } else if (assignment.getKind().equals("NEW")) {
            String assignmentString = visit(assignment);
            if (assignmentString.contains("\n")) {
                methodStr.append(assignmentString, 0, assignmentString.indexOf("\n"));
                assignmentString = assignmentString.substring(assignmentString.indexOf("\n") + 1);
            }
            methodStr.append(varAssign(identifier, var, arrayAccess));
            methodStr.append(assignmentString).append(";\n");
            if (!assignment.getChildren().get(0).getKind().equals("ARRAY"))
                methodStr.append("\t\tinvokespecial(").append(escapeVarName(var.getName())).append(".").append(parseType(var.getType().isArray() ? var.getType().getName() + " array" : var.getType().getName()))
                        .append(", \"<init>\").V;\n");
        } else {
            String assignString = visit(assignment);
            if (assignment.getKind().equals("ARRAY_ACCESS")) {
                String before;
                if (assignString.contains("\n")) {
                    before = assignString.substring(0, assignString.lastIndexOf("\n"));
                    if (assignString.lastIndexOf("\n") < assignString.lastIndexOf(":=.")) {
                        before += assignString.substring(assignString.lastIndexOf("\n"));
                        assignString = assignString.substring(assignString.lastIndexOf("\n") + 1, assignString.lastIndexOf(" :=."));
                    } else
                        assignString = assignString.substring(assignString.lastIndexOf("\n") + 1, assignString.lastIndexOf(";"));
                } else {
                    before = assignString;
                    assignString = assignString.substring(0, assignString.indexOf(' '));
                }
                methodStr.append(before).append("\n");
            } else if (assignment.getKind().equals("OBJECT_METHOD")) {
                if (assignString.contains("\n")) {
                    methodStr.append(assignString, 0, assignString.lastIndexOf("\n"));
                    assignString = assignString.substring(assignString.lastIndexOf("\n") + 1);
                } else if (assignString.contains(":=.")) {
                    methodStr.append(assignString);
                    assignString = assignString.substring(0, assignString.indexOf(" ")) + ";";
                }

            }
            if (assignString.contains("\n")) {
                methodStr.append(assignString.substring(0, assignString.lastIndexOf("\n") + 1));
                assignString = assignString.substring(assignString.lastIndexOf("\n") + 1);
            }

            methodStr.append(varAssign(identifier, var, arrayAccess));
            methodStr.append(assignString);

            if (!assignment.getKind().equals("OBJECT_METHOD"))
                methodStr.append(";");
            methodStr.append("\n");
        }
        /*
        if(identifier.getKind().equals("ARRAY_ACCESS"))
            var.setType(new Type("int", true));*/


        return methodStr.toString();
    }


    private Integer memberCallVisit(JmmNode memberCall, Integer dummy){
        //TODO VER TIPO EXPRESSÃO, CLASSE STATIC OU VIRTUAL
        // code.append("invokestatic(");
         
         //TODO ExprToOllir -> returns codeToPlaceBefore, valueToUse

 
        
        visit(memberCall.getJmmChild(0)); //TODO MUDAR PARA + COMPLEXAS
        code.append(", \"");

        visit(memberCall.getJmmChild(1));
        code.append("\"");

        visit(memberCall.getJmmChild(2));
        code.append(").").append(OllirUtils.getOllirOperator(memberCall)); // opearator i think

        return 0;
    }

    private String dealWithObjectMethod(JmmNode jmmNode, List<Report> reports) {
        StringBuilder methodStr = new StringBuilder();
        JmmNode identifier = jmmNode.getChildren().get(0);
        JmmNode call = jmmNode.getChildren().get(1);

        boolean grandchildren = false;
        List<String> params = new ArrayList<>();
        for (JmmNode grandchild : call.getChildren()) {
            grandchildren = true;
            String visitString;
            String param;
            visitString = visit(grandchild);
            if (isOp(grandchild)) {
                if (visitString.contains("\n")) {
                    if (visitString.lastIndexOf("\n") > visitString.lastIndexOf(":=.")) {
                        String substring = visitString.substring(visitString.lastIndexOf("."), visitString.length() - 1);
                        visitString = "\t\t" + visitString.substring(0, visitString.lastIndexOf("\n") + 1) + "\t\tt" + tempVar + substring + " :=" + substring + " " + visitString.substring(visitString.lastIndexOf("\n") + 1) + "\n";
                        param = "t" + tempVar + substring;
                        tempVar++;
                        while (skipTemps.contains(tempVar))
                            tempVar++;
                    } else
                        param = visitString.substring(visitString.lastIndexOf("\n"), visitString.lastIndexOf(":=."));
                } else {
                    String substring = visitString.substring(visitString.lastIndexOf("."), visitString.length() - 1);
                    visitString = "\t\tt" + tempVar + substring + " :=" + substring + " " + visitString + "\n";
                    param = "t" + tempVar + substring;
                    tempVar++;
                    while (skipTemps.contains(tempVar))
                        tempVar++;
                }

            } else if (grandchild.getKind().equals("OBJECT_METHOD")) {
                String substring = visitString.substring(visitString.contains(".array") ? visitString.indexOf(".array") : visitString.lastIndexOf("."), visitString.length() - 1);
                visitString = "\t\tt" + tempVar + substring + " :=" + substring + " " + visitString + "\n";
                param = "t" + tempVar + substring;
                tempVar++;
                while (skipTemps.contains(tempVar))
                    tempVar++;
            } else if (grandchild.getKind().equals("ARRAY_ACCESS")) {
                if (!visitString.contains("\n"))
                    param = visitString.substring(0, visitString.indexOf(" "));
                else {
                    String sub = visitString.substring(visitString.lastIndexOf("\n") + 1);
                    visitString = visitString.substring(0, visitString.lastIndexOf("\n") + 1);
                    param = sub.contains(":=.") ? sub.substring(0, sub.indexOf(" ")) : sub.substring(0, sub.length() - 1);
                    if (param.contains("[")) {
                        visitString += "\t\tt" + tempVar + ".i32 :=.i32 " + param + ";\n";
                        param = "t" + tempVar + ".i32";
                        tempVar++;
                        while (skipTemps.contains(tempVar))
                            tempVar++;
                    }

                }
            } else {
                if (!visitString.contains(":=.")) {
                    param = visitString;
                    visitString = "";
                } else {
                    param = visitString.substring(0, visitString.indexOf(":=."));
                    visitString = visitString.substring(0, visitString.indexOf(";") + 1) + "\n";
                }

            }

            params.add(param);
            methodStr.append(visitString);
        }

        if (identifier.getKind().equals("IDENTIFIER") && symbolTable.checkVariableInImports(identifier.get("name"))) {
            methodStr.append("\t\tinvokestatic(").append(identifier.get("name")).append(", \"").append(call.get("name")).append("\"");
            methodStr.append(this.buildMethodType(jmmNode, "", params, grandchildren));

        } else if (identifier.getKind().equals("NEW")) {
            String newVisit = visit(identifier);
            String subString = newVisit.substring(newVisit.lastIndexOf("."));
            methodStr.append("\t\tt").append(tempVar).append(subString).append(" :=").append(subString).append(" ").append(newVisit).append(";\n");
            methodStr.append("\t\tinvokespecial(").append("t" + tempVar + subString).append(", \"<init>\").V;\n");
            methodStr.append(classIsImported(identifier.getChildren().get(0))).append("t").append(tempVar).append(subString).append(", \"").append(call.get("name")).append("\"");
            methodStr.append(this.buildMethodType(jmmNode, call.get("name"), params, grandchildren));
            tempVar++;
            while (skipTemps.contains(tempVar))
                tempVar++;
        } else {
            String varName = this.getVarName(identifier);
            String callName = !call.getKind().equals("LENGTH") ? call.get("name") : "length";
            String type = !varName.equalsIgnoreCase("this") ? varName + "." : "";

            if (callName.equals("length")) {
                methodStr.append("t").append(tempVar).append(".i32 :=.i32 arraylength(").append(type).append("array.i32).i32");
                tempVar++;
                while (skipTemps.contains(tempVar))
                    tempVar++;
            } else {
                methodStr.append("invokevirtual(").append(type).append(getVariableType(varName, jmmNode)).append(", \"").append(callName).append("\"");
                methodStr.append(this.buildMethodType(jmmNode, callName, params, grandchildren));
            }
        }
        methodStr.append(";");
        return methodStr.toString();
    }

    private String dealWithReturn(JmmNode jmmNode, List<Report> reports) {
        StringBuilder methodStr = new StringBuilder();
        JmmNode identifier = jmmNode.getChildren().get(0);
        String child = visit(identifier);
        if (identifier.getKind().equals("OBJECT_METHOD") || isOp(identifier)) {
            String substring = child.substring(child.lastIndexOf("."), child.length() - 1);
            if (child.contains("\n")) {
                if (child.lastIndexOf("\n") > child.lastIndexOf(":=.")) {
                    methodStr.append("\t\t").append(child.substring(0, child.lastIndexOf("\n") + 1)).append("\t\tt").append(tempVar).append(substring).append(" :=").append(substring).append(" ").append(child.substring(child.lastIndexOf("\n") + 1)).append("\n");
                    child = "\tt" + tempVar + substring;
                    tempVar++;
                    while (skipTemps.contains(tempVar))
                        tempVar++;
                } else
                    child = child.substring(child.lastIndexOf("\n"), child.lastIndexOf(":=."));
            } else {
                methodStr.append("\t\tt").append(tempVar).append(substring).append(" :=").append(substring).append(" ").append(child).append("\n");
                child = "t" + tempVar + substring;
                tempVar++;
                while (skipTemps.contains(tempVar))
                    tempVar++;
            }
        }
        if (child.contains("\n")) {
            methodStr.append(child.substring(0, child.lastIndexOf("\n")));
            child = child.substring(child.lastIndexOf("\n") + 1);
        }
        methodStr.append("\t\t" + "ret");
        if (!child.contains("$"))
            methodStr.append(child.substring(child.indexOf("."))).append(" ").append(child).append(";\n");
        else {
            String subChild = child.substring(child.indexOf(".") + 1);
            methodStr.append(subChild.substring(subChild.indexOf("."))).append(" ").append(child).append(";\n");
        }

        return methodStr.toString();
    }

    private String visitExpression(JmmNode expression, Integer dummy){


        // Math and Boolean Expressions
        // if(Utils.isMathExpression(expression.getKind()))
        //     return ollirMathBooleanExpression(methodName, node, ".i32");
        // if(Utils.isBooleanExpression(expression))
        //     return ollirMathBooleanExpression(methodName, node, ".bool");

        switch(expression.getKind()){
            case "Identifier":
                return ollirFromIdentifierNode(methodName, node);
            case "True":
                return "1.bool";
            case "False":
                return "0.bool";
            case "Number":
                return expression.get("value") + ".i32";
            case "This":
                return "$0.this." + symbolTable.getClassName();
            // case "ArrayAccess":
            //     JmmNode arrayNode = expression.getChildren().get(0); 
            //     JmmNode indexNode = expression.getChildren().get(1); 
            //     return ollirArrayAccess(methodName,arrayNode,indexNode);
            // case "Dot":
            //     return ollirDotMethod(methodName, node, null);
            // case "NewObject":
            //     return ollirNewObject(node);
            // case "NewIntArray":
            //     return ollirNewIntArray(methodName, node);
            default:
                visit(expression);
                return "INVALID EXPRESSION";
        }
    }

    private String visitOperation(JmmNode operation, List<Report> reports) {
        String str = "";
        JmmNode lhs = operation.getChildren().get(0);
        JmmNode rhs = operation.getKind().equals("EXCLAMATION") ? lhs : operation.getChildren().get(1);
        String lhsString = visit(lhs);
        String rhsString = operation.getKind().equals("EXCLAMATION") ? lhsString : visit(rhs);
        List<String> lhsResult = checkForNested(lhs, lhsString);
        List<String> rhsResult = operation.getKind().equals("EXCLAMATION") ? lhsResult : checkForNested(rhs, rhsString);
        str += !lhsResult.get(0).contains("\n") ? lhsResult.get(0) + "\n" : lhsResult.get(0);
        lhsString = lhsResult.get(1);
        str += operation.getKind().equals("EXCLAMATION") ? "" : !rhsResult.get(0).contains("\n") ? rhsResult.get(0) + "\n" : rhsResult.get(0);
        rhsString = rhsResult.get(1);

        str += lhsString + " " + getOperator(jmmNode);
        if (operation.getKind().equals("OPERATION"))
            str += ".i32 ";
        else
            str += ".bool ";
        str += rhsString + ";";
        return str;
    }

     private String visitIdentifier(JmmNode identifier, Integer dummy) {
        Optional<JmmNode> ancestor = getAncestor(identifier, "MAIN", "METHOD_DECLARATION");
        Symbol var = symbolTable.getVariable(identifier.get("name"), ancestor.get().get("name"));
        String varName = escapeVarName(var.getName());
        String varType = parseType(var.getType().isArray() ? var.getType().getName() + " array" : var.getType().getName());
        String before = "";
        if (symbolTable.isGlobal(var.getName())) {
            before += "\t\tt" + tempVar + "." + varType + " :=." + varType + " getfield(this, " + varName + "." + varType + ")." + varType + ";\n";
            varName = "t" + tempVar;
            tempVar++;
            while (skipTemps.contains(tempVar))
                tempVar++;
        } else if (symbolTable.isParam(ancestor.get().get("name"), var.getName())) {
            before += "$" + (symbolTable.getMethod(ancestor.get().get("name")).getParamNumber(var.getName()) + 1) + ".";
        }
        return before + varName + "." + varType;
    }


    private String visitInt(JmmNode intNode, Integer dummy) {
        return intNode.get("value") + ".i32";
    }

    private String visitBoolean(JmmNode boolNode, Integer dummy) {
        return parseType(boolNode.getKind());
    }

    private String visitNew(JmmNode jmmNode, Integer dummy) {
        String str = "";
        JmmNode child = jmmNode.getChildren().get(0);
        if (child.getKind().equals("ARRAY")) {
            JmmNode grandchild = child.getChildren().get(0);
            String grandchildVisit = visit(grandchild);
            if (grandchild.getKind().equals("OBJECT_METHOD")) {
                str += grandchildVisit + "\n";
                grandchildVisit = grandchildVisit.substring(0, grandchildVisit.indexOf(" "));
            }
            str += "new(array, " + grandchildVisit + ").array.i32";
        } else if (child.getKind().equals("IDENTIFIER")) {
            str += "new(" + child.get("name") + ")." + child.get("name");
        } else {
            str += "new(" + child.get("name") + ")." + child.get("name");
        }
        return str;
    }

    private String classIsImported(JmmNode object) {
        if (symbolTable.checkVariableInImports(object.get("name")) || (symbolTable.getSuper() != null && symbolTable.getSuper().equals(object.get("name"))))
            return "\t\tinvokestatic(";
        return "\t\tinvokevirtual(";
    }


     private String getVarName(JmmNode identifier) {
        if (identifier.getKind().equals("THIS"))
            return "this";
        if (identifier.getKind().equals("NEW"))
            return identifier.getChildren().get(0).get("name");
        return identifier.get("name");
    }

    private String getVariableType(String varName, JmmNode currentNode) {
        if (varName.equalsIgnoreCase("this"))
            return varName;
        if (varName.equals(symbolTable.getClassName()))
            return varName;
        Optional<JmmNode> ancestor = getAncestor(currentNode, "MAIN", "METHOD_DECLARATION");
        return symbolTable.getVariable(varName, ancestor.get().get("name")).getType().isArray() ?
                symbolTable.getVariable(varName, ancestor.get().get("name")).getType().getName() + " array"
                : symbolTable.getVariable(varName, ancestor.get().get("name")).getType().getName();

    }

    private String varAssign(JmmNode node, Symbol var, boolean arrayAccess) {

        if (!arrayAccess)
            return "\t\t" + escapeVarName(var.getName()) + "." + parseType(var.getType().isArray() ? var.getType().getName() + " array" : var.getType().getName()) + " :=." +
                    parseType(var.getType().isArray() ? var.getType().getName() + " array" : var.getType().getName()) + " ";

        String access = visit(node);
        String before = "";
        String ret = "";
        if (access.contains("\n")) {
            before = access.substring(0, access.lastIndexOf("\n"));
            access = access.substring(access.lastIndexOf("\n"));
        }
        if (access.contains(":=."))
            access = access.substring(access.lastIndexOf(" "), access.lastIndexOf(";"));
        if (access.contains(";"))
            access = access.substring(0, access.lastIndexOf(";"));
        ret += before + "\t\t" + access + " :=.i32 ";
        return ret;
    }

    private String escapeVarName(String varName) {
        if (varName.charAt(0) == '$')
            return "d_" + varName.substring(1);
        else if (varName.charAt(0) == '_')
            return "u_" + varName.substring(1);
        else if (varName.startsWith("ret"))
            return "r_" + varName.substring(3);
        else if (varName.startsWith("array"))
            return "arr_" + varName.substring(5);
        else if (varName.startsWith("field"))
            return "fld_" + varName.substring(5);
        return varName;
    }

    private String visitVar(JmmNode jmmNode, Integer dummy) {
        Optional<JmmNode> ancestor = getAncestor(jmmNode, "METHOD_DECLARATION", "CLASS_DECLARATION");
        if (ancestor.get().getKind().equals("CLASS_DECLARATION") && symbolTable.isGlobal(jmmNode.get("name"))) {
            return ".field private " + escapeVarName(jmmNode.get("name")) +
                    getOllirType(symbolTable.getField(jmmNode.get("name")).getType().isArray() ? symbolTable.getField(jmmNode.get("name")).getType().getName() + " array"
                            : (symbolTable.getField(jmmNode.get("name")).getType().getName())) + ";\n";
        }

        return "";
    }

    private List<String> buildCondition(JmmNode condition, String condString, boolean isWhile) {
        String returnString = "";
        List<String> result = new ArrayList<>();
        if (condition.getKind().equals("OBJECT_METHOD")) {
            if (isWhile) {
                returnString += "\t\tt" + tempVar + ".bool :=.bool " + condString + "\n";
                condString = "t" + tempVar + ".bool ==.bool t" + tempVar + ".bool;";
            } else {
                if (!condString.contains("\n"))
                    returnString += "\t\tt" + tempVar + ".bool :=.bool " + condString + "\n";
                else {
                    returnString += condString.substring(0, condString.lastIndexOf("\n") + 1) + "\n\t\tt" + tempVar + ".bool :=.bool " + condString.substring(condString.lastIndexOf("\n")) + "\n";
                }
            }

            condString = "t" + tempVar + ".bool !.bool t" + tempVar + ".bool;";
            tempVar++;
            while (skipTemps.contains(tempVar))
                tempVar++;
        } else if (condition.getKind().equals("IDENTIFIER") || condition.getKind().equals("TRUE") || condition.getKind().equals("FALSE")) {
            if (isWhile)
                condString += " ==.bool " + condString;
            else
                condString += " !.bool " + condString;
        } else if (condition.getKind().equals("ARRAY_ACCESS")) {
            returnString += condString + "\n";
            condString = condString.substring(condString.lastIndexOf("\n") + 1, condString.lastIndexOf(":=."));
        }
        if (condString.contains("\n")) {
            returnString += condString.substring(0, condString.lastIndexOf("\n"));
            returnString += "t" + tempVar + ".bool :=.bool " + condString.substring(condString.lastIndexOf("\n") + 1) + "\n";
            condString = isWhile ? "t" + tempVar + ".bool ==.bool t" + tempVar + ".bool;" : "t" + tempVar + ".bool !.bool t" + tempVar + ".bool;";
            tempVar++;
            while (skipTemps.contains(tempVar))
                tempVar++;
        } else if (containsOps(condString)) {
            returnString += "t" + tempVar + ".bool :=.bool " + condString + "\n";
            condString = isWhile ? "t" + tempVar + ".bool ==.bool t" + tempVar + ".bool;" : "t" + tempVar + ".bool !.bool t" + tempVar + ".bool;";
            tempVar++;
            while (skipTemps.contains(tempVar))
                tempVar++;
        }
        if (condString.contains(";"))
            condString = condString.substring(0, condString.length() - 1);
        result.add(returnString);
        result.add(condString);
        return result;
    }

    
    private Optional<JmmNode> getAncestor(JmmNode jmmNode, String globalScope, String specificScope) {
        return jmmNode.getAncestor(globalScope).isPresent() ? jmmNode.getAncestor(globalScope) : jmmNode.getAncestor(specificScope);
    }

    private String newVar(String type, String methodName, JmmNode node){
        String value;
        if(node.getKind().equals("Dot"))
            value = ollirDotMethod(methodName, node, type);
        else value = ollirExpression(methodName, node);
        varCounter++;
        return "t" + varCounter + type + " :=" + type +" " + value + ";\n";
    }

     //compound
     //Var decl
};