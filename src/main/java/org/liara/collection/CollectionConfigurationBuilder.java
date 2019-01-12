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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.ordering.ExpressionOrder;
import org.liara.collection.operator.ordering.Order;

import java.util.*;
import java.util.Collection;

public class CollectionConfigurationBuilder
{
  @NonNull
  private final List<@NonNull Order> _orderings;

  @NonNull
  private final Set<@NonNull Filter> _filters;
  @NonNull
  private final Map<@NonNull String, @NonNull Join> _joins;
  @NonNull
  private       Cursor _cursor;

  public CollectionConfigurationBuilder () {
    _orderings = new ArrayList<>();
    _filters = new HashSet<>();
    _joins = new HashMap<>();
    _cursor = Cursor.ALL;
  }

  private CollectionConfigurationBuilder (
    @NonNull final CollectionConfigurationBuilder toCopy
  )
  {
    _orderings = new ArrayList<>(toCopy.getOrderings());
    _filters = new HashSet<>(toCopy.getFilters());
    _joins = new HashMap<>(toCopy.getJoins());
    _cursor = toCopy.getCursor();
  }

  private CollectionConfigurationBuilder (
    @NonNull final CollectionConfiguration toCopy
  )
  {
    _orderings = new ArrayList<>(toCopy.getOrderings());
    _filters = new HashSet<>(toCopy.getFilters());
    _joins = new HashMap<>(toCopy.getJoins());
    _cursor = toCopy.getCursor();
  }

  public static @NonNull CollectionConfigurationBuilder from (@NonNull final CollectionConfiguration configuration) {
    return new CollectionConfigurationBuilder(configuration);
  }

  public @NonNull CollectionConfiguration build () {
    return new CollectionConfiguration(this);
  }

  public @NonNull List<@NonNull Order> getOrderings () {
    return _orderings;
  }

  public @NonNull CollectionConfigurationBuilder setOrderings (
    final java.util.@NonNull Collection<@NonNull ExpressionOrder> orderings
  )
  {
    _orderings.clear();
    _orderings.addAll(orderings);
    return this;
  }

  public @NonNull CollectionConfigurationBuilder orderBy (@NonNull final Order order) {
    _orderings.add(order);
    return this;
  }

  public @NonNull CollectionConfigurationBuilder removeOrder (@NonNull final Order order) {
    _orderings.remove(order);
    return this;
  }

  public @NonNull Set<@NonNull Filter> getFilters () {
    return _filters;
  }

  public @NonNull CollectionConfigurationBuilder setFilters (
    final java.util.@NonNull Collection<@NonNull Filter> filters
  )
  {
    _filters.clear();
    _filters.addAll(filters);
    return this;
  }

  public @NonNull CollectionConfigurationBuilder addFilter (@NonNull final Filter filter) {
    _filters.add(filter);
    return this;
  }

  public @NonNull CollectionConfigurationBuilder removeFilter (@NonNull final Filter filter) {
    _filters.remove(filter);
    return this;
  }

  public @NonNull CollectionConfigurationBuilder filter (@NonNull final Filter filter) {
    _filters.add(filter);
    return this;
  }

  public @NonNull Cursor getCursor () {
    return _cursor;
  }

  public @NonNull CollectionConfigurationBuilder setCursor (@NonNull final Cursor cursor) {
    _cursor = cursor;
    return this;
  }

  public @NonNull Map<@NonNull String, @NonNull Join> getJoins () {
    return _joins;
  }

  public @NonNull CollectionConfigurationBuilder setJoins (@NonNull final Collection<@NonNull Join> joins) {
    _joins.clear();
    joins.forEach(this::join);
    return this;
  }

  public @NonNull CollectionConfigurationBuilder join (@NonNull final Join join) {
    if (_joins.containsKey(join.getName()) && !_joins.get(join.getName()).equals(join)) {
      throw new Error(
        "Unable to join to a collection of " + join.getClass() + " as " + join.getName() + " because another join" +
        " already exists with the same name. Please use different names for your joins.");
    }

    _joins.put(join.getName(), join);
    return this;
  }

  public @NonNull CollectionConfigurationBuilder disjoin (@NonNull final Join join) {
    if (_joins.get(join.getName()).equals(join)) {
      _joins.remove(join.getName());
    }

    return this;
  }

  public @NonNull CollectionConfigurationBuilder disjoin (@NonNull final String name) {
    _joins.remove(name);

    return this;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_cursor, _filters, _orderings, _joins);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof CollectionConfigurationBuilder) {
      @NonNull final CollectionConfigurationBuilder otherConfiguration = (CollectionConfigurationBuilder) other;

      return Objects.equals(_cursor, otherConfiguration.getCursor()) && Objects.equals(
        _filters,
        otherConfiguration.getFilters()
      ) && Objects.equals(_orderings, otherConfiguration.getOrderings()) && Objects.equals(
        _joins,
        otherConfiguration.getJoins()
      );
    }

    return false;
  }
}
