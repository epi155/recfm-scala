package io.github.epi155.recfm.scala.fields;

import io.github.epi155.recfm.scala.ScalaDoc;
import io.github.epi155.recfm.type.FieldDomain;
import io.github.epi155.recfm.util.GenerateArgs;
import io.github.epi155.recfm.util.IndentPrinter;
import io.github.epi155.recfm.util.MutableField;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.scala.ScalaTools.prefixOf;

public class Domain extends IndentPrinter implements MutableField<FieldDomain>, ScalaDoc {
    private final String name;

    public Domain(PrintWriter pw, IntFunction<String> pos, String name) {
        // access
        super(pw, pos);
        this.name = name;
    }

    public Domain(PrintWriter pw, String name) {
        super(pw);
        this.name = name;
    }

    @Override
    public void access(FieldDomain fld, String wrkName, int indent, GenerateArgs ga) {
        pushIndent(indent);
        if (ga.doc) docGetter(fld);
        printf("final def %s: String = {%n", fld.getName());
        if (ga.getCheck) chkGetter(fld);
        printf("  abc(%s, %d)%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (ga.doc) docSetter(fld);
        printf("final def %s_=(s: String): Unit = {%n", fld.getName());
        if (ga.setCheck) chkSetter(fld);
        printf("  abc(s, %s, %d)%n",
            pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        popIndent();
    }

    private void chkGetter(@NotNull FieldDomain fld) {
        printf("  testArray(%1$s, %2$d, %3$s.DOMAIN_AT%4$sPLUS%2$d)%n",
            pos.apply(fld.getOffset()), fld.getLength(),
            name, pos.apply(fld.getOffset()+1));
    }

    private void chkSetter(@NotNull FieldDomain fld) {
        printf("  testArray(s, %s.DOMAIN_AT%sPLUS%d)%n",
            name, pos.apply(fld.getOffset()+1), fld.getLength());
    }

    @Override
    public void initialize(@NotNull FieldDomain fld, int bias) {
        printf("    fill(%5d, %4d, %s.VALUE_AT%dPLUS%d);%n",
            fld.getOffset() - bias, fld.getLength(), name, fld.getOffset(), fld.getLength());
    }

    @Override
    public void validate(@NotNull FieldDomain fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.getAndSet(false));
        printf("%s checkArray(\"%s\"%s, %5d, %4d, handler, %s.DOMAIN_AT%dPLUS%d)%n", prefix,
            fld.getName(), fld.pad(w),
            fld.getOffset() - bias, fld.getLength(),
            name, fld.getOffset(), fld.getLength()
        );

    }

    @Override
    public String tag() {
        return "Dom";
    }
}
