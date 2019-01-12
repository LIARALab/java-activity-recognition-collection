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

import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.ordering.Order;

import java.util.*;

public class CollectionConfiguration
{
  @NonNull
  private final List<@NonNull Order> _orderings;

  @NonNull
  private final Set<@NonNull Filter> _filters;

  @NonNull
  private final Cursor _cursor;

  @NonNull
  private final Map<@NonNull String, @NonNull Join> _joins;

  public CollectionConfiguration () {
    _orderings = new ArrayList<>();
    _filters = new HashSet<>();
    _joins = new HashMap<>();
    _cursor = Cursor.ALL;
  }

  public CollectionConfiguration (@NonNull final CollectionConfigurationBuilder builder) {
    _orderings = new ArrayList<>(builder.getOrderings());
    _filters = new HashSet<>(builder.getFilters());
    _joins = new HashMap<>(builder.getJoins());
    _cursor = builder.getCursor();
  }

  public CollectionConfiguration (@NonNull final CollectionConfiguration toCopy) {
    _orderings = toCopy.getOrderings();
    _filters = toCopy.getFilters();
    _joins = toCopy.getJoins();
    _cursor = toCopy.getCursor();
  }

  public @NonNull List<@NonNull Order> getOrderings () {
    return Collections.unmodifiableList(_orderings);
  }

  public @NonNull Iterable<@NonNull Order> orderings () {
    return Collections.unmodifiableList(_orderings);
  }

  public @NonNull Order getOrdering (@NonNegative @LessThan("this.getOrderCount()") final int index) {
    return _orderings.get(index);
  }

  public @NonNegative int getOrderingCount () {
    return _orderings.size();
  }

  public @NonNull Set<@NonNull Filter> getFilters () {
    return Collections.unmodifiableSet(_filters);
  }

  public @NonNull Iterable<@NonNull Filter> filters () {
    return Collections.unmodifiableSet(_filters);
  }

  public @NonNegative int getFilterCount () {
    return _filters.size();
  }

  public @NonNull Cursor getCursor () {
    return _cursor;
  }

  public @NonNull Map<@NonNull String, @NonNull Join> getJoins () {
    return Collections.unmodifiableMap(_joins);
  }

  public @NonNull Iterable<@NonNull Join> joins () {
    return Collections.unmodifiableCollection(_joins.values());
  }

  @Override
  public int hashCode () {
    return Objects.hash(_cursor, _filters, _orderings, _joins);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof CollectionConfiguration) {
      @NonNull final CollectionConfiguration otherConfiguration = (CollectionConfiguration) other;

      return Objects.equals(_cursor, otherConfiguration.getCursor()) &&
             Objects.equals(_filters, otherConfiguration.getFilters()) && Objects.equals(
        _orderings,
        otherConfiguration.getOrderings()
      ) && Objects.equals(_joins, otherConfiguration.getJoins());
    }

    return false;
  }
}
