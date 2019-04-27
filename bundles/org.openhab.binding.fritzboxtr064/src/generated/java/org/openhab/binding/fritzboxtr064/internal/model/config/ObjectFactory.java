
package org.openhab.binding.fritzboxtr064.internal.model.config;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openhab.binding.fritzboxtr064.internal.model.config package. 
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

    private final static QName _Channels_QNAME = new QName("channelconfig", "channels");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openhab.binding.fritzboxtr064.internal.model.config
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ChannelsType }
     * 
     */
    public ChannelsType createChannelsType() {
        return new ChannelsType();
    }

    /**
     * Create an instance of {@link ItemType }
     * 
     */
    public ItemType createItemType() {
        return new ItemType();
    }

    /**
     * Create an instance of {@link ServiceType }
     * 
     */
    public ServiceType createServiceType() {
        return new ServiceType();
    }

    /**
     * Create an instance of {@link ParameterType }
     * 
     */
    public ParameterType createParameterType() {
        return new ParameterType();
    }

    /**
     * Create an instance of {@link ActionType }
     * 
     */
    public ActionType createActionType() {
        return new ActionType();
    }

    /**
     * Create an instance of {@link ChannelType }
     * 
     */
    public ChannelType createChannelType() {
        return new ChannelType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChannelsType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ChannelsType }{@code >}
     */
    @XmlElementDecl(namespace = "channelconfig", name = "channels")
    public JAXBElement<ChannelsType> createChannels(ChannelsType value) {
        return new JAXBElement<ChannelsType>(_Channels_QNAME, ChannelsType.class, null, value);
    }

}
