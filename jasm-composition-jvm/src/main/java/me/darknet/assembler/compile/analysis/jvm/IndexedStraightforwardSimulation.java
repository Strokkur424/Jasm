package me.darknet.assembler.compile.analysis.jvm;

import dev.xdark.blw.code.Code;
import dev.xdark.blw.code.CodeElement;
import dev.xdark.blw.code.Instruction;
import dev.xdark.blw.code.Label;
import dev.xdark.blw.simulation.ExecutionEngines;
import dev.xdark.blw.simulation.Simulation;

import java.util.List;

/**
 * A simulation which visits each {@link CodeElement} of a method in a linear
 * fashion. The simulation is not for proper stack/local analysis.
 */
public class IndexedStraightforwardSimulation implements Simulation<IndexedExecutionEngine, Code> {
    @Override
    public void execute(IndexedExecutionEngine engine, Code code) {
        if (code == null) throw new IllegalStateException("Cannot analyze 'null' code");
        List<CodeElement> elements = code.elements();
        for (int i = 0; i < elements.size(); i++) {
            engine.index(i);
            CodeElement element = elements.get(i);
            if (element instanceof Label label) {
                engine.label(label);
            } else if (element instanceof Instruction instruction) {
                ExecutionEngines.execute(engine, instruction);
            }
        }
    }
}
