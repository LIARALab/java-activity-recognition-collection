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
import org.liara.expression.Expression;
import org.liara.expression.ExpressionFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class JPACollectionDriver
{
  @NonNull
  private final ExpressionToJPACompiler _expressionToJPACompiler;

  @NonNull
  private final StringBuilder _output;

  @NonNull
  private final ExpressionFactory _expressionFactory;

  public JPACollectionDriver () {
    _expressionToJPACompiler = new ExpressionToJPACompiler();
    _output = new StringBuilder();
    _expressionFactory = new ExpressionFactory();
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
        _expressionToJPACompiler.setExpression(order.getExpression());
        _expressionToJPACompiler.compile(_output);
        _output.append(" ");
        _output.append(order.getDirection() == OrderingDirection.ASCENDING ? "ASC" : "DESC");

        if (index < size - 1) _output.append(", ");
      }

      @NonNull final String result = _output.toString();

      _output.setLength(0);
      _expressionToJPACompiler.setExpression(null);

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
    @NonNull final List<@NonNull Source> sources = new ArrayList<>();

    @Nullable Source source = collection.getSource();

    while (source != null) {
      sources.add(source);
      source = (source instanceof JoinSource) ? ((JoinSource) source).getOrigin() : null;
    }

    for (int index = sources.size(); index > 0; --index) {
      @NonNull final Source toRender = sources.get(index - 1);

      if (index != sources.size()) {
        _output.append(' ');
      }

      if (toRender instanceof TableSource) {
        renderTableSource((TableSource) toRender);
      } else if (toRender instanceof JoinSource) {
        renderJoinSource((JoinSource) toRender);
      } else {
        throw new Error("Unhandled source type " + toRender.getClass().getName() + ".");
      }
    }

    @NonNull final String result = _output.toString();

    _output.setLength(0);

    return result;
  }

  private void renderJoinSource (@NonNull final JoinSource source) {
    switch (source.getType()) {
      case INNER_JOIN: _output.append("INNER JOIN");
        break;
      case CROSS_JOIN: _output.append("CROSS JOIN");
        break;
      case LEFT_OUTER_JOIN: _output.append("LEFT OUTER JOIN");
        break;
      case RIGHT_OUTER_JOIN: _output.append("RIGHT OUTER JOIN");
        break;
    }

    _output.append(' ');
    _output.append(source.getJoined().getTable().getName());

    if (
      System.identityHashCode(source.getName()) != System.identityHashCode(
        source.getJoined().getTable().getName()
      )
    ) {
      _output.append(" AS ");
      _output.append(source.getName());
    }

    _output.append(" ON ");

    _expressionToJPACompiler.setExpression(source.getPredicate());
    _expressionToJPACompiler.compile(_output);
    _expressionToJPACompiler.setExpression(null);

  }

  private void renderTableSource (@NonNull final TableSource source) {
    _output.append(source.getTable().getName());

    if (
      System.identityHashCode(source.getName()) != System.identityHashCode(
        source.getTable().getName()
      )
    ) {
      _output.append(" AS ");
      _output.append(source.getName());
    }
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
      @NonNull final List<@NonNull Expression<Boolean>> filters = (
        collection.getFilters().stream().map(Filter::getExpression).collect(Collectors.toList())
      );

      _expressionToJPACompiler.setExpression(_expressionFactory.and(filters));
      _expressionToJPACompiler.compile(_output);
      _expressionToJPACompiler.setExpression(null);

      @NonNull final String result = _output.toString();
      _output.setLength(0);

      return Optional.of(result);
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

        _expressionToJPACompiler.setExpression(group.getExpression());
        _expressionToJPACompiler.compile(_output);
        _expressionToJPACompiler.setExpression(null);

        if (groups.hasNext()) {
          _output.append(", ");
        }
      }

      @NonNull final String result = _output.toString();
      _output.setLength(0);

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

      _expressionToJPACompiler.setExpression(select.getExpression());
      _expressionToJPACompiler.compile(_output);
      _expressionToJPACompiler.setExpression(null);

      if (select.getName() != null) {
        _output.append(" AS ");
        _output.append(select.getName());
      }

      if (selections.hasNext()) {
        _output.append(", ");
      }
    }

    @NonNull final String result = _output.toString();
    _output.setLength(0);

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

    _output.append("SELECT ");
    _output.append(selectClause);
    _output.append(" FROM ");
    _output.append(fromClause);

    if (filteringClause.isPresent()) {
      _output.append(" WHERE ");
      _output.append(filteringClause.get());
    }

    if (orderingClause.isPresent()) {
      _output.append(" ORDER BY ");
      _output.append(orderingClause.get());
    }

    if (groupingClause.isPresent()) {
      _output.append(" GROUP BY ");
      _output.append(groupingClause.get());
    }

    @NonNull final String query = _output.toString();
    _output.setLength(0);

    return query;
  }
}
