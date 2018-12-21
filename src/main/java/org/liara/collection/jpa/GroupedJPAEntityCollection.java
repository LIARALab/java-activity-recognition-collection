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
import org.checkerframework.com.google.common.collect.Iterables;
import org.checkerframework.common.value.qual.MinLen;
import org.liara.collection.Collection;
import org.liara.collection.operator.Composition;
import org.liara.collection.operator.Operator;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.cursoring.CursorableCollection;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.filtering.FilterableCollection;
import org.liara.collection.operator.grouping.Group;
import org.liara.collection.operator.grouping.GroupableCollection;
import org.liara.collection.operator.ordering.Order;
import org.liara.collection.operator.ordering.OrderableCollection;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class      GroupedJPAEntityCollection<Entity>
       implements Collection,
                  CursorableCollection,
                  OrderableCollection,
                  FilterableCollection,
                  GroupableCollection
{
  @NonNull
  private final JPAEntityCollection<Entity> _groupedCollection;

  @NonNull
  private final List<@NonNull Group> _groups;

  /**
   * Create a new grouped collection from an existing entity collection and a list of groups.
   *
   * @param groupedCollection An existing entity collection to group.
   * @param groups Groups to apply.
   */
  public GroupedJPAEntityCollection (
    @NonNull final JPAEntityCollection<Entity> groupedCollection,
    @NonNull @MinLen(1) final Iterable<@NonNull Group> groups
  ) {
    _groupedCollection = groupedCollection;

    @NonNull final LinkedList<@NonNull Group> groupList = new LinkedList<>();
    groups.forEach(groupList::addLast);
    _groups = new ArrayList<>(groupList);
  }

  /**
   * Create a copy of an existing grouped collection.
   *
   * @param toCopy A grouped collection to copy.
   */
  public GroupedJPAEntityCollection (
    @NonNull final GroupedJPAEntityCollection<Entity> toCopy
  ) {
    _groupedCollection = toCopy.getGroupedCollection();
    _groups = new ArrayList<>(toCopy.getGroups());
  }

  private GroupedJPAEntityCollection (
    @NonNull final GroupedJPAEntityCollection<Entity> toCopy,
    @NonNull final JPAEntityCollection<Entity> groupedCollection
  ) {
    _groupedCollection = groupedCollection;
    _groups = new ArrayList<>(toCopy.getGroups());
  }

  /**
   * Build an aggregation query and return the result.
   *
   * @param expression An aggregation expression.
   * @return An aggregation query.
   */
  public @NonNull Query aggregate (@NonNull final String expression) {
    @NonNull final Query result = getEntityManager().createQuery(
      getQuery(expression).toString()
    );

    for (final Map.Entry<String, Object> parameter : getParameters().entrySet()) {
      result.setParameter(parameter.getKey(), parameter.getValue());
    }

    return result;
  }

  /**
   * Build an aggregation query and return the result.
   *
   * @param expression An aggregation expression.
   * @param returnType Return type of the aggregation query.
   * @param <Result> Return type of the aggregation query.
   * @return An aggregation query.
   */
  public <Result> @NonNull TypedQuery<Result> aggregate (
    @NonNull final String expression,
    @NonNull Class<Result> returnType
  ) {
    @NonNull final TypedQuery<Result> result = getEntityManager().createQuery(
      getQuery(expression).toString(), returnType
    );

    for (final Map.Entry<String, Object> parameter : getParameters().entrySet()) {
      result.setParameter(parameter.getKey(), parameter.getValue());
    }

    return result;
  }

  /**
   * @return The grouping clause of this query if any.
   */
  public @NonNull Optional<CharSequence> getGroupingClause () {
    if (isGrouped()) {
      @NonNull final StringBuilder query = new StringBuilder();
      @NonNull final Iterator<@NonNull Group> groups = _groups.iterator();
      @NonNull final String entityName = getEntityName();

      while (groups.hasNext()) {
        @NonNull final Group group = groups.next();

        query.append(group.getExpression().replaceAll(":this", entityName));
        if (groups.hasNext()) query.append(", ");
      }

      return Optional.of(query);
    }

    return Optional.empty();
  }

  /**
   * @see JPAEntityCollection#getFromClause()
   */
  public @NonNull CharSequence getFromClause () { return _groupedCollection.getFromClause(); }

  /**
   * @see JPAEntityCollection#getQuery(String)
   */
  public @NonNull CharSequence getQuery (@NonNull final String selection) {
    @NonNull final Optional<CharSequence> groupingClause = getGroupingClause();

    if (groupingClause.isPresent()) {
      return new StringBuilder().append(
        _groupedCollection.getQuery(replaceGroupPlaceholders(selection))
      ).append(" GROUP BY ").append(groupingClause.get());
    }

    return _groupedCollection.getQuery(selection);
  }

  private @NonNull String replaceGroupPlaceholders (@NonNull final String selection) {
    @NonNull final Pattern       pattern = Pattern.compile(":groups(\\[(\\d+)])?");
    @NonNull final Matcher       matcher = pattern.matcher(selection);
    @NonNull final StringBuilder groups = new StringBuilder();

    for (int index = 0; index < getGroupCount(); ++index) {
      if (index > 0) {
        groups.append(", ");
      }
      groups.append(getGroup(index).getExpression());
    }

    return matcher.replaceAll(
      match -> (match.group(2) == null) ? groups.toString()
                                        : getGroup(Integer.parseInt(match.group(2))).getExpression()
    );
  }

  /**
   * @see JPAEntityCollection#getParameters()
   */
  public @NonNull Map<@NonNull String, @NonNull Object> getParameters () { return _groupedCollection.getParameters(); }

  /**
   * @see JPAEntityCollection#getEntityManager()
   */
  public @NonNull EntityManager getEntityManager () { return _groupedCollection.getEntityManager(); }

  /**
   * @see JPAEntityCollection#getEntityName()
   */
  public @NonNull String getEntityName () {return _groupedCollection.getEntityName();}

  /**
   * @see JPAEntityCollection#getEntityType()
   */
  public @NonNull Class<Entity> getEntityType () {return _groupedCollection.getEntityType();}

  /**
   * @see JPAEntityCollection#getOrderingClause()
   */
  public @NonNull Optional<CharSequence> getOrderingClause () {return _groupedCollection.getOrderingClause();}

  /**
   * @see JPAEntityCollection#getFilteringClause()
   */
  public @NonNull Optional<CharSequence> getFilteringClause () {return _groupedCollection.getFilteringClause();}

  /**
   * Return the underlying grouped collection.
   *
   * @return The underlying grouped collection.
   */
  public @NonNull JPAEntityCollection<Entity> getGroupedCollection () {
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
  public @NonNull GroupedJPAEntityCollection<Entity> setGroupedCollection (
    @NonNull final JPAEntityCollection<Entity> groupedCollection
  ) {
    return new GroupedJPAEntityCollection<>(
      this, groupedCollection
    );
  }

  /**
   * @see CursorableCollection#setCursor(Cursor)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> setCursor (@NonNull final Cursor cursor) {
    return new GroupedJPAEntityCollection<>(this, _groupedCollection.setCursor(cursor));
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
  public @NonNull GroupedJPAEntityCollection<Entity> addFilter (@NonNull final Filter filter) {
    return new GroupedJPAEntityCollection<>(this, _groupedCollection.addFilter(filter));
  }

  /**
   * @see FilterableCollection#removeFilter(Filter)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> removeFilter (@NonNull final Filter filter) {
    return new GroupedJPAEntityCollection<>(this, _groupedCollection.removeFilter(filter));
  }

  /**
   * @see FilterableCollection#getFilterCount()
   */
  @Override
  public @NonNegative int getFilterCount () {
    return _groupedCollection.getFilterCount();
  }

  /**
   * @see FilterableCollection#getFilters()
   */
  @Override
  public @NonNull Set<@NonNull Filter> getFilters () {
    return _groupedCollection.getFilters();
  }

  /**
   * @see FilterableCollection#filters()
   */
  @Override
  public @NonNull Iterable<@NonNull Filter> filters () {
    return _groupedCollection.filters();
  }

  /**
   * @see GroupableCollection#groupBy(Group)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> groupBy (@NonNull final Group group) {
    return new GroupedJPAEntityCollection<>(
      _groupedCollection,
      Iterables.concat(_groups, Collections.singleton(group))
    );
  }

  @Override
  public @NonNull GroupableCollection ungroup (@NonNull final Group group) {
    @NonNull final List<Group> groups = new ArrayList<>(_groups);
    groups.remove(group);

    return (groups.size() <= 0) ? getGroupedCollection() : new GroupedJPAEntityCollection<>(_groupedCollection, groups);
  }

  /**
   * @see GroupableCollection#getGroup(int)
   */
  @Override
  public @NonNull Group getGroup (@NonNegative @LessThan("this.getGroupCount()") final int index) {
    return _groups.get(index);
  }

  /**
   * @see GroupableCollection#getGroupCount()
   */
  @Override
  public @NonNegative int getGroupCount () {
    return _groups.size();
  }

  /**
   * @see GroupableCollection#getGroups()
   */
  @Override
  public @NonNull List<@NonNull Group> getGroups () {
    return Collections.unmodifiableList(_groups);
  }

  /**
   * @see GroupableCollection#groups()
   */
  @Override
  public @NonNull Iterable<@NonNull Group> groups () {
    return Collections.unmodifiableList(_groups);
  }

  /**
   * @see OrderableCollection#orderBy(Order)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> orderBy (@NonNull final Order order) {
    return new GroupedJPAEntityCollection<>(this, _groupedCollection.orderBy(order));
  }

  @Override
  public @NonNull OrderableCollection removeOrder (@NonNull final Order order) {
    return new GroupedJPAEntityCollection<>(this, _groupedCollection.removeOrder(order));
  }

  /**
   * @see OrderableCollection#getOrdering(int)
   */
  @Override
  public @NonNull Order getOrdering (@NonNegative @LessThan("this.getOrderingCount()") int index) {
    return _groupedCollection.getOrdering(index);
  }

  /**
   * @see OrderableCollection#getOrderingCount()
   */
  @Override
  public @NonNegative int getOrderingCount () {
    return _groupedCollection.getOrderingCount();
  }

  /**
   * @see OrderableCollection#getOrderings()
   */
  @Override
  public @NonNull List<@NonNull Order> getOrderings () {
    return _groupedCollection.getOrderings();
  }

  /**
   * @see OrderableCollection#orderings()
   */
  @Override
  public @NonNull Iterable<@NonNull Order> orderings () {
    return _groupedCollection.orderings();
  }

  @Override
  public JPAEntityCollection<Entity> clear () {
    return _groupedCollection.clear();
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

    if (other instanceof GroupedJPAEntityCollection) {
      @NonNull final GroupedJPAEntityCollection<?> otherCollection = (GroupedJPAEntityCollection<?>) other;

      return Objects.equals(otherCollection.getGroupedCollection(), _groupedCollection) &&
             Objects.equals(otherCollection.getGroups(), _groups);
    }

    return false;
  }
}
