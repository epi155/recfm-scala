package io.github.epi155.recfm.scala;

import io.github.epi155.recfm.type.NakedField;
import org.jetbrains.annotations.NotNull;

public interface ScalaDoc {
    default void docSetter(@NotNull NakedField fld) {
        printf("/**%n");
        printf(" * %s @%d+%d%n", tag(), fld.getOffset(), fld.getLength());
        printf(" * @param s string value%n");
        printf(" */%n");
    }

    default void docGetter(@NotNull NakedField fld) {
        printf("/**%n");
        printf(" * %s @%d+%d%n", tag(), fld.getOffset(), fld.getLength());
        printf(" * @return string value%n");
        printf(" */%n");
    }

    String tag();

    void printf(String format, Object... args);

}
