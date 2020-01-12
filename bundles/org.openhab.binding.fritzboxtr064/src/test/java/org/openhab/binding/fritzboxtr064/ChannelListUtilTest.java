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
package org.openhab.binding.fritzboxtr064;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.fritzboxtr064.internal.Tr064BindingConstants;
import org.openhab.binding.fritzboxtr064.internal.model.config.ChannelType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

/**
 * The {@link ChannelListUtilTest} is a tool for documentation generation
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ChannelListUtilTest {

    @Test
    public void createChannelListTest() {
        try {
            final Writer writer = new OutputStreamWriter(new FileOutputStream("target/channelList.asc"),
                    StandardCharsets.UTF_8);

            Tr064BindingConstants.CHANNEL_TYPES.stream().sorted(Comparator.comparing(ChannelType::getName))
                    .forEach(channel -> {
                        String description = channel.getDescription() == null ? channel.getLabel() : channel.getDescription();
                        String channelString = String
                                .format("| `%s` | `%s`| %s |%c", channel.getName(), channel.getItem().getType(), description, 13);
                        try {
                            writer.write(channelString);
                        } catch (IOException e) {
                            Assert.fail(e.getMessage());
                        }
                    });

            writer.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

}