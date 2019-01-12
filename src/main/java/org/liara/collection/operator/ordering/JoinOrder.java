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
import org.liara.collection.Collection;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.joining.JoinableCollection;

import java.util.Objects;

public class JoinOrder
  implements Order
{
  @NonNull
  private final Join _join;

  @NonNull
  private final Order _order;

  public JoinOrder (@NonNull final Join join, @NonNull final Order order) {
    _join = join;
    _order = order;
  }

  public JoinOrder (@NonNull final JoinOrder toCopy) {
    _join = toCopy.getJoin();
    _order = toCopy.getOrder();
  }

  @Override
  public @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof JoinableCollection) {
      @NonNull final JoinableCollection collection = ((JoinableCollection) input).join(_join);

      if (collection instanceof OrderableCollection) {
        return ((OrderableCollection) collection).orderBy(this);
      }
    }

    return input;
  }

  @Override
  public @NonNull String getExpression () {
    return _order.getExpression().replace(":this", _join.identifier());
  }

  @Override
  public @NonNull OrderingDirection getDirection () {
    return _order.getDirection();
  }

  @Override
  public @NonNull JoinOrder setDirection (@NonNull final OrderingDirection direction) {
    return new JoinOrder(_join, _order.setDirection(direction));
  }

  @Override
  public @NonNull JoinOrder ascending () {
    return new JoinOrder(_join, _order.ascending());
  }

  @Override
  public @NonNull JoinOrder descending () {
    return new JoinOrder(_join, _order.descending());
  }

  public @NonNull Join getJoin () {
    return _join;
  }

  public @NonNull Order getOrder () {
    return _order;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_join, _order);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof JoinOrder) {
      final JoinOrder otherOrder = (JoinOrder) other;
      return Objects.equals(_join, otherOrder.getJoin()) && Objects.equals(_order, otherOrder.getOrder());
    }

    return false;
  }
}
