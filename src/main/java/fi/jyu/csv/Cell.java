package fi.jyu.csv;

import fi.jyu.utils.TextUtil;


/**
 *
 * @author edris
 */
public class Cell {
    String columnName;
    String record;
    boolean columnNameSet= false;

    public void setColumnNameSet(boolean columnNameSet) {
        this.columnNameSet = columnNameSet;
    }

    public boolean isColumnNameSet() {
        return columnNameSet;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String name) {
        String tName = name;
        if(columnNameSet == false){
          tName= TextUtil.formatOntologyName(name); 
          columnNameSet = true;
        }
        this.columnName = tName;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record.replaceAll("\\s", "");
    }

    @Override
    public String toString() {
        return "InstanceProperty{" + "name=" + columnName + ", record=" + record.toString() + '}';
    }
    
    
    
}
