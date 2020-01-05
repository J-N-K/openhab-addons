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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.fritzboxtr064.internal.config.Tr064ChannelConfig;
import org.openhab.binding.fritzboxtr064.internal.model.config.ChannelType;
import org.openhab.binding.fritzboxtr064.internal.model.scpd.root.SCPDServiceType;
import org.openhab.binding.fritzboxtr064.internal.model.scpd.service.SCPDActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openhab.binding.fritzboxtr064.internal.Tr064BindingConstants.REQUEST_TIMEOUT;

/**
 * The {@link SOAPConnector} provides communication with a remote SOAP device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SOAPConnector {
    private final Logger logger = LoggerFactory.getLogger(SOAPConnector.class);
    private final HttpClient httpClient;
    private final String endpointBaseURL;
    private final @Nullable DigestAuthorization digestAuthorization;
    private final SOAPValueConverter soapValueConverter;

    public SOAPConnector(HttpClient httpClient, @Nullable DigestAuthorization digestAuthorization,
            String endpointBaseURL) {
        this.httpClient = httpClient;
        this.digestAuthorization = digestAuthorization;
        this.endpointBaseURL = endpointBaseURL;
        soapValueConverter = new SOAPValueConverter(httpClient);
    }

    /**
     * prepare a SOAP request for an action request to a service
     *
     * @param service the service
     * @param soapAction the action to send
     * @param arguments arguments to send along with the request
     * @return a jetty Request containing the full SOAP message
     * @throws IOException if a problem while writing the SOAP message to the Request occurs
     * @throws SOAPException if a problem with creating the SOAP message occurs
     */
    private HttpRequest.Builder prepareSOAPRequest(SCPDServiceType service, String soapAction,
            Map<String, String> arguments) throws IOException, SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
        envelope.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");

        // SOAP body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement(soapAction, "u", service.getServiceType());
        arguments.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(argument -> {
            try {
                SOAPElement argumentElement = soapBodyElem.addChildElement(argument.getKey());
                argumentElement.setTextContent(argument.getValue());
            } catch (SOAPException e) {
                logger.warn("Could not add {}:{} to SOAP Request: {}", argument.getKey(), argument.getValue(),
                        e.getMessage());
            }
        });

        // create Request and add headers and content
        URI uri = URI.create(endpointBaseURL + service.getControlURL());
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).timeout(REQUEST_TIMEOUT);
        requestBuilder.header("SOAPAction", service.getServiceType() + "#" + soapAction);
        requestBuilder.header("Accept", "text/xml"); // headers.getHeader("Accept")[0]);
        requestBuilder.header("Content-Type", "text/xml; charset=utf-8");

        if (digestAuthorization != null) {
            String authHeader = digestAuthorization.getAuthorization(uri, "POST");
            if (authHeader != null) {
                requestBuilder.header("Authorization", authHeader);
            }
        }

        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            soapMessage.writeTo(os);
            String content = new String(os.toByteArray(), StandardCharsets.UTF_8);
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(content, StandardCharsets.UTF_8));
            logger.debug("Request-Body: {}", content);
        }

        return requestBuilder;
    }

    /**
     * execute a SOAP request
     *
     * @param service the service to send the action to
     * @param soapAction the action itself
     * @param arguments arguments to send along with the request
     * @return the SOAPMessage answer from the remote host
     * @throws Tr064CommunicationException if an error occurs during the request
     */
    public synchronized SOAPMessage doSOAPRequest(SCPDServiceType service, String soapAction,
            Map<String, String> arguments) throws Tr064CommunicationException {
        try {
            HttpRequest.Builder requestBuilder = prepareSOAPRequest(service, soapAction, arguments);
            HttpRequest request = requestBuilder.build();
            logger.trace("Sending Request with headers: {}", request.headers());
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            logger.trace("Response: {} - {}", response.headers(), response.body());

            if (response.statusCode() == 401) {
                digestAuthorization.updateNonce(response.headers());
                requestBuilder.setHeader("Authorization", digestAuthorization.getAuthorization(response.uri(), "POST"));
                logger.trace("Resending Request with new authorization header");
                response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
                logger.trace("Response: {} - {}", response.headers(), response.body());
            }

            try (final ByteArrayInputStream is = new ByteArrayInputStream(response.body())) {
                SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
                if (soapMessage.getSOAPBody().hasFault()) {
                    String soapError = "unknown";
                    String soapReason = "unknown";
                    NodeList nodeList = soapMessage.getSOAPBody().getElementsByTagName("errorCode");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        soapError = nodeList.item(0).getTextContent();
                    }
                    nodeList = soapMessage.getSOAPBody().getElementsByTagName("errorDescription");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        soapReason = nodeList.item(0).getTextContent();
                    }
                    String error = String
                            .format("HTTP-Response-Status-Code %d, SOAP-Fault: %s (%s)", response.statusCode(),
                                    soapError, soapReason);
                    throw new Tr064CommunicationException(error);
                }
                return soapMessage;
            }
        } catch (IOException | SOAPException | InterruptedException e) {
            throw new Tr064CommunicationException(e);
        }

    }

    /**
     * send a command to the remote device
     *
     * @param channelConfig the channel config containing all information
     * @param command the command to send
     */
    public void sendChannelCommandToDevice(Tr064ChannelConfig channelConfig, Command command) {
        soapValueConverter.getSOAPValueFromCommand(command, channelConfig.getDataType(),
                channelConfig.getChannelType().getItem().getUnit()).ifPresentOrElse(value -> {
            final ChannelType channelType = channelConfig.getChannelType();
            final SCPDServiceType service = channelConfig.getService();
            logger.debug("Sending {} as {} to {}/{}", command, value, service.getServiceId(),
                    channelType.getSetAction().getName());
            try {
                Map<String, String> arguments = new HashMap<>();
                if (channelType.getSetAction().getArgument() != null) {
                    arguments.put(channelType.getSetAction().getArgument(), value);
                }
                String parameter = channelConfig.getParameter();
                if (parameter != null) {
                    arguments.put(channelConfig.getChannelType().getGetAction().getParameter().getName(), parameter);
                }
                doSOAPRequest(service, channelType.getSetAction().getName(), arguments);
            } catch (Tr064CommunicationException e) {
                logger.warn("Could not send command {}: {}", command, e.getMessage());
            }
        }, () -> logger.info("Could not convert {} to SOAP value", command));
    }

    /**
     * get a value from the remote device - updates state cache for all possible channels
     *
     * @param channelConfig the channel config containing all information
     * @param channelConfigMap map of all channels in the device
     * @param stateCache the ExpiringCacheMap for states of the device
     * @return the value for the requested channel
     */
    public State getChannelStateFromDevice(final Tr064ChannelConfig channelConfig,
            Map<ChannelUID, Tr064ChannelConfig> channelConfigMap, ExpiringCacheMap<ChannelUID, State> stateCache) {
        try {
            final SCPDActionType getAction = channelConfig.getGetAction();
            if (getAction == null) {
                // channel has no get action, return a default
                switch (channelConfig.getDataType()) {
                    case "boolean":
                        return OnOffType.OFF;
                    case "string":
                        return StringType.EMPTY;
                    default:
                        return UnDefType.UNDEF;
                }
            }

            // get value(s) from remote device
            Map<String, String> arguments = new HashMap<>();
            String parameter = channelConfig.getParameter();
            if (parameter != null && !channelConfig.getChannelType().getGetAction().getParameter().isInternalOnly()) {
                arguments.put(channelConfig.getChannelType().getGetAction().getParameter().getName(), parameter);
            }
            SOAPMessage soapResponse = doSOAPRequest(channelConfig.getService(), getAction.getName(), arguments);

            String argumentName = channelConfig.getChannelType().getGetAction().getArgument();
            // find all other channels with the same action that are already in cache, so we can update them
            Map<ChannelUID, Tr064ChannelConfig> channelsInRequest = channelConfigMap.entrySet().stream()
                    .filter(map -> getAction.equals(map.getValue().getGetAction()) && stateCache
                            .containsKey(map.getKey()) && !argumentName
                            .equals(map.getValue().getChannelType().getGetAction().getArgument()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            channelsInRequest.forEach((channelUID, channelConfig1) -> soapValueConverter
                    .getStateFromSOAPValue(soapResponse, channelConfig1.getChannelType().getGetAction().getArgument(),
                            channelConfig1).ifPresent(state -> stateCache.putValue(channelUID, state)));

            return soapValueConverter.getStateFromSOAPValue(soapResponse, argumentName, channelConfig).orElseThrow(
                    () -> new Tr064CommunicationException(
                            "failed to transform '" + channelConfig.getChannelType().getGetAction()
                                    .getArgument() + "'"));
        } catch (Tr064CommunicationException e) {
            logger.info("Failed to get {}: {}", channelConfig, e.getMessage());
            return UnDefType.UNDEF;
        }
    }
}
