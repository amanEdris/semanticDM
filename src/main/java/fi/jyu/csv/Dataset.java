/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.jyu.csv;

import java.util.ArrayList;

/**
 *
 * 
 *
 * @author edris
 */
public class Dataset {

    private ArrayList<Row> dataset;
    
  
    public Dataset(ArrayList<Row> rows) {
        this.dataset = rows;

    }

    public Dataset() {
    }

    public void setDataset(ArrayList<Row> features) {
        for (Row i : features) {
            this.dataset.add(i);
        }
    }

    public ArrayList<Row> getDataset() {
        return dataset;
    }
    
    public Row  getHeader(){
        return dataset.get(0);
    }
    
    public void printDataset() {
        for (Row data : dataset) {
            System.out.println("===================================================================");
            ArrayList<Cell> instanceFeature = data.getColumnList();
            for (Cell i : instanceFeature) {
                System.out.println("Name:" + i.getColumnName() + "Value:" + i.getRecord() + "type:" + i.getRecord());
            }
        }
    }
    
    @Override
    public String toString() {
        return "Dataset{" + "dataset=" + dataset + '}';
    }
}
