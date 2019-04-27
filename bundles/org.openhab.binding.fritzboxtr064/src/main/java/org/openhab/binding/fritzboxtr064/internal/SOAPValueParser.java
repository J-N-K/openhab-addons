/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
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

/**
 * The {@link SOAPValueParser} is a set of helper functions for SOAP
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SOAPValueParser {
    private final Logger logger = LoggerFactory.getLogger(SOAPValueParser.class);

    private final HttpClient httpClient;

    public SOAPValueParser(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

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

    public Optional<State> getStateFromSOAPValue(SOAPMessage soapMessage, String element) {
        return getStateFromSOAPValue(soapMessage, element, null);
    }

    public Optional<State> getStateFromSOAPValue(SOAPMessage soapMessage, String element,
            @Nullable Tr064ChannelConfig channelConfig) {
        String dataType = channelConfig != null ? channelConfig.getDataType() : "string";
        String unit = channelConfig != null ? channelConfig.getChannelType().getItem().getUnit() : "";
        try {
            Optional<State> returnState = Optional.empty();

            NodeList nodeList = soapMessage.getSOAPBody().getElementsByTagName(element);
            if (nodeList != null && nodeList.getLength() > 0) {
                String rawValue = nodeList.item(0).getTextContent();
                switch (dataType) {
                    case "boolean":
                        returnState = Optional.of(rawValue.equals("0") ? OnOffType.OFF : OnOffType.ON);
                        break;
                    case "string":
                        returnState = Optional.of(new StringType(rawValue));
                        break;
                    case "ui2":
                    case "ui4":
                        if (!unit.isEmpty()) {
                            returnState = Optional.of(new QuantityType<>(rawValue + " " + unit));
                        } else {
                            returnState = Optional.of(new DecimalType(rawValue));
                        }
                        break;
                    default:
                }
            }

            // check if we need post processing
            if (channelConfig == null || channelConfig.getChannelType().getGetAction().getPostProcessor() == null) {
                return returnState;
            }

            String postProcessor = channelConfig.getChannelType().getGetAction().getPostProcessor();
            try {
                Method method = SOAPValueParser.class.getDeclaredMethod(postProcessor, Optional.class,
                        Tr064ChannelConfig.class);
                Object o = method.invoke(this, returnState, channelConfig);
                if (o instanceof Optional) {
                    Optional<?> optional = (Optional<?>) o;
                    if (optional.isPresent() && optional.get() instanceof State) {
                        return (Optional<State>) o;
                    } else {
                        return Optional.empty();
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                logger.warn("post processor {} not found, this most likely is a programming error", postProcessor, e);
                return Optional.empty();
            }

        } catch (SOAPException e) {
            logger.warn("Could not get expected element {} from SOAP message {}", element, soapMessage);
        }
        return Optional.empty();
    }

    private Optional<State> processTamListURL(Optional<State> soapState, Tr064ChannelConfig channelConfig) {
        if (!soapState.isPresent()) {
            return Optional.empty();
        }

        State state = soapState.get();
        try {
            ContentResponse response = httpClient.newRequest(state.toString()).timeout(1000, TimeUnit.MILLISECONDS)
                    .send();
            String responseContent = response.getContentAsString();
            int messageCount = responseContent.split("<New>1</New>").length - 1;

            return Optional.of(new DecimalType(messageCount));
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.info("Failed to get TAM list from URL {}", state);
            return Optional.empty();
        }

    }

    private Optional<State> processCallList(Optional<State> soapState, @Nullable String days, String type) {
        if (!soapState.isPresent()) {
            return Optional.empty();
        }

        State state = soapState.get();
        try {
            ContentResponse response = httpClient.newRequest(state.toString() + "&days=" + days)
                    .timeout(1000, TimeUnit.MILLISECONDS).send();
            String responseContent = response.getContentAsString();
            int callCount = responseContent.split("<Type>" + type + "</Type>").length - 1;

            return Optional.of(new DecimalType(callCount));
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.info("Failed to get Call list from URL {}", state);
            return Optional.empty();
        }
    }

    private Optional<State> processMissedCalls(Optional<State> soapState, Tr064ChannelConfig channelConfig) {
        return processCallList(soapState, channelConfig.getParameter(), "2");
    }

    private Optional<State> processInboundCalls(Optional<State> soapState, Tr064ChannelConfig channelConfig) {
        return processCallList(soapState, channelConfig.getParameter(), "1");
    }

    private Optional<State> processRejectedCalls(Optional<State> soapState, Tr064ChannelConfig channelConfig) {
        return processCallList(soapState, channelConfig.getParameter(), "3");
    }

    private Optional<State> processOutboundCalls(Optional<State> soapState, Tr064ChannelConfig channelConfig) {
        return processCallList(soapState, channelConfig.getParameter(), "4");
    }
}
