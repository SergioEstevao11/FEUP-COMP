package pt.up.fe.comp.ast;

import pt.up.fe.specs.util.SpecsStrings;

public enum ASTNode {

    PROGRAM,
    IMPORT_DECL,
    CLASS_DECL,
    METHOD_DECL,
    ID;

    private final String name;

    private ASTNode(){
        this.name = SpecsStrings.toCamelCase(name(), "_", true);
    }

    @Override
    public String toString(){
        return name;
    }
    
}
