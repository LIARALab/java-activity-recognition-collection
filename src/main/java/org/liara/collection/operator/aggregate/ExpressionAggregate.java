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

package org.liara.collection.operator.aggregate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.joining.JoinableOperator;

import java.util.Objects;

public class ExpressionAggregate
  implements Aggregate, JoinableOperator
{
  @NonNull
  private final String _expression;

  public ExpressionAggregate (@NonNull final String expression) {
    _expression = expression;
  }

  public ExpressionAggregate (@NonNull final ExpressionAggregate toCopy) {
    _expression = toCopy.getExpression();
  }

  public static @NonNull ExpressionAggregate expression (@NonNull final String expression) {
    return new ExpressionAggregate(expression);
  }

  @Override
  public @NonNull Operator join (@NonNull final Join join) {
    return new JoinAggregate(join, this);
  }

  @Override
  public @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof AggregableCollection) {
      return ((AggregableCollection) input).aggregate(this);
    }

    return input;
  }

  @Override
  public @NonNull String getExpression () {
    return _expression;
  }

  public @NonNull ExpressionAggregate setExpression (@NonNull final String expression) {
    return new ExpressionAggregate(expression);
  }

  @Override
  public int hashCode () {
    return Objects.hash(_expression);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ExpressionAggregate) {
      @NonNull final ExpressionAggregate otherGroup = (ExpressionAggregate) other;
      return Objects.equals(_expression, otherGroup.getExpression());
    }

    return false;
  }
}
