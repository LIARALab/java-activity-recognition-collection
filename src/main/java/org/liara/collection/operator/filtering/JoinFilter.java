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
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.joining.JoinableCollection;
import org.liara.collection.operator.joining.JoinableOperator;

import java.util.Map;
import java.util.Objects;

public class JoinFilter
  implements Filter,
             JoinableOperator
{
  @NonNull
  private final Join _join;

  @NonNull
  private final Filter _filter;

  public JoinFilter (@NonNull final Join join, @NonNull final Filter filter) {
    _join = join;
    _filter = filter;
  }

  @Override
  public <Model> @NonNull Collection<Model> apply (@NonNull final Collection<Model> input) {
    if (input instanceof JoinableCollection) {
      @NonNull final JoinableCollection<Model> collection =
        ((JoinableCollection<Model>) input).join(
        _join);

      if (collection instanceof FilterableCollection) {
        return ((FilterableCollection) collection).addFilter(this);
      }
    }

    return input;
  }

  @Override
  public @NonNull String getExpression () {
    return _filter.getExpression().replace(":this", _join.identifier());
  }

  @Override
  public @NonNull JoinFilter setParameter (
    @NonNull final String name, @Nullable final Object value
  )
  {
    return new JoinFilter(_join, _filter.setParameter(name, value));
  }

  @Override
  public @NonNull Filter removeParameter (@NonNull final String name) {
    return new JoinFilter(_join, _filter.removeParameter(name));
  }

  @Override
  public @NonNull Map<@NonNull String, @NonNull Object> getParameters () {
    return _filter.getParameters();
  }

  @Override
  public @NonNull JoinFilter setParameters (@NonNull final Map<@NonNull String, @NonNull Object> parameters) {
    return new JoinFilter(_join, _filter.setParameters(parameters));
  }

  @Override
  public @NonNull Operator join (@NonNull final Join join) {
    return new JoinFilter((Join) join.apply(_join), _filter);
  }

  public @NonNull Join getJoin () {
    return _join;
  }

  public @NonNull Filter getFilter () {
    return _filter;
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode () {
    return Objects.hash(_join, _filter);
  }

  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof JoinFilter) {
      @NonNull final JoinFilter otherFilter = (JoinFilter) other;

      return _filter.equals(otherFilter.getFilter()) && _join.equals(otherFilter.getJoin());
    }

    return false;
  }
}
