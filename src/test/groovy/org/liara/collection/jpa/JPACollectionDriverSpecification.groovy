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

package org.liara.collection.jpa

import org.liara.collection.GraphCollection
import org.liara.collection.Specification
import org.liara.collection.operator.filtering.Filter
import org.liara.collection.operator.grouping.Group
import org.liara.collection.operator.ordering.Order
import org.liara.collection.operator.selection.Select
import org.liara.collection.source.GraphSource
import org.liara.collection.source.Source
import org.liara.collection.source.TableSource
import org.liara.data.graph.Graph
import org.liara.data.graph.builder.StaticGraphBuilder
import org.liara.data.primitive.Primitives
import org.liara.expression.ExpressionFactory

class JPACollectionDriverSpecification
  extends Specification
{
  Graph getSomeGraph () {
    final StaticGraphBuilder builder = new StaticGraphBuilder()

    builder.table("users")
           .column("identifier").ofType(Primitives.INTEGER)
           .column("created_at").ofType(Primitives.DATE_TIME)
           .column("name").ofType(Primitives.STRING)
           .column("gender").ofType(Primitives.CHARACTER)
           .endTable()

    builder.table("roles")
           .column("identifier").ofType(Primitives.INTEGER)
           .column("name").ofType(Primitives.STRING)
           .endTable()

    builder.table("users_roles")
           .column("identifier").ofType(Primitives.INTEGER)
           .column("user_identifier").ofType(Primitives.INTEGER)
           .column("role_identifier").ofType(Primitives.INTEGER)
           .endTable()

    return builder.build()
  }

  def "#getOrderingClause it allows to get an ordering clause from a collection" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "a source"
    final GraphSource source = Source.from(graph.getTable("users"))

    and: "an ordered collection"
    final GraphCollection collection = new GraphCollection(source).orderBy(
      Order.expression(source.getOwnPlaceholder("created_at")).ascending()
    ).orderBy(
      Order.expression(source.getOwnPlaceholder("name")).descending()
    )

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getOrderingClause on the given collection"
    final Optional<String> clause = driver.getOrderingClause(collection)

    then: "we expect to get a valid ordering clause"
    clause.present
    clause.get() == "users.created_at ASC, users.name DESC"
  }

  def "#getOrderingClause returns an empty clause when the collection is not ordered" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "a source"
    final GraphSource source = Source.from(graph.getTable("users"))

    and: "an ordered collection"
    final GraphCollection collection = new GraphCollection(source)

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getOrderingClause on the given collection"
    final Optional<String> clause = driver.getOrderingClause(collection)

    then: "we expect to get a valid ordering clause"
    !clause.present
  }

  def "#getFromClause returns a valid from clause for a table source" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "a table source"
    final GraphSource source = Source.from(graph.getTable("users"))

    and: "a collection"
    final GraphCollection collection = new GraphCollection(source)

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getFromClause on the given collection"
    final String clause = driver.getFromClause(collection)

    then: "we expect to get a valid from clause"
    clause == "users"
  }

  def "#getFromClause returns a valid from clause for an aliased table source" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "an aliased table source"
    final GraphSource source = Source.from(graph.getTable("users"), "table")

    and: "a collection"
    final GraphCollection collection = new GraphCollection(source)

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getFromClause on the given collection"
    final String clause = driver.getFromClause(collection)

    then: "we expect to get a valid from clause"
    clause == "users AS table"
  }

  def "#getFromClause returns a valid from clause for a joined source" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "an expression factory"
    final ExpressionFactory factory = new ExpressionFactory()

    and: "a joined source"
    final TableSource users = Source.from(graph.getTable("users"), "table")
    final TableSource usersRoles = Source.from(graph.getTable("users_roles"))
    final GraphSource source = users.innerJoin(
      usersRoles, factory.equal(
      users.getOwnPlaceholder(Primitives.INTEGER, "identifier"),
      usersRoles.getOwnPlaceholder(Primitives.INTEGER, "user_identifier")
    )
    )

    and: "an ordered collection"
    final GraphCollection collection = new GraphCollection(source)

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getFromClause on the given collection"
    final String clause = driver.getFromClause(collection)

    then: "we expect to get a valid from clause"
    clause == "users AS table INNER JOIN users_roles ON table.identifier = users_roles.user_identifier"
  }

  def "#getFromClause returns a valid from clause for a joined source with alias" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "an expression factory"
    final ExpressionFactory factory = new ExpressionFactory()

    and: "a joined source"
    final TableSource users = Source.from(graph.getTable("users"), "table")
    final TableSource usersRoles = Source.from(graph.getTable("users_roles"), "joined")
    final GraphSource source = users.innerJoin(
      usersRoles, factory.equal(
      users.getOwnPlaceholder(Primitives.INTEGER, "identifier"),
      usersRoles.getOwnPlaceholder(Primitives.INTEGER, "user_identifier")
    )
    )

    and: "an ordered collection"
    final GraphCollection collection = new GraphCollection(source)

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getFromClause on the given collection"
    final String clause = driver.getFromClause(collection)

    then: "we expect to get a valid from clause"
    clause == "users AS table INNER JOIN users_roles AS joined ON table.identifier = joined.user_identifier"
  }

  def "#getWhereClause returns a valid where clause for a filtered collection" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "an expression factory"
    final ExpressionFactory factory = new ExpressionFactory()

    and: "a source"
    final TableSource source = Source.from(graph.getTable("users"), "x")

    and: "a filtered collection"
    final GraphCollection collection = new GraphCollection(source).addFilter(
      Filter.expression(
        factory.or(
          factory.equal(
            source.getOwnPlaceholder(Primitives.INTEGER, "identifier"),
            factory.nonnull(5)
          ),
          factory.equal(
            source.getOwnPlaceholder(Primitives.STRING, "name"),
            factory.nonnull("rambo")
          )
        )
      )
    ).addFilter(
      Filter.expression(
        factory.greaterThan(
          source.getOwnPlaceholder(Primitives.INTEGER, "identifier"),
          factory.nonnull(3)
        )
      )
    ).addFilter(
      Filter.expression(
        factory.lessThan(
          source.getOwnPlaceholder(Primitives.INTEGER, "identifier"),
          factory.nonnull(13)
        )
      )
    )

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getWhereClause on the given collection"
    final Optional<String> clause = driver.getWhereClause(collection)

    then: "we expect to get a valid where clause"
    clause.present
    clause.get() == "(x.identifier = 5 OR x.name = \"rambo\") AND x.identifier > 3 AND x.identifier < 13"
  }

  def "#getWhereClause returns an empty where clause for an unfiltered collection" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "a source"
    final TableSource source = Source.from(graph.getTable("users"), "x")

    and: "a collection"
    final GraphCollection collection = new GraphCollection(source)

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getWhereClause on the given collection"
    final Optional<String> clause = driver.getWhereClause(collection)

    then: "we expect to get a valid where clause"
    !clause.present
  }

  def "#getGroupingClause returns an empty grouping clause for an ungrouped collection" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "a source"
    final TableSource source = Source.from(graph.getTable("users"), "x")

    and: "a collection"
    final GraphCollection collection = new GraphCollection(source)

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getGroupingClause on the given collection"
    final Optional<String> clause = driver.getGroupingClause(collection)

    then: "we expect to get a valid grouping clause"
    !clause.present
  }

  def "#getGroupingClause returns a grouping clause for a grouped collection" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "an expression factory"
    final ExpressionFactory factory = new ExpressionFactory()

    and: "a source"
    final TableSource source = Source.from(graph.getTable("users"), "x")

    and: "a collection"
    final GraphCollection collection = new GraphCollection(source).groupBy(
      Group.expression(
        factory.modulus(
          source.getOwnPlaceholder(Primitives.INTEGER, "identifier"),
          factory.nonnull(5)
        )
      )
    ).groupBy(Group.expression(source.getOwnPlaceholder("name")))

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getGroupingClause on the given collection"
    final Optional<String> clause = driver.getGroupingClause(collection)

    then: "we expect to get a valid grouping clause"
    clause.present
    clause.get() == "x.identifier % 5, x.name"
  }

  def "#getSelectClause returns the selection clause of a collection" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "a source"
    final TableSource source = Source.from(graph.getTable("users"), "x")

    and: "a collection"
    final GraphCollection collection = new GraphCollection(source).select(
      Select.expression(source.getOwnPlaceholder(Primitives.INTEGER, "identifier"), "id")
    ).select(
      Select.expression(source.getOwnPlaceholder(Primitives.STRING, "name"))
    )

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getGroupingClause on the given collection"
    final String clause = driver.getSelectClause(collection)

    then: "we expect to get a valid grouping clause"
    clause == "x.identifier AS id, x.name"
  }

  def "#getQuery returns a complete selection query from a collection" () {
    given: "a graph"
    final Graph graph = getSomeGraph()

    and: "an expression factory"
    final ExpressionFactory factory = new ExpressionFactory()

    and: "a source"
    final TableSource source = Source.from(graph.getTable("users"), "x")

    and: "a collection"
    final GraphCollection collection = new GraphCollection(source).select(
      Select.expression(source.getOwnPlaceholder(Primitives.INTEGER, "identifier"), "id")
    ).select(
      Select.expression(source.getOwnPlaceholder(Primitives.STRING, "name"))
    ).orderBy(
      Order.expression(source.getOwnPlaceholder("name"))
    ).addFilter(
      Filter.expression(
        factory.greaterThan(
          source.getOwnPlaceholder(Primitives.INTEGER, "identifier"),
          factory.nonnull(5)
        )
      )
    )

    and: "a driver"
    final JPACollectionDriver driver = new JPACollectionDriver()

    when: "we call #getQuery on the given collection"
    final String query = driver.getQuery(collection)

    then: "we expect to get a valid query"
    query == "SELECT x.identifier AS id, x.name FROM users AS x WHERE x.identifier > 5 ORDER BY x.name ASC"
  }
}
