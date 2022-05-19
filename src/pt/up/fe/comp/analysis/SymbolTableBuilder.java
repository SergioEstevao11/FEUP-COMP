package pt.up.fe.comp.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class SymbolTableBuilder implements SymbolTable {

    private final List<String> imports;
    private String className;
    private String superClass;

    private final List<String> methods;
    private final Map<String, Type> methodReturnType;
    private final Map<String, List<Symbol>> methodParams;
    private final Map<Symbol, Boolean> fields;
    private final Map<Symbol, Boolean> localVariables;

    public SymbolTableBuilder() {
        this.imports = new ArrayList<>();
        this.className = null;
        this.superClass = null;
        this.methods = new ArrayList<>();
        this.methodReturnType = new HashMap<>();
        this.methodParams = new HashMap<>();
        this.fields = new HashMap<>();
        this.localVariables = new HashMap<>();
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    public void addImport(String importString) {
        imports.add(importString);
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return superClass;
    }

    @Override
    public List<Symbol> getFields() {
        return new ArrayList<>(this.fields.keySet());
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
        for (String method : this.methods){
            if (method.equals(methodSignature)){
                return methodParams.get(methodSignature);
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return new ArrayList<>(this.localVariables.keySet());
    }

    public static Type getType(JmmNode node, String attribute) {
        Type type;
        if (node.get(attribute).equals("int[]"))
            type = new Type("int", true);
        else if (node.get(attribute).equals("int"))
            type = new Type("int", false);
        else
            type = new Type(node.get(attribute), false);

        return type;
    }


    public void addField(Symbol field) {
        fields.put(field, false);
    }

    public void addMethod(String methodSignature, Type returnType, List<Symbol> params) {
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

    public boolean hasField(String name) {
        for (Symbol field : this.fields.keySet()) {
            if (field.getName().equals(name))
                return true;
        }
        return false;
    }

    public boolean hasMethod(String methodName) {
        return false;
    }
}
    /*


    public boolean fieldExists(String name) {
        for (Symbol field : this.fields.keySet()) {
            if (field.getName().equals(name))
                return true;
        }
        return false;
    }

    public JmmMethod getMethod(String name, List<Type> params, Type returnType) throws NoSuchMethod {
        for (JmmMethod method : methods) {
            if (method.getName().equals(name) && returnType.equals(method.getReturnType()) && params.size() == method.getParameters().size()) {
                if (JmmMethod.matchParameters(params, method.getParameterTypes())) {
                    return method;
                }
            }
        }

        throw new NoSuchMethod(name);
    }

    public Map.Entry<Symbol, Boolean> getField(String name) {
        for (Map.Entry<Symbol, Boolean> field : this.fields.entrySet()) {
            if (field.getKey().getName().equals(name))
                return field;
        }
        return null;
    }

    public boolean initializeField(Symbol symbol) {
        if (this.fields.containsKey(symbol)) {
            this.fields.put(symbol, true);
            return true;
        }
        return false;
    }

    public void addMethod(String name, Type returnType) {
        currentMethod = new JmmMethod(name, returnType);
        methods.add(currentMethod);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SYMBOL TABLE\n");
        builder.append("Imports").append("\n");
        for (String importStmt : imports)
            builder.append("\t").append(importStmt).append("\n");

        builder.append("Class Name: ").append(className).append(" | Extends: ").append(superClassName).append("\n");

        builder.append("--- Local Variables ---").append("\n");
        for (Map.Entry<Symbol, Boolean> field : fields.entrySet())
            builder.append("\t").append(field.getKey()).append(" Initialized: ").append(field.getValue()).append("\n");

        builder.append("--- Methods ---").append("\n");
        for (JmmMethod method : this.methods) {
            builder.append(method);
            builder.append("---------").append("\n");
        }

        return builder.toString();
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() { return className; }

    @Override
    public String getSuper() {
        return superClassName;
    }

    @Override
    public List<Symbol> getFields() {
        return new ArrayList<>(this.fields.keySet());
    }

    @Override
    public List<String> getMethods() {
        List<String> methods = new ArrayList<>();
        for (JmmMethod method : this.methods) {
            methods.add(method.getName());
        }

        return methods;
    }

    public JmmMethod getCurrentMethod() {
        return currentMethod;
    }

    @Override
    public Type getReturnType(String methodName) {
        List<Type> params = new ArrayList<>();
        String[] parts = methodName.split("::");
        methodName = parts[0];

        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                String[] parts2 = parts[i].split(":");
                params.add(new Type(parts2[0], parts2[1].equals("true")));
            }
        } else {
            for (JmmMethod method : methods) {
                if(method.getName().equals(methodName)) {
                    return method.getReturnType();
                }
            }
        }

        for (JmmMethod method : methods) {
            if(method.getName().equals(methodName)) {
                List<Symbol> currentparams = method.getParameters();
                boolean found = true;
                if (currentparams.size() != params.size()) continue;
                for (int i=0; i<params.size(); i++) {
                    if (!currentparams.get(i).getType().equals(params.get(i))) {
                        found = false;
                        break;
                    }
                }
                if (found) return method.getReturnType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        for (JmmMethod method : this.methods){
            if (method.getName().equals(methodName)){
                return method.getParameters();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        return null;
    }
}*/
