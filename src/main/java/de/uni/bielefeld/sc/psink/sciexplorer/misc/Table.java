/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.misc;

import java.util.List;

/**
 *
 * @author ABOROWI
 */
public class Table
{
    private final List<String> header;
    private final List<List<String>> data;
    
    public Table(List<String> header, List<List<String>> data)
    {
        this.header = header;
        this.data = data;
    }

    public List<String> getHeader() {
        return header;
    }

    public List<List<String>> getData() {
        return data;
    }
}
