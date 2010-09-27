package net.xeoh.plugins.base.util;

import net.xeoh.plugins.base.Option;

/**
 * Handle options
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