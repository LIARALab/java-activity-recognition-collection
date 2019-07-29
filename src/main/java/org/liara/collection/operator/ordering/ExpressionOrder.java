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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.expression.Expression;

import java.util.Objects;

/**
 * An operator that describe a given way to ordering fields.
 */
public class ExpressionOrder
  implements Order
{
  @NonNull
  private final Expression<?> _expression;

  @NonNull
  private final OrderingDirection _direction;

  /**
   * Create a new ascending ordering operation for a given expression.
   *
   * @param expression A expression to order.
   */
  public ExpressionOrder (
    @NonNull final Expression<?> expression
  ) {
    _expression = expression;
    _direction = OrderingDirection.ASCENDING;
  }

  /**
   * Create a new ordering operation for a given expression.
   *
   * @param expression A expression to order.
   * @param direction  An ordering direction.
   */
  public ExpressionOrder (
    @NonNull final Expression<?> expression,
    @NonNull final OrderingDirection direction
  ) {
    _expression = expression;
    _direction = direction;
  }

  /**
   * Create a copy of another ordering operator.
   *
   * @param toCopy An ordering operator to copy.
   */
  public ExpressionOrder (
    @NonNull final ExpressionOrder toCopy
  )
  {
    _expression = toCopy.getExpression();
    _direction = toCopy.getDirection();
  }

  /**
   * @return The expression to order.
   */
  @Override
  public @NonNull Expression<?> getExpression () {
    return _expression;
  }

  /**
   * Return a new ordering operator instance based on this one with another ordered expression.
   *
   * @param expression The new expression to order.
   *
   * @return A new ordering operator instance based on this one with another ordered expression.
   */
  public @NonNull ExpressionOrder setExpression (@NonNull final Expression<?> expression) {
    return new ExpressionOrder(expression, _direction);
  }

  /**
   * @return The ordering direction of this operator.
   */
  @Override
  public @NonNull OrderingDirection getDirection () {
    return _direction;
  }

  /**
   * Return a new ordering operator instance based on this one with another ordering direction.
   *
   * @param direction The new ordering direction.
   *
   * @return A new ordering operator instance based on this one with another ordering direction.
   */
  @Override
  public @NonNull ExpressionOrder setDirection (@NonNull final OrderingDirection direction) {
    return new ExpressionOrder(_expression, direction);
  }

  @Override
  public @NonNull ExpressionOrder ascending () {
    return setDirection(OrderingDirection.ASCENDING);
  }

  @Override
  public @NonNull ExpressionOrder descending () {
    return setDirection(OrderingDirection.DESCENDING);
  }

  /**
   * @see ExpressionOrder#hashCode()
   */
  @Override
  public int hashCode () {
    return Objects.hash(_expression, _direction);
  }

  /**
   * @see ExpressionOrder#equals(Object)
   */
  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ExpressionOrder) {
      final ExpressionOrder otherOrder = (ExpressionOrder) other;
      return Objects.equals(_expression, otherOrder.getExpression()) && Objects.equals(
        _direction,
        otherOrder.getDirection()
      );
    }

    return false;
  }
}
