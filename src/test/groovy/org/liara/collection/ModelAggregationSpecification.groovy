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

import org.liara.collection.operator.cursoring.Cursor
import org.liara.collection.operator.filtering.Filter
import org.liara.collection.operator.grouping.Group
import org.liara.collection.operator.ordering.Order
import org.mockito.Mockito

class ModelAggregationSpecification
  extends Specification
{
  def "it can be instantiated from a model collection and an iterable of groups" () {
    given: "a JPA entity collection"
    final ModelCollection<Object> collection = Mockito.mock(ModelCollection.class)

    and: "a list of group operators"
    final Group[] groups = [
      Group.expression(":this.first"),
      Group.expression(":this.second"),
      Group.expression(":this.last")
    ]

    when: "we instantiate a grouped JPA entity collection from the given collection and the given list of groups"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = new Groups(groups)
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(collection)

    then: "we expect to get a valid grouped JPA entity collection instance"
    groupedCollection.groupedCollection.is(collection)
    groupedCollection.groups == Arrays.asList(groups)
  }

  def "it can be instantiated as a copy of another collection" () {
    given: "a JPA entity collection"
    final ModelCollection<Object> collection = Mockito.mock(ModelCollection.class)

    and: "a list of group operators"
    final Group[] groups = [
      Group.expression(":this.first"),
      Group.expression(":this.second"),
      Group.expression(":this.last")
    ]

    and: "a source grouped JPA entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = new Groups(groups)
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> sourceCollection = builder.aggregate(collection)

    when: "we create a copy of the source collection"
    final ModelAggregation<Object> result = new ModelAggregation<>(sourceCollection)

    then: "we expect to get a valid copy of the source collection"
    !result.is(sourceCollection)
    result == sourceCollection
  }

  def "it allows to get the queried entity name from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final ModelCollection<Object> collection = Mockito.mock(ModelCollection.class)
    Mockito.when(collection.getEntityName()).thenReturn("ENTITY NAME")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(collection)

    when: "we get the queried entity name from the grouped collection"
    final String result = groupedCollection.entityName

    then: "we expect to get the queried entity name from the underlying collection"
    isTrue Mockito.verify(collection).getEntityName()
    result == "ENTITY NAME"
  }

  def "it allows to get the queried entity type from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final ModelCollection<Object> collection = Mockito.mock(ModelCollection.class)
    Mockito.when(collection.getModelClass()).thenReturn(Object.class)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(collection)

    when: "we get the entity type from the grouped collection"
    final Class<Object> result = groupedCollection.modelClass

    then: "we expect to get the entity type from the underlying collection"
    isTrue Mockito.verify(collection).getModelClass()
    result == Object.class
  }

  def "it allows to get the cursor from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final ModelCollection<Object> collection = Mockito.mock(ModelCollection.class)
    Mockito.when(collection.getCursor()).thenReturn(Cursor.DEFAULT)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(collection)

    when: "we get the cursor from the grouped collection"
    final Cursor result = groupedCollection.cursor

    then: "we expect to get the cursor from the underlying collection"
    isTrue Mockito.verify(collection).getCursor()
    result == Cursor.DEFAULT
  }

  def "it allows you to change the grouped collection" () {
    given: "two collections"
    final ModelCollection<Object> first = Mockito.mock(ModelCollection.class)
    final ModelCollection<Object> second = Mockito.mock(ModelCollection.class)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(first)

    when: "we set the grouped collection to the second one"
    final ModelAggregation<Object> result = groupedCollection.setGroupedCollection(second)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == second
  }

  def "it allows you to update the underlying collection's cursor" () {
    given: "two collections"
    final ModelCollection<Object> first = Mockito.mock(ModelCollection.class)
    final ModelCollection<Object> updated = Mockito.mock(ModelCollection.class)

    Mockito.when(first.setCursor(Mockito.any(Cursor.class))).thenReturn(updated)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(first)

    when: "we set the cursor of the grouped collection"
    final Cursor cursor = new Cursor(10, 5)
    final ModelAggregation<Object> result = groupedCollection.setCursor(cursor)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == updated
    isTrue Mockito.verify(first).setCursor(cursor)
  }

  def "it allows you to add a filter to the underlying collection" () {
    given: "two collections"
    final ModelCollection<Object> first = Mockito.mock(ModelCollection.class)
    final ModelCollection<Object> updated = Mockito.mock(ModelCollection.class)

    Mockito.when(first.addFilter(Mockito.any(Filter.class))).thenReturn(updated)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(first)

    when: "we add a filter to the grouped collection"
    final Filter filter = Mockito.mock(Filter.class)
    final ModelAggregation<Object> result = groupedCollection.addFilter(filter)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == updated
    isTrue Mockito.verify(first).addFilter(filter)
  }

  def "it allows you to remove a filter from the underlying collection" () {
    given: "two collections"
    final ModelCollection<Object> first = Mockito.mock(ModelCollection.class)
    final ModelCollection<Object> updated = Mockito.mock(ModelCollection.class)

    Mockito.when(first.removeFilter(Mockito.any(Filter.class))).thenReturn(updated)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(first)

    when: "we remove a filter of the grouped collection"
    final Filter filter = Mockito.mock(Filter.class)
    final ModelAggregation<Object> result = groupedCollection.removeFilter(filter)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == updated
    isTrue Mockito.verify(first).removeFilter(filter)
  }

  def "it allows you to get all filters of the underlying collection" () {
    given: "a JPA entity collection"
    final ModelCollection<Object> collection = Mockito.mock(ModelCollection.class)
    final Set<Filter> filters = [Mockito.mock(Filter.class), Mockito.mock(Filter.class)]
    Mockito.when(collection.getFilters()).thenReturn(filters)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(collection)

    when: "we get all filters of the grouped collection"
    final Set<Filter> result = groupedCollection.filters

    then: "we expect to get all filters of the underlying collection"
    isTrue Mockito.verify(collection).getFilters()
    result == filters
  }

  def "it allows you to group the collection" () {
    given: "a JPA entity collection"
    final ModelCollection<Object> collection = Mockito.mock(ModelCollection.class)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = new Groups(Group.expression(":this.first"))
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(collection)

    when: "we group the collection"
    final ModelAggregation<Object> result = groupedCollection.groupBy(Group.expression(":this.second"))

    then: "we expect to get an updated version of the collection"
    !result.is(groupedCollection)
    result.groups.size() == 2
    groupedCollection.groups.size() == 1
    groupedCollection.groups == [Group.expression(":this.first")]
    result.groups == [Group.expression(":this.first"), Group.expression(":this.second")]
  }

  def "it allows you to order the underlying query" () {
    given: "two collections"
    final ModelCollection<Object> first = Mockito.mock(ModelCollection.class)
    final ModelCollection<Object> updated = Mockito.mock(ModelCollection.class)

    Mockito.when(first.orderBy(Mockito.any(Order.class))).thenReturn(updated)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(first)

    when: "we order the grouped collection"
    final Order order = Mockito.mock(Order.class)
    final ModelAggregation<Object> result = groupedCollection.orderBy(order)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == updated
    isTrue Mockito.verify(first).orderBy(order)
  }

  def "it allows you to get all orders from the underlying collection" () {
    given: "a JPA entity collection"
    final ModelCollection<Object> collection = Mockito.mock(ModelCollection.class)
    final List<Order> orders = [
      Mockito.mock(Order.class),
      Mockito.mock(Order.class),
      Mockito.mock(Order.class),
      Mockito.mock(Order.class)
    ]

    Mockito.when(collection.getOrderings()).thenReturn(orders)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = Groups.EMPTY
    builder.aggregates = Aggregates.EMPTY
    final ModelAggregation<Object> groupedCollection = builder.aggregate(collection)

    when: "we get all orders from the grouped collection"
    final List<Order> result = groupedCollection.orderings

    then: "we expect to get all orders from the underlying collection"
    isTrue Mockito.verify(collection).getOrderings()
    result == orders
  }

  def 'it define a custom equals method' () {
    given: "two JPA entities collection"
    final ModelCollection<Object> first = Mockito.mock(ModelCollection.class)
    final ModelCollection<Object> second = Mockito.mock(ModelCollection.class)

    and: "some groups operators"
    final List<Group> groups = [
      Group.expression(":this.first"),
      Group.expression(":this.second"),
      Group.expression(":this.third")
    ]
    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = new Groups(groups)
    builder.aggregates = Aggregates.EMPTY

    expect: 'equals operator to behave accordingly with the standards'
    builder.aggregate(first) != null
    builder.aggregate(first) != new Object()
    builder.aggregate(first) != builder.aggregate(second)
    builder.aggregate(first) == builder.aggregate(first)

    builder.setGroups(Groups.EMPTY)
    final ModelAggregation<Object> base = builder.aggregate(second)

    for (int x = 0; x < groups.size(); ++x) {
      for (int y = 0; y < groups.size(); ++y) {
        (groups[x].apply(base) == groups[y].apply(base)) == (x == y)
      }
    }
  }

  def 'it define a custom hashcode method' () {
    given: "two JPA entities collection"
    final ModelCollection<Object> first = Mockito.mock(ModelCollection.class)
    final ModelCollection<Object> second = Mockito.mock(ModelCollection.class)

    and: "some groups operators"
    final List<Group> groups = [
      Group.expression(":this.first"),
      Group.expression(":this.second"),
      Group.expression(":this.third")
    ]

    final ModelAggregationBuilder builder = new ModelAggregationBuilder()
    builder.groups = new Groups(groups)
    builder.aggregates = Aggregates.EMPTY

    expect: 'hashcode operator to behave accordingly with the standards'
    builder.aggregate(first).hashCode() != builder.aggregate(second).hashCode()
    builder.aggregate(first).hashCode() == builder.aggregate(first).hashCode()

    builder.setGroups(Groups.EMPTY)
    final ModelAggregation<Object> base = builder.aggregate(second)

    for (int x = 0; x < groups.size(); ++x) {
      for (int y = 0; y < groups.size(); ++y) {
        (groups[x].apply(base).hashCode() == groups[y].apply(base).hashCode()) == (x == y)
      }
    }
  }
}
