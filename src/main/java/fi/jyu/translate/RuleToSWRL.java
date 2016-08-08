/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.jyu.translate;

import fi.jyu.utils.TextUtil;
import fi.jyu.utils.XMLUtil;
import fi.jyu.wordnet.WordnetSimilarity;
import fi.jyu.wordnet.WordnetSynonym;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.util.TextUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * This class handles the transformation of XML based if-then rules to Domain
 * Ontology based rules(SWRL)
 *
 *
 */
public class RuleToSWRL {

    private OWLOntology domainOntology = null;
    private OWLOntologyManager manager = null;
    private Document doc = null;
    private OWLDataFactory factory = null;
    private Map<String, String> literalVocabulary;
    private Map<String, OWLClass> ontologyOwlClassVocabulary;
    private Map<String, OWLDataProperty> ontologyOWLDataPropertyVocabulary;
    private Map<String, OWLNamedIndividual> ontologyOWLNamedIndividualVocabulary;
    private Map<String, OWLObjectProperty> ontologyOWLObjectPropertylVocabulary;

    public void setDomainOntology(OWLOntology domainOntology) {
        this.domainOntology = domainOntology;
    }

    public void setManager(OWLOntologyManager manager) {
        this.manager = manager;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public void setFactory(OWLDataFactory factory) {
        this.factory = factory;
    }

    public RuleToSWRL(String rulePath) {
        try {
            XMLUtil xmlUtil = new XMLUtil();
            this.setDoc(xmlUtil.parseStringAsdoc(rulePath));
            this.literalVocabulary = new HashMap<>();
        } catch (Exception ex) {
            Logger.getLogger(RuleToSWRL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Search term in the ontology
     *
     * @param term
     * @return
     */
    private boolean searchOntology(String term) {
        boolean found;
        if (ontologyOwlClassVocabulary.containsKey(term)
                || ontologyOWLDataPropertyVocabulary.containsKey(term)
                || ontologyOWLNamedIndividualVocabulary.containsKey(term)
                || ontologyOWLObjectPropertylVocabulary.containsKey(term)) {
            found = true;
        } else {
            found = false;
        }
        return found;
    }

    /**
     * Extract all the naming used for individuals, class, object and data
     * properties and create a search vocabulary
     *
     */
    public void constructOntologyVocabulary() {
        ontologyOwlClassVocabulary = new HashMap<>();
        ontologyOWLDataPropertyVocabulary = new HashMap<>();
        ontologyOWLNamedIndividualVocabulary = new HashMap<>();
        ontologyOWLObjectPropertylVocabulary = new HashMap<>();

        Set<OWLClass> classes = domainOntology.getClassesInSignature();
        Set<OWLNamedIndividual> individualPropertyAtom = domainOntology.getIndividualsInSignature();
        Set<OWLDataProperty> dataProperty = domainOntology.getDataPropertiesInSignature();
        Set<OWLObjectProperty> OWLObjectPropertyAtom = domainOntology.getObjectPropertiesInSignature();
        String tempName = null;

        for (OWLClass c : classes) {
            tempName = c.toString();
            tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
            tempName = TextUtil.formatName(tempName);
            ontologyOwlClassVocabulary.put(tempName, c);
        }
        for (OWLDataProperty d : dataProperty) {
            tempName = d.toString();
            tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
            tempName = TextUtil.formatName(tempName);
            ontologyOWLDataPropertyVocabulary.put(tempName, d);
        }
        for (OWLNamedIndividual i : individualPropertyAtom) {
            tempName = i.toString();
            tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
            tempName = TextUtil.formatName(tempName);
            ontologyOWLNamedIndividualVocabulary.put(tempName, i);
        }
        for (OWLObjectProperty o : OWLObjectPropertyAtom) {
            tempName = o.toString();
            tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
            tempName = TextUtil.formatName(tempName);
            ontologyOWLObjectPropertylVocabulary.put(tempName, o);
        }
    }

    /**
     * Return a set of SWRL by finding individual rules added in the rule XML
     *
     * @return Set of SWRL
     */
    public Set<SWRLRule> translate() throws Exception {
        NodeList nodeList = doc.getElementsByTagName("rule");
        if (nodeList == null) {
            throw new Exception("No rule tag in xml file");
        }
        Set<SWRLRule> ruleSwrl = new HashSet<SWRLRule>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Set<SWRLAtom> antecedent = new HashSet<SWRLAtom>();
            Set<SWRLAtom> consequent = new HashSet<SWRLAtom>();
            Node node = nodeList.item(i);
            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                switch (child.getNodeName()) {
                    case "if": {
                        generateAntecedent((Element) child, antecedent);
                        break;
                    }
                    case "then": {
                        generateConsequent((Element) child, consequent);
                        break;
                    }
                }
            }
            SWRLRule rules = factory.getSWRLRule(antecedent, consequent);
            ruleSwrl.add(rules);
        }
        return ruleSwrl;
    }

    /**
     * Create body part of the rule
     *
     * @param element
     * @param antecedent
     */
    private void generateAntecedent(Element element, Set<SWRLAtom> antecedent) throws Exception {
        String name = element.getAttribute("name");
        name = TextUtil.formatName(name);
        String operator = element.getAttribute("operator");
        String value = element.getAttribute("value");
        constructAtom(name, antecedent, value, operator);
        //System.out.printf("%s %s %s\n", name, operator, value);
    }

    /**
     * Create head part of the rule
     *
     * @param element
     * @param consequent
     */
    private void generateConsequent(Element element, Set<SWRLAtom> consequent) throws Exception {
        String cLass = element.getAttribute("class");
        cLass = TextUtil.formatName(cLass);
        String operator = element.getAttribute("operator");
        String score = element.getAttribute("score");
        constructAtom(cLass, consequent, score, operator);
        //System.out.printf("%s %s %s\n", cLass, operator, score);
    }

    /**
     * Check the term in the ontology and select the method applicable for
     * individuals to construct the rule atom for class, data Property, and
     * object Property.
     *
     * @param name
     * @param antecedent
     * @param value
     * @param operator
     */
    private void constructAtom(String name, Set<SWRLAtom> antecedent, String value, String operator) throws Exception {
        if (searchOntology(name)) {
            if (ontologyOwlClassVocabulary.containsKey(name)) {
                constructClassAtom(name, antecedent, value, operator);
            } else if (ontologyOWLDataPropertyVocabulary.containsKey(name)) {
                constructOwlDataPropertyAtom(name, antecedent, value, operator);
            } else if (ontologyOWLObjectPropertylVocabulary.containsKey(name)) {
                constructObjectPropertyAtom(name, antecedent, value, operator);
            } else {
                //@todo add more necessary swrl atom types 
            }
        } else {
            name = prepareTerm(name);
            constructAtom(name, antecedent, value, operator);
        }

    }

    private ArrayList<String> getOntologyTerms() {
        ArrayList<String> al = new ArrayList<>();
        Set<OWLClass> classes = domainOntology.getClassesInSignature();
        Set<OWLNamedIndividual> individualPropertyAtom = domainOntology.getIndividualsInSignature();
        Set<OWLDataProperty> dataProperty = domainOntology.getDataPropertiesInSignature();
        Set<OWLObjectProperty> OWLObjectPropertyAtom = domainOntology.getObjectPropertiesInSignature();
        String tempName = null;

        for (OWLClass c : classes) {
            tempName = c.toString();
            tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
            al.add(tempName.replaceAll("^\\s+|\\s+$|[-]+", "").replaceAll("[^\\p{Alpha}]", ""));
        }
        for (OWLDataProperty d : dataProperty) {
            tempName = d.toString();
            tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
            al.add(tempName.replaceAll("^\\s+|\\s+$|[-]+", "").replaceAll("[^\\p{Alpha}]", ""));
        }
        for (OWLNamedIndividual i : individualPropertyAtom) {
            tempName = i.toString();
            tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
            al.add(tempName.replaceAll("^\\s+|\\s+$|[-]+", "").replaceAll("[^\\p{Alpha}]", ""));
        }
        for (OWLObjectProperty o : OWLObjectPropertyAtom) {
            tempName = o.toString();
            tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
            al.add(tempName.replaceAll("^\\s+|\\s+$|[-]+", "").replaceAll("[^\\p{Alpha}]", ""));
        }
        return al;
    }

    /**
     * Prepare term
     *
     * @param term
     */
    private String prepareTerm(String term) throws Exception {
        String tempTerm = null;
        ArrayList<String> synonym = WordnetSynonym.findSynonyms(term);
        ArrayList<String> ontologyTerms = getOntologyTerms();

        Map<String, Map<String, Double>> tempTerms = new HashMap();
        double threshold = 0.8;
        for (int j = 0; j < synonym.size(); j++) {
            Map<String, Double> mapSimilarity = WordnetSimilarity.getSimilarList(synonym.get(j), ontologyTerms);
            String bMatch = WordnetSimilarity.getBestMatch(mapSimilarity);
            if (mapSimilarity.get(bMatch) >= threshold) {
                Map<String, Double> tempSimliarWords = new HashMap();
                tempSimliarWords.put(bMatch, mapSimilarity.get(bMatch));
                tempTerms.put(synonym.get(j), tempSimliarWords);
            } else {
                synonym.remove(j);
            }
        }

        if (tempTerms.size() > 1) {
            Map<String, Double> ms = WordnetSimilarity.getSimilarList(term, synonym);
            String b = WordnetSimilarity.getBestMatch(ms);
            tempTerm = tempTerms.get(b).keySet().toString();
            tempTerm = TextUtil.formatName(tempTerm);
        } else if (tempTerms.size() == 1) {
            Map<String, Double> ms = tempTerms.get(tempTerms.keySet().toArray()[0]);
            tempTerm = (String) ms.keySet().toArray()[0];
            tempTerm = TextUtil.formatName(tempTerm);

        } else {
            throw new Exception("There is no term in the ontology tha matches " + term);
        }

        return tempTerm;
    }

    /**
     * Prepare an atom for object property type
     *
     * @param name
     * @param antecedent
     * @param value
     * @param operator
     */
    private void constructObjectPropertyAtom(String name, Set<SWRLAtom> antecedent, String value, String operator) {

        SWRLVariable var1 = null, var2 = null, var3 = null;
        String classNm, classObject = null;
        OWLObjectProperty o = ontologyOWLObjectPropertylVocabulary.get(name);

        classNm = constructObjectSubjectAtom(name, antecedent);
        classObject = constructObjectObjectAtom(name, antecedent);
        var2 = initalizeVariable(classNm, var2);
        var3 = initalizeVariable(classObject, var3);
        antecedent.add(factory.getSWRLObjectPropertyAtom(o, var2, var3));

        constructBuiltinAtom(classObject, operator, value, null, antecedent);

    }

    /**
     * Prepare a class atom for class type objects
     *
     * @param name
     * @param antecedent
     * @param value
     * @param operator
     */
    private void constructClassAtom(String name, Set<SWRLAtom> antecedent, String value, String operator) {
        SWRLVariable var = null;
        OWLClass c = ontologyOwlClassVocabulary.get(name);

        var = initalizeVariable(name, var);

        SWRLClassAtom classRule = factory.getSWRLClassAtom(c, var);
        antecedent.add(classRule);

        if (!TextUtils.isBlank(value) && !TextUtils.isBlank(operator)) {
            constructBuiltinAtom(name, operator, value, null, antecedent);
        }
    }

    /**
     * Construct the subject atom of a OWL data property
     *
     * @param name
     * @param antecedent
     * @return
     */
    private String constructpropertySubjectAtom(String name, Set<SWRLAtom> antecedent) {
        String className = null;

        OWLDataProperty p = ontologyOWLDataPropertyVocabulary.get(name);
        Set<OWLDataPropertyDomainAxiom> sgrp = domainOntology.getDataPropertyDomainAxioms(p);

        for (OWLDataPropertyDomainAxiom a : sgrp) {
            OWLClassExpression cl = a.getDomain().getNNF();
            className = TextUtil.formatName(cl.toString().substring(cl.toString().lastIndexOf("/") + 1));
            constructClassAtom(className, antecedent, " ", " ");
        }
        return className;
    }

    /**
     * Construct an object atom of an OWL object property
     *
     * @param name
     * @param antecedent
     * @return
     */
    private String constructObjectSubjectAtom(String name, Set<SWRLAtom> antecedent) {
        String className = null;

        OWLObjectProperty o = ontologyOWLObjectPropertylVocabulary.get(name);
        Set<OWLObjectPropertyDomainAxiom> sgrp = domainOntology.getObjectPropertyDomainAxioms(o);

        for (OWLObjectPropertyDomainAxiom a : sgrp) {
            OWLClassExpression cl = a.getDomain().getNNF();
            className = TextUtil.formatName(cl.toString().substring(cl.toString().lastIndexOf("/") + 1));
            constructClassAtom(className, antecedent, " ", " ");
        }
        return className;
    }

    /**
     * Construct the object class atom and object property
     *
     * @param name
     * @param antecedent
     * @return
     */
    private String constructObjectObjectAtom(String name, Set<SWRLAtom> antecedent) {
        String className = null;

        OWLObjectProperty o = ontologyOWLObjectPropertylVocabulary.get(name);
        Set<OWLObjectPropertyRangeAxiom> sgrp = domainOntology.getObjectPropertyRangeAxioms(o);

        for (OWLObjectPropertyRangeAxiom a : sgrp) {
            OWLClassExpression cl = a.getRange().getNNF();
            className = TextUtil.formatName(cl.toString().substring(cl.toString().lastIndexOf("/") + 1));
            constructClassAtom(className, antecedent, " ", " ");
        }
        return className;
    }

    /**
     * Construct the OWL data property atom of a given name
     *
     * @param name
     * @param antecedent
     * @param value
     * @param operator
     */
    private void constructOwlDataPropertyAtom(String name, Set<SWRLAtom> antecedent, String value, String operator) {
        SWRLVariable var1 = null, var2;
        String classNm = null;

        var1 = initalizeVariable(name, var1);
        OWLDataProperty p = ontologyOWLDataPropertyVocabulary.get(name);

        classNm = constructpropertySubjectAtom(name, antecedent);

        var2 = factory.getSWRLVariable(IRI.create(manager.getOntologyDocumentIRI(domainOntology) + "#" + literalVocabulary.get(classNm)));
        antecedent.add(factory.getSWRLDataPropertyAtom(p, var2, var1));
        Set<OWLDataPropertyRangeAxiom> sgdp = domainOntology.getDataPropertyRangeAxioms(p);
        OWLDataRange r = null;
        for (OWLDataPropertyRangeAxiom a : sgdp) {
            r = a.getRange();
        }
        constructBuiltinAtom(name, operator, value, r.asOWLDatatype(), antecedent);

    }

    /**
     * Construct individual builtin for less than ,greater than ,equal ,not
     * equal ,less and greater
     *
     * @param name
     * @param operator
     * @param value
     * @param od
     * @param antecedent
     */
    private void constructBuiltinAtom(String name, String operator, String value, OWLDatatype od, Set<SWRLAtom> antecedent) {
        if (!TextUtils.isBlank(value) && !TextUtils.isBlank(operator)) {
            if (od == null) {
                SWRLIArgument ind = factory.getSWRLIndividualArgument(ontologyOWLNamedIndividualVocabulary.get(TextUtil.formatName(value)));
                SWRLIArgument variable = factory.getSWRLIndividualArgument(factory.getOWLNamedIndividual(IRI.create(manager.getOntologyDocumentIRI(domainOntology) + "#" + literalVocabulary.get(name))));

                switch (operator) {
                    case "equal": {
                        SWRLSameIndividualAtom sameInd = factory.getSWRLSameIndividualAtom(ind, variable);
                        antecedent.add(sameInd);
                        break;
                    }
                    case "notEqual": {
                        SWRLDifferentIndividualsAtom diffInd = factory.getSWRLDifferentIndividualsAtom(ind, variable);
                        antecedent.add(diffInd);
                        break;
                    }
                }
            } else {
                SWRLVariable var2 = factory.getSWRLVariable(IRI.create(manager.getOntologyDocumentIRI(domainOntology) + "#" + literalVocabulary.get(name)));
                OWLLiteral lit = factory.getOWLLiteral(value, od);
                SWRLLiteralArgument litArg = factory.getSWRLLiteralArgument(lit);
                List<SWRLDArgument> list = new ArrayList<>();
                list.add(var2);
                list.add(litArg);

                switch (operator) {
                    case "equal": {
                        SWRLBuiltInAtom builtins = factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.EQUAL.getIRI(), list);
                        antecedent.add(builtins);
                        break;
                    }
                    case "notEqual": {
                        SWRLBuiltInAtom builtins = factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.NOT_EQUAL.getIRI(), list);
                        antecedent.add(builtins);
                        break;
                    }
                    case "lessThan": {
                        SWRLBuiltInAtom builtins = factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.LESS_THAN.getIRI(), list);
                        antecedent.add(builtins);
                        break;
                    }
                    case "lessOrEqual": {
                        SWRLBuiltInAtom builtins = factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.LESS_THAN_OR_EQUAL.getIRI(), list);
                        antecedent.add(builtins);
                        break;
                    }
                    case "greaterThan": {
                        SWRLBuiltInAtom builtins = factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.GREATER_THAN.getIRI(), list);
                        antecedent.add(builtins);
                        break;
                    }
                    case "greaterOrEqual": {
                        SWRLBuiltInAtom builtins = factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.GREATER_THAN_OR_EQUAL.getIRI(), list);
                        antecedent.add(builtins);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Initalize variable of each atom and check the variable name is not
     * reserved/used
     *
     * @param name
     * @param var1
     * @return
     */
    private SWRLVariable initalizeVariable(String name, SWRLVariable var1) {

        if (literalVocabulary.containsKey(name)) {
            var1 = factory.getSWRLVariable(IRI.create(manager.getOntologyDocumentIRI(domainOntology) + "#" + literalVocabulary.get(name)));
        } else {
            char variable = this.generateVariable();
            literalVocabulary.put(name, String.valueOf(variable));
            var1 = factory.getSWRLVariable(IRI.create(manager.getOntologyDocumentIRI(domainOntology) + "#" + variable));
        }

        return var1;
    }

    private char generateVariable() {
        char tempKey = TextUtil.getRandomCharacter();
        if (!literalVocabulary.keySet().contains(tempKey)) {
            tempKey = TextUtil.getRandomCharacter();
        }
        return tempKey;
    }
}
