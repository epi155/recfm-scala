package io.github.epi155.recfm.scala.factory;

import io.github.epi155.recfm.scala.fields.*;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.InitializeField;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;

@Slf4j
public class InitializeFactory {
    private final InitializeField<FieldAbc> delegateAbc;
    private final InitializeField<FieldNum> delegateNum;
    private final InitializeField<FieldCustom> delegateCus;
    private final InitializeField<FieldFiller> delegateFil;
    private final InitializeField<FieldConstant> delegateVal;
    private final InitializeField<FieldDomain> delegateDom;

    public InitializeFactory(PrintWriter pw, ClassDefine struct, Defaults defaults) {
        this.delegateAbc = new Abc(pw, defaults.getAbc());
        this.delegateNum = new Num(pw);
        this.delegateCus = new Custom(pw, defaults.getCus(), struct.getName());
        this.delegateDom = new Domain(pw, struct.getName());
        this.delegateFil = new Filler(pw, defaults.getFil());
        this.delegateVal = new Constant(pw, struct.getName());
    }

    protected void initializeDom(FieldDomain fld, int bias) {
        delegateDom.initialize(fld, bias);
    }

    protected void initializeCus(FieldCustom fld, int bias) {
        if (fld.isRedefines()) return;
        delegateCus.initialize(fld, bias);
    }

    protected void initializeGrp(FieldGroup fld, int bias) {
        if (fld.isRedefines()) return;
        fld.getFields().forEach(it -> field(it, bias));
    }

    protected void initializeOcc(FieldOccurs fld, int bias) {
        if (fld.isRedefines()) return;
        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
            int backShift = shift;
            fld.getFields().forEach(it -> field(it, bias - backShift));
        }
    }

    protected void initializeFil(FieldFiller fld, int bias) {
        delegateFil.initialize(fld, bias);
    }

    protected void initializeVal(FieldConstant fld, int bias) {
        delegateVal.initialize(fld, bias);
    }

    protected void initializeNum(FieldNum fld, int bias) {
        if (fld.isRedefines()) return;
        delegateNum.initialize(fld, bias);
    }

    protected void initializeAbc(FieldAbc fld, int bias) {
        if (fld.isRedefines()) return;
        delegateAbc.initialize(fld, bias);
    }

    public void field(NakedField fld, int bias) {
        if (fld instanceof FieldAbc) {
            initializeAbc((FieldAbc) fld, bias);
        } else if (fld instanceof FieldNum) {
            initializeNum((FieldNum) fld, bias);
        } else if (fld instanceof FieldCustom) {
            initializeCus((FieldCustom) fld, bias);
        } else if (fld instanceof FieldDomain) {
            initializeDom((FieldDomain) fld, bias);
        } else if (fld instanceof FieldConstant) {
            initializeVal((FieldConstant) fld, bias);
        } else if (fld instanceof FieldFiller) {
            initializeFil((FieldFiller) fld, bias);
        } else if (fld instanceof FieldOccurs) {
            initializeOcc((FieldOccurs) fld, bias);
        } else if (fld instanceof FieldGroup) {
            initializeGrp((FieldGroup) fld, bias);
        } else {
            log.warn("Unknown field type {}", fld.getClass().getSimpleName());
        }
    }
}
