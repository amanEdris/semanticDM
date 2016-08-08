/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.jyu.csv;

import java.util.ArrayList;

/**
 *
 * @author edris
 */
public class Row {

    private ArrayList<Cell> cellList;

   
    public Row() {
    }

    public Row(ArrayList<Cell> columnList) {
        this.setColumnList(columnList);
    }

    public ArrayList<Cell> getColumnList() {
        return cellList;
    }

    public void setColumnList(ArrayList<Cell> columnList) {
        this.cellList =(columnList);
    }
    


    @Override
    public String toString() {
        return "Instance{" + "columnList=" + cellList.toString() ;
    }
}
