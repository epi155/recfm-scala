package io.github.epi155.recfm.scala.fields;

import io.github.epi155.recfm.type.FieldDomain;
import io.github.epi155.recfm.util.AbstractPrinter;
import io.github.epi155.recfm.util.PrepareField;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PreDomain extends AbstractPrinter implements PrepareField<FieldDomain> {
    public PreDomain(PrintWriter pw) {
        super(pw);
    }

    @Override
    public void prepare(@NotNull FieldDomain fld, int bias) {
        val items = fld.getItems();
        printf("  private val VALUE_AT%dPLUS%d = \"%s\";%n",
                fld.getOffset(), fld.getLength(), StringEscapeUtils.escapeJava(items[0]));
        val works = Arrays.asList(items);
        val domain = works.stream()
                .sorted()
                .map(it -> "\"" + StringEscapeUtils.escapeJava(it) + "\"")
                .collect(Collectors.joining(","));
        printf("  private val DOMAIN_AT%dPLUS%d = Array( %s )%n", fld.getOffset(), fld.getLength(), domain);
    }

}
