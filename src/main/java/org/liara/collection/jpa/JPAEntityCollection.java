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
package org.liara.collection.jpa;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;
import org.liara.collection.CollectionConfiguration;
import org.liara.collection.CollectionConfigurationBuilder;
import org.liara.collection.operator.Composition;
import org.liara.collection.operator.Operator;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.cursoring.CursorableCollection;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.filtering.FilterableCollection;
import org.liara.collection.operator.grouping.Group;
import org.liara.collection.operator.grouping.GroupableCollection;
import org.liara.collection.operator.joining.Embeddable;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.joining.JoinableCollection;
import org.liara.collection.operator.ordering.Order;
import org.liara.collection.operator.ordering.OrderableCollection;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * A collection of database entities.
 *
 * @param <Entity> Type of entity in the collection.
 *
 * @author C&eacute;dric DEMONGIVERT [cedric.demongivert@gmail.com](mailto:cedric.demongivert@gmail.com)
 */
@JsonSerialize(using = JPAEntityCollectionSerializer.class)
public class JPAEntityCollection<Entity>
  implements Collection<Entity>,
             CursorableCollection<Entity>,
             OrderableCollection<Entity>,
             FilterableCollection<Entity>,
             GroupableCollection<Entity>,
             JoinableCollection<Entity>
{
  @NonNull
  private static final List<@NonNull Group> EMPTY_GROUP_LIST = Collections.unmodifiableList(
    Collections.emptyList());

  @NonNull
  private final EntityManager _entityManager;

  @NonNull
  private final Class<Entity> _modelClass;

  @NonNull
  private final CollectionConfiguration _configuration;

  @NonNull
  private final JPAJoinClauseBuilder _jpaJoinClauseBuilder;

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
    _modelClass = entity;
    _configuration = new CollectionConfiguration();
    _jpaJoinClauseBuilder = new JPAJoinClauseBuilder();
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
    _modelClass = collection.getModelClass();
    _configuration = collection.getConfiguration();
    _jpaJoinClauseBuilder = new JPAJoinClauseBuilder();
  }

  /**
   * Create a collection of the same type of the given one with a different configuration.
   *
   * @param collection Collection to copy.
   * @param configuration New Configuration to apply.
   */
  private JPAEntityCollection (
    @NonNull final JPAEntityCollection<Entity> collection, @NonNull final CollectionConfiguration configuration
  )
  {
    _entityManager = collection.getEntityManager();
    _modelClass = collection.getModelClass();
    _configuration = configuration;
    _jpaJoinClauseBuilder = new JPAJoinClauseBuilder();
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

  /**
   * Compile and return this collection ordering clause.
   *
   * If this collection is not ordered, this method will return an empty optional.
   *
   * @return This collection ordering clause.
   */
  public @NonNull Optional<CharSequence> getOrderingClause () {
    return getOrderingClause(getEntityName());
  }

  public @NonNull Optional<CharSequence> getOrderingClause (@NonNull final String alias) {
    if (isOrdered()) {
      @NonNull final StringBuilder query         = new StringBuilder();
      @NonNegative final int       orderingCount = _configuration.getOrderings().size();

      for (int index = 0; index < orderingCount; ++index) {
        @NonNull final Order order = _configuration.getOrderings().get(index);
        query.append(order.getExpression().replaceAll(":this", alias));
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
   * Compile and return this collection join clause.
   *
   * @return This collection join clause.
   */
  private @NonNull Optional<CharSequence> getJoinClause () {
    return getJoinClause(getEntityName());
  }

  private @NonNull Optional<CharSequence> getJoinClause (@NonNull final String alias) {
    if (hasExplicitJoins()) {
      _jpaJoinClauseBuilder.setJoins(_configuration.getJoins());
      return Optional.of(_jpaJoinClauseBuilder.build()
                           .toString()
                           .replaceAll(":super", alias));
    }

    return Optional.empty();
  }

  private boolean hasExplicitJoins () {
    for (@NonNull final Join join : getJoins().values()) {
      if (!(join instanceof Embeddable)) {
        return true;
      }
    }

    return false;
  }


  /**
   * Compile and return this collection filtering clause.
   *
   * If this collection is not filtered, this method will return an empty optional.
   *
   * @return This collection filtering clause.
   */
  public @NonNull Optional<CharSequence> getFilteringClause () {
    return getFilteringClause(getEntityName());
  }


  public @NonNull Optional<CharSequence> getFilteringClause (@NonNull final String alias) {
    if (isFiltered()) {
      @NonNull final StringBuilder             query      = new StringBuilder();
      @NonNull final Iterator<@NonNull Filter> filters    = _configuration.getFilters().iterator();
      @NonNull final FilterNamespacer          namespacer = new FilterNamespacer(alias);

      while (filters.hasNext()) {
        @NonNull final Filter filter = filters.next();

        query.append(
          FilterNamespacer.PATTERN.matcher(filter.getExpression()).replaceAll(namespacer)
        );

        if (filters.hasNext()) {
          query.append(" AND ");
        }

        namespacer.next();
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
    return getFromClause(getEntityName());
  }

  public @NonNull CharSequence getFromClause (@NonNull final String alias) {
    return _modelClass.getName() + " " + alias;
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
    return getQuery(selection, getEntityName());
  }

  public @NonNull CharSequence getQuery (
    @NonNull final String selection,
    @NonNull final String alias
  ) {
    @NonNull final StringBuilder          query           = new StringBuilder();
    @NonNull final Optional<CharSequence> filteringClause = getFilteringClause(alias);
    @NonNull final Optional<CharSequence> orderingClause  = getOrderingClause(alias);
    @NonNull final Optional<CharSequence> joinClause      = getJoinClause(alias);

    query.append("SELECT ");
    query.append(selection.replaceAll(":this", alias));

    query.append(" FROM ");
    query.append(getFromClause(alias));

    if (joinClause.isPresent()) {
      query.append(" ");
      query.append(joinClause.get());
    }

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
  @Override
  public @NonNegative @NonNull Long count () {
    return select("COUNT(:this)", Long.class).getSingleResult();
  }

  /**
   * Return all elements selected by this collection.
   *
   * @return All the elements selected by this collection.
   */
  @Override
  public @NonNull List<@NonNull Entity> fetch () {
    return select(":this", _modelClass).getResultList();
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
    return new JPAEntityCollection<>(this,
      CollectionConfigurationBuilder.from(_configuration).setCursor(cursor).build()
    );
  }

  /**
   * @see OrderableCollection#orderBy(Order)
   * @param order
   */
  @Override
  public JPAEntityCollection<Entity> orderBy (final Order order) {
    return new JPAEntityCollection<>(this, CollectionConfigurationBuilder.from(_configuration).orderBy(order).build());
  }

  /**
   * @see OrderableCollection#removeOrder(Order)
   * @param order
   */
  @Override
  public JPAEntityCollection<Entity> removeOrder (final Order order) {
    return new JPAEntityCollection<>(this,
      CollectionConfigurationBuilder.from(_configuration).removeOrder(order).build()
    );
  }

  /**
   * @see OrderableCollection#getOrderings()
   */
  @Override
  public @NonNull List<@NonNull Order> getOrderings () {
    return _configuration.getOrderings();
  }

  /**
   * @see FilterableCollection#addFilter(Filter)
   */
  @Override
  public @NonNull JPAEntityCollection<Entity> addFilter (@NonNull final Filter filter) {
    return new JPAEntityCollection<>(this,
      CollectionConfigurationBuilder.from(_configuration).addFilter(filter).build()
    );
  }

  /**
   * @see FilterableCollection#removeFilter(Filter)
   */
  @Override
  public @NonNull JPAEntityCollection<Entity> removeFilter (@NonNull final Filter filter) {
    return new JPAEntityCollection<>(this,
      CollectionConfigurationBuilder.from(_configuration).removeFilter(filter).build()
    );
  }

  /**
   * @see FilterableCollection#getFilters()
   */
  @Override
  public @NonNull Set<@NonNull Filter> getFilters () {
    return _configuration.getFilters();
  }

  /**
   * @see GroupableCollection#groupBy(Group)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> groupBy (@NonNull final Group group) {
    return new GroupedJPAEntityCollection<>(this, Collections.singletonList(group));
  }

  @Override
  public @NonNull JPAEntityCollection<Entity> ungroup (@NonNull final Group group) {
    return this;
  }

  @Override
  public @NonNull List<@NonNull Group> getGroups () {
    return EMPTY_GROUP_LIST;
  }

  @Override
  public @NonNull JPAEntityCollection<Entity> join (@NonNull final Join<?> relation) {
    if (!_configuration.getJoins()
           .containsKey(relation.getName()) || !Objects.equals(_configuration.getJoins()
                                                                 .get(relation.getName()), relation)) {
      return new JPAEntityCollection<>(this,
        CollectionConfigurationBuilder.from(_configuration).join(relation).build()
      );
    } else {
      return this;
    }
  }

  @Override
  public @NonNull JPAEntityCollection<Entity> disjoin (@NonNull final Join<?> relation) {
    return getJoins().get(relation.getName()).equals(relation) ? new JPAEntityCollection<>(this,
      CollectionConfigurationBuilder.from(_configuration).disjoin(relation).build()
    ) : this;
  }

  @Override
  public @NonNull JPAEntityCollection<Entity> disjoin (@NonNull final String name) {
    return new JPAEntityCollection<>(this, CollectionConfigurationBuilder.from(_configuration).disjoin(name).build());
  }

  @Override
  public @NonNull Map<@NonNull String, @NonNull Join> getJoins () {
    return _configuration.getJoins();
  }

  private @NonNull CollectionConfiguration getConfiguration () {
    return _configuration;
  }

  @Override
  public @NonNull Collection<?> setOperator (@Nullable final Operator operator) {
    if (operator == null) {
      return new JPAEntityCollection<>(this, new CollectionConfiguration());
    } else {
      return operator.apply(new JPAEntityCollection<>(this, new CollectionConfiguration()));
    }

  }

  @Override
  public @NonNull Operator getOperator () {
    return Composition.of(
      Composition.of(_configuration.getOrderings().toArray(new Operator[0])),
      Composition.of(_configuration.getFilters().toArray(new Operator[0])),
      Composition.of(_configuration.getCursor())
    );
  }

  @Override
  public int hashCode () {
    return Objects.hash(_modelClass, _configuration);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof JPAEntityCollection) {
      @NonNull final JPAEntityCollection otherCollection = (JPAEntityCollection) other;

      return Objects.equals(_modelClass, otherCollection.getModelClass()) &&
             Objects.equals(_configuration, otherCollection.getConfiguration());
    }

    return false;
  }
}
