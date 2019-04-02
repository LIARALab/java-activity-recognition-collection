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

package org.liara.collection;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.aggregate.Aggregate;

import java.util.Iterator;
import java.util.Objects;

public class Aggregates
  implements Iterable<@NonNull Aggregate>
{
  @NonNull
  public static final Aggregates EMPTY = new Aggregates();

  @NonNull
  private final ImmutableList<@NonNull Aggregate> _aggregates;

  public Aggregates (@NonNull final Aggregate... aggregates) {
    _aggregates = ImmutableList.copyOf(aggregates);
  }

  public Aggregates (@NonNull final Iterator<@NonNull Aggregate> aggregates) {
    _aggregates = ImmutableList.copyOf(aggregates);
  }

  public Aggregates (final java.util.@NonNull Collection<@NonNull Aggregate> aggregates) {
    _aggregates = ImmutableList.copyOf(aggregates);
  }

  public Aggregates (@NonNull final ImmutableList<@NonNull Aggregate> aggregates) {
    _aggregates = aggregates;
  }

  public Aggregates (@NonNull final Aggregates toCopy) {
    _aggregates = toCopy.getAggregates();
  }

  public @NonNull Aggregates aggregate (@NonNull final Aggregate aggregate) {
    if (_aggregates.contains(aggregate)) {
      return this;
    } else {
      final ImmutableList.@NonNull Builder<@NonNull Aggregate> builder = ImmutableList.builder();
      builder.addAll(_aggregates);
      builder.add(aggregate);

      return new Aggregates(builder.build());
    }
  }

  public @NonNull Aggregates remove (@NonNull final Aggregate aggregate) {
    if (_aggregates.contains(aggregate)) {
      final ImmutableList.@NonNull Builder<@NonNull Aggregate> builder = ImmutableList.builder();

      for (@NonNull final Aggregate toAdd : _aggregates) {
        if (!aggregate.equals(toAdd)) {
          builder.add(toAdd);
        }
      }

      return new Aggregates(builder.build());
    } else {
      return this;
    }
  }

  public @NonNull Aggregates clear () {
    return Aggregates.EMPTY;
  }

  @Override
  public Iterator<@NonNull Aggregate> iterator () {
    return _aggregates.iterator();
  }

  public @NonNull ImmutableList<@NonNull Aggregate> getAggregates () {
    return _aggregates;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Aggregates) {
      @NonNull final Aggregates otherAggregates = (Aggregates) other;

      return Objects.equals(_aggregates, otherAggregates.getAggregates());
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_aggregates);
  }
}
