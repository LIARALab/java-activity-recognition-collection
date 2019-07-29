/*
 * Copyright (C) 2019 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
 *
 * Permission is hereby granted,  free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,  including without limitation the rights
 * to use,  copy, modify, merge,  publish,  distribute, sublicense,  and/or sell
 * copies  of the  Software, and  to  permit persons  to  whom  the  Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above  copyright  notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED,  INCLUDING  BUT  NOT LIMITED  TO THE  WARRANTIES  OF MERCHANTABILITY,
 * FITNESS  FOR  A PARTICULAR  PURPOSE  AND  NONINFRINGEMENT. IN NO  EVENT SHALL
 * THE  AUTHORS OR  COPYRIGHT  HOLDERS  BE  LIABLE FOR  ANY  CLAIM,  DAMAGES  OR
 * OTHER  LIABILITY, WHETHER  IN  AN  ACTION  OF  CONTRACT,  TORT  OR  OTHERWISE,
 * ARISING  FROM,  OUT  OF OR  IN  CONNECTION  WITH THE  SOFTWARE OR  THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.liara.collection.operator;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.com.google.common.collect.Iterators;
import org.liara.collection.Collection;

public interface Operator {
  /**
   * Apply this operator to another one and return a composition of both operators.
   * 
   * Return a composition operator that apply sequentially each of it's children operator to the given collection.
   * 
   * @param child An operator to compose.
   * 
   * @return A composition operator that apply sequentially each of it's children operator to the given collection.
   */
  default @NonNull Operator apply (@NonNull final Operator child) {
    if (child instanceof Composition) {
      final Composition composition = (Composition) child;
      return new Composition(Iterators.concat(Iterators.singletonIterator(this), composition.iterator()));
    } else {
      return new Composition(this, child);
    }
  }
  
  /**
   * Apply this operator to a given collection and return the result.
   *
   * @param input A collection to update.
   *
   * @return The result of the operation.
   */
  @NonNull Collection apply (@NonNull final Collection input);
}
