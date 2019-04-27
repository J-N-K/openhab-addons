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
package org.openhab.binding.fritzboxtr064.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Tr064ChannelParameter} class holds a channel definition
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064ChannelParameter {
    public String itemType;
    public String unit;
    public String stateDescriptionPattern;
    public String deviceType;
    public String serviceId;
    public String getActionName;
    public String getArgumentName;
    public String setActionName;
    public String setArgumentName;

    public Tr064ChannelParameter(String itemType, String unit, String stateDescriptionPattern, String deviceType,
            String serviceId, String getActionName, String getArgumentName, String setActionName,
            String setArgumentName) {
        this.itemType = itemType;
        this.unit = unit;
        this.stateDescriptionPattern = stateDescriptionPattern;
        this.deviceType = deviceType;
        this.serviceId = serviceId;
        this.getActionName = getActionName;
        this.getArgumentName = getArgumentName;
        this.setActionName = setActionName;
        this.setArgumentName = setArgumentName;
    }

    @Override
    public String toString() {
        return "[itemType=" + itemType + ",unit=" + unit + ",deviceId=" + deviceType + ",serviceId=" + serviceId
                + ",getActionName=" + getActionName + ",getArgumentName=" + getArgumentName + ",setActionName="
                + setActionName + ",setArgumentName=" + setArgumentName + "]";
    }
}
