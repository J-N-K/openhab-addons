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
package org.openhab.binding.fritzboxtr064.internal.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.fritzboxtr064.internal.SCPDException;
import org.openhab.binding.fritzboxtr064.internal.model.scpd.root.SCPDDeviceType;
import org.openhab.binding.fritzboxtr064.internal.model.scpd.root.SCPDRootType;
import org.openhab.binding.fritzboxtr064.internal.model.scpd.root.SCPDServiceType;
import org.openhab.binding.fritzboxtr064.internal.model.scpd.service.SCPDScpdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SCPDUtil} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SCPDUtil {
    private final Logger logger = LoggerFactory.getLogger(SCPDUtil.class);

    private final HttpClient httpClient;

    private SCPDRootType scpdRoot;
    private final List<SCPDDeviceType> scpdDevicesList = new ArrayList<>();
    private final Map<String, SCPDScpdType> serviceMap = new HashMap<>();

    public SCPDUtil(HttpClient httpClient, String endpoint) throws SCPDException {
        this.httpClient = httpClient;

        scpdRoot = getAndUnmarshalSCPD(endpoint + "/tr64desc.xml", SCPDRootType.class)
                .orElseThrow(() -> new SCPDException("could not get SCPD root"));
        scpdDevicesList.addAll(flatDeviceList(scpdRoot.getDevice()).collect(Collectors.toList()));
        for (SCPDDeviceType device : scpdDevicesList) {
            for (SCPDServiceType service : device.getServiceList()) {
                String serviceId = service.getServiceId();
                if (!serviceMap.containsKey(serviceId)) {
                    SCPDScpdType scpdService = getAndUnmarshalSCPD(endpoint + service.getSCPDURL(), SCPDScpdType.class)
                            .orElseThrow(() -> new SCPDException("could not get SCPD root"));
                    serviceMap.put(serviceId, scpdService);
                }
            }
        }
    }

    private <T> Optional<T> getAndUnmarshalSCPD(String uri, Class<T> clazz) {
        try {
            ContentResponse contentResponse = httpClient.newRequest(uri).timeout(5, TimeUnit.SECONDS)
                    .method(HttpMethod.GET).send();
            InputStream xml = new ByteArrayInputStream(contentResponse.getContent());

            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller um = context.createUnmarshaller();
            return Optional.ofNullable(um.unmarshal(new StreamSource(xml), clazz).getValue());
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.debug("HTTP Failed to GET uri '{}': {}", uri, e.getMessage());
        } catch (JAXBException e) {
            logger.debug("Unmarshalling failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Stream<SCPDDeviceType> flatDeviceList(SCPDDeviceType device) {
        return Stream.concat(Stream.of(device), device.getDeviceList().stream().flatMap(this::flatDeviceList));
    }

    public List<SCPDDeviceType> getAllSubDevices() {
        return scpdDevicesList.stream().filter(device -> !device.getUDN().equals(scpdRoot.getDevice().getUDN()))
                .collect(Collectors.toList());
    }

    public Optional<SCPDDeviceType> getDevice(String udn) {
        if (udn.isEmpty()) {
            return Optional.of(scpdRoot.getDevice());
        } else {
            return getAllSubDevices().stream().filter(device -> udn.equals(device.getUDN())).findFirst();
        }
    }

    // SCPD service files
    public Optional<SCPDScpdType> getService(String serviceId) {
        return Optional.ofNullable(serviceMap.get(serviceId));
    }
}
