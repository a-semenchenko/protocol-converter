package dev.semenchenko.jaxb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * Created by a.semenchenko on 19.06.2017.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="Envelope")
public class Envelope {
    @XmlElement
    private int token;
    @XmlElement
    private int cardNumber;
    @XmlElement
    private long requestId;
    @XmlElement
    private BigDecimal amount;
    @XmlElement
    private String currency;
}