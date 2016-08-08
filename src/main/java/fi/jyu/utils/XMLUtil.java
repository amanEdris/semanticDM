/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.jyu.utils;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author edris
 */
public class XMLUtil {
    
   public static String parseString(String fileName) throws Exception {
        XML xml = new XMLDocument(new File(fileName));
        String xmlString = xml.toString();
        return xmlString;
    }
    
   public Document parseXMLAsdoc(String fileName) throws Exception {
        File fr = new File(fileName);
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fac.newDocumentBuilder();
        Document doc = builder.parse(fr);
        return doc;
    }

   public Document parseStringAsdoc(String fileName) throws Exception {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fac.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(fileName.getBytes()));
        return doc;
    }

}
