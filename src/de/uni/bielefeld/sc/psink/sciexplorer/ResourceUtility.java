
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.sc.psink.sciexplorer;

import java.io.InputStream;

/**
 * Resource-utility. Is used to load resources.
 * 
 * @author ABOROWI
 */
public class ResourceUtility {
	public static InputStream getResourceAsStream(String resource) {
		InputStream is = ResourceUtility.class.getResourceAsStream(resource);
		if (is == null) {
			System.err.println("ResourceUtility: Could not open stream '" + resource + "'!");
		}
		return is;
	}
}
