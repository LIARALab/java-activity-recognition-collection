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

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.ModelAggregation;
import org.liara.collection.ModelCollection;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.grouping.Group;
import org.liara.collection.operator.joining.Embeddable;
import org.liara.collection.operator.joining.Join;
import org.liara.collection.operator.ordering.Order;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JPACollections
{
  public static @NonNull Optional<CharSequence> getOrderingClause (
    @NonNull final ModelCollection<?> modelCollection
  ) {
    return JPACollections.getOrderingClause(modelCollection, modelCollection.getEntityName());
  }

  public static @NonNull Optional<CharSequence> getOrderingClause (
    @NonNull final ModelCollection<?> modelCollection,
    @NonNull final String alias
  ) {
    if (modelCollection.isOrdered()) {
      @NonNull final StringBuilder query         = new StringBuilder();
      @NonNegative final int       orderingCount = modelCollection.getOrderings().size();

      for (int index = 0; index < orderingCount; ++index) {
        @NonNull final Order order = modelCollection.getOrderings().get(index);
        query.append(order.getExpression().replaceAll(":this", alias));
        query.append(" ");
        switch (order.getDirection()) {
          case ASCENDING: query.append("ASC");
            break;
          case DESCENDING: query.append("DESC");
            break;
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
  public static @NonNull Optional<CharSequence> getJoinClause (
    @NonNull final ModelCollection<?> collection
  ) {
    return getJoinClause(collection, collection.getEntityName());
  }

  public static @NonNull Optional<CharSequence> getJoinClause (
    @NonNull final ModelCollection<?> collection,
    @NonNull final String alias
  ) {
    @NonNull final JPAJoinClauseBuilder builder = new JPAJoinClauseBuilder();
    if (hasExplicitJoins(collection)) {
      builder.setJoins(collection.getJoins());
      return Optional.of(builder.build().toString().replaceAll(":super", alias));
    }

    return Optional.empty();
  }

  public static boolean hasExplicitJoins (
    @NonNull final ModelCollection<?> collection
  ) {
    for (@NonNull final Join join : collection.getJoins().values()) {
      if (!(join instanceof Embeddable)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Compile and return this collection filtering clause.
   * <p>
   * If this collection is not filtered, this method will return an empty optional.
   *
   * @return This collection filtering clause.
   */
  public static @NonNull Optional<CharSequence> getFilteringClause (
    @NonNull final ModelCollection<?> modelCollection
  ) {
    return getFilteringClause(modelCollection, modelCollection.getEntityName());
  }

  public static @NonNull Optional<CharSequence> getFilteringClause (
    @NonNull final ModelCollection<?> modelCollection,
    @NonNull final String alias
  ) {
    if (modelCollection.isFiltered()) {
      @NonNull final StringBuilder             query      = new StringBuilder();
      @NonNull final Iterator<@NonNull Filter> filters    = modelCollection.getFilters().iterator();
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
  public static @NonNull CharSequence getFromClause (
    @NonNull final ModelCollection<?> modelCollection
  ) {
    return getFromClause(modelCollection, modelCollection.getEntityName());
  }

  public static @NonNull CharSequence getFromClause (
    @NonNull final ModelCollection<?> modelCollection,
    @NonNull final String alias
  ) {
    return modelCollection.getModelClass().getName() + " " + alias;
  }

  /**
   * Compile a typed query from this collection for a given selection.
   *
   * @param selection Expression to select.
   *
   * @return A typed query from this collection for a given selection.
   */
  public static @NonNull CharSequence getQuery (
    @NonNull final ModelCollection<?> modelCollection,
    @NonNull final String selection
  ) {
    return getQuery(modelCollection, selection, modelCollection.getEntityName());
  }

  public static @NonNull CharSequence getQuery (
    @NonNull final ModelCollection<?> modelCollection,
    @NonNull final String selection,
    @NonNull final String alias
  ) {
    @NonNull final StringBuilder          query           = new StringBuilder();
    @NonNull final Optional<CharSequence> filteringClause = getFilteringClause(
      modelCollection,
      alias
    );
    @NonNull final Optional<CharSequence> orderingClause  = getOrderingClause(
      modelCollection,
      alias
    );
    @NonNull final Optional<CharSequence> joinClause      = getJoinClause(modelCollection, alias);

    query.append("SELECT ");
    query.append(selection.replaceAll(":this", alias));

    query.append(" FROM ");
    query.append(getFromClause(modelCollection, alias));

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
  public static @NonNull Map<@NonNull String, @NonNull Object> getParameters (
    @NonNull final ModelCollection<?> modelCollection
  ) {
    @NonNull final Map<@NonNull String, @NonNull Object> parameters = new HashMap<>();

    @NonNull final Iterator<@NonNull Filter> filters = modelCollection.getFilters().iterator();
    int                                      index   = 0;

    while (filters.hasNext()) {
      final Filter filter = filters.next();
      for (final Map.Entry<@NonNull String, @NonNull Object> entry : filter.getParameters()
                                                                       .entrySet()) {
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
   * @return The grouping clause of this query if any.
   */
  public static @NonNull Optional<CharSequence> getGroupingClause (
    @NonNull final ModelAggregation<?> aggregation
  ) {
    return getGroupingClause(aggregation, aggregation.getEntityName());
  }

  /**
   * @return The grouping clause of this query if any.
   */
  public static @NonNull Optional<CharSequence> getGroupingClause (
    @NonNull final ModelAggregation<?> aggregation,
    @NonNull final String alias
  ) {
    if (aggregation.isGrouped()) {
      @NonNull final StringBuilder            query  = new StringBuilder();
      @NonNull final Iterator<@NonNull Group> groups = aggregation.getGroups().iterator();

      while (groups.hasNext()) {
        @NonNull final Group group = groups.next();

        query.append(group.getExpression().replaceAll(":this", alias));
        if (groups.hasNext()) query.append(", ");
      }

      return Optional.of(query);
    }

    return Optional.empty();
  }

  public static @NonNull CharSequence getQuery (
    @NonNull final ModelAggregation<?> aggregation,
    @NonNull final String selection
  ) {
    @NonNull final Optional<CharSequence> groupingClause = getGroupingClause(aggregation);

    if (groupingClause.isPresent()) {
      return new StringBuilder().append(
        getQuery(
          aggregation.getGroupedCollection(),
          replaceGroupPlaceholders(aggregation, selection)
        )
      ).append(" GROUP BY ").append(groupingClause.get());
    }

    return getQuery(aggregation.getGroupedCollection(), selection);
  }

  private static @NonNull String replaceGroupPlaceholders (
    @NonNull final ModelAggregation<?> aggregation,
    @NonNull final String selection
  ) {
    @NonNull final Pattern       pattern = Pattern.compile(":groups(\\[(\\d+)])?");
    @NonNull final Matcher       matcher = pattern.matcher(selection);
    @NonNull final StringBuilder groups  = new StringBuilder();

    for (int index = 0; index < aggregation.getGroups().size(); ++index) {
      if (index > 0) {
        groups.append(", ");
      }
      groups.append(aggregation.getGroups().get(index).getExpression());
    }

    return matcher.replaceAll(
      match -> (match.group(2) == null) ? groups.toString()
                                        : aggregation.getGroups()
                                            .get(Integer.parseInt(match.group(2)))
                                            .getExpression()
    );
  }

}
