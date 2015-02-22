package org.anarres.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Set;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.Cookable;
import org.codehaus.commons.compiler.Location;
import org.codehaus.commons.compiler.jdk.ByteArrayJavaFileManager;
import org.codehaus.commons.compiler.jdk.JavaFileManagerClassLoader;
import org.junit.Test;

/**
 *
 * @author shevek
 */
public class JavaCompilerTest {

    public static final String text = "public class SC { Object evaluate() { return 5; } }";

    private ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
    private ClassLoader result;
    private boolean debugSource;
    private boolean debugLines;
    private boolean debugVars;

    public ClassLoader
            getClassLoader() {
        this.assertCooked();
        return this.result;
    }

    public void
            cook(String optionalFileName, final Reader r) throws CompileException, IOException {
        this.assertNotCooked();

        // Create one Java source file in memory, which will be compiled later.
        JavaFileObject compilationUnit;
        {
            URI uri;
            try {
                uri = new URI("simplecompiler");
            } catch (URISyntaxException use) {
                throw new RuntimeException(use);
            }
            compilationUnit = new SimpleJavaFileObject(uri, Kind.SOURCE) {

                @Override
                public boolean
                        isNameCompatible(String simpleName, Kind kind) {
                    return true;
                }

                @Override
                public Reader
                        openReader(boolean ignoreEncodingErrors) throws IOException {
                    return r;
                }

                @Override
                public CharSequence
                        getCharContent(boolean ignoreEncodingErrors) throws IOException {
                    return Cookable.readString(this.openReader(ignoreEncodingErrors));
                }

                @Override
                public String
                        toString() {
                    return String.valueOf(this.uri);
                }
            };
        }

        // Find the JDK Java compiler.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new CompileException(
                    "JDK Java compiler not available - probably you're running a JRE, not a JDK",
                    null
            );
        }

        // Get the original FM, which reads class files through this JVM's BOOTCLASSPATH and
        // CLASSPATH.
        final JavaFileManager fm = compiler.getStandardFileManager(null, null, null);

        // Wrap it so that the output files (in our case class files) are stored in memory rather
        // than in files.
        final JavaFileManager fileManager = new ForwardingJavaFileManager<JavaFileManager>(
                new ByteArrayJavaFileManager<JavaFileManager>(fm)
        ) {

            @Override
            public String inferBinaryName(Location location, JavaFileObject file) {
                String name = file.getName();
                if (name.contains("Class2")) {
                    System.currentTimeMillis();
                }
                String res = super.inferBinaryName(location, file);
                return res;
            }

            @Override
            public Iterable<JavaFileObject> list(final Location location, final String packageName, final Set<Kind> kinds, final boolean recurse)
                    throws IOException {
                if ("pkg2".equals(packageName)) {
                    final URL url = JavaCompilerTest.this.parentClassLoader.getResource("pkg2/Class2.class");
                    URI uri;
                    try {
                        uri = url.toURI();
                    } catch (URISyntaxException use) {
                        throw new IOException(use);
                    }
                    return Collections.<JavaFileObject>singletonList(new SimpleJavaFileObject(uri, Kind.CLASS) {

                        @Override
                        public boolean isNameCompatible(String simpleName, Kind kind) {
                            return true;
                        }

                        @Override
                        public InputStream openInputStream() throws IOException {
                            return url.openStream();
                        }

                        @Override
                        public OutputStream openOutputStream() throws IOException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public String getName() {
                            return "pkg2/Class2";
                        }

                        @Override
                        public String toString() {
                            return String.valueOf(this.uri);
                        }
                    });
                }
                return super.list(location, packageName, kinds, recurse);
            }

            @Override
            public ClassLoader getClassLoader(javax.tools.JavaFileManager.Location location) {
                return JavaCompilerTest.this.parentClassLoader;
            }
        };

        // Run the compiler.
        try {
            final CompileException[] caughtCompileException = new CompileException[1];
            if (!compiler.getTask(
                    null, // out
                    fileManager, // fileManager
                    new DiagnosticListener<JavaFileObject>() { // diagnosticListener

                        @Override
                        public void
                        report(Diagnostic<? extends JavaFileObject> diagnostic) {

                            Location loc = new Location(
                                    null,
                                    (short) diagnostic.getLineNumber(),
                                    (short) diagnostic.getColumnNumber()
                            );
                            String message = diagnostic.getMessage(null) + " (" + diagnostic.getCode() + ")";

                            try {
                                switch (diagnostic.getKind()) {
                                    case ERROR:
                                        throw new CompileException(message, loc);
                                    case MANDATORY_WARNING:
                                    case WARNING:
                                        break;
                                    case NOTE:
                                    case OTHER:
                                    default:
                                        break;

                                }
                            } catch (CompileException ce) {
                                if (caughtCompileException[0] == null)
                                    caughtCompileException[0] = ce;
                            }
                        }
                    },
                    Collections.singletonList( // options
                            this.debugSource
                                    ? "-g:source" + (this.debugLines ? ",lines" : "") + (this.debugVars ? ",vars" : "")
                                    : this.debugLines
                                            ? "-g:lines" + (this.debugVars ? ",vars" : "")
                                            : this.debugVars
                                                    ? "-g:vars"
                                                    : "-g:none"
                    ),
                    null, // classes
                    Collections.singleton(compilationUnit) // compilationUnits
            ).call()) {
                if (caughtCompileException[0] != null)
                    throw caughtCompileException[0];
                throw new CompileException("Compilation failed", null);
            }
        } catch (RuntimeException rte) {

            // Unwrap the compilation exception and throw it.
            for (Throwable t = rte.getCause(); t != null; t = t.getCause()) {
                if (t instanceof CompileException) {
                    throw (CompileException) t; // SUPPRESS CHECKSTYLE AvoidHidingCause
                }
                if (t instanceof IOException) {
                    throw (IOException) t; // SUPPRESS CHECKSTYLE AvoidHidingCause
                }
            }
            throw rte;
        }

        // Create a ClassLoader that reads class files from our FM.
        this.result = AccessController.doPrivileged(new PrivilegedAction<JavaFileManagerClassLoader>() {

            @Override
            public JavaFileManagerClassLoader
                    run() {
                return new JavaFileManagerClassLoader(fileManager, JavaCompilerTest.this.parentClassLoader);
            }
        });
    }

//    protected void
//    cook(JavaFileObject compilationUnit) throws CompileException, IOException {
//
//        // Find the JDK Java compiler.
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        if (compiler == null) {
//            throw new CompileException(
//                "JDK Java compiler not available - probably you're running a JRE, not a JDK",
//                null
//            );
//        }
//
//        // Get the original FM, which reads class files through this JVM's BOOTCLASSPATH and
//        // CLASSPATH.
//        final JavaFileManager fm = compiler.getStandardFileManager(null, null, null);
//
//        // Wrap it so that the output files (in our case class files) are stored in memory rather
//        // than in files.
//        final JavaFileManager fileManager = new ByteArrayJavaFileManager<JavaFileManager>(fm);
//
//        // Run the compiler.
//        try {
//            if (!compiler.getTask(
//                null,                                  // out
//                fileManager,                           // fileManager
//                new DiagnosticListener<JavaFileObject>() { // diagnosticListener
//
//                    @Override public void
//                    report(Diagnostic<? extends JavaFileObject> diagnostic) {
//                        System.err.println("*** " + diagnostic.toString() + " *** " + diagnostic.getCode());
//
//                        Location loc = new Location(
//                            diagnostic.getSource().toString(),
//                            (short) diagnostic.getLineNumber(),
//                            (short) diagnostic.getColumnNumber()
//                        );
//                        String code    = diagnostic.getCode();
//                        String message = diagnostic.getMessage(null) + " (" + code + ")";
//
//                        // Wrap the exception in a RuntimeException, because "report()" does not declare checked
//                        // exceptions.
//                        throw new RuntimeException(new CompileException(message, loc));
//                    }
//                },
//                null,                                  // options
//                null,                                  // classes
//                Collections.singleton(compilationUnit) // compilationUnits
//            ).call()) {
//                throw new CompileException("Compilation failed", null);
//            }
//        } catch (RuntimeException rte) {
//
//            // Unwrap the compilation exception and throw it.
//            Throwable cause = rte.getCause();
//            if (cause != null) {
//                cause = cause.getCause();
//                if (cause instanceof CompileException) {
//                    throw (CompileException) cause; // SUPPRESS CHECKSTYLE AvoidHidingCause
//                }
//                if (cause instanceof IOException) {
//                    throw (IOException) cause; // SUPPRESS CHECKSTYLE AvoidHidingCause
//                }
//            }
//            throw rte;
//        }
//
//        // Create a ClassLoader that reads class files from our FM.
//        this.result = AccessController.doPrivileged(new PrivilegedAction<JavaFileManagerClassLoader>() {
//
//            @Override public JavaFileManagerClassLoader
//            run() { return new JavaFileManagerClassLoader(fileManager, SimpleCompiler.this.parentClassLoader); }
//        });
//    }
    public void
            setDebuggingInformation(boolean debugSource, boolean debugLines, boolean debugVars) {
        this.debugSource = debugSource;
        this.debugLines = debugLines;
        this.debugVars = debugVars;
    }

    public void
            setParentClassLoader(ClassLoader optionalParentClassLoader) {
        this.assertNotCooked();
        this.parentClassLoader = (optionalParentClassLoader != null
                ? optionalParentClassLoader
                : Thread.currentThread().getContextClassLoader());
    }

    /** Throw an {@link IllegalStateException} if this {@link Cookable} is not yet cooked. */
    protected void
            assertCooked() {
        if (this.result == null)
            throw new IllegalStateException("Not yet cooked");
    }

    /** Throw an {@link IllegalStateException} if this {@link Cookable} is already cooked. */
    protected void
            assertNotCooked() {
        if (this.result != null)
            throw new IllegalStateException("Already cooked");
    }

    @Test
    public void testCompiler() throws Exception {
        StringReader r = new StringReader(text);
        cook(null, r);
    }
}
