/*
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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

package org.liara.collection.jpa;

import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.ordering.Order;

import java.util.*;

class JPAEntityCollectionConfiguration
{
  @NonNull
  private final List<@NonNull Order> _orderings;

  @NonNull
  private final Set<@NonNull Filter> _filters;

  @NonNull
  private final Cursor _cursor;

  JPAEntityCollectionConfiguration () {
    _orderings = new ArrayList<>();
    _filters = new HashSet<>();
    _cursor = Cursor.ALL;
  }

  private JPAEntityCollectionConfiguration (@NonNull final JPAEntityCollectionConfiguration toCopy) {
    _orderings = new ArrayList<>(toCopy._orderings);
    _filters = new HashSet<>(toCopy._filters);
    _cursor = toCopy._cursor;
  }

  private JPAEntityCollectionConfiguration (
    @NonNull final JPAEntityCollectionConfiguration toCopy,
    @NonNull final Cursor cursor
  ) {
    _orderings = new ArrayList<>(toCopy._orderings);
    _filters = new HashSet<>(toCopy._filters);
    _cursor = cursor;
  }

  @NonNull List<@NonNull Order> getOrderings () {
    return Collections.unmodifiableList(_orderings);
  }

  @NonNull Iterable<@NonNull Order> orderings () {
    return Collections.unmodifiableList(_orderings);
  }

  @NonNull Order getOrdering (@NonNegative @LessThan("this.getOrderCount()") final int index) {
    return _orderings.get(index);
  }

  @NonNegative int getOrderingCount () {
    return _orderings.size();
  }

  @NonNull JPAEntityCollectionConfiguration orderBy (@NonNull final Order order) {
    @NonNull final JPAEntityCollectionConfiguration result = new JPAEntityCollectionConfiguration(this);
    result._orderings.add(order);
    return result;
  }

  @NonNull Set<@NonNull Filter> getFilters () {
    return Collections.unmodifiableSet(_filters);
  }

  @NonNull Iterable<@NonNull Filter> filters () {
    return Collections.unmodifiableSet(_filters);
  }

  @NonNegative int getFilterCount () {
    return _filters.size();
  }

  @NonNull JPAEntityCollectionConfiguration addFilter (@NonNull final Filter filter) {
    @NonNull final JPAEntityCollectionConfiguration result = new JPAEntityCollectionConfiguration(this);
    result._filters.add(filter);
    return result;
  }

  @NonNull JPAEntityCollectionConfiguration removeFilter (@NonNull final Filter filter) {
    @NonNull final JPAEntityCollectionConfiguration result = new JPAEntityCollectionConfiguration(this);
    result._filters.remove(filter);
    return result;
  }

  @NonNull Cursor getCursor () {
    return _cursor;
  }

  @NonNull JPAEntityCollectionConfiguration setCursor (@NonNull final Cursor cursor) {
    return new JPAEntityCollectionConfiguration(this, cursor);
  }

  @Override
  public int hashCode () {
    return Objects.hash(_cursor, _filters, _orderings);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof JPAEntityCollectionConfiguration) {
      @NonNull final JPAEntityCollectionConfiguration otherConfiguration = (JPAEntityCollectionConfiguration) other;

      return Objects.equals(_cursor, otherConfiguration.getCursor()) &&
             Objects.equals(_filters, otherConfiguration.getFilters()) &&
             Objects.equals(_orderings, otherConfiguration.getOrderings());
    }

    return false;
  }
}
