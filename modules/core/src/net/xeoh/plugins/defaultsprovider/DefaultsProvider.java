/*
 * DefaultsProvider.java
 *
 * Copyright (c) 2008, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.defaultsprovider;

import net.xeoh.plugins.base.Plugin;

/**
 * Interface for different defaults provider. A defaults provider is usable because: <br>
 * <br>
 * Ideally, if you create different plugins they should not share some common base class
 * as this would induce strong dependencies amonst them. On the other hand however,
 * methods will perform the same behavior however. You can solve this problem by creating
 * a defaults provider. It will return default for various tasks which can then be used by
 * different plugins while they still only see one interface and not an implementiation.
 * Thus the implementation can be changed easily then back and forth. <br>
 * <br>
 * The only reason why these interfaces are here (and do not have any sort of
 * implementation) yet is to remind you of this mechanism and encourage you to create your
 * own providers for your purposes.
 *
 * @author Ralf Biedert
 */
public interface DefaultsProvider extends Plugin {
    // Create your own provider.
}
