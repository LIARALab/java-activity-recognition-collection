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
import org.liara.collection.operator.Composition;
import org.liara.collection.operator.Operator;
import org.liara.collection.operator.aggregate.AggregableCollection;
import org.liara.collection.operator.aggregate.Aggregate;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.cursoring.CursorableCollection;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.filtering.FilterableCollection;
import org.liara.collection.operator.grouping.Group;
import org.liara.collection.operator.grouping.GroupableCollection;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.joining.JoinableCollection;
import org.liara.collection.operator.ordering.Order;
import org.liara.collection.operator.ordering.OrderableCollection;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ModelAggregation<Entity>
  implements Collection<Entity>,
             CursorableCollection<Entity>,
             OrderableCollection<Entity>,
             FilterableCollection<Entity>,
             GroupableCollection<Entity>,
             JoinableCollection<Entity>,
             AggregableCollection<Entity>
{
  @NonNull
  private final ModelCollection<Entity> _groupedCollection;

  @NonNull
  private final Groups _groups;

  @NonNull
  private final Aggregates _aggregates;

  /**
   * Create a new grouped collection from an existing entity collection and a list of groups.
   *
   * @param groupedCollection An existing entity collection to group.
   * @param builder A builder to use.
   */
  public ModelAggregation (
    @NonNull final ModelCollection<Entity> groupedCollection,
    @NonNull final ModelAggregationBuilder builder
  ) {
    _groupedCollection = groupedCollection;
    _groups = Objects.requireNonNull(builder.getGroups());
    _aggregates = Objects.requireNonNull(builder.getAggregates());
  }

  /**
   * Create a copy of an existing grouped collection.
   *
   * @param toCopy A grouped collection to copy.
   */
  public ModelAggregation (
    @NonNull final ModelAggregation<Entity> toCopy
  ) {
    _groupedCollection = toCopy.getGroupedCollection();
    _groups = toCopy._groups;
    _aggregates = toCopy._aggregates;
  }

  /**
   * @see ModelCollection#getEntityName()
   */
  public @NonNull String getEntityName () {return _groupedCollection.getEntityName();}

  /**
   * @see ModelCollection#getModelClass()
   */
  @Override
  public @NonNull Class<Entity> getModelClass () {return _groupedCollection.getModelClass();}

  /**
   * Return the underlying grouped collection.
   *
   * @return The underlying grouped collection.
   */
  public @NonNull ModelCollection<Entity> getGroupedCollection () {
    return _groupedCollection;
  }

  /**
   * Return a new grouped collection like this one except that the new operate over another underlying entity
   * collection.
   *
   * @param collection A collection to group like this one.
   *
   * @return A grouped collection that group the given collection like this one.
   */
  public @NonNull ModelAggregation<Entity> setGroupedCollection (
    @NonNull final ModelCollection<Entity> collection
  ) {
    return getBuilder().aggregate(collection);
  }

  /**
   * @see CursorableCollection#setCursor(Cursor)
   */
  @Override
  public @NonNull ModelAggregation<Entity> setCursor (@NonNull final Cursor cursor) {
    return getBuilder().aggregate(_groupedCollection.setCursor(cursor));
  }

  /**
   * @see CursorableCollection#getCursor()
   */
  @Override
  public @NonNull Cursor getCursor () {
    return _groupedCollection.getCursor();
  }

  /**
   * @see FilterableCollection#addFilter(Filter)
   */
  @Override
  public @NonNull ModelAggregation<Entity> addFilter (@NonNull final Filter filter) {
    return getBuilder().aggregate(_groupedCollection.addFilter(filter));
  }

  /**
   * @see FilterableCollection#removeFilter(Filter)
   */
  @Override
  public @NonNull ModelAggregation<Entity> removeFilter (@NonNull final Filter filter) {
    return getBuilder().aggregate(_groupedCollection.removeFilter(filter));
  }

  /**
   * @see FilterableCollection#getFilters()
   */
  @Override
  public @NonNull Set<@NonNull Filter> getFilters () {
    return _groupedCollection.getFilters();
  }

  /**
   * @see GroupableCollection#groupBy(Group)
   */
  @Override
  public @NonNull ModelAggregation<Entity> groupBy (@NonNull final Group group) {
    @NonNull final Groups nextGroups = _groups.groupBy(group);

    if (nextGroups == _groups) {
      return this;
    } else {
      @NonNull final ModelAggregationBuilder builder = getBuilder();
      builder.setGroups(nextGroups);
      return builder.aggregate(_groupedCollection);
    }
  }

  @Override
  public @NonNull GroupableCollection<Entity> ungroup (@NonNull final Group group) {
    @NonNull final Groups nextGroups = _groups.remove(group);

    if (nextGroups == _groups) {
      return this;
    } else {
      @NonNull final ModelAggregationBuilder builder = getBuilder();
      builder.setGroups(nextGroups);
      return builder.aggregate(_groupedCollection);
    }
  }

  /**
   * @see GroupableCollection#getGroups()
   */
  @Override
  public @NonNull List<@NonNull Group> getGroups () {
    return _groups.getGroups();
  }

  /**
   * @see OrderableCollection#orderBy(Order)
   * @param order
   */
  @Override
  public @NonNull ModelAggregation<Entity> orderBy (@NonNull final Order order) {
    return getBuilder().aggregate(_groupedCollection.orderBy(order));
  }

  @Override
  public @NonNull ModelAggregation<Entity> removeOrder (@NonNull final Order order) {
    return getBuilder().aggregate(_groupedCollection.removeOrder(order));
  }

  /**
   * @see OrderableCollection#getOrderings()
   */
  @Override
  public @NonNull List<@NonNull Order> getOrderings () {
    return _groupedCollection.getOrderings();
  }

  @Override
  public @NonNull ModelAggregation<Entity> join (@NonNull final Join<?> relation) {
    return getBuilder().aggregate(_groupedCollection.join(relation));
  }

  @Override
  public @NonNull ModelAggregation<Entity> disjoin (@NonNull final Join<?> relation) {
    return getBuilder().aggregate(_groupedCollection.disjoin(relation));
  }

  @Override
  public @NonNull ModelAggregation<Entity> disjoin (@NonNull final String name) {
    return getBuilder().aggregate(_groupedCollection.disjoin(name));
  }

  @Override
  public @NonNull Map<@NonNull String, @NonNull Join> getJoins () {
    return _groupedCollection.getJoins();
  }

  @Override
  public @NonNull Collection<?> setOperator (@NonNull final Operator operator) {
    return _groupedCollection.setOperator(operator);
  }

  @Override
  public @NonNull Operator getOperator () {
    return Composition.of(
      _groupedCollection.getOperator(),
      Composition.of(_groups.getGroups()),
      Composition.of(_aggregates.getAggregates())
    );
  }

  private @NonNull ModelAggregationBuilder getBuilder () {
    @NonNull final ModelAggregationBuilder builder = new ModelAggregationBuilder();
    builder.setGroups(_groups);
    builder.setAggregates(_aggregates);
    return builder;
  }

  @Override
  public @NonNull AggregableCollection<Entity> aggregate (final @NonNull Aggregate aggregate) {
    @NonNull final Aggregates aggregates = _aggregates.aggregate(aggregate);

    if (aggregates == _aggregates) {
      return this;
    } else {
      @NonNull final ModelAggregationBuilder builder = getBuilder();
      builder.setAggregates(aggregates);
      return builder.aggregate(_groupedCollection);
    }
  }

  @Override
  public @NonNull AggregableCollection<Entity> remove (final @NonNull Aggregate aggregate) {
    @NonNull final Aggregates aggregates = _aggregates.remove(aggregate);

    if (aggregates == _aggregates) {
      return this;
    } else {
      @NonNull final ModelAggregationBuilder builder = getBuilder();
      builder.setAggregates(aggregates);
      return builder.aggregate(_groupedCollection);
    }
  }

  @Override
  public @NonNull List<@NonNull Aggregate> getAggregations () {
    return _aggregates.getAggregates();
  }

  @Override
  public int hashCode () {
    return Objects.hash(_groupedCollection, _groups, _aggregates);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ModelAggregation) {
      @NonNull final ModelAggregation<?> otherCollection = (ModelAggregation<?>) other;

      return Objects.equals(otherCollection.getGroupedCollection(), _groupedCollection) &&
             Objects.equals(otherCollection._groups, _groups) &&
             Objects.equals(otherCollection._aggregates, _aggregates);
    }

    return false;
  }
}
