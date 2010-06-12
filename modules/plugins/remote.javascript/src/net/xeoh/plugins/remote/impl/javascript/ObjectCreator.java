package net.xeoh.plugins.remote.impl.javascript;

import org.directwebremoting.extend.AbstractCreator;
import org.directwebremoting.extend.Creator;

/**
 * Used to put in our own objects.
 * 
 * @author Thomas Lottermann
 *
 */
public class ObjectCreator extends AbstractCreator implements Creator {

    Object instance;

    /**
     * @param object
     */
    public ObjectCreator(final Object object) {
        this.instance = object;
        setJavascript(this.instance.getClass().getSimpleName());
    }

    /**
     * Gets the name of the class to create.
     * @return The name of the class to create
     */
    public String getClassName() {
        return this.instance.getClass().getSimpleName();
    }

    public Object getInstance() {
        return this.instance;
    }

    public Class<?> getType() {
        return this.instance.getClass();
    }

}
