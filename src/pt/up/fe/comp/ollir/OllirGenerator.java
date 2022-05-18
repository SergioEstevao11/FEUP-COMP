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
            addVisit(ASTNode.METHOD_DECL, this::methodDeclVisit);
            addVisit(ASTNode.EXPR, this::exprStmtVisit);
            addVisit(ASTNode.MEMBER_CALL, this::memberCallVisit);
            addVisit(ASTNode.ARGUMENTS, this::argumentsVisit);
            addVisit(ASTNode.ID, this::idVisit);
            addVisit(ASTNode.IF, this::ifStmtVisit);
            addVisit(ASTNode.ELSE, this::ifStmtVisit);
            addVisit(ASTNode.WHILE, this::whileStmtVisit);
            addVisit(ASTNode.ASSIGNMENT, this::assignmentVisit);
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



    private Integer argumentsVisit(JmmNode arguments, Integer dummy){
        for (var child: arguments.getChildren()){
            code.append(", ");
            visit(child);
        }

        return 0;
    }

    private Integer idVisit(JmmNode id, Integer dummy){
        code.append(id.get("name"));

        return 0;
    }
     
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

     //compound
     //Var decl
};


