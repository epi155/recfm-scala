package io.github.epi155.recfm.scala.factory;

import io.github.epi155.recfm.scala.fields.Abc;
import io.github.epi155.recfm.scala.fields.Custom;
import io.github.epi155.recfm.scala.fields.Domain;
import io.github.epi155.recfm.scala.fields.Num;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.AccessField;
import io.github.epi155.recfm.util.GenerateArgs;
import io.github.epi155.recfm.util.Tools;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.function.IntFunction;

/**
 * class that generates methods for accessing the fields in scala language.
 */
@Slf4j
public class AccessFactory {
    private final AccessField<FieldAbc> delegateAbc;
    private final AccessField<FieldNum> delegateNum;
    private final AccessField<FieldCustom> delegateCus;
    private final AccessField<FieldDomain> delegateDom;

    /**
     * Constructor
     *
     * @param pw  output writer
     * @param pos field offset to string form
     */
    public AccessFactory(PrintWriter pw, Defaults defaults, IntFunction<String> pos, String name) {
        delegateAbc = new Abc(pw, defaults.getAbc(), pos);
        delegateNum = new Num(pw, pos);
        delegateCus = new Custom(pw, defaults.getCus(), pos, name);
        delegateDom = new Domain(pw, pos, name);
    }

    protected void createMethodsDomain(FieldDomain fld, int indent, GenerateArgs ga) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateDom.access(fld, wrkName, indent, ga);
    }

    protected void createMethodsNum(@NotNull FieldNum fld, int indent, GenerateArgs ga) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateNum.access(fld, wrkName, indent, ga);
    }

    protected void createMethodsAbc(FieldAbc fld, int indent, GenerateArgs ga) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateAbc.access(fld, wrkName, indent, ga);
    }

    protected void createMethodsCustom(FieldCustom fld, int indent, GenerateArgs ga) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateCus.access(fld, wrkName, indent, ga);
    }

    public void createMethods(SettableField fld, int indent, GenerateArgs ga) {
        if (fld instanceof FieldAbc) {
            createMethodsAbc((FieldAbc) fld, indent, ga);
        } else if (fld instanceof FieldNum) {
            createMethodsNum((FieldNum) fld, indent, ga);
        } else if (fld instanceof FieldCustom) {
            createMethodsCustom((FieldCustom) fld, indent, ga);
        } else if (fld instanceof FieldDomain) {
            createMethodsDomain((FieldDomain) fld, indent, ga);
        } else {
            log.warn("Unknown field type {}: {}", fld.getName(), fld.getClass().getSimpleName());
        }
    }
}
