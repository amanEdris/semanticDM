/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.jyu.ontology;

import com.google.common.base.Optional;
import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang.WordUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

/**
 *
 * @author edris
 */
public class OntologyManager {

    private OWLOntologyManager manager = null;
    private OWLDataFactory factory = null;
    private OWLOntology ontology = null;
    private OWLClass ontologyClass = null;
    private String nameSpace = null;

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public void setManager(OWLOntologyManager manager) {
        this.manager = manager;
    }

    public OWLDataFactory getFactory() {
        return factory;
    }

    public void setFactory(OWLDataFactory factory) {
        this.factory = factory;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }

    public OWLClass getOntologyClass() {
        return ontologyClass;
    }

    public void setOntologyClass(OWLClass ontologyClass) {
        this.ontologyClass = ontologyClass;
    }

    public void createOntology() throws OWLOntologyCreationException {
        //Create IRI 
        IRI ontologyIRI = IRI.create(nameSpace);
        //Create version IRI
        IRI versionIRI = IRI.create(ontologyIRI + "version1");
        //specifiy ontology ID and Version IRI
        OWLOntologyID ontologyID2 = new OWLOntologyID(
                Optional.of(ontologyIRI), Optional.of(versionIRI));
        ontology = manager.createOntology(ontologyID2);
    }

    public void saveOntology(String filePath) throws OWLOntologyStorageException {
        //Create a file for the new format
        File fileformated = new File(filePath);
        //Save the ontology in a different format
        OWLDocumentFormat format = manager.getOntologyFormat(ontology);
        //Save ontology in Turtle format
        TurtleDocumentFormat turtleFormat = new TurtleDocumentFormat();

        if (format.isPrefixOWLOntologyFormat()) {
            turtleFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        manager.saveOntology(ontology, turtleFormat, IRI.create(fileformated.toURI()));
    }

    public void readOntology(String filePath) {
        String ontFile = filePath;
        String prefix = "file:";

        if (!ontFile.startsWith("/")) {
            prefix += "/";
        }

        URI basePhysicalURI = URI.create(prefix + ontFile.replace("\\", "/"));
        this.setManager(OWLManager.createOWLOntologyManager());
        try {
            this.setOntology(manager.loadOntologyFromOntologyDocument(IRI.create(basePhysicalURI)));
            this.setFactory(OWLManager.getOWLDataFactory());

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    public void setOntologyWithRules(Set<SWRLRule> swrlRules) throws IOException {
        Set<SWRLRule> rules = swrlRules;
        for (SWRLRule rule : rules) {
            manager.applyChange(new AddAxiom(this.getOntology(), rule));
        }
    }

    public Set<OWLDeclarationAxiom> createClass(String className) {
        IRI personIri = IRI.create(nameSpace + "#" + WordUtils.capitalizeFully(className, new char[]{'.'}));
        ontologyClass = factory.getOWLClass(personIri);
        //create declaration axiom
        Set<OWLDeclarationAxiom> classAxiom = new HashSet<OWLDeclarationAxiom>();
        classAxiom.add(factory.getOWLDeclarationAxiom(ontologyClass));
        //apply changes to ontology
        return classAxiom;
    }

    public void checkConsistency() {
        IRI ontologyIRI = IRI.create(nameSpace);
        //get ontology 
        ontology = manager.getOntology(ontologyIRI);
        //checking consistency
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences();
        boolean consistent = reasoner.isConsistent();
        System.out.println("The ontology is consitsence:" + consistent);
    }

    public OWLDataProperty createOwlDataProperty(String name) {
        OWLDataProperty property = factory.getOWLDataProperty(IRI.create(this.nameSpace + name));
        return property;
    }

    public SWRLLiteralArgument createSwrlLiteralArgument(String value, OWLDatatype dataType) {
        OWLLiteral lit = factory.getOWLLiteral(value, dataType);
        SWRLLiteralArgument litArg = factory.getSWRLLiteralArgument(lit);
        return litArg;
    }

    private static String formatDataPropertyName(String h) {
        //remove inital space, lowercase letters, remove hypens
        h = WordUtils.capitalizeFully(h.trim().toLowerCase().replaceAll("[-]+", " "));
        //inital letter to lowercase
        h = Introspector.decapitalize(h);
        return h.replaceAll("[^A-Za-z]", "");//remove all spaces
    }

    public static IRI getDataType(String dataType) {
        IRI dataTypeIri = null;
        if (dataType == "String") {
            dataTypeIri = OWL2Datatype.XSD_STRING.getIRI();
        } else if (dataType == "Integer") {
            dataTypeIri = OWL2Datatype.XSD_INTEGER.getIRI();
        } else if (dataType == "Boolean") {
            dataTypeIri = OWL2Datatype.XSD_BOOLEAN.getIRI();
        } else if (dataType == "Float") {
            dataTypeIri = OWL2Datatype.XSD_FLOAT.getIRI();
        }

        return dataTypeIri;
    }

    public void printOntologyInturtleFormat(OWLOntology ontology) throws OWLOntologyStorageException {
        //get the format of the ontology
        OWLDocumentFormat format = manager.getOntologyFormat(ontology);
        System.out.println("Inital ontology format:" + format);
        //Save ontology in Turtle format
        TurtleDocumentFormat turtleFormat = new TurtleDocumentFormat();

        if (format.isPrefixOWLOntologyFormat()) {
            turtleFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        manager.saveOntology(ontology, turtleFormat, System.out);
    }

    private char getRandomCharacter() {
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        return alphabet[random(alphabet.length)];

    }

    public String generateNames() {
        StringBuilder sb = new StringBuilder();
        int size = random(25) + random(25);
        for (int i = 0; i < (size == 0 ? 1 : size); i++) {
            sb.append(getRandomCharacter());
        }

        return sb.toString();
    }

    private int random(int length) {
        return new Random().nextInt(length);
    }
}
