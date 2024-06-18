package me.darknet.assembler.compile.analysis.jvm;

import dev.xdark.blw.type.*;
import me.darknet.assembler.compile.analysis.Local;
import me.darknet.assembler.compile.analysis.VariableNameLookup;
import me.darknet.assembler.compile.analysis.frame.FrameOps;
import me.darknet.assembler.compile.analysis.frame.TypedFrame;
import me.darknet.assembler.compile.analysis.frame.TypedFrameOps;

import dev.xdark.blw.code.instruction.*;
import dev.xdark.blw.constant.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * JVM engine which tracks types of items in the stack/locals.
 */
public class TypedJvmAnalysisEngine extends JvmAnalysisEngine<TypedFrame> {
    public TypedJvmAnalysisEngine(@NotNull VariableNameLookup variableNameLookup) {
        super(variableNameLookup);
    }

    @Override
    public FrameOps<?> newFrameOps() {
        return new TypedFrameOps();
    }

    @Override
    public void execute(SimpleInstruction instruction) {
        int opcode = instruction.opcode();
        switch (opcode) {
            case DUP -> frame.pushType(frame.peek());
            case DUP_X1 -> {
                ClassType type1 = frame.pop();
                ClassType type2 = frame.pop();
                frame.pushTypes(type1, type2, type1);
            }
            case DUP_X2 -> {
                ClassType type1 = frame.pop();
                ClassType type2 = frame.pop();
                ClassType type3 = frame.pop();
                frame.pushTypes(type1, type3, type2, type1);
            }
            case DUP2 -> {
                ClassType type1 = frame.pop();
                ClassType type2 = frame.pop();
                frame.pushTypes(type2, type1, type2, type1);
            }
            case DUP2_X1 -> {
                ClassType type1 = frame.pop();
                ClassType type2 = frame.pop();
                ClassType type3 = frame.pop();
                frame.pushTypes(type2, type1, type3, type2, type1);
            }
            case DUP2_X2 -> {
                ClassType type1 = frame.pop();
                ClassType type2 = frame.pop();
                ClassType type3 = frame.pop();
                ClassType type4 = frame.pop();
                frame.pushTypes(type2, type1, type4, type3, type2, type1);
            }
            case POP, IINC, IRETURN, FRETURN, ARETURN, MONITORENTER, MONITOREXIT -> frame.pop();
            case POP2, LRETURN, DRETURN -> frame.pop2();
            case SWAP -> {
                ClassType type1 = frame.pop();
                ClassType type2 = frame.pop();
                frame.pushTypes(type1, type2);
            }
            case IADD, ISUB, IMUL, IDIV, IREM, ISHL, ISHR, IUSHR, IAND, IOR, IXOR, FCMPG, FCMPL -> {
                frame.pop2();
                frame.pushType(Types.INT);
            }
            case LADD, LSUB, LMUL, LDIV, LREM, LAND, LOR, LXOR -> {
                frame.pop(4); // LONG + TOP, LONG + TOP
                frame.pushType(Types.LONG);
            }
            case LSHL, LSHR, LUSHR -> {
                frame.pop();
                frame.pop2();
                frame.pushType(Types.LONG);
            }
            case FADD, FSUB, FMUL, FDIV, FREM -> {
                frame.pop2();
                frame.pushType(Types.FLOAT);
            }
            case DADD, DSUB, DMUL, DDIV, DREM -> {
                frame.pop(4);
                frame.pushType(Types.DOUBLE);
            }
            case DCMPL, DCMPG, LCMP -> {
                frame.pop(4);
                frame.pushType(Types.INT);
            }
            case INEG -> {
                frame.pop();
                frame.pushType(Types.INT);
            }
            case FNEG -> {
                frame.pop();
                frame.pushType(Types.FLOAT);
            }
            case LNEG -> {
                frame.pop2();
                frame.pushType(Types.LONG);
            }
            case DNEG -> {
                frame.pop2();
                frame.pushType(Types.DOUBLE);
            }
            case ATHROW -> frame.getStack().clear();
            case ACONST_NULL -> frame.pushNull();
            case RETURN -> {
                /* no-op */ }
            case IASTORE, FASTORE , BASTORE, AASTORE, CASTORE, SASTORE -> frame.pop(3); // arrayref, index, value
            case DASTORE, LASTORE -> frame.pop(4);
            case IALOAD -> {
                frame.pop(2); // arrayref, index
                frame.pushType(Types.INT);
            }
            case FALOAD -> {
                frame.pop(2); // arrayref, index
                frame.pushType(Types.FLOAT);
            }
            case BALOAD -> {
                frame.pop(2); // arrayref, index
                frame.pushType(Types.BYTE);
            }
            case CALOAD -> {
                frame.pop(2); // arrayref, index
                frame.pushType(Types.CHAR);
            }
            case SALOAD -> {
                frame.pop(2); // arrayref, index
                frame.pushType(Types.SHORT);
            }
            case DALOAD -> {
                frame.pop(2); // arrayref, index
                frame.pushType(Types.DOUBLE);
            }
            case LALOAD -> {
                frame.pop(2); // arrayref, index
                frame.pushType(Types.LONG);
            }
            case AALOAD -> {
                frame.pop(); // index
                ClassType arrayRef = frame.pop();
                if (arrayRef instanceof ArrayType arrayType) {
                    if (arrayType.dimensions() == 1) {
                        frame.pushType(arrayType.componentType());
                    } else {
                        frame.pushType(Types.arrayTypeFromDescriptor(arrayType.descriptor().substring(1)));
                    }
                } else {
                    frame.pushType(Types.OBJECT);
                }
            }
            case ARRAYLENGTH -> {
                frame.pop();
                frame.pushType(Types.INT);
            }
            case NOP -> {}
            default -> throw new IllegalStateException("Unhandled simple insn: " + opcode);
        }
    }

    @Override
    public void execute(ConstantInstruction<?> instruction) {
        Constant constant = instruction.constant();
        if (constant instanceof OfInt) {
            frame.pushType(Types.INT);
        } else if (constant instanceof OfLong) {
            frame.pushType(Types.LONG);
        } else if (constant instanceof OfFloat) {
            frame.pushType(Types.FLOAT);
        } else if (constant instanceof OfDouble) {
            frame.pushType(Types.DOUBLE);
        } else if (constant instanceof OfString) {
            frame.pushType(Types.type(String.class));
        } else if (constant instanceof OfMethodHandle) {
            // push java/lang/invoke/MethodHandle
            frame.pushType(METHOD_HANDLE);
        } else if (constant instanceof OfDynamic dyn) {
            frame.pushType(dyn.value().type());
        } else if (constant instanceof OfType tp) {
            Type type = tp.value();
            if (type instanceof ClassType ct)
                frame.pushType(ct);
            else if (type instanceof MethodType mt)
                // push java/lang/invoke/MethodType
                frame.pushType(METHOD_TYPE);
        }
    }

    @Override
    public void execute(VarInstruction instruction) {
        final int index = instruction.variableIndex();
        final int opcode = instruction.opcode();
        switch (opcode) {
            case ILOAD, LLOAD, FLOAD, DLOAD, ALOAD -> {
                // Check if the index is a known null value, or actually a non-existent local.
                ClassType type = frame.getLocalType(index);
                if (type == null && frame.getLocal(index) == null) {
                    // We only adapt these loads if the value is a truly unknown local.
                    // If the local is known to be null, we keep it as-is.
                    type = switch (opcode) {
                        case ILOAD -> Types.INT;
                        case LLOAD -> Types.LONG;
                        case FLOAD -> Types.FLOAT;
                        case DLOAD -> Types.DOUBLE;
                        case ALOAD -> Types.OBJECT;
                        default -> throw new IllegalStateException("Unexpected opcode: " + opcode);
                    };
                }
                frame.pushType(type);
            }
            case ISTORE, LSTORE, FSTORE, DSTORE, ASTORE -> {
                String name = variableNameLookup.getVarName(index);
                ClassType type = opcode == LSTORE || opcode == DSTORE ? frame.pop2() : frame.pop();
                frame.setLocal(index, new Local(index, name, type));
            }
        }
    }

    @Override
    public void execute(InstanceofInstruction instruction) {
        frame.pop();
        frame.pushType(Types.INT);
    }

    @Override
    public void execute(CheckCastInstruction instruction) {
        frame.pop();
        frame.pushType(instruction.type());
    }

    @Override
    public void execute(MethodInstruction instruction) {
        MethodType methodType = instruction.type();
        List<ClassType> types = methodType.parameterTypes();
        for (ClassType type : types)
            frame.pop(type);
        if (instruction.opcode() != INVOKESTATIC)
            frame.pop();
        if (methodType.returnType() != Types.VOID)
            frame.pushType(methodType.returnType());
    }

    @Override
    public void execute(FieldInstruction instruction) {
        int opcode = instruction.opcode();
        switch (opcode) {
            case GETFIELD -> {
                frame.pop();
                frame.pushType(instruction.type());
            }
            case GETSTATIC -> frame.pushType(instruction.type());
            case PUTFIELD -> {
                frame.pop(instruction.type());
                frame.pop();
            }
            case PUTSTATIC -> frame.pop(instruction.type());
            default -> throw new IllegalStateException("Unknown field insn: " + opcode);
        }
    }

    @Override
    public void execute(InvokeDynamicInstruction instruction) {
        MethodType type = (MethodType) instruction.type();
        List<ClassType> types = type.parameterTypes();
        for (ClassType classType : types)
            frame.pop(classType);
        if (type.returnType() != Types.VOID)
            frame.pushType(type.returnType());
    }

    @Override
    public void execute(PrimitiveConversionInstruction instruction) {
        frame.pop(instruction.from());
        frame.pushType(instruction.to());
    }

    @Override
    public void execute(LookupSwitchInstruction instruction) {
        frame.pop();
    }

    @Override
    public void execute(TableSwitchInstruction instruction) {
        frame.pop();
    }

    @Override
    public void execute(VariableIncrementInstruction instruction) {
        // no-op
    }
}
