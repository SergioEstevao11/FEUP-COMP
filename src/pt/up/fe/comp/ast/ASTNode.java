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
    EXPRESSION_STATEMENT,
    EXPRESSION,
    LESS_DECLARATION,
    ADD_SUB_DECLARATION,
    MULT_DIV_DECLARATION,
    NOT_DECLARATION,
    DOT_ACCESS,
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
