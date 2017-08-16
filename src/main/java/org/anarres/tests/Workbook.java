/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.tests;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author shevek
 */
@XmlRootElement(name = "workbook")
public class Workbook {

    @XmlElementWrapper(name="datasources")
    @XmlElement(name="datasource")
    public List<Datasource> datasources;
}
