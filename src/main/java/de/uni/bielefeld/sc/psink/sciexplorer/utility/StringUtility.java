/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.utility;

import java.util.Collection;

/**
 * String-utility
 * @author ABOROWI
 */
public class StringUtility
{   
    @SuppressWarnings("rawtypes")
	public static String join(String separator, Iterable objects)
    {
        StringBuilder builder = new StringBuilder();
        for(Object object : objects)
        {
            builder.append(object.toString());
            builder.append(separator);
        }
        if(builder.length() > 0)
        {
            builder.setLength(builder.length() - separator.length());
        }
        return builder.toString();
    }
    
    public static String join(String separator, Collection<String> parts)
    {
        StringBuilder builder = new StringBuilder();
        for(String part : parts)
        {
            builder.append(part);
            builder.append(separator);
        }
        if(builder.length() > 0)
        {
            builder.setLength(builder.length() - separator.length());
        }
        return builder.toString();
    }
    
    public static String join(String separator, String... parts)
    {
        StringBuilder builder = new StringBuilder();
        for(String part : parts)
        {
            builder.append(part);
            builder.append(separator);
        }
        if(builder.length() > 0)
        {
            builder.setLength(builder.length() - separator.length());
        }
        return builder.toString();
    }
}
