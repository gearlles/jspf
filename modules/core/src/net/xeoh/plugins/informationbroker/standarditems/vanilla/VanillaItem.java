/*
 * StringItem.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.informationbroker.standarditems.vanilla;

import net.xeoh.plugins.informationbroker.InformationBroker;
import net.xeoh.plugins.informationbroker.InformationItem;
import net.xeoh.plugins.informationbroker.InformationItemIdentifier;

/**
 * Represents a vanilla information broker item. Direct use of this class is discouraged
 * as it detroys type safety, but feel free to derive from it as necessary.
 * 
 * @author Ralf Biedert
 * @param <O> Type of the item.
 * @see InformationBroker
 */
public class VanillaItem<O extends Object> implements InformationItem<O> {

    O content;

    VanillaID<O, VanillaItem<O>> id;

    /**
     * Constructs a new item.
     * 
     * @param id The ID to use.
     * @param content The actual content.
     */
    public VanillaItem(final String id, final O content) {
        this.id = new VanillaID<O, VanillaItem<O>>(id);
        this.content = content;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.informationbroker.InformationItem#getContent()
     */
    public O getContent() {
        return this.content;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.informationbroker.InformationItem#getIdentifier()
     */
    public InformationItemIdentifier<O, VanillaItem<O>> getIdentifier() {
        return this.id;
    }
}
