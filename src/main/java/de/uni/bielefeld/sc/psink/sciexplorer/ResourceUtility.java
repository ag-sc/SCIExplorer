
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer;

import java.io.InputStream;

/**
 * Resource-utility. Is used to load resources.
 * 
 * @author ABOROWI
 */
public class ResourceUtility {
	public static InputStream getResourceAsStream(String resource) {
            System.out.println("ResourceUtility:");
            System.out.println("\tresource: " + resource);
            System.out.println("\tclass: " + ResourceUtility.class);
		InputStream is = ResourceUtility.class.getResourceAsStream("/" + resource);
		if (is == null) {
			System.err.println("ResourceUtility: Could not open stream '" + resource + "'!");
		}
		return is;
	}
}
