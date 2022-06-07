package pt.up.fe.comp.ollir;
import pt.up.fe.comp.ArrayAccess;
import pt.up.fe.comp.IDENTIFIER;
import pt.up.fe.comp.INTEGERLITERAL;
import pt.up.fe.comp.MethodBody;
import pt.up.fe.comp.ast.ASTNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.lang.reflect.Method;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, String>{

    static int varCounter;
    static int markerCounter;
    private final StringBuilder code;
    private final SymbolTable symbolTable;

    public OllirGenerator(SymbolTable symbolTable){
            this.varCounter = 0;
            this.code = new StringBuilder();
            this.symbolTable = symbolTable;

            addVisit(ASTNode.START, this::programVisit);
            addVisit(ASTNode.IMPORT_DECLARATION, this::importDeclVisit);
            addVisit(ASTNode.VAR_DECLARATION, this::varDeclVisit);
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
            addVisit(ASTNode.ELSE, this::elseVisit);
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
        //todo construct stmts
        for (var child: classDecl.getChildren())
            if (!child.getKind().equals("Identifier")) classString.append(visit(child));


        classString.append("}\n");
        
        return classString.toString();
    }

    private String varDeclVisit(JmmNode varDecl, Integer dummy){
        StringBuilder varStr = new StringBuilder();
        JmmNode parent = varDecl.getJmmParent();
        JmmNode type = varDecl.getJmmChild(0);
        JmmNode identifier = varDecl.getJmmChild(1);

        if (parent.getKind().equals("ClassDeclaration")) {
            varStr.append(".field private ");
            varStr.append(visit(identifier)).append(".");
            varStr.append(OllirUtils.getOllirType(getFieldType(identifier.get("name"))));
        }

        return varStr.toString() + ";\n";
    }

    private String methodDeclVisit(JmmNode methodDecl, Integer dummy){
        StringBuilder methodString = new StringBuilder();
        var methodType = methodDecl.getJmmChild(0);

        boolean isMain = methodType.getKind().equals("MainMethodHeader");

        methodString.append(" .method public ");
        if (isMain)
            methodString.append("static main(");

        else methodString.append(methodDecl.getJmmChild(0).getJmmChild(1).get("name")); //TODO CHECKAR COMMON METHOD


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
        StringBuilder ifStr = new StringBuilder();
        JmmNode conditionStmt = ifStmt.getJmmChild(0);
        JmmNode bodyBlock = ifStmt.getJmmChild(1);
        JmmNode elseBlock = ifStmt.getJmmChild(2);

        String condStr = visit(conditionStmt);

        List<String> editedCond = buildCondition(conditionStmt, condStr, false);
        ifStr.append(editedCond.get(0));
        condStr = editedCond.get(1);

        ifStr.append("\t\tif (").append(condStr).append(") goto Else").append(++markerCounter).append(";\n");

        ifStr.append("\t").append(visit(bodyBlock));
        ifStr.append("\t").append(visit(elseBlock));

        ifStr.append("\t\tgoto EndIf").append(markerCounter).append(";\n");

        return ifStr.toString();
    }
    private String elseVisit(JmmNode jmmNode, Integer dummy) {
        //int localLabel = ifCount;
        StringBuilder elseStr = new StringBuilder();
        elseStr.append("\t\tElse" + markerCounter + ":\n");

        for (JmmNode child : jmmNode.getChildren()) {
            elseStr.append("\t").append(visit(child));
        }
        elseStr.append("\t\tEndIf").append(markerCounter).append(":\n");
        //ifCount--;
        return elseStr.toString();
    }

    private String whileVisit(JmmNode whileStmt, Integer dummy){
        StringBuilder whileStr = new StringBuilder();
        JmmNode conditionStmt = whileStmt.getChildren().get(0);
        JmmNode bodyStmts = whileStmt.getChildren().get(1);
        String condStr = visit(conditionStmt);

        List<String> editedCond= this.buildCondition(conditionStmt, condStr, true);
        condStr = editedCond.get(1);

        whileStr.append("\t\tLoop" + markerCounter + ":\n");
        whileStr.append(editedCond.get(0));
        whileStr.append("\t\t\tif (").append(condStr).append(") goto Body").append(markerCounter);
        whileStr.append(";\n\t\t\tgoto EndLoop").append(markerCounter).append(";\n\t\tBody").append(markerCounter).append(":\n");

        visit(bodyStmts);

        whileStr.append("\t\tgoto Loop;\n").append(markerCounter).append("\n\t\tEndLoop").append(markerCounter).append(":\n");

            
        return whileStr.toString();
    }

     private String assignmentVisit(JmmNode assignmentStmt, Integer dummy){
         StringBuilder methodStr = new StringBuilder();
         JmmNode identifier = assignmentStmt.getJmmChild(0);
         JmmNode assignment = assignmentStmt.getJmmChild(1);
         JmmNode parent = assignmentStmt.getJmmParent();

         String idStr = visit(identifier);
         String assignmentStr = visit(assignment);
         int currentCount = varCounter;

         List<Symbol> fields = symbolTable.getFields();
         Symbol var = null;
         boolean isField = false;
         for (Symbol symbol : fields) {
             if (symbol.getName().equals(identifier.getJmmChild(0).get("name"))) {
                 isField = true;
                 break;
             }
         }
        //TODO IF ARGS APPEND $<ARG_NUMBER>


        if (assignmentStr.contains("\n")){
            methodStr.append(assignmentStr);
            methodStr.append(idStr).append("t").append(varCounter).append(";\n");
        }
        else methodStr.append(idStr).append(assignmentStr);

         if (isField) methodStr.append(").V\n");
         else methodStr.append(";\n");

         return methodStr.toString();
     }

    private String varAccess(JmmNode varAccess, Integer dummy) {
        StringBuilder varStr = new StringBuilder();

        List<Symbol> fields = symbolTable.getFields();
        List<String> methods = symbolTable.getMethods();

        JmmNode method = varAccess.getJmmParent().getJmmParent();

        JmmNode id = varAccess.getJmmChild(0);
        JmmNode array = varAccess.getJmmChild(1);

        Symbol var = null;
        boolean isField = false;
        for (Symbol symbol : fields) {
            if (symbol.getName().equals(id.get("name"))) {
                isField = true;
                var = symbol;
                break;
            }
        }
        if (!isField){
            List<Symbol> localVars = symbolTable.getLocalVariables(method.get("name"));
            for (Symbol localVar:localVars){
                if (localVar.getName().equals(id.get("name"))){
                    var = localVar;
                    break;
                }
            }
        }
        //putfield(this, a.i32, $1.n.i32).V;
        if (isField){
            varStr.append("putfield(this, ").append(var.getName())
                    .append(OllirUtils.getOllirType(var.getType()))
                    .append(", "); // TODO ON THE ASSIGNMENT CHECK IF FIELD AND PUT THE REST
            return var.toString();
        }

        if (array == null){
            varStr.append(var.getName()).append(".").append(OllirUtils.getOllirType(var.getType()))
                    .append(" :=").append(OllirUtils.getOllirType(var.getType())).append(" ");
        }
        else{
            varStr.append(visit(array));
            varStr.append("t").append(varCounter).append(".").append(OllirUtils.getOllirType(var.getType()))
                    .append(" :=").append(OllirUtils.getOllirType(var.getType())).append(" ");
        }

        return varStr.toString();

    }
     public String arrayAccessVisit(JmmNode arrayAccess, Integer dummy){
         StringBuilder retStr = new StringBuilder();
         JmmNode identifier = arrayAccess.getChildren().get(0);
         JmmNode indexNode = arrayAccess.getChildren().get(1);

         String idStr = visit(identifier);
         String indexStr = visit(indexNode);

         JmmNode ancestor = arrayAccess;
         while (!ancestor.getKind().equals("MethodBody")){
             ancestor = ancestor.getJmmParent();
         }
         ancestor = ancestor.getJmmParent();


         //idStr = idStr.substring(0, idStr.indexOf("."));
         if (indexNode.getKind().equals("Number")) {
             retStr.append("\t\tt").append(++varCounter).append(".i32 :=.i32 ").append(indexStr).append(";\n");
             retStr.append(idStr).append("[t").append(varCounter).append(".i32]").append(".i32;");
         }
         else if (indexNode.getKind().equals("Identifier")) {
             retStr.append("\t\tt").append(++varCounter).append(".i32 :=.i32 ").append(indexStr).append(";\n");
             retStr.append(idStr).append("[t").append(varCounter).append(".i32]").append
                     (OllirUtils.getType(identifier.get("name"), symbolTable.getFields(),
                             symbolTable.getLocalVariables(ancestor.get("name")),
                             symbolTable.getParameters(ancestor.get("name"))));
             retStr.append(indexStr);

             String prefix = indexStr.substring(0, indexStr.lastIndexOf("\n"));
             String postfix = indexStr.substring(indexStr.lastIndexOf("\n"));

             retStr.append(prefix).append(idStr).append("[").append(postfix).append(".i32").append("]")
                     .append(OllirUtils.getType(identifier.get("name"), symbolTable.getFields(),
                             symbolTable.getLocalVariables(ancestor.get("name")),
                             symbolTable.getParameters(ancestor.get("name")))).append(";");
         }
         return retStr.toString();
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

        } else if (id.getKind().equals("New")) {

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
        if (identifier.getKind().equals("This"))
            return "this";
        if (identifier.getKind().equals("New"))
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
        if (op.equals("And") || op.equals("Less") || op.equals("Not")) return "bool";
        return "int";
    }

    private String defaultVisit(JmmNode defaultNode, Integer dummy) {
        StringBuilder visitStr = new StringBuilder();
        for (JmmNode child : defaultNode.getChildren()) {
            visitStr.append(visit(child, dummy));
        }
        return visitStr.toString();
    }

    private String getOperator(JmmNode jmmNode) {
        if (jmmNode.getKind().equals("Plus"))
            return " +";
        if (jmmNode.getKind().equals("Minus"))
            return " -";
        if (jmmNode.getKind().equals("Times"))
            return " *";
        if (jmmNode.getKind().equals("Divide"))
            return " /";
        if (jmmNode.getKind().equals("Less"))
            return " <";
        if (jmmNode.getKind().equals("And"))
            return " &&";
        if (jmmNode.getKind().equals("Not"))
            return " !";
        return "";
    }

    private String operationVisit(JmmNode operation, Integer dummy) {
        StringBuilder operationStr = new StringBuilder();
        boolean isNot = operation.getKind().equals("Not");

        JmmNode left = operation.getChildren().get(0);
        JmmNode right = (!isNot)? operation.getChildren().get(1) : null;

        String leftStr = visit(left);
        String rightStr = (!isNot)? visit(right) : "";

        List<String> leftOp = buildOperation(left, leftStr);
        List<String> rightOp = (!isNot) ? buildOperation(right, rightStr) : leftOp;

        operationStr.append(leftOp.get(0));
        if (!isNot) operationStr.append(rightOp);

        operationStr.append(leftOp.get(1)).append(getOperator(operation));

        if(operation.getKind().equals("Plus") || operation.getKind().equals("Minus") ||
                operation.getKind().equals("Times") || operation.getKind().equals("Divide"))
            operationStr.append(".i32");
        else operationStr.append(".bool");

        operationStr.append(rightOp.get(1)).append(";\n");

        return operationStr.toString();
    }

    private List<String> buildOperation(JmmNode operation, String opStr) {
        String str = "";
        List<String> ret = new ArrayList<>();

        if (OllirUtils.isOperation(operation) || operation.getKind().equals("OBJECT_METHOD") || operation.getKind().equals("ARRAY_ACCESS")) {

            String substring;
            String prefix = "";
            if (!opStr.contains(":=."))
                substring = opStr.substring(opStr.lastIndexOf("."), opStr.length() - 1);
            else {
                substring = opStr.substring(opStr.indexOf("."), opStr.indexOf(" "));
                if (opStr.contains("\n")) {
                    prefix = opStr.substring(0, opStr.lastIndexOf("\n") + 1);
                    opStr = opStr.substring(opStr.lastIndexOf("\n") + 1);
                } else {
                    opStr = opStr.substring(opStr.lastIndexOf(" "));
                }
            }
            if (!operation.getKind().equals("ARRAY_ACCESS")) {
                str = prefix + "t" + varCounter + substring + " :=" + substring + " " + opStr + "\n";
                opStr = "t" + varCounter + substring;
                varCounter++;
                //while (skipTemps.contains(varCounter))
                    //varCounter++;
            } else {
                if (opStr.contains(":=.")) {
                    prefix += opStr;
                    opStr = opStr.substring(0, opStr.indexOf(" "));
                } else
                    opStr = opStr.substring(0, opStr.length() - 1);
            }

        } else {
            if (opStr.contains("\n")) {
                String[] lines = opStr.split("\n");
                if (lines[lines.length - 1].contains(":=.")) {
                    for (String line : lines)
                        str += line;

                    opStr = lines[lines.length - 1].substring(0, lines[lines.length - 1].indexOf(" "));
                } else {
                    for (String line : Arrays.copyOfRange(lines, 0, lines.length - 1))
                        str += line;


                    opStr = lines[lines.length - 1];
                }
            } else if (opStr.contains(":=.")) {
                str = opStr;
                opStr = opStr.substring(0, opStr.indexOf(" "));
            }
        }
        ret.add(str);
        ret.add(opStr);
        return ret;
    }


    private List<String> buildCondition(JmmNode condition, String condStr, boolean isWhile) {
        String finalStr = "";
        List<String> result = new ArrayList<>(2);
        if (condition.getKind().equals("DotAccess")) {
            if (isWhile) {
                finalStr += "\t\tt" + varCounter + ".bool :=.bool " + condStr + "\n";
            } else {
                if (!condStr.contains("\n"))
                    finalStr += "\t\tt" + varCounter + ".bool :=.bool " + condStr + "\n";
                else {
                    finalStr += condStr.substring(0, condStr.lastIndexOf("\n") + 1) + "\n\t\tt" + varCounter + ".bool :=.bool " + condStr.substring(condStr.lastIndexOf("\n")) + "\n";
                }
            }

            condStr = "t" + varCounter + ".bool !.bool t" + varCounter + ".bool;";
            varCounter++;
            //while (skipTemps.contains(varCounter))
                //varCounter++;
        } else if (condition.getKind().equals("Identifier") || condition.getKind().equals("True") || condition.getKind().equals("False")) {
            if (isWhile)
                condStr += " ==.bool " + condStr;
            else
                condStr += " !.bool " + condStr;
        } else if (condition.getKind().equals("ArrayAccess")) {
            finalStr += condStr + "\n";
            condStr = condStr.substring(condStr.lastIndexOf("\n") + 1, condStr.lastIndexOf(":=."));
        }

        if (condStr.contains("\n")) {
            finalStr += condStr.substring(0, condStr.lastIndexOf("\n"));
            finalStr += "t" + varCounter + ".bool :=.bool " + condStr.substring(condStr.lastIndexOf("\n") + 1) + "\n";
            condStr = isWhile ? "t" + varCounter + ".bool ==.bool t" + varCounter + ".bool;" : "t" + varCounter + ".bool !.bool t" + varCounter + ".bool;";
            varCounter++;
            //while (skipTemps.contains(varCounter))
              //  varCounter++;
        } else if (containsOps(condStr)) {
            finalStr += "t" + varCounter + ".bool :=.bool " + condStr + "\n";
            condStr = isWhile ? "t" + varCounter + ".bool ==.bool t" + varCounter + ".bool;" : "t" + varCounter + ".bool !.bool t" + varCounter + ".bool;";
            varCounter++;
            //while (skipTemps.contains(varCounter))
                //varCounter++;
        }
        if (condStr.contains(";"))
            condStr = condStr.substring(0, condStr.length() - 1);
        result.add(finalStr);
        result.add(condStr);
        return result;
    }

    private boolean containsOps(String condStr) {
        return condStr.contains("+") || condStr.contains("-") || condStr.contains("*") || condStr.contains("/") || condStr.contains("<") || condStr.contains("&&");
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


