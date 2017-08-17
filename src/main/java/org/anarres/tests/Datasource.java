/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.tests;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 *
 * @author shevek
 */
public class Datasource {

    public static class Column {

        @XmlAttribute
        public String role; // measure

        public static class Calculation {

            @XmlAttribute(name = "class")
            public String _class;   // tableau
            @XmlAttribute
            public String formula;
        }

        @XmlElement(name = "calculation")
        public Calculation calculation;

        public static class Member {

            @XmlAttribute
            public String value;
        }

        @XmlElementWrapper(name = "members")
        @XmlElement(name = "member")
        public List<Member> members;
    }

    @XmlElement(name = "column")
    public List<Column> columns;
}
