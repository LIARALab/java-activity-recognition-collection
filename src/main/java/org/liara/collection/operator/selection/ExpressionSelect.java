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

package org.liara.collection.operator.selection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.expression.Expression;

import java.util.Objects;

public class ExpressionSelect<Type>
  implements Select<Type>
{
  @Nullable
  private final String _name;

  @NonNull
  private final Expression<Type> _expression;

  @NonNull
  private final SelectionPlaceholder<Type> _placeholder;

  public ExpressionSelect (
    @NonNull final Expression<Type> expression,
    @Nullable final String name
  ) {
    _name = name;
    _expression = expression;
    _placeholder = new StaticSelectionPlaceholder<>(this);

  }

  @Override
  public @Nullable String getName () {
    return _name;
  }

  @Override
  public @NonNull Expression<Type> getExpression () {
    return _expression;
  }

  @Override
  public @NonNull SelectionPlaceholder<Type> getPlaceholder () {
    return _placeholder;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ExpressionSelect) {
      @NonNull final ExpressionSelect otherExpressionSelection = (ExpressionSelect) other;

      return (
        Objects.equals(
          _name,
          otherExpressionSelection.getName()
        ) &&
        Objects.equals(
          _expression,
          otherExpressionSelection.getExpression()
        ) &&
        Objects.equals(
          _placeholder,
          otherExpressionSelection.getPlaceholder()
        )
      );
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_name, _expression, _placeholder);
  }
}
