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

import java.util.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.cursoring.CursorableCollection;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.filtering.FilterableCollection;
import org.liara.collection.operator.grouping.Group;
import org.liara.collection.operator.grouping.GroupableCollection;
import org.liara.collection.operator.ordering.Order;
import org.liara.collection.operator.ordering.OrderableCollection;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * A collection of database entities.
 *
 * @param <Entity> Type of entity in the collection.
 *
 * @author C&eacute;dric DEMONGIVERT [cedric.demongivert@gmail.com](mailto:cedric.demongivert@gmail.com)
 */
@JsonSerialize(using = JPAEntityCollectionSerializer.class)
public class JPAEntityCollection<Entity>
       implements Collection,
                  CursorableCollection,
                  OrderableCollection,
                  FilterableCollection,
                  GroupableCollection
{
  @NonNull
  private final EntityManager _entityManager;

  @NonNull
  private final Class<Entity> _contentType;

  @NonNull
  private final JPAEntityCollectionConfiguration _configuration;

  /**
   * Create a collection of a given entity and managed by a given manager instance.
   *
   * @param entityManager Entity manager that manage this collection.
   * @param entity        Entity type stored into this collection.
   */
  public JPAEntityCollection (
    @NonNull final EntityManager entityManager,
    @NonNull final Class<Entity> entity
  )
  {
    _entityManager = entityManager;
    _contentType = entity;
    _configuration = new JPAEntityCollectionConfiguration();
  }

  /**
   * Create a copy of another collection.
   *
   * @param collection Collection to copy.
   */
  public JPAEntityCollection (
    @NonNull final JPAEntityCollection<Entity> collection
  )
  {
    _entityManager = collection.getEntityManager();
    _contentType = collection.getEntityType();
    _configuration = collection.getConfiguration();
  }

  /**
   * Create a collection of the same type of the given one with a different configuration.
   *
   * @param collection Collection to copy.
   * @param configuration New Configuration to apply.
   */
  private JPAEntityCollection (
    @NonNull final JPAEntityCollection<Entity> collection,
    @NonNull final JPAEntityCollectionConfiguration configuration
  )
  {
    _entityManager = collection.getEntityManager();
    _contentType = collection.getEntityType();
    _configuration = configuration;
  }

  /**
   * Return the name used to identify the entity of this query.
   *
   * @return The name used to identify the entity of this query.
   */
  public @NonNull String getEntityName () {
    @NonNull final String typeName = _contentType.getSimpleName();
    return Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
  }

  /**
   * Compile and return this collection ordering clause.
   *
   * If this collection is not ordered, this method will return an empty optional.
   *
   * @return This collection ordering clause.
   */
  public @NonNull Optional<CharSequence> getOrderingClause () {
    if (isOrdered()) {
      @NonNull final String entityName = getEntityName();
      @NonNull final StringBuilder query = new StringBuilder();
      @NonNegative final int orderingCount = _configuration.getOrderingCount();

      for (int index = 0; index < orderingCount; ++index) {
        @NonNull final Order order = _configuration.getOrdering(index);
        query.append(order.getField().replaceAll(":this", entityName));
        query.append(" ");
        switch (order.getDirection()) {
          case ASCENDING: query.append("ASC"); break;
          case DESCENDING: query.append("DESC"); break;
        }

        if (index < orderingCount - 1) {
          query.append(", ");
        }
      }

      return Optional.of(query);
    }

    return Optional.empty();
  }

  /**
   * Compile and return this collection filtering clause.
   *
   * If this collection is not filtered, this method will return an empty optional.
   *
   * @return This collection filtering clause.
   */
  public @NonNull Optional<CharSequence> getFilteringClause () {
    if (isFiltered()) {
      @NonNull final String entityName = getEntityName();
      @NonNull final StringBuilder query = new StringBuilder();
      @NonNull final Iterator<@NonNull Filter> filters = _configuration.getFilters().iterator();
      @NonNegative int index = 0;

      while (filters.hasNext()) {
        @NonNull final Filter filter = filters.next();

        query.append(
          filter.getExpression()
                .replaceAll(":this", entityName)
                .replaceAll(
                  ":([a-zA-Z0-9_]+)",
                  String.join("", ":filter", String.valueOf(index), "_$1")
                )
        );

        if (filters.hasNext()) {
          query.append(" AND ");
        }

        index += 1;
      }

      return Optional.of(query);
    }

    return Optional.empty();
  }

  /**
   * Return this collection from clause.
   *
   * @return This collection from clause.
   */
  public @NonNull CharSequence getFromClause () {
    return _contentType.getName() + " " + getEntityName();
  }

  /**
   * Compile a typed query from this collection for a given selection.
   *
   * @param selection Expression to select.
   *
   * @return A typed query from this collection for a given selection.
   */
  public @NonNull CharSequence getQuery (
    @NonNull final String selection
  ) {
    @NonNull final StringBuilder query = new StringBuilder();
    @NonNull final Optional<CharSequence> filteringClause = getFilteringClause();
    @NonNull final Optional<CharSequence> orderingClause = getOrderingClause();
    query.append("SELECT ");
    query.append(selection.replaceAll(":this", getEntityName()));
    query.append(" FROM ");
    query.append(getFromClause());

    if (filteringClause.isPresent()) {
      query.append(" WHERE ");
      query.append(filteringClause.get());
    }

    if (orderingClause.isPresent()) {
      query.append(" ORDER BY ");
      query.append(orderingClause.get());
    }

    return query;
  }

  /**
   * Return the parameters to bind to this collection query.
   *
   * @return The parameters to bind to this collection query.
   */
  public @NonNull Map<@NonNull String, @NonNull Object> getParameters () {
    @NonNull final Map<@NonNull String, @NonNull Object> parameters = new HashMap<>();

    @NonNull final Iterator<@NonNull Filter> filters = _configuration.getFilters().iterator();
    int index = 0;

    while (filters.hasNext()) {
      final Filter filter = filters.next();
      for (final Map.Entry<@NonNull String, @NonNull Object> entry : filter.getParameters().entrySet()) {
        parameters.put(
          String.join("", "filter", String.valueOf(index), "_", entry.getKey()),
          entry.getValue()
        );
      }
      index += 1;
    }

    return parameters;
  }

  /**
   * Compile a typed query from this collection for a given selection.
   *
   * @param selection Expression to select.
   * @param selectionType Result type of the returned typed query.
   * @param <Selection> Type of entity selected by the returned query.
   *
   * @return A typed query from this collection for a given selection.
   */
  public <Selection> @NonNull TypedQuery<Selection> select (
    @NonNull final String selection,
    @NonNull final Class<Selection> selectionType
  ) {
    @NonNull final TypedQuery<Selection> result = _entityManager.createQuery(
      getQuery(selection).toString(),
      selectionType
    );
    @NonNull final Cursor cursor = _configuration.getCursor();

    for (final Map.Entry<@NonNull String, @NonNull Object> parameter : getParameters().entrySet()) {
      result.setParameter(parameter.getKey(), parameter.getValue());
    }

    result.setFirstResult(cursor.getOffset());

    if (cursor.hasLimit()) {
      result.setMaxResults(cursor.getLimit());
    }

    return result;
  }

  /**
   * Return the number of elements selected by this collection.
   *
   * @return The number of elements selected by this collection.
   */
  public @NonNegative long findSize () {
    return select("COUNT(:this)", Long.class).getSingleResult();
  }

  /**
   * Return all elements selected by this collection.
   *
   * @return All the elements selected by this collection.
   */
  public @NonNull List<@NonNull Entity> find () {
    return select(":this", _contentType).getResultList();
  }

  /**
   * Return the type of entity stored into this collection.
   *
   * @return The type of entity stored into this collection.
   */
  public @NonNull Class<Entity> getEntityType () {
    return _contentType;
  }

  /**
   * Return the entity manager related to this collection.
   *
   * @return The entity manager related to this collection.
   */
  public @NonNull EntityManager getEntityManager () { return _entityManager; }


  /**
   * @see CursorableCollection#getCursor()
   */
  public @NonNull Cursor getCursor () {
    return _configuration.getCursor();
  }

  /**
   * @see CursorableCollection#setCursor(Cursor)
   */
  public @NonNull JPAEntityCollection<Entity> setCursor (@NonNull final Cursor cursor) {
    return new JPAEntityCollection<>(this, _configuration.setCursor(cursor));
  }

  /**
   * @see OrderableCollection#orderBy(Order)
   */
  @Override
  public @NonNull JPAEntityCollection<Entity> orderBy (@NonNull final Order order) {
    return new JPAEntityCollection<>(this, _configuration.orderBy(order));
  }

  /**
   * @see OrderableCollection#getOrdering(int)
   */
  @Override
  public @NonNull Order getOrdering (@NonNegative @LessThan("this.getOrderingCount()") int index) {
    return _configuration.getOrdering(index);
  }

  /**
   * @see OrderableCollection#getOrderingCount()
   */
  @Override
  public @NonNegative int getOrderingCount () {
    return _configuration.getOrderingCount();
  }

  /**
   * @see OrderableCollection#getOrderings()
   */
  @Override
  public @NonNull List<@NonNull Order> getOrderings () {
    return _configuration.getOrderings();
  }

  /**
   * @see OrderableCollection#orderings()
   */
  @Override
  public @NonNull Iterable<@NonNull Order> orderings () {
    return _configuration.orderings();
  }

  /**
   * @see FilterableCollection#addFilter(Filter)
   */
  @Override
  public @NonNull JPAEntityCollection<Entity> addFilter (@NonNull final Filter filter) {
    return new JPAEntityCollection<>(this, _configuration.addFilter(filter));
  }

  /**
   * @see FilterableCollection#removeFilter(Filter)
   */
  @Override
  public @NonNull JPAEntityCollection<Entity> removeFilter (@NonNull final Filter filter) {
    return new JPAEntityCollection<>(this, _configuration.removeFilter(filter));
  }

  /**
   * @see FilterableCollection#getFilterCount()
   */
  @Override
  public @NonNegative int getFilterCount () {
    return _configuration.getFilterCount();
  }

  /**
   * @see FilterableCollection#getFilters()
   */
  @Override
  public @NonNull Set<@NonNull Filter> getFilters () {
    return _configuration.getFilters();
  }

  /**
   * @see FilterableCollection#filters()
   */
  @Override
  public @NonNull Iterable<@NonNull Filter> filters () {
    return _configuration.filters();
  }

  /**
   * @see GroupableCollection#groupBy(Group)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> groupBy (@NonNull final Group group) {
    return new GroupedJPAEntityCollection<>(this, Collections.singletonList(group));
  }

  /**
   * @see GroupableCollection#getGroup(int)
   */
  @Override
  public @NonNull Group getGroup (@NonNegative @LessThan("this.getGroupCount()") int index) {
    throw new IndexOutOfBoundsException("Index out of bounds " + index + ".");
  }

  @Override
  public @NonNegative int getGroupCount () {
    return 0;
  }

  @Override
  public @NonNull List<@NonNull Group> getGroups () {
    return Collections.emptyList();
  }

  @Override
  public @NonNull Iterable<@NonNull Group> groups () {
    return Collections.emptyList();
  }

  private @NonNull JPAEntityCollectionConfiguration getConfiguration () {
    return _configuration;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_contentType, _configuration);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof JPAEntityCollection) {
      @NonNull final JPAEntityCollection otherCollection = (JPAEntityCollection) other;

      return Objects.equals(_contentType, otherCollection.getEntityType()) &&
             Objects.equals(_configuration, otherCollection.getConfiguration());
    }

    return false;
  }
}
