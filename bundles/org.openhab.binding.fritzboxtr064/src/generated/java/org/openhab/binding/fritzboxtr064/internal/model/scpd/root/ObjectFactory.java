
package org.openhab.binding.fritzboxtr064.internal.model.scpd.root;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openhab.binding.fritzboxtr064.internal.model.scpd.root package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Root_QNAME = new QName("urn:dslforum-org:device-1-0", "root");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openhab.binding.fritzboxtr064.internal.model.scpd.root
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SCPDRootType }
     * 
     */
    public SCPDRootType createSCPDRootType() {
        return new SCPDRootType();
    }

    /**
     * Create an instance of {@link SCPDSpecVersionType }
     * 
     */
    public SCPDSpecVersionType createSCPDSpecVersionType() {
        return new SCPDSpecVersionType();
    }

    /**
     * Create an instance of {@link SCPDSystemVersionType }
     * 
     */
    public SCPDSystemVersionType createSCPDSystemVersionType() {
        return new SCPDSystemVersionType();
    }

    /**
     * Create an instance of {@link SCPDIconType }
     * 
     */
    public SCPDIconType createSCPDIconType() {
        return new SCPDIconType();
    }

    /**
     * Create an instance of {@link SCPDIconListType }
     * 
     */
    public SCPDIconListType createSCPDIconListType() {
        return new SCPDIconListType();
    }

    /**
     * Create an instance of {@link SCPDServiceType }
     * 
     */
    public SCPDServiceType createSCPDServiceType() {
        return new SCPDServiceType();
    }

    /**
     * Create an instance of {@link SCPDDeviceType }
     * 
     */
    public SCPDDeviceType createSCPDDeviceType() {
        return new SCPDDeviceType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SCPDRootType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link SCPDRootType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:dslforum-org:device-1-0", name = "root")
    public JAXBElement<SCPDRootType> createRoot(SCPDRootType value) {
        return new JAXBElement<SCPDRootType>(_Root_QNAME, SCPDRootType.class, null, value);
    }

}
