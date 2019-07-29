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

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.filtering.Filter;

import java.util.Iterator;
import java.util.Objects;

public class Filters
  implements Iterable<@NonNull Filter>
{
  public static final @NonNull Filters EMPTY = new Filters();

  @NonNull
  private final ImmutableSet<@NonNull Filter> _filters;

  public Filters (@NonNull final ImmutableSet<@NonNull Filter> filters) {
    _filters = filters;
  }

  public Filters (@NonNull final Iterator<@NonNull Filter> filters) {
    _filters = ImmutableSet.copyOf(filters);
  }

  public Filters (final java.util.@NonNull Collection<@NonNull Filter> filters) {
    _filters = ImmutableSet.copyOf(filters);
  }

  public Filters (@NonNull final Filters filters) {
    _filters = filters.getFilters();
  }

  public Filters (@NonNull final Filter... filters) {
    _filters = ImmutableSet.copyOf(filters);
  }

  public @NonNull Filters add (@NonNull final Filter filter) {
    if (_filters.contains(filter)) {
      return this;
    } else {
      final ImmutableSet.@NonNull Builder<@NonNull Filter> builder = ImmutableSet.builder();
      builder.addAll(_filters);
      builder.add(filter);

      return new Filters(builder.build());
    }
  }

  public @NonNull Filters remove (@NonNull final Filter filter) {
    if (_filters.contains(filter)) {
      final ImmutableSet.@NonNull Builder<@NonNull Filter> builder = ImmutableSet.builder();

      for (@NonNull final Filter toAdd : _filters) {
        if (!toAdd.equals(filter)) {
          builder.add(toAdd);
        }
      }

      return new Filters(builder.build());
    }

    return this;
  }

  public @NonNull Filters clear () {
    return Filters.EMPTY;
  }

  public boolean contains (@NonNull final Filter filter) {
    return _filters.contains(filter);
  }

  @Override
  public @NonNull Iterator<@NonNull Filter> iterator () {
    return _filters.iterator();
  }

  public @NonNull ImmutableSet<@NonNull Filter> getFilters () {
    return _filters;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Filters) {
      @NonNull final Filters otherFilters = (Filters) other;

      return Objects.equals(_filters, otherFilters.getFilters());
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_filters);
  }
}
