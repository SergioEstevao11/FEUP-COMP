package pt.up.fe.comp.ast;

import pt.up.fe.specs.util.SpecsStrings;

public enum ASTNode {

    START,
    IMPORT_DECLARATION,
    CLASS_DECLARATION,
    METHOD_DECLARATION,
    IDENTIFIER,
    INT,
    TRUE,
    FALSE,
    NEW,
    OPERATION,
    LESS,
    AND,
    NOT,
    EXPR,
    MEMBER_CALL,
    ARGUMENTS,
    IF,
    ELSE,
    WHILE,
    ASSIGNMENT,
    RETURN;



    private final String name;

    private ASTNode(){
        this.name = SpecsStrings.toCamelCase(name(), "_", true);
    }

    @Override
    public String toString(){
        return name;
    }
    
}
