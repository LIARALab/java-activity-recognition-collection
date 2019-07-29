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

package org.liara.collection.operator.filtering;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;
import org.liara.expression.Expression;

public interface Filter
  extends Operator
{
  /**
   * Return a new filtering operation based upon the given predicate.
   *
   * @param predicate A predicate to use for filtering.
   *
   * @return A new filtering operation based upon the given predicate.
   */
  static @NonNull Filter expression (@NonNull final Expression<@NonNull Boolean> predicate) {
    return new ExpressionFilter(predicate);
  }

  /*
    static @NonNull Filter exists (@NonNull final ModelCollection collection) {
      return new ExistsFilter(collection);
    }
  */

  /**
   * @see Operator#apply(Collection)
   */
  @Override
  default @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof FilterableCollection) {
      return ((FilterableCollection) input).addFilter(this);
    }

    return input;
  }

  /**
   * @return The filtering predicate.
   */
  @NonNull Expression<@NonNull Boolean> getExpression ();
}
