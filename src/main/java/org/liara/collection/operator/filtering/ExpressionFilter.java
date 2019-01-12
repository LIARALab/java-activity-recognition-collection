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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.joining.JoinableOperator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExpressionFilter
  implements Filter,
             JoinableOperator
{
  @NonNull
  private final Map<@NonNull String, @NonNull Object> _parameters;

  @NonNull
  private final String _expression;

  public ExpressionFilter (@NonNull final String expression) {
    _expression = expression;
    _parameters = Collections.emptyMap();
  }

  public ExpressionFilter (
    @NonNull final String expression,
    @NonNull final Map<@NonNull String, @NonNull Object> values
  ) {
    _expression = expression;
    _parameters = new HashMap<>(values);
  }

  public ExpressionFilter (
    @NonNull final ExpressionFilter toCopy
  ) {
    _expression = toCopy.getExpression();
    _parameters = new HashMap<>(toCopy.getParameters());
  }

  public @NonNull ExpressionFilter setExpression (@NonNull final String expression) {
    return new ExpressionFilter(expression, _parameters);
  }

  @Override
  public @NonNull String getExpression () {
    return _expression;
  }

  @Override
  public @NonNull ExpressionFilter setParameter (
    @NonNull final String name,
    @Nullable final Object value
  ) {
    if (value == null) {
      return removeParameter(name);
    } else {
      final Map<@NonNull String, @NonNull Object> result = new HashMap<>(_parameters);
      result.put(name, value);
      return new ExpressionFilter(_expression, result);
    }
  }

  @Override
  public @NonNull ExpressionFilter setParameters (
    @NonNull final Map<@NonNull String, @NonNull Object> parameters
  )
  {
    return new ExpressionFilter(_expression, parameters);
  }

  @Override
  public @NonNull ExpressionFilter removeParameter (
    @NonNull final String name
  ) {
    if (_parameters.containsKey(name)) {
      final Map<@NonNull String, @NonNull Object> result;
      result = new HashMap<>(_parameters);
      result.remove(name);
      return new ExpressionFilter(_expression, result);
    }

    return this;
  }

  @Override
  public @NonNull Map<@NonNull String, @NonNull Object> getParameters () {
    return Collections.unmodifiableMap(_parameters);
  }

  @Override
  public @NonNull Filter join (@NonNull final Join join) {
    return new JoinFilter(join, this);
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode () {
    return Objects.hash(_expression, _parameters);
  }

  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ExpressionFilter) {
      final ExpressionFilter otherFilter = (ExpressionFilter) other;

      return _expression.equals(otherFilter.getExpression()) &&
             _parameters.equals(otherFilter.getParameters());
    }

    return false;
  }
}
