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

package org.liara.collection.operator.ordering;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;
import org.liara.expression.Expression;

/**
 * An ordering operation.
 */
public interface Order
  extends Operator
{
  /**
   * Create and return a new ascending ordering operation of the given expression.
   *
   * @param expression An expression to order.
   *
   * @return A new ascending ordering operation of the given expression.
   */
  static @NonNull Order expression (@NonNull final Expression<?> expression) {
    return new ExpressionOrder(expression);
  }

  /**
   * Create and return a new ordering operation of the given expression in the given direction.
   *
   * @param expression An expression to order.
   * @param direction  The ordering direction.
   *
   * @return A new ascending ordering operation of the given expression in the given direction.
   */
  static @NonNull Order expression (
    @NonNull final Expression<?> expression,
    @NonNull final OrderingDirection direction
  ) {
    return new ExpressionOrder(expression, direction);
  }

  /**
   * @see Operator#apply(Collection)
   */
  @Override
  default @NonNull Collection apply (@NonNull final Collection collection) {
    if (collection instanceof OrderableCollection) {
      return ((OrderableCollection) collection).orderBy(this);
    }

    return collection;
  }

  /**
   * @return The expression that compute the value that is ordered.
   */
  @NonNull Expression<?> getExpression ();

  /**
   * @return The direction of the ordering operation.
   */
  @NonNull OrderingDirection getDirection ();

  /**
   * Return a new ordering operation that is a copy of this one with the given ordering direction.
   *
   * @param direction A new ordering direction.
   *
   * @return A new ordering operation that is a copy of this one with the given ordering direction.
   */
  @NonNull Order setDirection (@NonNull final OrderingDirection direction);

  /**
   * Alias of setDirection(OrderingDirection.ASCENDING);
   *
   * @see #setDirection(OrderingDirection)
   */
  default @NonNull Order ascending () {
    return setDirection(OrderingDirection.ASCENDING);
  }

  /**
   * Alias of setDirection(OrderingDirection.DESCENDING);
   *
   * @see #setDirection(OrderingDirection)
   */
  default @NonNull Order descending () {
    return setDirection(OrderingDirection.DESCENDING);
  }
}
