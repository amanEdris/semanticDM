package fi.jyu.transform;

import fi.jyu.csv.Cell;
import fi.jyu.csv.CSVParser;
import fi.jyu.csv.Row;
import fi.jyu.csv.Dataset;
import fi.jyu.ontology.OntologyManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.util.TextUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class CsvToOntology {


    public Set<SWRLRule> createSWRLRule(int classColumn, OntologyManager manager, CSVParser csvClass) {
        Set<SWRLRule> ruleSwrl = new HashSet<SWRLRule>();

        Dataset dataset = csvClass.getDataset();
        ArrayList<Row> rows = dataset.getDataset();

        IRI ontologyIRI = IRI.create(manager.getNameSpace());
        SWRLVariable var = manager.getFactory().getSWRLVariable(IRI.create(ontologyIRI + "#x"));
        SWRLClassAtom classRule = manager.getFactory().getSWRLClassAtom(manager.getOntologyClass(), var);

        for (Row r : rows) {
            int counter = 1;
            Set<SWRLAtom> antecedent = new HashSet<SWRLAtom>();
            Set<SWRLAtom> consequent = new HashSet<SWRLAtom>();
            antecedent.add(classRule);
            antecedent.add(classRule);
            ArrayList<Cell> columns = r.getColumnList();
            int colNum = 1;
            for (Cell c : columns) {
                if (counter != classColumn) {
                    OWLDataProperty property = manager.createOwlDataProperty(c.getColumnName());
                    OWLDatatype dataType = manager.getFactory().getOWLDatatype(manager.getDataType(csvClass.getColumnRange().get(c.getColumnName())));
                    OWLLiteral lit = manager.getFactory().getOWLLiteral(c.getRecord(), dataType);
                    SWRLLiteralArgument litArg = manager.getFactory().getSWRLLiteralArgument(lit);
                    antecedent.add(manager.getFactory().getSWRLDataPropertyAtom(property, var, litArg));

                } else {
                    OWLDataProperty property = manager.createOwlDataProperty(c.getColumnName());
                    OWLDatatype dataType = manager.getFactory().getOWLDatatype(manager.getDataType(csvClass.getColumnRange().get(c.getColumnName())));
                    OWLLiteral lit = manager.getFactory().getOWLLiteral(c.getRecord(), dataType);
                    SWRLLiteralArgument litArg = manager.getFactory().getSWRLLiteralArgument(lit);
                    consequent.add(manager.getFactory().getSWRLDataPropertyAtom(property, var, litArg));
                }
                counter++;
            }
            SWRLRule rule = manager.getFactory().getSWRLRule(antecedent, consequent);
            ruleSwrl.add(rule);
        }
        return ruleSwrl;
    }

    public static void createLiteralProperty(OntologyManager manager, CSVParser csvClass) {
        IRI ontologyIRI = IRI.create(manager.getNameSpace());
        PrefixManager pm = new DefaultPrefixManager(null, null, ontologyIRI.toString());
        ArrayList<Row> rows = csvClass.getDataset().getDataset();
        for (Row rn : rows) {
            Set<OWLDataPropertyAssertionAxiom> domainsValues = new HashSet<OWLDataPropertyAssertionAxiom>();
            String individualName = manager.generateNames()+"d";
            OWLNamedIndividual individual = manager.getFactory().getOWLNamedIndividual(":" + individualName, pm);
            ArrayList<Cell> col = rn.getColumnList();
            OWLLiteral lit;
            for (Cell c : col) {
                OWLDataProperty property = manager.createOwlDataProperty(c.getColumnName());
                OWLDatatype dataType = manager.getFactory().getOWLDatatype(manager.getDataType(csvClass.getColumnRange().get(c.getColumnName())));
                String value = c.getRecord();
                if (TextUtils.isEmpty(value) || TextUtils.isBlank(value)) {
                    lit = manager.getFactory().getOWLLiteral("g", dataType);
                } else {
                    lit = manager.getFactory().getOWLLiteral(value, dataType);
                }
                domainsValues.add(manager.getFactory().getOWLDataPropertyAssertionAxiom(property, individual, lit));
            }
            manager.getManager().addAxioms(manager.getOntology(), domainsValues);
        }
    }


    public static void createOntologyClass(CSVParser csvClass, OntologyManager manager){
        createClass(manager, csvClass);
        createDataProperties(csvClass, manager);
    }
    
    
    public static void createDataProperties(CSVParser csvClass, OntologyManager manager) {
        Dataset d = csvClass.getDataset();
        Row r = d.getHeader();
        ArrayList<Cell> columns = r.getColumnList();
        Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();

        for (Cell header : columns) {
            OWLDataProperty property = manager.createOwlDataProperty(header.getColumnName());
            IRI dataType = manager.getDataType(csvClass.getColumnRange().get(header.getColumnName()));
            domainsAndRanges.add(manager.getFactory().getOWLDataPropertyDomainAxiom(property, manager.getOntologyClass()));
            domainsAndRanges.add(manager.getFactory().getOWLDataPropertyRangeAxiom(property, manager.getFactory().getOWLDatatype(manager.getDataType(csvClass.getColumnRange().get(header.getColumnName())))));
        }
        manager.getManager().addAxioms(manager.getOntology(),domainsAndRanges);
    }

    public static void createClass(OntologyManager manager, CSVParser csvClass) {
        Set<OWLDeclarationAxiom> classAxiom = new HashSet<OWLDeclarationAxiom>();
        classAxiom = manager.createClass((csvClass.getCSVClassName()));
        manager.getManager().addAxioms(manager.getOntology(), classAxiom);
    }
}
