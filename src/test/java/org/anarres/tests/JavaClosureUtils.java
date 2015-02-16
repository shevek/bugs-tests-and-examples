/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.tests;

import com.esotericsoftware.reflectasm.MethodAccess;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import org.codehaus.commons.compiler.IClassBodyEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class JavaClosureUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JavaClosureUtils.class);
    public static final boolean JAVA5 = false;

    // Using the JDK ClassBodyEvaluator causes:
    // java.lang.NoClassDefFoundError: org/junit/runners/model/MultipleFailureException
    @Nonnull
    public static IClassBodyEvaluator newClassBodyEvaluator(boolean usejdk) {
        // This can compile generics.
        IClassBodyEvaluator evaluator;
        if (usejdk) {
            evaluator = new org.codehaus.commons.compiler.jdk.ClassBodyEvaluator();
        } else {
            evaluator = new org.codehaus.janino.ClassBodyEvaluator();
        }
        // CompilerFactoryFactory.getDefaultCompilerFactory().newClassBodyEvaluator();
        // LOG.info("IClassBodyEvaluator is an " + evaluator.getClass().getName());
        return evaluator;
    }

    @Nonnull
    public static JavaClosure newClassBodyEvaluator(@Nonnull String code, boolean usejdk)
            throws Exception {
        IClassBodyEvaluator evaluator = newClassBodyEvaluator(usejdk);
        // evaluator.setImplementedInterfaces(new Class<?>[]{iface});
        evaluator.setDefaultImports(new String[]{ // Named.class.getCanonicalName(),
        // RT.class.getCanonicalName()
        });
        evaluator.cook(code);

        Class<?> type = evaluator.getClazz();
        final Object instance = type.newInstance();
        final MethodAccess methodAccess = MethodAccess.get(type);
        final int methodIndex = methodAccess.getIndex(JavaClosure.METHOD_NAME);
        return new JavaClosure() {
            @Override
            public Object evaluate(Object... args) throws InvocationTargetException {
                return methodAccess.invoke(instance, methodIndex, args);
            }
        };
    }

}
