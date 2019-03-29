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
import org.checkerframework.com.google.common.collect.Iterables;
import org.checkerframework.common.value.qual.MinLen;
import org.liara.collection.operator.Composition;
import org.liara.collection.operator.Operator;
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

public class ModelAggregation<Entity>
  implements Collection<Entity>,
             CursorableCollection<Entity>,
             OrderableCollection<Entity>,
             FilterableCollection<Entity>,
             GroupableCollection<Entity>,
             JoinableCollection<Entity>
{
  @NonNull
  private final ModelCollection<Entity> _groupedCollection;

  @NonNull
  private final List<@NonNull Group> _groups;

  @NonNull
  private final List<@NonNull Group> _unmodifiableGroups;

  /**
   * Create a new grouped collection from an existing entity collection and a list of groups.
   *
   * @param groupedCollection An existing entity collection to group.
   * @param groups Groups to apply.
   */
  public ModelAggregation (
    @NonNull final ModelCollection<Entity> groupedCollection,
    @NonNull @MinLen(1) final Iterable<@NonNull Group> groups
  ) {
    _groupedCollection = groupedCollection;

    @NonNull final LinkedList<@NonNull Group> groupList = new LinkedList<>();
    groups.forEach(groupList::addLast);
    _groups = new ArrayList<>(groupList);
    _unmodifiableGroups = Collections.unmodifiableList(_groups);
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
    _groups = new ArrayList<>(toCopy.getGroups());
    _unmodifiableGroups = Collections.unmodifiableList(_groups);
  }

  private ModelAggregation (
    @NonNull final ModelAggregation<Entity> toCopy,
    @NonNull final ModelCollection<Entity> groupedCollection
  ) {
    _groupedCollection = groupedCollection;
    _groups = new ArrayList<>(toCopy.getGroups());
    _unmodifiableGroups = Collections.unmodifiableList(_groups);
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
   * @param groupedCollection A collection to group like this one.
   *
   * @return A grouped collection that group the given collection like this one.
   */
  public @NonNull ModelAggregation<Entity> setGroupedCollection (
    @NonNull final ModelCollection<Entity> groupedCollection
  ) {
    return new ModelAggregation<>(
      this, groupedCollection
    );
  }

  /**
   * @see CursorableCollection#setCursor(Cursor)
   */
  @Override
  public @NonNull ModelAggregation<Entity> setCursor (@NonNull final Cursor cursor) {
    return new ModelAggregation<>(this, _groupedCollection.setCursor(cursor));
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
    return new ModelAggregation<>(this, _groupedCollection.addFilter(filter));
  }

  /**
   * @see FilterableCollection#removeFilter(Filter)
   */
  @Override
  public @NonNull ModelAggregation<Entity> removeFilter (@NonNull final Filter filter) {
    return new ModelAggregation<>(this, _groupedCollection.removeFilter(filter));
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
    return new ModelAggregation<>(
      _groupedCollection,
      Iterables.concat(_groups, Collections.singleton(group))
    );
  }

  @Override
  public @NonNull GroupableCollection<Entity> ungroup (@NonNull final Group group) {
    @NonNull final List<Group> groups = new ArrayList<>(_groups);
    groups.remove(group);

    return (groups.size() <= 0) ? getGroupedCollection()
                                : new ModelAggregation<>(_groupedCollection, groups);
  }

  /**
   * @see GroupableCollection#getGroups()
   */
  @Override
  public @NonNull List<@NonNull Group> getGroups () {
    return _unmodifiableGroups;
  }

  /**
   * @see OrderableCollection#orderBy(Order)
   * @param order
   */
  @Override
  public @NonNull ModelAggregation<Entity> orderBy (@NonNull final Order order) {
    return new ModelAggregation<>(this, _groupedCollection.orderBy(order));
  }

  @Override
  public @NonNull ModelAggregation<Entity> removeOrder (@NonNull final Order order) {
    return new ModelAggregation<>(this, _groupedCollection.removeOrder(order));
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
    return new ModelAggregation<>(this, _groupedCollection.join(relation));
  }

  @Override
  public @NonNull ModelAggregation<Entity> disjoin (@NonNull final Join<?> relation) {
    return new ModelAggregation<>(this, _groupedCollection.disjoin(relation));
  }

  @Override
  public @NonNull ModelAggregation<Entity> disjoin (@NonNull final String name) {
    return new ModelAggregation<>(this, _groupedCollection.disjoin(name));
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
    return Composition.of(_groupedCollection.getOperator(), Composition.of(_groups.toArray(new Operator[0])));
  }

  @Override
  public int hashCode () {
    return Objects.hash(_groupedCollection, _groups);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ModelAggregation) {
      @NonNull final ModelAggregation<?> otherCollection = (ModelAggregation<?>) other;

      return Objects.equals(otherCollection.getGroupedCollection(), _groupedCollection) &&
             Objects.equals(otherCollection.getGroups(), _groups);
    }

    return false;
  }
}
