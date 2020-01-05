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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.fritzboxtr064.internal.model.config.ChannelType;
import org.openhab.binding.fritzboxtr064.internal.util.Util;

/**
 * The {@link Tr064BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064BindingConstants {
    private static final String BINDING_ID = "fritzboxtr064";

    public static final ThingTypeUID THING_TYPE_ROOTDEVICE = new ThingTypeUID(BINDING_ID, "rootdevice");
    public static final ThingTypeUID THING_TYPE_SUBDEVICE = new ThingTypeUID(BINDING_ID, "subdevice");
    public static final ThingTypeUID THING_TYPE_SUBDEVICE_LAN = new ThingTypeUID(BINDING_ID, "subdeviceLan");

    public static final List<ChannelType> CHANNEL_TYPES = new ArrayList<>();

    public static final Duration CONNECT_TIMEOUT = Duration.ofMillis(1500);
    public static final Duration REQUEST_TIMEOUT = Duration.ofMillis(1500);

    static {
        CHANNEL_TYPES.addAll(Util.readXMLChannelConfig());
    }
}
