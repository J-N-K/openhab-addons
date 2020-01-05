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

import java.net.URI;
import java.net.http.HttpHeaders;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The{@link DigestAuthorization} is a catched Exception that is thrown in case of errors during SCPD processing
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DigestAuthorization {
    private static final Pattern HEADER_NONCE_PATTERN = Pattern.compile(".*nonce=\"([A-Za-z0-9]+)\".*");
    private final Logger logger = LoggerFactory.getLogger(DigestAuthorization.class);

    private final String ha1;
    private final String username;
    private final String realm;

    private String nonce = "";
    private int nonceCounter = 0;

    private MessageDigest messageDigest;

    public DigestAuthorization(String username, String password, String realm) throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update((username + ":" + realm + ":" + password).getBytes());
        ha1 = HexUtils.bytesToHex(messageDigest.digest()).toLowerCase();

        this.realm = realm;
        this.username = username;
    }

    public void updateNonce(HttpHeaders headers) throws Tr064CommunicationException {
        String authHeader = headers.firstValue("www-authenticate")
                .orElseThrow(() -> new Tr064CommunicationException("Could not get Auth Header"));
        Matcher matcher = HEADER_NONCE_PATTERN.matcher(authHeader);
        if (!matcher.matches()) {
            throw new Tr064CommunicationException("Could not extract nonce from header");
        }
        nonce = matcher.group(1);
        nonceCounter = 0;
    }

    public @Nullable String getAuthorization(URI uri, String method) {
        if (nonce.isEmpty()) {
            return null;
        }

        messageDigest.reset();
        messageDigest.update((method + ":" + uri.getPath()).getBytes());
        String ha2 = HexUtils.bytesToHex(messageDigest.digest()).toLowerCase();

        // create cnonce
        messageDigest.reset();
        messageDigest.update((uri.getPath() + ":" + System.currentTimeMillis()).getBytes());
        String cnonce = HexUtils.bytesToHex(messageDigest.digest()).toLowerCase();

        nonceCounter++;

        String nc = String.format("%08d", nonceCounter);

        // calculate response value
        messageDigest.reset();
        messageDigest.update((ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":auth:" + ha2).getBytes());
        String response = HexUtils.bytesToHex(messageDigest.digest()).toLowerCase();

        // create auth string
        String auth = String.format(
                "Digest username=\"%s\", realm=\"%s\", nonce=\"%s\", uri=\"%s\", cnonce=\"%s\", nc=%s, qop=auth, response=\"%s\", algorithm=\"MD5\"",
                username, realm, nonce, uri.getPath(), cnonce, nc, response);

        logger.trace("Created auth header: {}", auth);
        return auth;
    }
}
