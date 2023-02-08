package io.github.epi155.recfm.scala;

public class ScalaTools {
    private ScalaTools() {
    }

    public static String prefixOf(boolean isFirst) {
        if (isFirst) {
            return "    var error =";
        } else {
            return "    error |=";
        }
    }
}
