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

package org.liara.collection

import org.liara.collection.operator.Operator
import org.liara.collection.operator.cursoring.Cursor
import org.liara.collection.operator.filtering.Filter
import org.liara.collection.operator.grouping.Group
import org.liara.collection.operator.ordering.Order
import org.liara.collection.source.GraphSource
import org.liara.expression.Expression
import org.mockito.Mockito

class GraphCollectionSpecification
  extends Specification
{
  def "#GraphCollection instantiate a collection based upon a given source" () {
    given: "a source"
    final GraphSource source = Mockito.mock(GraphSource.class)

    when: "we create a collection based upon a given source"
    final GraphCollection collection = new GraphCollection(source)

    then: "we expect to get a well-configured collection"
    collection.source == source
    collection.cursor == Cursor.ALL
    !collection.ordered
    !collection.filtered
    !collection.grouped
    !collection.hasSelections()
  }

  def "it can be ordered" () {
    given: "a collection"
    final GraphCollection collection = new GraphCollection(Mockito.mock(GraphSource.class))

    and: "a chain of ordering operators"
    final Order[] orderings = [
      Order.expression(Mockito.mock(Expression.class)).descending(),
      Order.expression(Mockito.mock(Expression.class)).ascending(),
      Order.expression(Mockito.mock(Expression.class)).descending()
    ]

    when: "we order the collection"
    GraphCollection orderedCollection = collection

    for (final Order order in orderings) {
      orderedCollection = orderedCollection.orderBy(order)
    }

    then: "we expect to get a fully ordered copy of the original collection"
    orderedCollection.ordered
    orderedCollection.orderings.size() == 3
    orderedCollection.orderings[0] == orderings[0]
    orderedCollection.orderings[1] == orderings[1]
    orderedCollection.orderings[2] == orderings[2]
    !orderedCollection.getOrderings().is(orderings)
    !orderedCollection.is(collection)
    !collection.ordered
  }

  def "it can be cursored" () {
    given: "a collection"
    final GraphCollection collection = new GraphCollection(Mockito.mock(GraphSource.class))

    and: "a cursor"
    final Cursor cursor = Cursor.DEFAULT

    when: "we apply the cursor to the collection"
    final GraphCollection cursoredCollection = collection.setCursor(cursor)

    then: "we expect to get a cursored copy of the original collection"
    cursoredCollection.cursor == cursor
    collection.cursor == Cursor.ALL
    !cursoredCollection.is(collection)
  }

  def "it can be filtered" () {
    given: "a collection"
    final GraphCollection collection = new GraphCollection(Mockito.mock(GraphSource.class))

    and: "some filters"
    final Filter[] filters = [
      Filter.expression(Mockito.mock(Expression.class)),
      Filter.expression(Mockito.mock(Expression.class)),
      Filter.expression(Mockito.mock(Expression.class))
    ]

    when: "we add all filters to the collection"
    GraphCollection filteredCollection = collection

    for (final Filter filter : filters) {
      filteredCollection = filteredCollection.addFilter(filter)
    }

    then: "we expect that the collection was updated accordingly"
    collection.filters == new HashSet<Filter>()
    filteredCollection.filters == new HashSet<Filter>(Arrays.asList(filters))
    !filteredCollection.is(collection)
  }

  def "it allows to remove a filter of the collection" () {
    given: "some filters"
    final Filter[] filters = [
      Filter.expression(Mockito.mock(Expression.class)),
      Filter.expression(Mockito.mock(Expression.class)),
      Filter.expression(Mockito.mock(Expression.class))
    ]

    and: "a collection"
    GraphCollection collection = new GraphCollection(Mockito.mock(GraphSource.class))
    for (final Filter filter : filters) {
      collection = collection.addFilter(filter)
    }

    when: "we remove a filter from the collection"
    final GraphCollection result = collection.removeFilter(filters[1])

    then: "to get a copy of the collection without the given filter"
    !result.is(collection)

    collection.filters.size() == 3
    collection.filters.containsAll(Arrays.asList(filters))

    result.filters.size() == 2
    result.filters.containsAll([filters[0], filters[2]])
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    new GraphCollection(Mockito.mock(GraphSource.class)) != null
    new GraphCollection(Mockito.mock(GraphSource.class)) != new Object()
    new GraphCollection(Mockito.mock(GraphSource.class)) != new GraphCollection(Mockito.mock(GraphSource.class))

    final GraphSource source = Mockito.mock(GraphSource.class)

    new GraphCollection(source) == new GraphCollection(source)

    GraphCollection base = new GraphCollection(source)

    final Operator[] operators = [
      Filter.expression(Mockito.mock(Expression.class)),
      Filter.expression(Mockito.mock(Expression.class)),
      Order.expression(Mockito.mock(Expression.class)).ascending(),
      Order.expression(Mockito.mock(Expression.class)).descending(),
      Cursor.ALL, Cursor.DEFAULT
    ]

    for (int x = 0; x < operators.size(); ++x) {
      for (int y = 0; y < operators.size(); ++y) {
        (operators[x].apply(base) == operators[y].apply(base)) == (x == y)
      }
    }
  }

  def 'it define a custom hashcode method' () {
    expect: 'hashcode operator to behave accordingly with the standards'
    final GraphSource source = Mockito.mock(GraphSource.class)

    new GraphCollection(source).hashCode() != new GraphCollection(Mockito.mock(GraphSource.class)).hashCode()
    new GraphCollection(source).hashCode() == new GraphCollection(source).hashCode()

    GraphCollection base = new GraphCollection(source)

    final Operator[] operators = [
      Filter.expression(Mockito.mock(Expression.class)),
      Filter.expression(Mockito.mock(Expression.class)),
      Order.expression(Mockito.mock(Expression.class)).ascending(),
      Order.expression(Mockito.mock(Expression.class)).descending(),
      Cursor.ALL, Cursor.DEFAULT
    ]

    for (int x = 0; x < operators.size(); ++x) {
      for (int y = 0; y < operators.size(); ++y) {
        (operators[x].apply(base).hashCode() == operators[y].apply(base).hashCode()) == (x == y)
      }
    }
  }

  def "it allows you to group the collection" () {
    given: "a JPA entity collection"
    final GraphCollection collection = new GraphCollection(Mockito.mock(GraphSource.class))

    and: "some groups"
    final Group[] groups = [
      Group.expression(Mockito.mock(Expression.class)),
      Group.expression(Mockito.mock(Expression.class)),
      Group.expression(Mockito.mock(Expression.class))
    ]

    when: "we add all filters to the collection"
    GraphCollection groupedCollection = collection

    for (final Group group : groups) {
      groupedCollection = groupedCollection.groupBy(group)
    }

    then: "we expect that the collection was updated accordingly"
    collection.groups == new ArrayList<Group>(0)
    groupedCollection.groups == Arrays.asList(groups)
    !groupedCollection.is(collection)
  }
}
