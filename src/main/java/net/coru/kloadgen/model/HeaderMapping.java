package net.coru.kloadgen.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.jmeter.testelement.AbstractTestElement;

@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HeaderMapping extends AbstractTestElement {

    public static final String HEADER_NAME = "headerName";

    public static final String HEADER_VALUE = "headerValue";

    private String headerName;

    private String headerValue;

    public HeaderMapping(String headerName, String headerValue) {
        this.setHeaderName(headerName);
        this.setHeaderValue(headerValue);
        init();
    }

    public String getHeaderName() {
        return getPropertyAsString(HEADER_NAME);
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
        setProperty(HEADER_NAME, headerName);
    }

    public String getHeaderValue() {
        return getPropertyAsString(HEADER_VALUE);
    }

    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
        setProperty(HEADER_VALUE, headerValue);
    }

    public void init() {
        this.setName("Header Field");
    }
}
