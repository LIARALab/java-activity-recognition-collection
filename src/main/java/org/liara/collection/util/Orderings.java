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

package org.liara.collection.util;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.ordering.Order;

import java.util.Iterator;
import java.util.Objects;

public class Orderings
  implements Iterable<@NonNull Order>
{
  @NonNull
  public static final Orderings EMPTY = new Orderings();

  @NonNull
  private final ImmutableList<@NonNull Order> _orderings;

  public Orderings (@NonNull final Order... orders) {
    _orderings = ImmutableList.copyOf(orders);
  }

  public Orderings (@NonNull final Iterator<@NonNull Order> orders) {
    _orderings = ImmutableList.copyOf(orders);
  }

  public Orderings (final java.util.@NonNull Collection<@NonNull Order> orders) {
    _orderings = ImmutableList.copyOf(orders);
  }

  public Orderings (@NonNull final ImmutableList<@NonNull Order> orders) {
    _orderings = orders;
  }

  public Orderings (@NonNull final Orderings toCopy) {
    _orderings = toCopy.getOrderings();
  }

  public @NonNull Orderings orderBy (@NonNull final Order order) {
    if (_orderings.contains(order)) {
      return this;
    } else {
      final ImmutableList.@NonNull Builder<@NonNull Order> builder = ImmutableList.builder();
      builder.addAll(_orderings);
      builder.add(order);

      return new Orderings(builder.build());
    }
  }

  public @NonNull Orderings remove (@NonNull final Order order) {
    if (_orderings.contains(order)) {
      final ImmutableList.@NonNull Builder<@NonNull Order> builder = ImmutableList.builder();

      for (@NonNull final Order toAdd : _orderings) {
        if (!order.equals(toAdd)) {
          builder.add(toAdd);
        }
      }

      return new Orderings(builder.build());
    } else {
      return this;
    }
  }

  public @NonNull Orderings clear () {
    return Orderings.EMPTY;
  }

  @Override
  public Iterator<@NonNull Order> iterator () {
    return _orderings.iterator();
  }

  public @NonNull ImmutableList<@NonNull Order> getOrderings () {
    return _orderings;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Orderings) {
      @NonNull final Orderings otherOrderings = (Orderings) other;

      return Objects.equals(_orderings, otherOrderings.getOrderings());
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_orderings);
  }
}
