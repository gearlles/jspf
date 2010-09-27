package net.xeoh.plugins.base.util;

import net.xeoh.plugins.base.Option;

/**
 * Handles options. Only used internally.
 * 
 * @author Ralf Biedert
 * 
 * @param <T> Type parameter.
 */
public interface OptionHandler<T extends Option> {
    /**
     * Called with e
     * 
     * @param option
     */
    public void handle(T option);
}