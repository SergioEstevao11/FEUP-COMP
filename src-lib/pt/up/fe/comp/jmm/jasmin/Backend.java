package pt.up.fe.comp.jmm.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;


public class Backend implements JasminBackend{
    private String className;
    private String extendsDef;
    private ArrayList<String> imports;

    private int conditionals;
    private int comparisons;
    private HashMap<String, Descriptor> currVarTable;
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        try {

            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method

            this.className = ollirClass.getClassName();
            this.extendsDef = ollirClass.getSuperClass();
            this.imports = ollirClass.getImports();

            StringBuilder jasminCode = new StringBuilder();


            jasminCode.append(this.generateClassDecl(ollirClass));
            jasminCode.append(this.generateClassMethods(ollirClass));


            List<Report> reports = new ArrayList<>();

            return new JasminResult(ollirResult, jasminCode.toString(), reports);

        } catch (OllirErrorException e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    Arrays.asList(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }

    }

    private String generateClassDecl(ClassUnit ollirClass) {
        StringBuilder classCode = new StringBuilder();

        // Class: Definition
        classCode.append(".class");
        if (ollirClass.getClassAccessModifier() != AccessModifiers.DEFAULT) {
            classCode.append(" ").append(ollirClass.getClassAccessModifier().toString().toLowerCase());
        }
        else {
            classCode.append(" public");
        }

        classCode.append(" ").append(className).append("\n");

        // Class: Extends
        classCode.append(".super ").append(generateSuper()).append("\n");

        // Class: Fields
        for(Field field: ollirClass.getFields()) {
            classCode.append(this.generateClassField(field));
        }

        return classCode.toString();
    }
    private String generateClassField(Field field) {
        StringBuilder FieldCode = new StringBuilder();

        FieldCode.append(".field");

        if (field.getFieldAccessModifier() != AccessModifiers.DEFAULT) {
            FieldCode.append(" ").append(field.getFieldAccessModifier().toString().toLowerCase());
        }else{
            FieldCode.append(" public");
        }

        if (field.isStaticField()) {
            FieldCode.append(" static");
        }
        if (field.isFinalField()) {
            FieldCode.append(" final");
        }

        FieldCode.append(" " + field.getFieldName() + " " + getJasminType(field.getFieldType()));

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
        StringBuilder methodHeaderCode = new StringBuilder();

        methodHeaderCode.append(".method");

        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT)
            methodHeaderCode.append(" ").append(method.getMethodAccessModifier().toString().toLowerCase());
        else
            methodHeaderCode.append(" public");

        if (method.isStaticMethod())
            methodHeaderCode.append(" static");
        if (method.isFinalMethod())
            methodHeaderCode.append(" final");

        if (method.isConstructMethod())
            methodHeaderCode.append(" <init>");
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
        StringBuilder instructions = new StringBuilder();
        currVarTable = method.getVarTable();
        HashMap<String, Instruction> labels = method.getLabels();


        for (int i = 0; i < method.getInstructions().size(); i++) {
            Instruction instruction = method.getInstr(i);
            for (String key : labels.keySet()) {

                if (labels.get(key) == instruction) {
                    instructions.append(key).append(":\n");
                }
            }

            //gets instr code
            instructions.append(getJasminInst(instruction));

            if (instruction.getInstType() == InstructionType.CALL) {
                if (((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID) {
                    instructions.append("\tpop\n");
                }
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


    private String getJasminType(Type type){
        if (type.getTypeOfElement() == ElementType.ARRAYREF){
            return "[" + getJasminType( ((ArrayType)type).getTypeOfElements());
        }
        else if(type.getTypeOfElement() == ElementType.OBJECTREF) {
            String className = ((ClassType) type).getName();
            for (String imported : this.imports) {
                if (imported.endsWith("." + className))
                    return  "L" + imported.replace('.', '/') + ";";
            }
            return  "L" + className + ";";
        }


        return getJasminType(type.getTypeOfElement());
    }

    private String getJasminType(ElementType type) {
        String jasminType;

        if(type == ElementType.INT32) {jasminType = "I";}
        else if(type == ElementType.BOOLEAN) {jasminType = "Z";}
        else if(type == ElementType.VOID) {jasminType = "V";}
        else if(type == ElementType.STRING) {jasminType = "Ljava/lang/String;";}
        else{throw new IllegalStateException("Unexpected JasminType");}

        return jasminType;
    }

    private String getJasminInst(Instruction instr) {

        if (instr instanceof SingleOpInstruction)
            return this.generateSingleOp((SingleOpInstruction) instr);//feito
        if (instr instanceof AssignInstruction)
            return this.generateAssignOp((AssignInstruction) instr);//feito
        if (instr instanceof BinaryOpInstruction)
            return this.generateBinaryOp((BinaryOpInstruction) instr); //feito?
        if (instr instanceof CallInstruction)
            return this.generateCallOp((CallInstruction) instr); //feito
        if (instr instanceof GotoInstruction)
            return this.generateGotoOp((GotoInstruction) instr); //feito
        if (instr instanceof ReturnInstruction)
            return this.generateReturnOp((ReturnInstruction) instr); //feito
        if (instr instanceof CondBranchInstruction)
            return this.generateBranchOp((CondBranchInstruction) instr);//feito-ish
        if (instr instanceof GetFieldInstruction)
            return this.generateGetFieldOp((GetFieldInstruction) instr);//feito
        if (instr instanceof PutFieldInstruction)
            return this.generatePutFieldOp((PutFieldInstruction) instr); //feito
        return "ERROR: instruction doesn't exist";

    }

    private String generatePutFieldOp(PutFieldInstruction instr) {
        String jasminCode = loadElement(instr.getFirstOperand()) +
                loadElement(instr.getThirdOperand()) +
                "\tputfield " +
                getObjectName(((Operand) instr.getFirstOperand()).getName()) +
                "/" + ((Operand) instr.getSecondOperand()).getName() +
                " " + getJasminType(instr.getSecondOperand().getType()) + "\n";


        return jasminCode;
    }

    private String generateGetFieldOp(GetFieldInstruction instr) {

        String jasminCode = loadElement(instr.getFirstOperand()) +
                "\tgetfield " +
                getObjectName(((Operand) instr.getFirstOperand()).getName()) +
                "/" + ((Operand) instr.getSecondOperand()).getName() +
                " " + getJasminType(instr.getFieldType()) + "\n";

        return jasminCode;

    }

    private String generateBranchOp(CondBranchInstruction instr) {

        Element leftElem = instr.getOperands().get(0);

        if (instr instanceof OpCondInstruction) {

            Element rightElem = instr.getOperands().get(1);
            OpCondInstruction opCondInstr = (OpCondInstruction) instr;
            if (opCondInstr.getCondition().getOperation().getOpType() == OperationType.ANDB) {
                comparisons++;

                return loadElement(leftElem) +
                        "\tifeq False" + comparisons + "\n" +
                        loadElement(rightElem) +
                        "\tifeq False" + comparisons + "\n" +
                        "\tgoto " + opCondInstr.getLabel() + "\n" +
                        "False" + comparisons + ":\n";
            }

            return loadElement(leftElem)
                    + loadElement(rightElem)
                    + "\t" + getComparison(opCondInstr.getCondition().getOperation()) + " " + instr.getLabel() + "\n";
        }



        //SingleOpCondInstruction
        return "";

    }

    private String generateReturnOp(ReturnInstruction instr) {
        if (!instr.hasReturnValue())
            return "\treturn\n";

        ElementType returnType = instr.getOperand().getType().getTypeOfElement();

        String jasminCode = loadElement(instr.getOperand())
                + "\t" + ((returnType == ElementType.INT32 || returnType == ElementType.BOOLEAN) ? "i" : "a") + "return\n";


        return jasminCode;
    }

    private String generateGotoOp(GotoInstruction instr) {
        return "\tgoto " + instr.getLabel() + "\n";
    }


    private String generateCallOp(CallInstruction instr) {
        switch (instr.getInvocationType()){
            case invokevirtual:
                return generateInvokeVirtual(instr);

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
        return loadElement(instr.getFirstArg());
    }

    private String generateArrayLength(CallInstruction instr) {
        return loadElement(instr.getFirstArg()) + "\tarraylength\n";
    }

    private String generateNew(CallInstruction instr) {
        StringBuilder jasminCode = new StringBuilder();

        if (instr.getReturnType().getTypeOfElement() == ElementType.OBJECTREF) {
            for (Element e : instr.getListOfOperands()) {
                jasminCode.append(loadElement(e));
            }

            jasminCode.append("\tnew ")
                    .append(((Operand) instr.getFirstArg()).getName()).append("\n")
                    .append("\tdup\n");
        } else if (instr.getReturnType().getTypeOfElement() == ElementType.ARRAYREF) {
            for (Element e : instr.getListOfOperands()) {
                jasminCode.append(loadElement(e));
            }

            jasminCode.append("\tnewarray ");
            if (instr.getListOfOperands().get(0).getType().getTypeOfElement() == ElementType.INT32)
                jasminCode.append("int\n");
            else
                jasminCode.append("array type not implemented\n");
        } else
            jasminCode.append("\tnew (not implemented)\n");


        return jasminCode.toString();
    }

    private String generateInvokeSpecial(CallInstruction instr) {
        StringBuilder jasminCode = new StringBuilder();
        jasminCode.append(loadElement(instr.getFirstArg()));

        jasminCode.append("\tinvokespecial ")
                .append((instr.getFirstArg().getType().getTypeOfElement() == ElementType.THIS) ? generateSuper() : className)
                .append("/<init>(");

        for (Element e : instr.getListOfOperands())
            jasminCode.append(getJasminType(e.getType()));

        jasminCode.append(")").append(getJasminType(instr.getReturnType())).append("\n");

        return jasminCode.toString();
    }

    private String generateInvokeInterface(CallInstruction instr) {
        return "";
    }

    private String generateInvokeVirtual(CallInstruction instr) {
        StringBuilder jasminCode = new StringBuilder();
        jasminCode.append(loadElement(instr.getFirstArg()));

        for (Element e : instr.getListOfOperands())
            jasminCode.append(loadElement(e));


        jasminCode.append("\tinvokevirtual ")
                .append(getObjectName(((ClassType) instr.getFirstArg().getType()).getName()))
                .append(".").append(((LiteralElement) instr.getSecondArg()).getLiteral().replace("\"", ""))
                .append("(");

        for (Element e : instr.getListOfOperands())
            jasminCode.append(getJasminType(e.getType()));

        jasminCode.append(")").append(getJasminType(instr.getReturnType())).append("\n");

        return jasminCode.toString();
    }

    private String generateInvokeStatic(CallInstruction instr) {
        StringBuilder jasminCode = new StringBuilder();

        for (Element e : instr.getListOfOperands())
            jasminCode.append(loadElement(e));

        jasminCode.append("\tinvokestatic ")
                .append(getObjectName(((Operand) instr.getFirstArg()).getName()))
                .append(".").append(((LiteralElement) instr.getSecondArg()).getLiteral().replace("\"", ""))
                .append("(");

        for (Element e : instr.getListOfOperands())
            jasminCode.append(getJasminType(e.getType()));

        jasminCode.append(")").append(getJasminType(instr.getReturnType())).append("\n");

        return jasminCode.toString();
    }

    private String generateBinaryOp(BinaryOpInstruction instr) {

        OperationType opType = instr.getOperation().getOpType();

        if (opType == OperationType.NOTB || opType == OperationType.NOT) {
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

        if (opType == OperationType.EQ || opType == OperationType.EQI32){
            //TODO
        }

        if (opType == OperationType.LTH || opType == OperationType.LTHI32) {
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

        return loadElement(instr.getLeftOperand()) + loadElement(instr.getRightOperand())
                + "\t" + getJasminNumOperation(instr.getOperation()) + "\n";
    }



    private String getJasminNumOperation(Operation operation) {
        OperationType opType = operation.getOpType();
        if (opType == OperationType.ANDB || opType == OperationType.ANDI32)
            return "iand";

        if (opType == OperationType.ORB || opType == OperationType.ORI32)
            return "ior";

        if (opType == OperationType.ADD || opType == OperationType.ADDI32)
            return "iadd";

        if (opType == OperationType.SUB || opType == OperationType.SUBI32)
            return "imul";

        if (opType == OperationType.MUL || opType == OperationType.MULI32)
            return "isub";

        if (opType == OperationType.DIV || opType == OperationType.DIVI32)
            return "idiv";

        return "ERROR operation not implemented yet";
    }

    private String generateAssignOp(AssignInstruction instr) {
        StringBuilder jasminCode = new StringBuilder();

        Operand op = (Operand) instr.getDest();
        int reg = currVarTable.get(op.getName()).getVirtualReg();

        if (instr.getRhs().getInstType() == InstructionType.BINARYOPER) {

            BinaryOpInstruction binOp = (BinaryOpInstruction) instr.getRhs();
            if (binOp.getOperation().getOpType() == OperationType.ADD) {

                if (!binOp.getLeftOperand().isLiteral() && binOp.getRightOperand().isLiteral()) {
                    if (((Operand) binOp.getLeftOperand()).getName().equals(op.getName())
                            && Integer.parseInt(((LiteralElement) binOp.getRightOperand()).getLiteral()) == 1) {
                        return "\tiinc " + reg + " 1\n";
                    }
                }
                else if (binOp.getLeftOperand().isLiteral() && !binOp.getRightOperand().isLiteral()) {
                    if (((Operand) binOp.getRightOperand()).getName().equals(op.getName())
                            && Integer.parseInt(((LiteralElement) binOp.getLeftOperand()).getLiteral()) == 1) {
                        return "\tiinc " + reg + " 1\n";
                    }
                }
            }
        }

        if (currVarTable.get(op.getName()).getVarType().getTypeOfElement() == ElementType.ARRAYREF
                && op.getType().getTypeOfElement() != ElementType.ARRAYREF) {
            ArrayOperand arrayOp = (ArrayOperand) op;
            Element index = arrayOp.getIndexOperands().get(0);

            jasminCode.append(loadDescriptor(currVarTable.get(op.getName())))
                    .append(loadElement(index));
        }

        jasminCode.append(getJasminInst(instr.getRhs()));


        if (op.getType().getTypeOfElement() == ElementType.INT32 || op.getType().getTypeOfElement() == ElementType.BOOLEAN)

            if (currVarTable.get(op.getName()).getVarType().getTypeOfElement() == ElementType.ARRAYREF) {
                jasminCode.append("\tiastore\n");

                return jasminCode.toString();
            } else
                jasminCode.append("\tistore");
        else {
            jasminCode.append("\tastore");
        }

        if(reg <=3){
            jasminCode.append("_");
        }else{
            jasminCode.append(" ");
        }

        jasminCode.append(reg).append("\n");


        return jasminCode.toString();
    }

    private String generateSingleOp(SingleOpInstruction instr) {
        return loadElement(instr.getSingleOperand());
    }

    private String loadElement(Element elem) {
        if (elem.isLiteral())
            return loadLiteral((LiteralElement) elem);

        Descriptor descriptor = currVarTable.get(((Operand) elem).getName());
        if (descriptor == null)
            return "";

        try {
            if (elem.getType().getTypeOfElement() != ElementType.ARRAYREF
                    && descriptor.getVarType().getTypeOfElement() == ElementType.ARRAYREF) {
                ArrayOperand arrayOp = (ArrayOperand) elem;
                Element index = arrayOp.getIndexOperands().get(0);
                return loadDescriptor(descriptor) + loadElement(index) + "\tiaload\n";
            }
        } catch (NullPointerException | ClassCastException except) {
            System.out.println(((Operand) elem).getName());
            System.out.println(descriptor.getVirtualReg() + " " + descriptor.getVarType());
        }

        return loadDescriptor(descriptor);
    }

    private String loadDescriptor(Descriptor descriptor) {
        StringBuilder jasminCode = new StringBuilder("\t");

        ElementType t = descriptor.getVarType().getTypeOfElement();
        if (t == ElementType.THIS) {
            jasminCode.append("aload_0\n");
            return jasminCode.toString();
        }

        if (t == ElementType.INT32 || t == ElementType.BOOLEAN){
            jasminCode.append("i");
        }else{
            jasminCode.append("a");
        }

        jasminCode.append("load");
        int virtualReg = descriptor.getVirtualReg();
        if (virtualReg <=3){
            jasminCode.append("_");
        }else{
            jasminCode.append(" ");
        }

        jasminCode.append(virtualReg).append("\n");

        return jasminCode.toString();
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

    private String getObjectName(String name) {
        if (name.equals("this"))
            return className;
        return name;
    }



}
