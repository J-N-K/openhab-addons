
package org.openhab.binding.fritzboxtr064.internal.model.scpd.root;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for specVersionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="specVersionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="major" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *         &lt;element name="minor" type="{http://www.w3.org/2001/XMLSchema}byte"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "specVersionType", propOrder = {
    "major",
    "minor"
})
public class SCPDSpecVersionType implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected byte major;
    protected byte minor;

    /**
     * Gets the value of the major property.
     * 
     */
    public byte getMajor() {
        return major;
    }

    /**
     * Sets the value of the major property.
     * 
     */
    public void setMajor(byte value) {
        this.major = value;
    }

    /**
     * Gets the value of the minor property.
     * 
     */
    public byte getMinor() {
        return minor;
    }

    /**
     * Sets the value of the minor property.
     * 
     */
    public void setMinor(byte value) {
        this.minor = value;
    }

}
