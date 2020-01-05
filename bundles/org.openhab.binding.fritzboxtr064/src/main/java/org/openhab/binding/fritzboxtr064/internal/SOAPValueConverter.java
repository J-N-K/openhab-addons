/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.fritzboxtr064.internal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.fritzboxtr064.internal.config.Tr064ChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import static org.openhab.binding.fritzboxtr064.internal.Tr064BindingConstants.REQUEST_TIMEOUT;

/**
 * The {@link SOAPValueConverter} converts SOAP values and openHAB states
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SOAPValueConverter {
    private final Logger logger = LoggerFactory.getLogger(SOAPValueConverter.class);
    private final HttpClient httpClient;

    public SOAPValueConverter(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * convert an openHAB command to a SOAP value
     *
     * @param command the command to be converted
     * @param dataType the datatype to send
     * @param unit if available, the unit of the converted value
     * @return a string optional containing the converted value
     */
    public Optional<String> getSOAPValueFromCommand(Command command, String dataType, String unit) {
        if (dataType.isEmpty()) {
            // we don't have data to send
            return Optional.of("");
        }
        if (command instanceof QuantityType) {
            QuantityType<?> value = (unit.isEmpty()) ? ((QuantityType<?>) command)
                    : ((QuantityType<?>) command).toUnit(unit);
            if (value == null) {
                logger.info("Could not convert {} to unit {}", command, unit);
                return Optional.empty();
            }
            switch (dataType) {
                case "ui2":
                    return Optional.of(String.valueOf(value.shortValue()));
                case "ui4":
                    return Optional.of(String.valueOf(value.intValue()));
                default:
            }
        } else if (command instanceof DecimalType) {
            BigDecimal value = ((DecimalType) command).toBigDecimal();
            switch (dataType) {
                case "ui2":
                    return Optional.of(String.valueOf(value.shortValue()));
                case "ui4":
                    return Optional.of(String.valueOf(value.intValue()));
                default:
            }
        } else if (command instanceof StringType) {
            if (dataType.equals("string")) {
                return Optional.of(command.toString());
            }
        } else if (command instanceof OnOffType) {
            if (dataType.equals("boolean"))
                return Optional.of(OnOffType.ON.equals(command) ? "1" : "0");
        }
        return Optional.empty();
    }

    /**
     * convert the value from a SOAP message to a openHAB value
     *
     * @param soapMessage the inbound SSOAP message
     * @param element the element that needs to be extracted
     * @param channelConfig the channel config containing additional information (if null a data-type "string" and
     *            missing unit is assumed)
     * @return an Optional of State conating the converted value
     */
    @SuppressWarnings("null")
    public Optional<State> getStateFromSOAPValue(SOAPMessage soapMessage, String element,
            @Nullable Tr064ChannelConfig channelConfig) {
        String dataType = channelConfig != null ? channelConfig.getDataType() : "string";
        String unit = channelConfig != null ? channelConfig.getChannelType().getItem().getUnit() : "";
        State returnState = null;

        try {
            NodeList nodeList = soapMessage.getSOAPBody().getElementsByTagName(element);
            if (nodeList != null && nodeList.getLength() > 0) {
                String rawValue = nodeList.item(0).getTextContent();
                switch (dataType) {
                    case "boolean":
                        returnState = rawValue.equals("0") ? OnOffType.OFF : OnOffType.ON;
                        break;
                    case "string":
                        returnState = new StringType(rawValue);
                        break;
                    case "ui2":
                    case "ui4":
                        if (!unit.isEmpty()) {
                            returnState = new QuantityType<>(rawValue + " " + unit);
                        } else {
                            returnState = new DecimalType(rawValue);
                        }
                        break;
                    default:
                }
            }

            // check if we need post processing
            if (returnState != null && channelConfig != null
                    && channelConfig.getChannelType().getGetAction().getPostProcessor() != null) {
                String postProcessor = channelConfig.getChannelType().getGetAction().getPostProcessor();
                try {
                    Method method = SOAPValueConverter.class.getDeclaredMethod(postProcessor, State.class,
                            Tr064ChannelConfig.class);
                    Object o = method.invoke(this, returnState, channelConfig);
                    if (o instanceof State) {
                        returnState = (State) o;
                    }
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    logger.warn("Postprocessor {} not found, this most likely is a programming error", postProcessor,
                            e);
                } catch (InvocationTargetException e) {
                    logger.info("Postprocessor {} failed: {}", postProcessor, e.getCause().getMessage());
                }
            }
        } catch (SOAPException e) {
            logger.warn("Could not get expected element {} from SOAP message {}", element, soapMessage);
        }
        return Optional.ofNullable(returnState);
    }

    /**
     * post processor for answering machine new messages channel
     *
     * @param state the message list URL
     * @param channelConfig channel config of the TAM new message channel
     * @return the number of new messages
     * @throws PostProcessingException if the message list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processTamListURL(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(state.toString())).GET()
                    .timeout(REQUEST_TIMEOUT).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int messageCount = response.body().split("<New>1</New>").length - 1;

            return new DecimalType(messageCount);
        } catch (InterruptedException | IOException e) {
            throw new PostProcessingException("Failed to get TAM list from URL " + state.toString());
        }
    }

    /**
     * post processor for missed calls
     *
     * @param state the call list URL
     * @param channelConfig channel config of the missed call channel (contains day number)
     * @return the number of missed calls
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processMissedCalls(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), "2");
    }

    /**
     * post processor for inbound calls
     *
     * @param state the call list URL
     * @param channelConfig channel config of the inbound call channel (contains day number)
     * @return the number of inbound calls
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processInboundCalls(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), "1");
    }

    /**
     * post processor for rejected calls
     *
     * @param state the call list URL
     * @param channelConfig channel config of the rejected call channel (contains day number)
     * @return the number of rejected calls
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processRejectedCalls(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), "3");
    }

    /**
     * post processor for outbound calls
     *
     * @param state the call list URL
     * @param channelConfig channel config of the outbound call channel (contains day number)
     * @return the number of outbound calls
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processOutboundCalls(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), "4");
    }

    /**
     * internal helper for call list post processors
     *
     * @param state the call list URL
     * @param days number of days to get
     * @param type type of call (1=missed 2=inbound 3=rejected 4=outbund)
     * @return the quantity of calls of the given type within the given number of days
     * @throws PostProcessingException if the call list could not be retrjeved
     */
    private State processCallList(State state, @Nullable String days, String type) throws PostProcessingException {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(state.toString() + "&days=" + days)).GET()
                    .timeout(REQUEST_TIMEOUT).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int callCount = response.body().split("<Type>" + type + "</Type>").length - 1;

            return new DecimalType(callCount);
        } catch (InterruptedException | IOException e) {
            throw new PostProcessingException("Failed to get Call list from URL " + state.toString());
        }
    }
}
