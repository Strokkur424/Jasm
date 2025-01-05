package me.darknet.assembler.compile.analysis.func;

import me.darknet.assembler.compile.analysis.Value;
import me.darknet.assembler.compile.analysis.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Static {@code float method(float)} executor.
 */
public interface F2FStaticFunc extends StaticFunc {
    float apply(float f);

    @Override
    @Nullable
    default Value apply(@NotNull List<Value> params) {
        if (params.size() == 1 &&
                params.getFirst() instanceof Value.KnownFloatValue a)
            return Values.valueOf(apply(a.value()));
        return Values.FLOAT_VALUE;
    }
}
