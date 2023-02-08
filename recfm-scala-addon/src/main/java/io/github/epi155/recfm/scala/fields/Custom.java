package io.github.epi155.recfm.scala.fields;

import io.github.epi155.recfm.scala.ScalaDoc;
import io.github.epi155.recfm.type.Defaults;
import io.github.epi155.recfm.type.FieldCustom;
import io.github.epi155.recfm.util.GenerateArgs;
import io.github.epi155.recfm.util.IndentPrinter;
import io.github.epi155.recfm.util.MutableField;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.scala.ScalaTools.prefixOf;
import static io.github.epi155.recfm.util.Tools.notNullOf;

public class Custom extends IndentPrinter implements MutableField<FieldCustom>, ScalaDoc {
    private final String name;
    private final Defaults.CusDefault defaults;

    public Custom(PrintWriter pw, Defaults.CusDefault defaults, String name) {
        super(pw);
        this.defaults = defaults;
        this.name = name;
    }

    public Custom(PrintWriter pw, Defaults.CusDefault defaults, IntFunction<String> pos, String name) {
        super(pw, pos);
        this.defaults = defaults;
        this.name = name;
    }

    @Override
    public void initialize(@NotNull FieldCustom fld, int bias) {
        val init = notNullOf(fld.getInitChar(), defaults.getInit());
        printf("    fill(%5d, %4d, '%c')%n", fld.getOffset() - bias, fld.getLength(), init);
    }

    @Override
    public void validate(@NotNull FieldCustom fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.get());
        if (fld.getRegex() != null) {
            printf("%s checkRegex(\"%s\"%s, %5d, %4d, handler, %s.PATTERN_AT%dPLUS%d)%n", prefix,
                fld.getName(), fld.pad(w),
                fld.getOffset() - bias, fld.getLength(),
                name, fld.getOffset(), fld.getLength()
            );
            isFirst.set(false);
            return;
        }
        switch (notNullOf(fld.getCheck(), defaults.getCheck())) {
            case None:
                break;
            case Ascii:
                printf("%s checkAscii(\"%s\"%s, %5d, %4d, handler)%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case Latin1:
                printf("%s checkLatin(\"%s\"%s, %5d, %4d, handler)%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case Valid:
                printf("%s checkValid(\"%s\"%s, %5d, %4d, handler)%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case Digit:
                printf("%s checkDigit(\"%s\"%s, %5d, %4d, handler)%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case DigitOrBlank:
                printf("%s checkDigitBlank(\"%s\"%s, %5d, %4d, handler)%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
        }
    }

    @Override
    public void access(FieldCustom fld, String wrkName, int indent, GenerateArgs ga) {
        pushIndent(indent);
        buildGetter(fld, ga);
        buildSetter(fld, ga);
        popIndent();
    }

    private void buildGetter(FieldCustom fld, GenerateArgs ga) {
        if (ga.doc) docGetter(fld);
        printf("final def %s: String = {%n", fld.getName());
        if (ga.getCheck) chkGetter(fld);
        printf("  abc(%s, %d)%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
    }

    private void buildSetter(FieldCustom fld, GenerateArgs ga) {
        if (ga.doc) docSetter(fld);
        printf("final def %s_=(s: String): Unit = {%n", fld.getName());
        val align = notNullOf(fld.getAlign(), defaults.getAlign());
        val pad = notNullOf(fld.getPadChar(), defaults.getPad());
        val init = notNullOf(fld.getInitChar(), defaults.getInit());
        if (ga.setCheck) {
            printf("  val r = normalize(s, OverflowAction.%s, UnderflowAction.%s, '%c', '%c', %s, %d)%n",
                fld.safeOverflow().of(align), fld.safeUnderflow().of(align), pad, init,
                pos.apply(fld.getOffset()), fld.getLength()
            );
            chkSetter(fld);
            printf("  abc(r, %s, %d)%n",
                pos.apply(fld.getOffset()), fld.getLength(), fld.safeOverflow().of(align), fld.safeUnderflow().of(align), fld.getPadChar());
        } else {
            printf("  abc(s, %s, %d, OverflowAction.%s, UnderflowAction.%s, '%c', '%s')%n",
                pos.apply(fld.getOffset()), fld.getLength(),
                fld.safeOverflow().of(align), fld.safeUnderflow().of(align), pad, init);
        }
        printf("}%n");
    }

    private void chkSetter(FieldCustom fld) {
        if (fld.getRegex() != null) {
            printf("  testRegex(r, %s.PATTERN_AT%sPLUS%d)%n", name, pos.apply(fld.getOffset() + 1), fld.getLength());
            return;
        }
        switch (notNullOf(fld.getCheck(), defaults.getCheck())) {
            case None:
                break;
            case Ascii:
                printf("  testAscii(r)%n");
                break;
            case Latin1:
                printf("  testLatin(r)%n");
                break;
            case Valid:
                printf("  testValid(r)%n");
                break;
            case Digit:
                printf("  testDigit(r)%n");
                break;
            case DigitOrBlank:
                printf("  testDigitBlank(r)%n");
                break;
        }
    }

    private void chkGetter(FieldCustom fld) {
        if (fld.getRegex() != null) {
            printf("  testRegex(%1$s, %2$d, %3$s.PATTERN_AT%4$sPLUS%2$d)%n",
                pos.apply(fld.getOffset()), fld.getLength(), name, pos.apply(fld.getOffset() + 1));
            return;
        }
        switch (notNullOf(fld.getCheck(), defaults.getCheck())) {
            case None:
                break;
            case Ascii:
                printf("  testAscii(%s, %d)%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
            case Latin1:
                printf("  testLatin(%s, %d)%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
            case Valid:
                printf("  testValid(%s, %d)%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
            case Digit:
                printf("  testDigit(%s, %d)%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
            case DigitOrBlank:
                printf("  testDigitBlank(%s, %d)%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
        }
    }

    @Override
    public String tag() {
        return "Cus";
    }
}
