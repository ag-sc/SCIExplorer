/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql;

/**
 *
 * @author ABOROWI
 */
public class SPARQLUtility
{
    public static String getValue(String value)
    {
        return value.substring(value.lastIndexOf("/") + 1);
    }
    
    public static int getIntegerValue(String value)
    {
        return Integer.parseInt(value.substring(0, value.indexOf("^")));
    }
}
