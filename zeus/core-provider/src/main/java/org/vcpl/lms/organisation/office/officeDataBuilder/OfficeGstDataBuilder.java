package org.vcpl.lms.organisation.office.officeDataBuilder;

import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.organisation.office.data.OfficeGstData;

import java.math.BigDecimal;

public class OfficeGstDataBuilder {

    private  Long id;
    private  String gstNumber;
    private  CodeValueData state;
    private  BigDecimal cgst;
    private  BigDecimal sgst;
    private  BigDecimal igst;

    public OfficeGstDataBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public OfficeGstDataBuilder setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
        return this;
    }

    public OfficeGstDataBuilder setState(CodeValueData state) {
        this.state = state;
        return this;
    }

    public OfficeGstDataBuilder setCgst(BigDecimal cgst) {
        this.cgst = cgst;
        return this;
    }

    public OfficeGstDataBuilder setSgst(BigDecimal sgst) {
        this.sgst = sgst;
        return this;
    }

    public OfficeGstDataBuilder setIgst(BigDecimal igst) {
        this.igst = igst;
        return this;
    }

    public OfficeGstData getOfficeGstData(){
        return new OfficeGstData( id, gstNumber, state, cgst,sgst, igst);
    }
}
