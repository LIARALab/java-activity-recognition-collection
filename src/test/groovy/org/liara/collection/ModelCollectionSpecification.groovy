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

import org.liara.collection.operator.Composition
import org.liara.collection.operator.Operator
import org.liara.collection.operator.cursoring.Cursor
import org.liara.collection.operator.filtering.Filter
import org.liara.collection.operator.grouping.ExpressionGroup
import org.liara.collection.operator.grouping.GroupableCollection
import org.liara.collection.operator.ordering.Order

class ModelCollectionSpecification
  extends Specification
{
  def "it can be instantiated from a model class" () {
    when: "we create a collection with a given model class"
    final ModelCollection<Object> collection = new ModelCollection<>(Object.class)

    then: "we expect to get a well-configured collection"
    collection.modelClass == Object.class
    collection.cursor == Cursor.ALL
    !collection.ordered
    !collection.filtered
    !collection.hasJoins()
  }

  def "it can be instantiated as a copy of another collection" () {
    given: "a composition of operators"
    final Operator operators = Composition.of(
      Cursor.ALL,
      Filter.expression(':this.first = :value').setParameter("value", 10),
      Order.expression(':this.second').descending(),
      Filter.expression(':this.second > :value').setParameter('value', 65)
    )

    and: "a source collection"
    final ModelCollection<Object> source = operators.apply(new ModelCollection(Object.class)) as ModelCollection<Object>

    when: "we instantiate a copy of the source collection"
    final ModelCollection<Object> copy = new ModelCollection<>(source)

    then: "we expect to get a valid copy of the source collection"
    copy == source
    !copy.is(source)
  }

  def "it generate a simple readable name for the queried entity" () {
    expect: "to get a simple readable name for each collection's queried entity"
    new ModelCollection(Object.class).entityName == "object"
    new ModelCollection(Number.class).entityName == "number"
    new ModelCollection(GroupableCollection.class).entityName == "groupableCollection"
  }

  def "it can be ordered" () {
    given: "a collection"
    final ModelCollection<Object> collection = new ModelCollection<>(Object.class)

    and: "a chain of ordering operators"
    final Order[] orderings = [
      Order.expression(":this.first").descending(),
      Order.expression(":this.second").ascending(),
      Order.expression(":this.third").descending()
    ]

    when: "we order the collection"
    ModelCollection<Object> orderedCollection = collection

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
    final ModelCollection<Object> collection = new ModelCollection<>(Object.class)

    and: "a cursor"
    final Cursor cursor = Cursor.DEFAULT

    when: "we apply the cursor to the collection"
    final ModelCollection<Object> orderedCollection = collection.setCursor(cursor)

    then: "we expect to get a cursored copy of the original collection"
    orderedCollection.cursor == cursor
    collection.cursor == Cursor.ALL
    !orderedCollection.is(collection)
  }

  def "it can be filtered" () {
    given: "a collection"
    final ModelCollection<Object> collection = new ModelCollection<>(Object.class)

    and: "some filters"
    final Filter[] filters = [
      Filter.expression(":this.first = 5"),
      Filter.expression(":this.second = :name").setParameter("name", "plopl"),
      Filter.expression(":this.third IN :types").setParameter("types", ["banana", "apple"])
    ]

    when: "we add all filters to the collection"
    ModelCollection<Object> result = collection

    for (final Filter filter : filters) {
      result = result.addFilter(filter)
    }

    then: "we expect that the collection was updated accordingly"
    collection.filters == new HashSet<Filter>()
    result.filters == new HashSet<Filter>(Arrays.asList(filters))
    !result.is(collection)
  }

  def "it does not have grouped fields by default" () {
    given: "a collection"
    final ModelCollection<Object> collection = new ModelCollection<>(Object.class)

    expect: "the collection to not have any grouped expression"
    collection.groups.empty
    !collection.grouped
  }

  def "it allows to remove a filter of the collection" () {
    given: "some filters"
    final Filter[] filters = [
      Filter.expression(':this.first = 5'),
      Filter.expression(':this.second LIKE :value').setParameter('value', 'value'),
      Filter.expression(':this.third IN :values').setParameter('values', [1, 2, 3])
    ]

    and: "a collection"
    ModelCollection<Object> collection = new ModelCollection<Object>(Object.class)
    for (final Filter filter : filters) {
      collection = collection.addFilter(filter)
    }

    when: "we remove a filter from the collection"
    final ModelCollection<Object> result = collection.removeFilter(filters[1])

    then: "to get a copy of the collection without the given filter"
    !result.is(collection)

    collection.filters.size() == 3
    collection.filters.containsAll(Arrays.asList(filters))

    result.filters.size() == 2
    result.filters.containsAll([filters[0], filters[2]])
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    new ModelCollection<>(Object.class) != null
    new ModelCollection<>(Object.class) != new Object()
    new ModelCollection<>(Object.class) != new ModelCollection<>(Integer.class)
    new ModelCollection<>(Object.class) == new ModelCollection<>(Object.class)

    final ModelCollection<Object> base = new ModelCollection<>(Object.class)
    final Operator[] operators = [
      Filter.expression(":this.first = 3"),
      Filter.expression(":this.first = :value").setParameter("value", 3),
      Order.expression(":this.first").ascending(),
      Order.expression(":this.first").descending(),
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
    new ModelCollection<>(Object.class).hashCode() != new ModelCollection<>(Integer.class).hashCode()
    new ModelCollection<>(Object.class).hashCode() == new ModelCollection<>(Object.class).hashCode()
    final ModelCollection<Object> base = new ModelCollection<>(Object.class)

    final Operator[] operators = [
      Filter.expression(":this.first = 3"),
      Filter.expression(":this.first = :value").setParameter("value", 3),
      Order.expression(":this.first").ascending(),
      Order.expression(":this.first").descending(),
      Cursor.ALL, Cursor.DEFAULT
    ]

    for (int x = 0; x < operators.size(); ++x) {
      for (int y = 0; y < operators.size(); ++y) {
        (operators[x].apply(base).hashCode() == operators[y].apply(base).hashCode()) == (x == y)
      }
    }
  }

  def "it return a model aggregation when you trying to group the collection" () {
    given: 'an entity collection'
    final ModelCollection<Object> collection = Composition.of(
      Cursor.DEFAULT,
      Filter.expression(":this.first = 5"),
      Order.expression("plopl").ascending()
    ).apply(new ModelCollection<>(Object.class)) as ModelCollection<Object>

    and: 'a group operator'
    final ExpressionGroup group = ExpressionGroup.expression(":this.type")

    when: 'we apply a grouping operator to the collection'
    final Collection result = group.apply(collection)

    then: 'we expect to get a valid grouped collection instance'
    result instanceof ModelAggregation
    (result as ModelAggregation).groupedCollection.is(collection)
    (result as ModelAggregation).groups[0] == group
    (result as ModelAggregation).groups.size() == 1
  }
}
