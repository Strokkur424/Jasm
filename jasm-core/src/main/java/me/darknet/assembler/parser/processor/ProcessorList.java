package me.darknet.assembler.parser.processor;

import me.darknet.assembler.ast.ASTElement;
import me.darknet.assembler.ast.primitive.ASTIdentifier;
import me.darknet.assembler.ast.primitive.ASTString;
import me.darknet.assembler.ast.specific.ASTAnnotation;
import me.darknet.assembler.ast.specific.ASTInner;

import java.util.ArrayList;
import java.util.List;

public class ProcessorList {

    private final List<ASTElement> result = new ArrayList<>();

    private ProcessorAttributes attributes = new ProcessorAttributes();

    public void add(ASTElement element) {
        result.add(element);
    }

    public List<ASTElement> getResult() {
        return result;
    }

    public ProcessorAttributes collectAttributes() {
        ProcessorAttributes attributes = this.attributes;
        this.attributes = new ProcessorAttributes();

        result.removeAll(attributes.attributes);

        return attributes;
    }

    private void addAttribute(ASTElement element) {
        attributes.attributes.add(element);
    }

    // mutators

    public void addAnnotation(ASTAnnotation annotation) {
        this.attributes.annotations.add(annotation);
        addAttribute(annotation);
    }

    public void setSignature(ASTString signature) {
        this.attributes.signature = signature;
        addAttribute(signature);
    }

    public void addInterface(ASTIdentifier interfaceName) {
        this.attributes.interfaces.add(interfaceName);
        addAttribute(interfaceName);
    }

    public void setSuperName(ASTIdentifier superName) {
        this.attributes.superName = superName;
        addAttribute(superName);
    }

    public void addPermittedSubclass(ASTIdentifier subclassName) {
        this.attributes.permittedSubclasses.add(subclassName);
        addAttribute(subclassName);
    }

    public void addInner(ASTInner inner) {
        this.attributes.inners.add(inner);
        addAttribute(inner);
    }

    public void setSourceFile(ASTString sourceFile) {
        this.attributes.sourceFile = sourceFile;
        addAttribute(sourceFile);
    }

    public void addNestMember(ASTIdentifier nestMember) {
        this.attributes.nestMembers.add(nestMember);
        addAttribute(nestMember);
    }

    public void addNestHost(ASTIdentifier nestHost) {
        this.attributes.nestHosts.add(nestHost);
        addAttribute(nestHost);
    }

    public void removeAnnotation(ASTAnnotation value) {
        this.attributes.annotations.remove(value);
        this.attributes.attributes.remove(value);
    }

}
