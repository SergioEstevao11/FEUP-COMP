package pt.up.fe.comp.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class SymbolTableBuilder implements SymbolTable{

    private final List<String> imports;
    private String className;
    private String superClass;

    private final List<String> methods;
    private final Map<String,Type> methodReturnType;
    private final Map<String,List<Symbol>> methodParams;

    public SymbolTableBuilder() {
        this.imports = new ArrayList<>();
        this.className = null;
        this.superClass = null;
        this.methods = new ArrayList<>();
        this.methodReturnType = new HashMap<>();
        this.methodParams = new HashMap<>();
    }


    @Override
    public List<String> getImports() {
        return imports;
    }

    public void addImport(String importString){
        imports.add(importString);
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return null;
    }

    @Override
    public List<Symbol> getFields() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getMethods() {
        return methods;
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return methodReturnType.get(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return methodParams.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return Collections.emptyList();
    }

    public void addMethod(String methodSignature, Type returnType, List<Symbol> params){
        methods.add(methodSignature);
        methodReturnType.put(methodSignature, returnType);
        methodParams.put(methodSignature, params);

    }

    public void setClassName(String className) {
        this.className = className;
    }


    public Object setSuperClass(String superClass) {
        this.superClass = superClass;

        return this.superClass;
    }


    public boolean hasMethod(String methodName) {
        return false;
    }
    
}
