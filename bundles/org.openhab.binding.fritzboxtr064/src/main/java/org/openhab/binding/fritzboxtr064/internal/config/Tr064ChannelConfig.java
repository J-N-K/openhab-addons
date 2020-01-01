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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fritzboxtr064.internal.model.config.ChannelType;
import org.openhab.binding.fritzboxtr064.internal.model.scpd.root.SCPDServiceType;
import org.openhab.binding.fritzboxtr064.internal.model.scpd.service.SCPDActionType;

/**
 * The {@link Tr064ChannelConfig} class holds a channel configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064ChannelConfig {
    private ChannelType channelType;
    private SCPDServiceType service;
    private @Nullable SCPDActionType getAction;
    private String dataType = "";
    private @Nullable String parameter;

    public Tr064ChannelConfig(ChannelType channelType, SCPDServiceType service) {
        this.channelType = channelType;
        this.service = service;
    }

    public Tr064ChannelConfig(Tr064ChannelConfig o) {
        this.channelType = o.channelType;
        this.service = o.service;
        this.getAction = o.getAction;
        this.dataType = o.dataType;
        this.parameter = o.parameter;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public SCPDServiceType getService() {
        return service;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public @Nullable SCPDActionType getGetAction() {
        return getAction;
    }

    public void setGetAction(SCPDActionType getAction) {
        this.getAction = getAction;
    }

    public @Nullable String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        final SCPDActionType getAction = this.getAction;
        return "Tr064ChannelConfig{" + "channelType=" + channelType.getName() + ", getAction="
                + ((getAction == null) ? "(null)" : getAction.getName()) + ", dataType='" + dataType + ", parameter='"
                + parameter + "'}";
    }
}
