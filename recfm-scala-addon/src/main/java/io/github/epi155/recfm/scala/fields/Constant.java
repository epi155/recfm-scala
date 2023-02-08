package io.github.epi155.recfm.scala.fields;

import io.github.epi155.recfm.type.FieldConstant;
import io.github.epi155.recfm.util.ImmutableField;
import io.github.epi155.recfm.util.IndentPrinter;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.epi155.recfm.scala.ScalaTools.prefixOf;

public class Constant extends IndentPrinter implements ImmutableField<FieldConstant> {
    private final String name;

    public Constant(PrintWriter pw, String name) {
        super(pw);
        this.name = name;
    }

    @Override
    public void initialize(@NotNull FieldConstant fld, int bias) {
        printf("    fill(%5d, %4d, %s.VALUE_AT%dPLUS%d)%n",
            fld.getOffset() - bias, fld.getLength(), name, fld.getOffset(), fld.getLength());
    }

    @Override
    public void validate(@NotNull FieldConstant fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.getAndSet(false));
        printf("%s checkEqual(\"VALUE\"%s, %5d, %4d, handler, %s.VALUE_AT%dPLUS%d)%n", prefix, fld.pad(5, w),
            fld.getOffset() - bias, fld.getLength(), name, fld.getOffset(), fld.getLength());
    }

}
