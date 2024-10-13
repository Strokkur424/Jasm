package me.darknet.assembler.ast.specific;

import me.darknet.assembler.ast.ASTElement;
import me.darknet.assembler.ast.ElementType;
import me.darknet.assembler.ast.primitive.ASTCode;
import me.darknet.assembler.ast.primitive.ASTIdentifier;
import me.darknet.assembler.ast.primitive.ASTInstruction;
import me.darknet.assembler.ast.primitive.ASTLabel;
import me.darknet.assembler.error.ErrorCollector;
import me.darknet.assembler.instructions.Instruction;
import me.darknet.assembler.parser.BytecodeFormat;
import me.darknet.assembler.visitor.ASTInstructionVisitor;
import me.darknet.assembler.visitor.ASTMethodVisitor;
import me.darknet.assembler.visitor.Modifiers;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ASTMethod extends ASTMember {

    private final List<ASTIdentifier> parameters;
    private final List<ASTException> exceptions;
    private final ASTElement defaultValue;
    private final ASTCode code;
    private final List<Instruction<?>> instructions;
    private final BytecodeFormat format;

    public ASTMethod(Modifiers modifiers, ASTIdentifier name, ASTIdentifier descriptor, List<ASTIdentifier> parameters,
                     ASTElement defaultValue, List<ASTException> exceptions, ASTCode code,
                     List<Instruction<?>> instructions, BytecodeFormat format) {
        super(ElementType.METHOD, modifiers, name, descriptor);
        this.parameters = parameters;
        this.exceptions = exceptions;
        this.defaultValue = defaultValue;
        this.code = code;
        this.instructions = instructions;
        this.format = format;
        addChildren(parameters);
        addChildren(exceptions);
        if (defaultValue != null) addChild(defaultValue);
        if (code != null) addChild(code);
    }

    public @Nullable ASTElement getAnnotationDefaultValue() {
        return defaultValue;
    }

    public List<ASTIdentifier> parameters() {
        return parameters;
    }

    public List<ASTException> exceptions() {
        return exceptions;
    }

    public ASTCode code() {
        return code;
    }

    public List<Instruction<?>> instructions() {
        return instructions;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public void accept(ErrorCollector collector, ASTMethodVisitor visitor) {
        super.accept(collector, visitor);
        List<ASTIdentifier> localParams = parameters;
        for (int i = 0; i < localParams.size(); i++) {
            visitor.visitParameter(i, localParams.get(i));
        }
        if (this.defaultValue != null) {
            visitor.visitAnnotationDefaultValue(defaultValue);
        }
        if (this.code == null) {
            visitor.visitEnd();
            return;
        }

        ASTInstructionVisitor instructionVisitor = switch (format) {
            case JVM -> visitor.visitJvmCode(collector);
            case DALVIK -> null;
        };
        if (instructionVisitor != null) {
            int instructionIndex = 0;
            List<ASTInstruction> localAstInstructions = code.instructions();
            List<Instruction<?>> localIrInstructions = instructions;
            for (ASTInstruction instruction : localAstInstructions) {
                if (instruction instanceof ASTLabel lab) {
                    instructionVisitor.visitLabel(lab.identifier());
                } else {
                    if (!instruction.identifier().content().equals("line"))
                        instructionVisitor.visitInstruction(instruction);
                    localIrInstructions.get(instructionIndex++).transform(instruction, instructionVisitor);
                }
            }

            for (ASTException exception : exceptions) {
                instructionVisitor.visitException(
                        exception.start(), exception.end(), exception.handler(), exception.exceptionType()
                );
            }

            instructionVisitor.visitEnd();
        }

        visitor.visitEnd();
    }
}
