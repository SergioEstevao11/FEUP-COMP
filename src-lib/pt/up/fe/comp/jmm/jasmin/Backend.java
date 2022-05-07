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
    private int StackSize = 0;
    private int MaxStackSize = 0;
    private final List<Report> reports = new ArrayList<>();

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        this.className = ollirClass.getClassName();
        this.extendsDef = ollirClass.getSuperClass();
        StringBuilder jasminCode = new StringBuilder();


        jasminCode.append(this.generateClassDecl(ollirClass)); //feito
        jasminCode.append(this.generateClassMethods(ollirClass)); //ainda tem erros

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
            System.out.println("ERROR");
            e.printStackTrace();
            return null;
        }

    }

    private String generateClassDecl(ClassUnit ollirClass) {
        StringBuilder classCode = new StringBuilder();

        // Class: Definition
        classCode.append(".class public ").append(ollirClass.getClassName()).append("\n");

        // Class: Extends
        classCode.append(".super ").append(this.generateSuper()).append("\n");

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
                    .append(Backend.getJasminType(field.getFieldType()))
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
                    classMethodsCode.append(Backend.getJasminType(param.getType()));
                }

                String body = this.generateClassMethodBody(method.getInstructions());

                classMethodsCode.append(")")
                        .append(Backend.getJasminType(method.getReturnType()))
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
            this.StackSize = 0;
            this.MaxStackSize = 0;
        }

        return classMethodsCode.toString();
        
    }

    private static String getJasminType(Type type){
        if (type instanceof ArrayType){
            return "[" + getJasminType(((ArrayType)type).getTypeOfElements());
        }
        else if(type.getTypeOfElement() == ElementType.OBJECTREF) {return "L" + ((ClassType) type).getName() + ";";}
        else if(type.getTypeOfElement() == ElementType.CLASS) {return "L" + ((ClassType) type).getName() + ";";}

        return getJasminType(type.getTypeOfElement());
    }

    private static String getJasminType(ElementType type) {
        String jasminType = "";

        if(type == ElementType.INT32) {jasminType = "I";}
        else if(type == ElementType.BOOLEAN) {jasminType = "Z";}
        else if(type == ElementType.VOID) {jasminType = "V";}
        else if(type == ElementType.STRING) {jasminType = "Ljava/lang/String;";}
        else{throw new IllegalStateException("Unexpected value: " + type);}

        return jasminType;
    }

    private String generateClassMethodBody(ArrayList<Instruction> instructions) {
        StringBuilder methodInstCode = new StringBuilder();

        for(var instr: instructions) {

            methodInstCode.append(this.getJasminInst(instr));
        }

        return methodInstCode.toString();
    }

    private String getJasminInst(Instruction instr) {

        if (instr instanceof SingleOpInstruction)
            return this.generateSingleOp((SingleOpInstruction) instr);
        if (instr instanceof AssignInstruction)
            return this.generateAssignOp((AssignInstruction) instr);
        if (instr instanceof BinaryOpInstruction)
            return this.generateBinaryOp((BinaryOpInstruction) instr);
        if (instr instanceof CallInstruction)
            return this.generateCallOp((CallInstruction) instr);
        if (instr instanceof GetFieldInstruction)
            return this.generateGetFieldOp((GetFieldInstruction) instr);
        if (instr instanceof PutFieldInstruction)
            return this.generatePutFieldOp((PutFieldInstruction) instr);
        if (instr instanceof GotoInstruction)
            return this.generateGotoOp((GotoInstruction) instr);
        if (instr instanceof ReturnInstruction)
            return this.generateReturnOp((ReturnInstruction) instr);
        if (instr instanceof CondBranchInstruction)
            return this.generateBranchOp((CondBranchInstruction) instr);
        return "";

    }

    private String generateBranchOp(CondBranchInstruction instr) {
        var jasminCode = new StringBuilder();

        return jasminCode.toString();
    }

    private String generateReturnOp(ReturnInstruction instr) {
        var jasminCode = new StringBuilder();

        return jasminCode.toString();
    }

    private String generateGotoOp(GotoInstruction instr) {
        var jasminCode = new StringBuilder();

        return jasminCode.toString();
    }

    private String generatePutFieldOp(PutFieldInstruction instr) {
        var jasminCode = new StringBuilder();

        return jasminCode.toString();
    }

    private String generateGetFieldOp(GetFieldInstruction instr) {
        var jasminCode = new StringBuilder();

        return jasminCode.toString();
    }

    private String generateCallOp(CallInstruction instr) {
        var jasminCode = new StringBuilder();

        switch (instr.getInvocationType()){
            case invokestatic:
                jasminCode.append("invokestatic ");
                break;

            default:
                throw new IllegalStateException("Error");
        }

        return jasminCode.toString();
    }

    private String generateBinaryOp(BinaryOpInstruction instr) {
        var jasminCode = new StringBuilder();

        return jasminCode.toString();
    }

    private String generateAssignOp(AssignInstruction instr) {
        var jasminCode = new StringBuilder();

        return jasminCode.toString();
    }

    private String generateSingleOp(SingleOpInstruction instr) {
        var jasminCode = new StringBuilder();

        return jasminCode.toString();
    }



    //Stack Functions
    private String generateStackLimits()
    {
        return "\t\t.limit stack " + 99 + "\n";
    }

    private String generatePops() {
        StringBuilder pop = new StringBuilder();

        for(int i = this.StackSize; i > 0; i--) {
            if(i > 1) {
                pop.append("\t\tpop2\n");
                i--;
            } else {
                pop.append("\t\tpop\n");
            }
        }

        this.StackSize = 0;
        return pop.toString();
    }

    private String generateLocalLimits() {

        return "\t\t.limit locals " + 99 + "\n";

    }

}
