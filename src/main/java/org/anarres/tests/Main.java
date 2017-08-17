/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.tests;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.io.Resources;
import java.io.InputStream;
import java.net.URL;
import javax.annotation.Nonnull;
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

    private void read(@Nonnull String path) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JaxbAnnotationModule());

        URL url = Resources.getResource(path);
        try (InputStream in = Resources.asByteSource(url).openBufferedStream()) {
            xmlMapper.readValue(in, Workbook.class);
        }
    }

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

        // Unrecognized field "" (class org.anarres.tests.Datasource$Column), not marked as ignorable (3 known properties: "calculation", "role", "members"])
        read("data.xml");
        // Unrecognized field "class" (class org.anarres.tests.Datasource$Column), not marked as ignorable (3 known properties: "calculation", "role", "members"])
        read("data-bad-location.xml");
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.run(args);
    }
}
