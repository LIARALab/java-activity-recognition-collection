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
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.joining.JoinableCollection;
import org.liara.collection.operator.joining.JoinableOperator;

import java.util.Objects;

public class JoinGroup
  implements Group, JoinableOperator
{
  @NonNull
  private final Join _join;

  @NonNull
  private final Group _group;

  public JoinGroup (@NonNull final Join join, @NonNull final Group group) {
    _join = join;
    _group = group;
  }

  public JoinGroup (@NonNull final JoinGroup toCopy) {
    _join = toCopy.getJoin();
    _group = toCopy.getGroup();
  }

  @Override
  public @NonNull Operator join (@NonNull final Join join) {
    return new JoinGroup((Join) join.apply(_join), _group);
  }

  @Override
  public @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof JoinableCollection) {
      @NonNull final JoinableCollection collection = ((JoinableCollection) input).join(_join);

      if (collection instanceof GroupableCollection) {
        return ((GroupableCollection) collection).groupBy(this);
      }
    }

    return input;
  }

  @Override
  public @NonNull String getExpression () {
    return _group.getExpression();
  }

  public @NonNull Join getJoin () {
    return _join;
  }

  public @NonNull Group getGroup () {
    return _group;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof JoinGroup) {
      @NonNull final JoinGroup otherJoinGroup = (JoinGroup) other;

      return Objects.equals(
        _join,
        otherJoinGroup.getJoin()
      ) && Objects.equals(
        _group,
        otherJoinGroup.getGroup()
      );
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_join, _group);
  }
}
