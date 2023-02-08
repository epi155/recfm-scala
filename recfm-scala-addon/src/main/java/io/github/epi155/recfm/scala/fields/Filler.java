package io.github.epi155.recfm.scala.fields;

import io.github.epi155.recfm.type.Defaults;
import io.github.epi155.recfm.type.FieldFiller;
import io.github.epi155.recfm.util.ImmutableField;
import io.github.epi155.recfm.util.IndentPrinter;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.epi155.recfm.scala.ScalaTools.prefixOf;
import static io.github.epi155.recfm.util.Tools.notNullOf;

public class Filler extends IndentPrinter implements ImmutableField<FieldFiller> {
    private final Defaults.FilDefault defaults;

    public Filler(PrintWriter pw, Defaults.FilDefault defaults) {
        super(pw);
        this.defaults = defaults;
    }

    @Override
    public void initialize(@NotNull FieldFiller fld, int bias) {
        char c = fld.getFillChar() == null ? defaults.getFill() : fld.getFillChar();
        printf("    fill(%5d, %4d, '%s')%n",
            fld.getOffset() - bias, fld.getLength(), StringEscapeUtils.escapeJava(String.valueOf(c)));
    }

    @Override
    public void validate(@NotNull FieldFiller fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.get());
        switch (notNullOf(fld.getCheck(), defaults.getCheck())) {
            case None:
                break;
            case Ascii:
                printf("%s checkAscii(\"FILLER\"%s, %5d, %4d, handler)%n", prefix, fld.pad(6, w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case Latin1:
                printf("%s checkLatin(\"FILLER\"%s, %5d, %4d, handler)%n", prefix, fld.pad(6, w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case Valid:
                printf("%s checkValid(\"FILLER\"%s, %5d, %4d, handler)%n", prefix, fld.pad(6, w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
        }
    }

}
