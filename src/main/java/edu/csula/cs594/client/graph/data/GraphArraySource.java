/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.csula.cs594.client.graph.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public class GraphArraySource<K extends Integer,V extends Integer> implements Iterator<Entry<K,V>> {

    private final List<Integer> domain;
    private final List<Integer> value;
    private int pos = 0;
    private final int maxSize;
    

    public GraphArraySource(Integer[] domainArray, Integer[] valueArray) {        
        this.domain = Arrays.asList(domainArray);
        this.value = Arrays.asList(valueArray);
        
        if (null == domain || null == value) {
            throw new NullPointerException("Neither domain nor value may be null");
        }
        
        if (domain.size() != value.size()) {
            throw new IllegalArgumentException("domain and value arrays must be the same size");
        }
        
        maxSize = domain.size();
    }    
    
    public GraphArraySource(List<Integer> domain, List<Integer> value) {
        this.domain = domain;
        this.value = value;
        
        if (null == domain || null == value) {
            throw new NullPointerException("Neither domain nor value may be null");
        }
        
        if (domain.size() != value.size()) {
            throw new IllegalArgumentException("domain and value arrays must be the same size");
        }
        
        maxSize = domain.size();
    }

    @Override
    public boolean hasNext() {
        return (pos < maxSize);
    }

    @Override
    public Entry<K,V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Entry<K,V> e = new HashMap.SimpleEntry(domain.get(pos), value.get(pos));
        pos = pos + 1;
        return e;
    }

}
