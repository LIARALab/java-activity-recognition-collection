package org.liara.collection.operator;

import java.util.Iterator;

public class CompositionIterator implements Iterator<Operator>
{
  @Override
  public boolean hasNext () {
    return false;
  }

  @Override
  public Operator next () {
    return null;
  }
}
