package pt.up.fe.comp.jmm.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Backend implements JasminBackend{
    private String className;
    private String extendsDef = null;
    private Method currMethod;
    private int opLabel = 0;
    private int instrCurrStackSize = 0;
    private int instrMaxStackSize = 0;
    private final List<Report> reports = new ArrayList<>();

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        this.className = ollirClass.getClassName();
        this.extendsDef = ollirClass.getSuperClass();
        StringBuilder jasminCode = new StringBuilder();


        jasminCode.append(this.generateClassDecl(ollirClass));
        jasminCode.append(this.generateClassMethods(ollirClass));

        try {
            File jasminFile = new File(this.className + ".j");
            jasminFile.createNewFile();

            FileWriter myWriter = new FileWriter(jasminFile);
            myWriter.write(jasminCode.toString());
            myWriter.close();

            if (!Files.exists(Paths.get("generated/class")))
                new File("generated/class").mkdir();

            JasminUtils.assemble(new File("generated/jasmin/" + this.className + ".j"), new File("generated/class"));


            return new JasminResult(ollirResult, jasminCode.toString(), reports);

        }catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }

    }

    private String generateClassDecl(ClassUnit ollirClass) {
        StringBuilder classCode = new StringBuilder();

        // Class: Definition
        classCode.append(".class public ")
                .append(ollirClass.getClassName())
                .append("\n");

        // Class: Extends
        classCode.append(".super ")
                .append(this.generateSuper())
                .append("\n");

        // Class: Fields
        classCode.append(this.generateClassFields(ollirClass));

        // Class: Used to initialize a new instance of the class
        classCode.append("\t.method public <init>()V\n")
                .append("\t\taload_0\n")
                .append("\t\tinvokenonvirtual ")
                .append(this.generateSuper())
                .append("/<init>()V\n")
                .append("\t\treturn\n")
                .append("\t.end method\n\n");

        return classCode.toString();
    }
    private String generateClassFields(ClassUnit ollirClass) {
        StringBuilder classFieldsCode = new StringBuilder();

        for(Field field: ollirClass.getFields()) {
            classFieldsCode.append("\t.field private ")
                    .append(field.getFieldName())
                    .append(" ")
                    .append(Backend.generateType(field.getFieldType()))
                    .append("\n");
        }

        return classFieldsCode.append("\n").toString();
    }

    private String generateSuper() {
        return this.extendsDef == null ? "java/lang/Object" : this.extendsDef;
    }

    private String generateClassMethods(ClassUnit ollirClass) {
        StringBuilder classMethodsCode = new StringBuilder();

        for(Method method: ollirClass.getMethods()) {
            this.currMethod = method;
            if(method.isConstructMethod()) continue;

            if(method.getMethodName().equals("main")) {
                String body = this.generateClassMethodBody(method.getInstructions());
                classMethodsCode.append("\t.method public static main([Ljava/lang/String;)V\n")
                        .append(this.generateStackLimits())
                        .append(this.generateLocalLimits())
                        .append(body)
                        .append(this.generatePops())
                        .append("\t\treturn\n");
            }

            else {
                classMethodsCode.append(String.format("\t.method public %s(", method.getMethodName()));

                for(Element param:  method.getParams()) {
                    classMethodsCode.append(Backend.generateType(param.getType()));
                }

                String body = this.generateClassMethodBody(method.getInstructions());

                classMethodsCode.append(")")
                        .append(Backend.generateType(method.getReturnType()))
                        .append("\n")
                        .append(this.generateStackLimits())
                        .append(this.generateLocalLimits())
                        .append(body);

                if(method.getReturnType().getTypeOfElement() == ElementType.VOID) {
                    classMethodsCode.append(this.generatePops()).append("\t\treturn");
                }

                classMethodsCode.append("\n");
            }

            classMethodsCode.append("\t.end method\n\n");
            this.instrCurrStackSize = 0;
            this.instrMaxStackSize = 0;
        }

        return classMethodsCode.toString();
        
    }

    private static String generateType(Type type) {
        String jasminType = "";

        if(type.getTypeOfElement() == ElementType.ARRAYREF) {jasminType = "[I";}
        else if(type.getTypeOfElement() == ElementType.ARRAYREF) {jasminType = "I";}
        else if(type.getTypeOfElement() == ElementType.INT32) {jasminType = "Z";}
        else if(type.getTypeOfElement() == ElementType.VOID) {jasminType = "V";}
        else if(type.getTypeOfElement() == ElementType.STRING) {jasminType = "Ljava/lang/String;";}
        else if(type.getTypeOfElement() == ElementType.OBJECTREF) {jasminType = "L" + ((ClassType) type).getName() + ";";}
        else if(type.getTypeOfElement() == ElementType.CLASS) {jasminType = "L" + ((ClassType) type).getName() + ";";}
        else{throw new IllegalStateException("Unexpected value: " + type.getTypeOfElement());}

        return jasminType;
    }

    private String generateStackLimits() {
        return "\t\t.limit stack " + (this.instrMaxStackSize + 2) + "\n";
    }

    private String generateClassMethodBody(ArrayList<Instruction> instructions) {
        StringBuilder methodInstCode = new StringBuilder();

        for(Instruction instr: instructions) {
            for(Map.Entry<String, Instruction> entry: this.currMethod.getLabels().entrySet()) {
                if(entry.getValue().getId() == instr.getId()) {
                    methodInstCode.append(this.generatePops())
                            .append("\t")
                            .append(entry.getKey())
                            .append(":\n");
                }
            }

            methodInstCode.append(this.generateOperation(instr));
        }

        return methodInstCode.toString();
    }

    private String generateOperation(Instruction instr) {

        return "";
    }

    private String generatePops() {
        StringBuilder pop = new StringBuilder();

        for(int i = this.instrCurrStackSize; i > 0; i--) {
            if(i > 1) {
                pop.append("\t\tpop2\n");
                i--;
            } else {
                pop.append("\t\tpop\n");
            }
        }

        this.instrCurrStackSize = 0;
        return pop.toString();
    }

    private String generateLocalLimits() {
        if(this.currMethod.isConstructMethod()) {
            return "";
        }

        int locals = (int) this.currMethod.getVarTable()
                .values()
                .stream()
                .map(Descriptor::getVirtualReg)
                .distinct()
                .count();

        if(!this.currMethod.isStaticMethod()) {
            locals++;
        }

        return "\t\t.limit locals " + locals + "\n";

    }

}
