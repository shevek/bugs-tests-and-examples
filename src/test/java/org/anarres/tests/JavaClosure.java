package org.anarres.tests;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author shevek
 */
public interface JavaClosure {

    public static final String METHOD_NAME = "evaluate";

    public Object evaluate(Object... args) throws InvocationTargetException;

}
