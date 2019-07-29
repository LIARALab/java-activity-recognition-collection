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

package org.liara.collection.operator.grouping;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.expression.Expression;

import java.util.Objects;

public class ExpressionGroup
  implements Group
{
  @NonNull
  private final Expression<?> _expression;

  /**
   * Instantiate a new grouping operation of a given expression.
   *
   * @param expression An expression to group.
   */
  public ExpressionGroup (@NonNull final Expression<?> expression) {
    _expression = expression;
  }

  /**
   * Instantiate a new grouping operation that is a copy of another one.
   *
   * @param toCopy A grouping operation to copy.
   */
  public ExpressionGroup (@NonNull final ExpressionGroup toCopy) {
    _expression = toCopy.getExpression();
  }

  /**
   * @see Group#getExpression()
   */
  @Override
  public @NonNull Expression<?> getExpression () {
    return _expression;
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode () {
    return Objects.hash(_expression);
  }

  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ExpressionGroup) {
      @NonNull final ExpressionGroup otherGroup = (ExpressionGroup) other;
      return Objects.equals(_expression, otherGroup.getExpression());
    }

    return false;
  }
}
