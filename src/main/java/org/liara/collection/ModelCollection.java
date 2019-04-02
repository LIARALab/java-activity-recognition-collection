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

import java.util.*;

/**
 * A collection of database entities.
 *
 * @param <Entity> Type of entity in the collection.
 *
 * @author C&eacute;dric DEMONGIVERT [cedric.demongivert@gmail.com](mailto:cedric.demongivert@gmail.com)
 */
public class ModelCollection<Entity>
  implements Collection<Entity>,
             CursorableCollection<Entity>,
             OrderableCollection<Entity>,
             FilterableCollection<Entity>,
             GroupableCollection<Entity>,
             JoinableCollection<Entity>,
             AggregableCollection<Entity>
{
  @NonNull
  private static final List<@NonNull Group> EMPTY_GROUP_LIST = Collections.unmodifiableList(
    Collections.emptyList());

  @NonNull
  private static final List<@NonNull Aggregate> EMPTY_AGGREGATE_LIST = Collections.unmodifiableList(
    Collections.emptyList());

  @NonNull
  private final Filters _filters;

  @NonNull
  private final Cursor _cursor;

  @NonNull
  private final Orderings _orderings;

  @NonNull
  private final Joins _joins;

  @NonNull
  private final Class<Entity> _modelClass;

  public static <T> @NonNull ModelCollection<T> create (@NonNull final Class<T> modelClass) {
    return new ModelCollection<>(modelClass);
  }

  /**
   * Create a collection of all instances of a given model.
   *
   * @param modelClass Type of model stored into this collection.
   */
  public ModelCollection (@NonNull final Class<Entity> modelClass) {
    _modelClass = modelClass;
    _filters = Filters.EMPTY;
    _cursor = Cursor.ALL;
    _orderings = Orderings.EMPTY;
    _joins = Joins.EMPTY;
  }

  /**
   * Create a copy of another model collection.
   *
   * @param toCopy Collection to copy.
   */
  public ModelCollection (@NonNull final ModelCollection<Entity> toCopy) {
    _modelClass = toCopy.getModelClass();
    _filters = toCopy._filters;
    _cursor = toCopy._cursor;
    _orderings = toCopy._orderings;
    _joins = toCopy._joins;
  }

  /**
   * Create a model collection from a builder instance.
   *
   * @param modelClass Type of model stored into this collection.
   * @param builder The builder instance to use for instantiating this collection.
   */
  public ModelCollection (
    @NonNull final Class<Entity> modelClass,
    @NonNull final ModelCollectionBuilder builder
  ) {
    _modelClass = modelClass;
    _filters = Objects.requireNonNull(builder.getFilters());
    _cursor = Objects.requireNonNull(builder.getCursor());
    _orderings = Objects.requireNonNull(builder.getOrderings());
    _joins = Objects.requireNonNull(builder.getJoins());
  }

  /**
   * Return the name used to identify the entity of this query.
   *
   * @return The name used to identify the entity of this query.
   */
  public @NonNull String getEntityName () {
    @NonNull final String typeName = _modelClass.getSimpleName();
    return Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
  }

  public @NonNull ModelCollectionBuilder getBuilder () {
    @NonNull final ModelCollectionBuilder builder = new ModelCollectionBuilder();
    builder.setCursor(_cursor);
    builder.setFilters(_filters);
    builder.setOrderings(_orderings);
    builder.setJoins(_joins);

    return builder;
  }

  /**
   * Return the type of entity stored into this collection.
   *
   * @return The type of entity stored into this collection.
   */
  @Override
  public @NonNull Class<Entity> getModelClass () {
    return _modelClass;
  }

  /**
   * @see CursorableCollection#getCursor()
   */
  public @NonNull Cursor getCursor () {
    return _cursor;
  }

  /**
   * @see CursorableCollection#setCursor(Cursor)
   */
  public @NonNull ModelCollection<Entity> setCursor (@NonNull final Cursor cursor) {
    if (cursor.equals(_cursor)) {
      return this;
    } else {
      @NonNull final ModelCollectionBuilder builder = getBuilder();
      builder.setCursor(cursor);
      return new ModelCollection<>(_modelClass, builder);
    }
  }

  /**
   * @see OrderableCollection#orderBy(Order)
   * @param order
   */
  @Override
  public ModelCollection<Entity> orderBy (@NonNull final Order order) {
    @NonNull final Orderings nextOrderings = _orderings.orderBy(order);

    if (nextOrderings == _orderings) {
      return this;
    } else {
      @NonNull final ModelCollectionBuilder builder = getBuilder();
      builder.setOrderings(nextOrderings);
      return new ModelCollection<>(_modelClass, builder);
    }
  }

  /**
   * @see OrderableCollection#removeOrder(Order)
   * @param order
   */
  @Override
  public ModelCollection<Entity> removeOrder (final Order order) {
    @NonNull final Orderings nextOrderings = _orderings.remove(order);

    if (nextOrderings == _orderings) {
      return this;
    } else {
      @NonNull final ModelCollectionBuilder builder = getBuilder();
      builder.setOrderings(nextOrderings);
      return new ModelCollection<>(_modelClass, builder);
    }
  }

  /**
   * @see OrderableCollection#getOrderings()
   */
  @Override
  public @NonNull List<@NonNull Order> getOrderings () {
    return _orderings.getOrderings();
  }

  /**
   * @see FilterableCollection#addFilter(Filter)
   */
  @Override
  public @NonNull ModelCollection<Entity> addFilter (@NonNull final Filter filter) {
    @NonNull final Filters nextFilter = _filters.add(filter);

    if (nextFilter == _filters) {
      return this;
    } else {
      @NonNull final ModelCollectionBuilder builder = getBuilder();
      builder.setFilters(nextFilter);
      return new ModelCollection<>(_modelClass, builder);
    }
  }

  /**
   * @see FilterableCollection#removeFilter(Filter)
   */
  @Override
  public @NonNull ModelCollection<Entity> removeFilter (@NonNull final Filter filter) {
    @NonNull final Filters nextFilter = _filters.remove(filter);

    if (nextFilter == _filters) {
      return this;
    } else {
      @NonNull final ModelCollectionBuilder builder = getBuilder();
      builder.setFilters(nextFilter);
      return new ModelCollection<>(_modelClass, builder);
    }
  }

  /**
   * @see FilterableCollection#getFilters()
   */
  @Override
  public @NonNull Set<@NonNull Filter> getFilters () {
    return _filters.getFilters();
  }

  /**
   * @see GroupableCollection#groupBy(Group)
   */
  @Override
  public @NonNull ModelAggregation<Entity> groupBy (@NonNull final Group group) {
    @NonNull final ModelAggregationBuilder builder = new ModelAggregationBuilder();
    builder.setGroups(new Groups(group));
    builder.setAggregates(Aggregates.EMPTY);
    return builder.aggregate(this);
  }

  @Override
  public @NonNull ModelCollection<Entity> ungroup (@NonNull final Group group) {
    return this;
  }

  @Override
  public @NonNull List<@NonNull Group> getGroups () {
    return EMPTY_GROUP_LIST;
  }

  @Override
  public @NonNull ModelCollection<Entity> join (@NonNull final Join<?> relation) {
    @NonNull final Joins nextJoins = _joins.join(relation);

    if (nextJoins == _joins) {
      return this;
    } else {
      @NonNull final ModelCollectionBuilder builder = getBuilder();
      builder.setJoins(nextJoins);
      return new ModelCollection<>(_modelClass, builder);
    }
  }

  @Override
  public @NonNull ModelCollection<Entity> disjoin (@NonNull final Join<?> relation) {
    @NonNull final Joins nextJoins = _joins.disjoin(relation);

    if (nextJoins == _joins) {
      return this;
    } else {
      @NonNull final ModelCollectionBuilder builder = getBuilder();
      builder.setJoins(nextJoins);
      return new ModelCollection<>(_modelClass, builder);
    }
  }

  @Override
  public @NonNull ModelCollection<Entity> disjoin (@NonNull final String name) {
    @NonNull final Joins nextJoins = _joins.disjoin(name);

    if (nextJoins == _joins) {
      return this;
    } else {
      @NonNull final ModelCollectionBuilder builder = getBuilder();
      builder.setJoins(nextJoins);
      return new ModelCollection<>(_modelClass, builder);
    }
  }

  @Override
  public @NonNull Map<@NonNull String, @NonNull Join> getJoins () {
    return _joins.getJoins();
  }

  @Override
  public @NonNull AggregableCollection<Entity> aggregate (final @NonNull Aggregate aggregate) {
    @NonNull final ModelAggregationBuilder builder = new ModelAggregationBuilder();
    builder.setGroups(Groups.EMPTY);
    builder.setAggregates(new Aggregates(aggregate));
    return builder.aggregate(this);
  }

  @Override
  public @NonNull AggregableCollection<Entity> remove (final @NonNull Aggregate aggregate) {
    return this;
  }

  @Override
  public @NonNull List<@NonNull Aggregate> getAggregations () {
    return EMPTY_AGGREGATE_LIST;
  }

  @Override
  public @NonNull Collection<?> setOperator (@Nullable final Operator operator) {
    if (operator == null) {
      return new ModelCollection<>(_modelClass);
    } else {
      return operator.apply(new ModelCollection<>(_modelClass));
    }
  }

  @Override
  public @NonNull Operator getOperator () {
    return Composition.of(
      Composition.of(_orderings.getOrderings().toArray(new Operator[0])),
      Composition.of(_filters.getFilters().toArray(new Operator[0])),
      Composition.of(_cursor)
    );
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ModelCollection) {
      @NonNull final ModelCollection otherModelCollection = (ModelCollection) other;

      return Objects.equals(
        _filters,
        otherModelCollection._filters
      ) && Objects.equals(
        _cursor,
        otherModelCollection._cursor
      ) && Objects.equals(
        _orderings,
        otherModelCollection._orderings
      ) && Objects.equals(
        _modelClass,
        otherModelCollection.getModelClass()
      );
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_filters, _cursor, _orderings, _modelClass);
  }
}
