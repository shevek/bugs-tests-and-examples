/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.tests;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.anarres.jdiagnostics.ProductMetadata;

/**
 *
 * @author shevek
 */
public class Main {

    private final OptionParser parser = new OptionParser();
    private final OptionSpec<?> helpOption = parser.accepts("help", "Displays command-line help.").forHelp();
    private final OptionSpec<?> versionOption = parser.accepts("version", "Displays the product version and exits.").forHelp();

    public void run(String... args) throws Exception {
        OptionSet o = parser.parse(args);
        if (o.has(helpOption)) {
            parser.printHelpOn(System.err);
            return;
        }
        if (o.has(versionOption)) {
            System.err.println(new ProductMetadata());
            return;
        }

        // ...
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.run(args);
    }
}
