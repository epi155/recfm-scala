package io.github.epi155.recfm.scala.fields;

import io.github.epi155.recfm.type.FieldConstant;
import io.github.epi155.recfm.util.AbstractPrinter;
import io.github.epi155.recfm.util.PrepareField;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

public class PreConstant extends AbstractPrinter implements PrepareField<FieldConstant> {
    public PreConstant(PrintWriter pw) {
        super(pw);
    }

    @Override
    public void prepare(@NotNull FieldConstant fld, int bias) {
        printf("  private val VALUE_AT%dPLUS%d = \"%s\";%n",
                fld.getOffset(), fld.getLength(), StringEscapeUtils.escapeJava(fld.getValue()));

    }
}
