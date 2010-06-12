/*
 * Plugin.java
 *
 * Copyright (c) 2007, Ralf Biedert All rights reserved.
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
package net.xeoh.plugins.base;

/**
 * The base class of all plugins. Plugin creation is fairly simple: <br>
 * <br>
 * 1. Create a new package for your plugin <br>
 * <br>
 * 2. Create an interface within that package extending Plugin. Add all the
 * methods you like. To keep the interfaces small, you can also add several
 * ones. <br>
 * <br>
 * 3. Create an impl package. This name is not required, but should be kept as a
 * convention. <br>
 * <br>
 * 4. Create a class implementing your interface. <br>
 * <br>
 * 5. Add the PluginImplementation annotation (see its definition for detailed
 * options) <br>
 * <br>
 * 6. You're done. Technically your plugin is ready now to use. It can be
 * compiled now (Eclipse will probably have done this for you already). You
 * might want to have a look at the PluginManager documentation.
 *
 * <br>
 * <br>
 * You have to <b>ensure that an implementation of such a plugin is thradsafe in
 * every manner</b>. Expect your functions to be called every time in every
 * state, unless however, you know your application (and all your users) very,
 * very well.
 *
 *
 * @author Ralf Biedert
 *
 */
public interface Plugin extends Pluggable {
    // Nothing in here. All things are done by annotations.
}
