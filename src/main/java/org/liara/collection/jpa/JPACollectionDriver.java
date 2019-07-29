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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.GraphCollection;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.grouping.Group;
import org.liara.collection.operator.ordering.Order;
import org.liara.collection.operator.ordering.OrderingDirection;
import org.liara.collection.operator.selection.Select;
import org.liara.collection.source.JoinSource;
import org.liara.collection.source.Source;
import org.liara.collection.source.TableSource;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public final class JPACollectionDriver
{
  @NonNull
  private final JPAExpressionTranspiler _jpaExpressionTranspiler;

  @NonNull
  private final StringBuilder _builder;

  public JPACollectionDriver () {
    _jpaExpressionTranspiler = new JPAExpressionTranspiler();
    _builder = new StringBuilder();
  }

  /**
   * Return a valid JPA ordering clause for the given collection if any.
   *
   * @param collection A graph collection from which building an ordering clause.
   *
   * @return An ordering clause for the given collection if any.
   */
  public @NonNull Optional<String> getOrderingClause (@NonNull final GraphCollection collection) {
    if (collection.isOrdered()) {
      @NonNull final List<@NonNull Order> orders = collection.getOrderings();

      for (int index = 0, size = orders.size(); index < size; ++index) {
        @NonNull final Order order = orders.get(index);
        _builder.append(_jpaExpressionTranspiler.transpile(order.getExpression()));
        _builder.append(" ");
        _builder.append(order.getDirection() == OrderingDirection.ASCENDING ? "ASC" : "DESC");

        if (index < size - 1) _builder.append(", ");
      }

      @NonNull final String result = _builder.toString();
      _builder.setLength(0);

      return Optional.of(result);
    }

    return Optional.empty();
  }

  /**
   * Return a valid JPA from clause for the given collection.
   *
   * @param collection A graph collection from which building a from clause.
   *
   * @return A from clause for the given collection.
   */
  public @NonNull String getFromClause (@NonNull final GraphCollection collection) {
    @Nullable Source source = collection.getSource();

    while (source != null) {
      if (source instanceof TableSource) {
        source = renderTableSource((TableSource) source);
      } else if (source instanceof JoinSource) {
        source = renderJoinSource((JoinSource) source);
      } else {
        throw new IllegalStateException("Unhandled source of type " + source.getClass());
      }
    }

    @NonNull final String result = _builder.toString();

    _builder.setLength(0);

    return result;
  }

  private @Nullable Source renderJoinSource (@NonNull final JoinSource source) {
    _builder.insert(0, _jpaExpressionTranspiler.transpile(source.getPredicate()));
    _builder.insert(0, " ON ");

    if (source.getName() != source.getJoined().getName()) {
      _builder.insert(0, source.getName());
      _builder.insert(0, " AS ");
    }

    _builder.insert(0, source.getJoined().getName());

    switch (source.getType()) {
      case INNER_JOIN: _builder.insert(0, "INNER JOIN");
        break;
      case CROSS_JOIN: _builder.insert(0, "CROSS JOIN");
        break;
      case LEFT_OUTER_JOIN: _builder.insert(0, "LEFT OUTER JOIN");
        break;
      case RIGHT_OUTER_JOIN: _builder.insert(0, "RIGHT OUTER JOIN");
        break;
    }

    return source.getOrigin();
  }

  private @Nullable Source renderTableSource (@NonNull final TableSource source) {
    if (source.getName() != source.getTable().getName()) {
      _builder.insert(0, source.getName());
      _builder.insert(0, " AS ");
    }

    _builder.insert(0, source.getTable().getName());

    return null;
  }

  /**
   * Build and return a valid JPA where clause for the given collection if any.
   *
   * @param collection A collection from which extracting the where clause.
   *
   * @return A valid JPA where clause for the given collection if any.
   */
  public @NonNull Optional<String> getWhereClause (@NonNull final GraphCollection collection) {
    if (collection.isFiltered()) {
      @NonNull final Iterator<@NonNull Filter> filters = collection.getFilters().iterator();

      _builder.append('(');
      while (filters.hasNext()) {
        @NonNull final Filter filter = filters.next();
        _builder.append(_jpaExpressionTranspiler.transpile(filter.getExpression()));

        if (filters.hasNext()) {
          _builder.append(") AND (");
        }
      }
      _builder.append(')');

      @NonNull final String result = _builder.toString();
      _builder.setLength(0);

      return Optional.of("(" + result + ")");
    }

    return Optional.empty();
  }

  /**
   * Build and return a valid JPA grouping clause for the given collection if any.
   *
   * @param collection A collection from which extracting the grouping clause.
   *
   * @return A valid JPA grouping clause for the given collection if any.
   */
  public @NonNull Optional<String> getGroupingClause (@NonNull final GraphCollection collection) {
    if (collection.isGrouped()) {
      @NonNull final Iterator<@NonNull Group> groups = collection.getGroups().iterator();

      while (groups.hasNext()) {
        @NonNull final Group group = groups.next();

        _builder.append(_jpaExpressionTranspiler.transpile(group.getExpression()));

        if (groups.hasNext()) {
          _builder.append(", ");
        }
      }

      @NonNull final String result = _builder.toString();
      _builder.setLength(0);

      return Optional.of(result);
    }

    return Optional.empty();
  }

  /**
   * Build and return a valid JPA select clause for the given collection if any.
   *
   * @param collection A collection from which extracting the select clause.
   *
   * @return A valid JPA select clause for the given collection if any.
   */
  public @NonNull String getSelectClause (@NonNull final GraphCollection collection) {
    @NonNull final Iterator<@NonNull Select> selections = collection.getSelections().iterator();

    while (selections.hasNext()) {
      @NonNull final Select select = selections.next();

      _builder.append(_jpaExpressionTranspiler.transpile(select.getExpression()));
      _builder.append(" AS ");
      _builder.append(select.getName());

      if (selections.hasNext()) {
        _builder.append(", ");
      }
    }

    @NonNull final String result = _builder.toString();
    _builder.setLength(0);

    return result;
  }

  /**
   * Return a complete JPA query for the given collection.
   *
   * @param collection A collection from which building a new JPA Query.
   *
   * @return A complete JPA query for the given collection.
   */
  public @NonNull String getQuery (@NonNull final GraphCollection collection) {
    @NonNull final Optional<String> filteringClause = getWhereClause(collection);
    @NonNull final Optional<String> orderingClause  = getOrderingClause(collection);
    @NonNull final String           selectClause    = getSelectClause(collection);
    @NonNull final String           fromClause      = getFromClause(collection);
    @NonNull final Optional<String> groupingClause  = getGroupingClause(collection);

    _builder.append("SELECT ");
    _builder.append(selectClause);
    _builder.append(" FROM ");
    _builder.append(fromClause);

    if (filteringClause.isPresent()) {
      _builder.append(" WHERE ");
      _builder.append(filteringClause.get());
    }

    if (orderingClause.isPresent()) {
      _builder.append(" ORDER BY ");
      _builder.append(orderingClause.get());
    }

    if (groupingClause.isPresent()) {
      _builder.append(" GROUP BY ");
      _builder.append(groupingClause.get());
    }

    @NonNull final String query = _builder.toString();
    _builder.setLength(0);

    return query;
  }
}
