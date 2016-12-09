package org.vitrivr.cineast.core.data;

import java.io.Serializable;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class DynamicGrid<T> implements Serializable {

  private static final long serialVersionUID = -2284330171007553138L;
  private final TIntObjectHashMap<TIntObjectHashMap<T>> grid = new TIntObjectHashMap<TIntObjectHashMap<T>>();
  private final T defaultElement;

  public DynamicGrid(T defaultElement) {
    this.defaultElement = defaultElement;
  }

  public DynamicGrid() {
    this(null);
  }
  
  public DynamicGrid(T defaultElement, T[][] data){
    this(defaultElement);
    for(int x = 0; x < data.length; ++x){
      T[] arr = data[x];
      TIntObjectHashMap<T> map = new TIntObjectHashMap<>();
      for(int y = 0; y < arr.length; ++y){
        T element = arr[y];
        if(element != null){
          map.put(y, element);
        }
      }
      map.compact();
      grid.put(x, map);
    }
    grid.compact();
  }

  public DynamicGrid<T> setElement(int x, int y, T element) {
    if (!grid.containsKey(x)) {
      grid.put(x, new TIntObjectHashMap<>());
    }
    grid.get(x).put(y, element);
    return this;
  }

  public T get(int x, int y) {
    if (!grid.containsKey(x)) {
      return defaultElement;
    }
    TIntObjectHashMap<T> map = grid.get(x);
    if (!map.containsKey(y)) {
      return defaultElement;
    }
    return map.get(y);
  }

  public boolean isset(int x, int y) {
    if (!grid.containsKey(x)) {
      return false;
    }
    return grid.get(x).containsKey(y);
  }
  
  /**
   * returns and removes the element at (x,y).
   * @return the element at position (x,y) or null if there was no such element 
   */
  public T remove(int x, int y){
    if (!grid.containsKey(x)) {
      return null;
    }
    TIntObjectHashMap<T> map = grid.get(x);
    if (!map.containsKey(y)) {
      return null;
    }
    T _return = map.remove(y);
    if(map.isEmpty()){
      grid.remove(x);
    }
    return _return;
  }
  
  /**
   * compacts the grid by removing unnecessary data structures.
   */
  public void compact(){
    TIntIterator iter = grid.keySet().iterator();
    while(iter.hasNext()){
      int x = iter.next();
      TIntObjectHashMap<T> map = grid.get(x);
      if(map.isEmpty()){
        grid.remove(x);
      }else{
        map.compact();
      }
    }
    grid.compact();
  }

}
