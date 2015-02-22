package org.anarres.tests;

import javax.annotation.Nonnull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class JavaClosureUtilsTest {

    private static final Logger LOG = LoggerFactory.getLogger(JavaClosureUtilsTest.class);

    public void testJavaCodeGenerator(@Nonnull String code, boolean usejdk) throws Exception {
        LOG.info("Java class is " + code);
        JavaClosure evaluator = JavaClosureUtils.newClassBodyEvaluator(code, usejdk);
        LOG.info("Evaluator is " + evaluator);
    }

    // @Test
    public void testJanino() throws Exception {
        testJavaCodeGenerator("Object evaluate() { return 5; }", false);
    }

    @Test
    public void testJdk() throws Exception {
        testJavaCodeGenerator("Object evaluate() { return 5; }", true);
    }

}
