/*
 * PluginExport.java
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.xeoh.plugins.remote.util.internal;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.logging.Logger;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.util.PluginUtil;

/**
 * Some utils for exporting plugins. Only used internally.
 * 
 * @author Ralf Biedert
 */
public class PluginExport {
    /**
     * @param plugin
     * 
     * @return .
     */
    public static String getExportName(Plugin plugin) {
        final Collection<Class<? extends Plugin>> primaryInterfaces = new PluginUtil(plugin).getPrimaryInterfaces();
        final Logger logger = Logger.getLogger(PluginExport.class.getName());

        if (primaryInterfaces.size() < 1) {
            logger.severe("CRITICAL ERROR: UNABLE TO GET ANY INTERFACE NAME FOR " + plugin);
            return "ERROR";
        }

        if (primaryInterfaces.size() > 1) {
            logger.warning("Multiple plugin names found ... that is very bad and means your export names will be unstable!");
        }

        Class<? extends Plugin> next = primaryInterfaces.iterator().next();

        return next.getCanonicalName();
    }

    /**
     * @param plugin
     * 
     * @return .
     */
    @SuppressWarnings("boxing")
    public static String getHashedName(Plugin plugin) {
        final String exportName = getExportName(plugin);

        try {
            // Convert name to bytes
            final byte[] bytes = exportName.getBytes("UTF-8");
            final MessageDigest digest = java.security.MessageDigest.getInstance("MD5");

            // Update the hash
            digest.update(bytes, 0, bytes.length);

            // Get result
            final byte[] hash = digest.digest();

            // Assemble hash string
            final StringBuilder sb = new StringBuilder();
            for (final byte b : hash) {
                final String format = String.format("%02x", b);
                sb.append(format);
            }

            char replacements[] = { 'a', 'b', 'c', 'd', 'e', 'f', 'a', 'b', 'c', 'd', 'e' };
            String rval = sb.toString();
            char charAt = rval.charAt(0);
            if (Character.isDigit(charAt)) {
                int i = Integer.parseInt("" + charAt);
                rval = replacements[i] + rval.substring(1);
            }

            return rval;
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "ERROR_CREATING_FINGERPRINT";

    }
}
