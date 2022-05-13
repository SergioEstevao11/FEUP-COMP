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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Backend implements JasminBackend{
    private String className;
    private String extendsDef = null;
    private int StackSize = 0;
    private int MaxStackSize = 0;
    private int conditionals;
    private HashMap<String, Descriptor> currVarTable;
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

            List<Report> reports = new ArrayList<>();
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
        classCode.append(".class");
        if (ollirClass.getClassAccessModifier() != AccessModifiers.DEFAULT)
            classCode.append(" ").append(ollirClass.getClassAccessModifier().toString().toLowerCase());

        // Class: Extends
        classCode.append(" ").append(ollirClass.getClassName()).append("\n")
                .append(".super ").append(this.generateSuper()).append("\n");

        // Class: Fields
        for(Field field: ollirClass.getFields()) {
            classCode.append(this.generateClassField(field));
        }

        return classCode.toString();
    }
    private String generateClassField(Field field) {
        StringBuilder FieldCode = new StringBuilder();

        FieldCode.append(".field");

        AccessModifiers accessModifier = field.getFieldAccessModifier();
        if (accessModifier != AccessModifiers.DEFAULT)
            FieldCode.append(" ").append(field.getFieldAccessModifier().toString().toLowerCase());

        if (field.isStaticField())
            FieldCode.append(" static");
        if (field.isFinalField())
            FieldCode.append(" final");

        FieldCode.append(" ").append(field.getFieldName())
                .append(" ").append(getJasminType(field.getFieldType()));

        if (field.isInitialized()) {
            FieldCode.append(" = ").append(field.getInitialValue());
        }
        FieldCode.append("\n");
        return FieldCode.toString();

    }

    private String generateSuper() {
        return this.extendsDef == null ? "java/lang/Object" : this.extendsDef;
    }

    private String generateClassMethods(ClassUnit ollirClass) {
        StringBuilder classMethodsCode = new StringBuilder();

        for(Method method: ollirClass.getMethods()) {
            classMethodsCode.append("\n")
                            .append(generateClassMethodHeader(method))
                            .append(generateClassMethodBody(method));
        }

        return classMethodsCode.toString();
        
    }

    private String generateClassMethodHeader(Method method) {
        StringBuilder methodHeaderCode = new StringBuilder(".method");

        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT)
            methodHeaderCode.append(" ").append(method.getMethodAccessModifier().toString().toLowerCase());

        if (method.isStaticMethod())
            methodHeaderCode.append(" static");
        if (method.isFinalMethod())
            methodHeaderCode.append(" final");

        if (method.isConstructMethod())
            methodHeaderCode.append("<init>()\n\taload_0\n\tinvokenonvirtual ")
                            .append(generateSuper())
                            .append("/<init>()V\n\treturn\n.end method\n\n").toString();
        else
            methodHeaderCode.append(" ").append(method.getMethodName());

        methodHeaderCode.append("(");
        for (Element param : method.getParams())
            methodHeaderCode.append(getJasminType(param.getType()));
        methodHeaderCode.append(")").append(getJasminType(method.getReturnType())).append("\n");

        return methodHeaderCode.toString();
    }

    private String generateClassMethodBody(Method method) {
        StringBuilder methodBodyCode = new StringBuilder();

        currVarTable = method.getVarTable();
        StringBuilder instructions = new StringBuilder();

        HashMap<String, Instruction> labels = method.getLabels();
        for (int i = 0; i < method.getInstructions().size(); i++) {
            Instruction instruction = method.getInstr(i);
            for (String s : labels.keySet()) {
                if (labels.get(s) == instruction) {
                    instructions.append(s).append(":\n");
                }
            }

            instructions.append(getJasminInst(instruction));
            if (instruction.getInstType() == InstructionType.CALL) {
                if (((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID)
                    instructions.append("\tpop\n");
            }
        }

        methodBodyCode.append(generateStackLimits());

        methodBodyCode.append(generateLocalLimits());

        methodBodyCode.append(instructions);

        if (method.isConstructMethod())
            methodBodyCode.append("\treturn\n");

        methodBodyCode.append(".end method\n");

        return methodBodyCode.toString();
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
        return "ERROR: instruction doesn't exist";

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
            case invokevirtual:
                return generateInvokeVirtual(instr);
                
            case invokeinterface:
                return generateInvokeInterface(instr);
                
            case invokespecial:
                return generateInvokeSpecial(instr);
                
            case invokestatic:
                return generateInvokeStatic(instr);
                
            case NEW:
                return generateNew(instr);
                
            case arraylength:
                return generateArrayLength(instr);
                
            case ldc:
                return generateLdc(instr);

            default:
                throw new IllegalStateException("Error");
        }

    }

    private String generateLdc(CallInstruction instr) {
        return "";
    }

    private String generateArrayLength(CallInstruction instr) {
        return "";
    }

    private String generateNew(CallInstruction instr) {
        return "";
    }

    private String generateInvokeSpecial(CallInstruction instr) {
        return "";
    }

    private String generateInvokeInterface(CallInstruction instr) {
        return "";
    }

    private String generateInvokeVirtual(CallInstruction instr) {
        return "";
    }

    private String generateInvokeStatic(CallInstruction instr) {
        var code = new StringBuilder();

        code.append("invokestatic ");
        var methodClass = ((Operand) instr.getFirstArg()).getName();

        //code.append(getFullyQuealifiedName(methodClass));


        return code.toString();
    }

    private String generateBinaryOp(BinaryOpInstruction instr) {
        if (instr.getOperation().getOpType() == OperationType.ANDB) {
            conditionals++;


            return loadElement(instr.getLeftOperand()) +
                    "\tifeq False" + conditionals + "\n" +
                    loadElement(instr.getRightOperand()) +
                    "\tifeq False" + conditionals + "\n" +
                    "\ticonst_1\n" +
                    "\tgoto Store" + conditionals + "\n" +
                    "False" + conditionals + ":\n" +
                    "\ticonst_0\n" +
                    "Store" + conditionals + ":\n";
        }

        if (instr.getOperation().getOpType() == OperationType.NOTB) {
            conditionals++;


            String jasminCode = loadElement(instr.getLeftOperand());
            if (((Operand) instr.getRightOperand()).getName().equals(
                    ((Operand) instr.getLeftOperand()).getName())) {
                jasminCode += "\tifeq";

            } else {
                jasminCode += loadElement(instr.getRightOperand()) +
                        "\t" + getComparison(instr.getOperation());
            }

            return jasminCode + " True" + conditionals + "\n" +
                    "\ticonst_0\n" +
                    "\tgoto Store" + conditionals + "\n" +
                    "True" + conditionals + ":\n" +
                    "\ticonst_1\n" +
                    "Store" + conditionals + ":\n";
        }

        if (instr.getOperation().getOpType() == OperationType.LTH) {
            conditionals++;
            String jasminCode = loadElement(instr.getLeftOperand()) +
                    loadElement(instr.getRightOperand()) +
                    "\t" + getComparison(instr.getOperation()) + " True" + conditionals + "\n" +
                    "\ticonst_0\n" +
                    "\tgoto Store" + conditionals + "\n" +
                    "True" + conditionals + ":\n" +
                    "\ticonst_1\n" +
                    "Store" + conditionals + ":\n";

            return jasminCode;
        }

        return loadElement(instr.getLeftOperand()) +
                unaryOpInstruction(instr);
    }

    private String unaryOpInstruction(Instruction instr) {
        String jasminCode = "";
        return jasminCode;
    }

    private String generateAssignOp(AssignInstruction instr) {
        StringBuilder jasminCode = new StringBuilder();

        Operand o = (Operand) instr.getDest();
        int reg = currVarTable.get(o.getName()).getVirtualReg();

        // case i = i + 1 => iinc i
        if (instr.getRhs().getInstType() == InstructionType.BINARYOPER) {
            BinaryOpInstruction op = (BinaryOpInstruction) instr.getRhs();
            if (op.getOperation().getOpType() == OperationType.ADD) {
                if (!op.getLeftOperand().isLiteral() && op.getRightOperand().isLiteral()) {
                    if (((Operand) op.getLeftOperand()).getName().equals(o.getName())
                            && Integer.parseInt(((LiteralElement) op.getRightOperand()).getLiteral()) == 1) {
                        return "\tiinc " + reg + " 1\n";
                    }
                } else if (op.getLeftOperand().isLiteral() && !op.getRightOperand().isLiteral()) {
                    if (((Operand) op.getRightOperand()).getName().equals(o.getName())
                            && Integer.parseInt(((LiteralElement) op.getLeftOperand()).getLiteral()) == 1) {
                        return "\tiinc " + reg + " 1\n";
                    }
                }
            }
        }

        if (currVarTable.get(o.getName()).getVarType().getTypeOfElement() == ElementType.ARRAYREF
                && o.getType().getTypeOfElement() != ElementType.ARRAYREF) {
            ArrayOperand arrayOp = (ArrayOperand) o;
            Element index = arrayOp.getIndexOperands().get(0);

            jasminCode.append(loadDescriptor(currVarTable.get(o.getName())))
                    .append(loadElement(index));
        }

        jasminCode.append(getJasminInst(instr.getRhs()));


        if (o.getType().getTypeOfElement() == ElementType.INT32 || o.getType().getTypeOfElement() == ElementType.BOOLEAN)
            if (currVarTable.get(o.getName()).getVarType().getTypeOfElement() == ElementType.ARRAYREF) {
                jasminCode.append("\tiastore\n");

                return jasminCode.toString();
            } else
                jasminCode.append("\tistore");
        else {
            jasminCode.append("\tastore");
        }

        jasminCode.append((reg <= 3) ? "_" : " ").append(reg).append("\n");


        return jasminCode.toString();
    }

    private String generateSingleOp(SingleOpInstruction instr) {
        return loadElement(instr.getSingleOperand());
    }

    private String loadElement(Element elem) {
        if (elem.isLiteral())
            return loadLiteral((LiteralElement) elem);

        Descriptor d = currVarTable.get(((Operand) elem).getName());
        if (d == null)
            return "!!!" + ((Operand) elem).getName();

        try {
            if (elem.getType().getTypeOfElement() != ElementType.ARRAYREF
                    && d.getVarType().getTypeOfElement() == ElementType.ARRAYREF) {
                ArrayOperand arrayOp = (ArrayOperand) elem;
                Element index = arrayOp.getIndexOperands().get(0);
                return loadDescriptor(d) + loadElement(index) + "\tiaload\n";
            }
        } catch (NullPointerException | ClassCastException except) {
            System.out.println(((Operand) elem).getName());
            System.out.println(d.getVirtualReg() + " " + d.getVarType());
        }

        return loadDescriptor(d);
    }

    private String loadDescriptor(Descriptor descriptor) {
        ElementType t = descriptor.getVarType().getTypeOfElement();
        if (t == ElementType.THIS)
            return "\taload_0\n";

        int reg = descriptor.getVirtualReg();
        return "\t" + ((t == ElementType.INT32 || t == ElementType.BOOLEAN) ? "i" : "a") + "load" +
                ((reg <= 3) ? "_" : " ") + reg + "\n";
    }

    private String loadLiteral(LiteralElement elem) {
        String jasminCode = "\t";
        int n = Integer.parseInt(elem.getLiteral());
        if (elem.getType().getTypeOfElement() == ElementType.INT32 || elem.getType().getTypeOfElement() == ElementType.BOOLEAN) {
            if (n <= 5 && n >= -1)
                jasminCode += "iconst_";
            else if (n > 255 || n < -1)
                jasminCode += "ldc ";
            else if (n > 127)
                jasminCode += "sipush ";
            else
                jasminCode += "bipush ";
        } else
            jasminCode += "ldc ";

        if (n == -1)
            return jasminCode + "m1\n";

        return jasminCode + n + "\n";
    }


    private String getComparison(Operation operation) {
        switch (operation.getOpType()) {
            case GTE:
                return "if_icmpge";
            case LTH:
                return "if_icmplt";
            case EQ:
                return "if_icmpeq";
            case NOTB:
            case NEQ:
                return "if_icmpne";
            default:
                System.out.println(operation.getOpType());
                return "ERROR comparison not implemented yet";
        }
    }

    //Stack Functions
    private String generateStackLimits()
    {
        return "\t.limit stack " + 99 + "\n";
    }

    private String generateLocalLimits() {

        return "\t.limit locals " + 99 + "\n";

    }

}
