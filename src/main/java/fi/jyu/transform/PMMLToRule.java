package fi.jyu.transform;

import org.w3c.dom.Document;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PMMLToRule {
    static  String PMML_file_path;
    static  final String XSL_file_path_decisionTree = "src/resources/xsl/decisionToSWRL.xsl";
    
    public PMMLToRule(String PMMLpath) {
        PMML_file_path = PMMLpath;
    }
//    public static void main(String[] args) {
//        try {
//            PMMLToRule ptor=new PMMLToRule("/Users/edris/Desktop/onto_test/Mastersproject/Decision tree PMML/WBC.pmml"
//                    );
//            ptor.Transform();
//        } catch (TransformerFactoryConfigurationError ex) {
//            Logger.getLogger(PMMLToRule.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            Logger.getLogger(PMMLToRule.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    /**
     * @return int mining type 0 DecisionTree, 1 Association Rules, 2 Clustering
     */
    private static int getMiningModel(Element p) throws Exception{
        int miningType = 0;
        Element pmml = p;
        Node dic = getNode(pmml, "DataDictionary");
        Node model = dic.getNextSibling().getNextSibling();
        if (model.getNodeName().equals("AssociationModel")) {
            miningType = 1;
        } else if (model.getNodeName().equals("TreeModel")) {
            miningType = 0;
        }
        return miningType;
    }

    public static Node getNode(Element el, String elementName) {
        NodeList node = el.getChildNodes();
        for (int i = 0; i < node.getLength(); i++) {
            Node n = node.item(i);
            if (elementName.equals(n.getNodeName())) {
                return n;
            }
        }
        return null;
    }

    public static String Transform() throws TransformerFactoryConfigurationError, Exception {
        File stylesheet = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        String XMLFile = null;
        try {
            File datafile = new File(PMML_file_path);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(datafile);

            Element pmml = document.getDocumentElement();
            int getMiningModel = PMMLToRule.getMiningModel(pmml);
            if (getMiningModel == 0) {
                stylesheet = new File(XSL_file_path_decisionTree);
            }

            TransformerFactory tFactory = TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(stylesheet);
            Transformer transformer = tFactory.newTransformer(stylesource);
            StringWriter writer = new StringWriter();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            XMLFile = writer.toString();
            System.out.println("who is edits"+XMLFile);
            return XMLFile;
        } catch (TransformerConfigurationException tce) {
            // Error generated by the parser
            System.out.println("\n** Transformer Factory error");
            System.out.println("   " + tce.getMessage());
            
            Throwable x = tce;
            if (tce.getException() != null) {
                x = tce.getException();
            }

        } catch (TransformerException te) {
            System.out.println("\n** Transformation error");
            System.out.println("   " + te.getMessage());

            Throwable x = te;
            if (te.getException() != null) {
                x = te.getException();
            }

        } catch (IOException ioe) {
            throw new Exception("File not found");
        } catch (Exception ex) {
            Logger.getLogger(PMMLToRule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return XMLFile;
    }
}
