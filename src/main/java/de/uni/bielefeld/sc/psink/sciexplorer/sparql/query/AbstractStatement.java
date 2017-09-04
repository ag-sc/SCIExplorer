/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query;

import java.io.Serializable;

/**
 * Abstract SPARQL-statement.
 * @author ABOROWI
 */
public abstract class AbstractStatement implements Serializable
{
    @Override
    public abstract String toString();
}
