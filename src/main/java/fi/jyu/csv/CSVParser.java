/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.jyu.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.util.TextUtils;

/**
 * This class contains algorithm for recognizing data range
 * The algorithm has been adopted from the research topic
 * "Tabular data ontology generation" by Kumar Sharma1, Ujjal Marjit2*, and Utpal Biswas3
 * 
 * @author edris
 */

public class CSVParser {

    private String csvSplitBy = ",";
    private Dataset dataset;
    private String CSVclass;
    private String filePath;
    private BufferedReader br;
    private Map<String,String> columnRange;

    public Map<String, String> getColumnRange() {
        return columnRange;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public final void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public CSVParser(String filePath) throws FileNotFoundException {
        try {
            this.filePath = filePath;
            this.CSVclass = FilenameUtils.getBaseName(filePath);
            this.setDataset(this.readData());
            this.generateDataRange();

        } catch (IOException ex) {
            Logger.getLogger(CSVParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public final void generateDataRange() {
        columnRange = new HashMap<>();
        Row r = dataset.getHeader();
        ArrayList<Cell> columns = r.getColumnList();
        for (Cell header : r.getColumnList()) {
            columnRange.put(header.getColumnName(), this.getDataRange(header, this.getValuesForHeaders(header)));
        }
    }


    public String getDataRange(Cell header, Set<String> values) {
        Set<String> ranges = new HashSet<>();
        String range = "String";
        ranges.add("Boolean");
        ranges.add("Float");
        ranges.add("Integer");
        ranges.add("String");

        for (String rn : ranges) {
            Boolean matched = true;
            range = rn;
            String pattern = getPattern(range);
            for (Iterator<String> it = values.iterator(); it.hasNext();) {
                String v = it.next();
                if (!v.matches(pattern) || v.toCharArray().length > sizeOf(range)) {
                    matched = false;
                }
            }
            if (matched == true) {
                break;
            } else {
                range = "String";
            }

        }
        return range;

    }

    public Set<String> getValuesForHeaders(Cell header) {
        Set<String> values = new HashSet<>();

        ArrayList<Row> rows = dataset.getDataset();
        String range = null;
        for (Row r : rows) {
            if (!Arrays.asList(r).contains("?")) {
                ArrayList<Cell> columns = r.getColumnList();
                for (Cell c : columns) {
                    if (c.getColumnName().equalsIgnoreCase(header.getColumnName())) {
                        if (!TextUtils.isEmpty(c.getRecord())) {
                            values.add(c.getRecord().replaceAll("[-]+", ""));
                        }
                    }
                }
            }
        }
        return values;
    }

    public Dataset readData() throws IOException {
        String line;
        ArrayList<Row> rowDataset = new ArrayList<Row>();
        Row headerRow = readHeader();//skip header
        ArrayList<Cell> columns = headerRow.getColumnList();

        while ((line = br.readLine()) != null) {//Arrays.asList(row).contains(" ")
            String[] row = line.split(csvSplitBy);
            //remove missing value records
            if (!Arrays.asList(row).contains("?") || !Arrays.asList(row).contains("")) {
                ArrayList<Cell> tempColumns = new ArrayList<Cell>();
                for (int i = 0; i < row.length; i++) {
                    Cell tempColumn = new Cell();
                    String name = columns.get(i).getColumnName();
                    tempColumn.setColumnNameSet(true);
                    tempColumn.setColumnName(name);
                    tempColumn.setRecord(row[i]);
                    tempColumns.add(i, tempColumn);
                }
                Row newInstance = new Row(tempColumns);
                rowDataset.add(newInstance);
            }
        }
        this.dataset = new Dataset(rowDataset);    
        return this.dataset;
    }

    public Row readHeader() throws IOException {
        ArrayList<Cell> columns = new ArrayList<Cell>();
        Row row = new Row(columns);
        br = new BufferedReader(new FileReader(filePath));
        String text = br.readLine();
        List<String> list = new ArrayList<String>(Arrays.asList(text.split(csvSplitBy)));

        for (String name : list) {
            Cell c = new Cell();
            c.setColumnName(name);
            columns.add(c);
        }

        row.setColumnList(columns);
        return row;
    }

    public String getCSVClassName() {
        return CSVclass;
    }

    public void close() throws IOException {
        br.close();
    }

    private int sizeOf(String range) {
        int sizeThreshold = 0;

        switch (range) {
            case "Boolean": {
                sizeThreshold = 5;
                break;
            }
            case "Float": {
                sizeThreshold = 10;
                break;
            }
            case "Integer": {
                sizeThreshold = 13;
                break;
            }
            case "String": {
                sizeThreshold = 15;
                break;
            }
        }
        return sizeThreshold;
    }

    private String getPattern(String range) {
        String pattern = null;
        switch (range) {

            case "Float": {
                pattern = "\\d*\\.\\d+";
                break;
            }
            case "Integer": {
                pattern = "^[0-9]*";
                break;
            }
            case "String": {
                pattern = "[a-zA-Z]*";
                break;
            }
            case "Boolean": {
                pattern = "^(T|F|1|0|True|False|Yes|No)$";
                break;
            }
        }
        return pattern;
    }
}
