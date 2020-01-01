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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link Tr064HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { ThingHandlerFactory.class }, configurationPid = "binding.fritzboxtr064")
public class Tr064HandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(Tr064RootHandler.SUPPORTED_THING_TYPES.stream(), Tr064SubHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private final HttpClient httpClient;
    private final Tr064DynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Activate
    public Tr064HandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference Tr064DynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        httpClient = httpClientFactory.getCommonHttpClient();
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Tr064RootHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            Tr064RootHandler handler = new Tr064RootHandler((Bridge) thing, httpClient,
                    dynamicStateDescriptionProvider);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (Tr064SubHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new Tr064SubHandler(thing, dynamicStateDescriptionProvider);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof Tr064RootHandler) {
            final ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }

    /**
     * create and register a new discovery service for the given bridge handler
     *
     * @param bridgeHandler the bridgehandler (root device)
     */
    private synchronized void registerDeviceDiscoveryService(Tr064RootHandler bridgeHandler) {
        Tr064DiscoveryService discoveryService = new Tr064DiscoveryService(bridgeHandler);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}
