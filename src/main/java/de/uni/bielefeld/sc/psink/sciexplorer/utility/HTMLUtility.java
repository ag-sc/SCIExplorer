/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.utility;

/**
 * HTML-utility
 * @author ABOROWI
 */
public class HTMLUtility {

    public static final String NEWLINE = "<br/>";

    public static String noLineBreak(String string) {
        return "<nobr>" + string + "</nobr>";
    }

    public static String bold(String string) {
        return "<b>" + string + "</b>";
    }

    public static String link(String address, String text) {
        return "<a href=\"" + address + "\">" + text + "</a>";
    }

    public static String linkInNewTab(String address, String text) {
        return "<a href=\"" + address + "\" target=\"_blank\">" + text + "</a>";
    }
    
}
