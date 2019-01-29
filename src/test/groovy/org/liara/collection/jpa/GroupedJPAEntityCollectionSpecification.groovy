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


import org.liara.collection.Specification
import org.liara.collection.operator.cursoring.Cursor
import org.liara.collection.operator.filtering.Filter
import org.liara.collection.operator.grouping.Group
import org.liara.collection.operator.ordering.Order
import org.mockito.Mockito

import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery

class GroupedJPAEntityCollectionSpecification extends Specification {
  def "it can be instantiated from a JPA entity collection and an iterable of groups" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)

    and: "a list of group operators"
    final Group[] groups = [
      Group.expression(":this.first"),
      Group.expression(":this.second"),
      Group.expression(":this.last")
    ]

    when: "we instantiate a grouped JPA entity collection from the given collection and the given list of groups"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, Arrays.asList(groups)
    )

    then: "we expect to get a valid grouped JPA entity collection instance"
    groupedCollection.groupedCollection.is(collection)
    groupedCollection.groups == Arrays.asList(groups)
  }

  def "it can be instantiated as a copy of another collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)

    and: "a list of group operators"
    final Group[] groups = [
      Group.expression(":this.first"),
      Group.expression(":this.second"),
      Group.expression(":this.last")
    ]

    and: "a source grouped JPA entity collection"
    final GroupedJPAEntityCollection<Object> sourceCollection = new GroupedJPAEntityCollection<>(
      collection, Arrays.asList(groups)
    )

    when: "we create a copy of the source collection"
    final GroupedJPAEntityCollection<Object> result = new GroupedJPAEntityCollection<>(sourceCollection)

    then: "we expect to get a valid copy of the source collection"
    !result.is(sourceCollection)
    result == sourceCollection
  }

  def "it allows to get the from clause from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getFromClause()).thenReturn("FROM CLAUSE")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get the from clause from the grouped collection"
    final String fromClause = groupedCollection.fromClause

    then: "we expect to get the from clause from the underlying collection"
    isTrue Mockito.verify(collection).getFromClause()
    fromClause == "FROM CLAUSE"
  }

  def "it allows to get the manager from the underlying JPA entity collection" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getEntityManager()).thenReturn(manager)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get the manager from the grouped collection"
    final EntityManager result = groupedCollection.entityManager

    then: "we expect to get the manager from the underlying collection"
    isTrue Mockito.verify(collection).getEntityManager()
    result == manager
  }

  def "it allows to get the queried entity name from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getEntityName()).thenReturn("ENTITY NAME")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get the queried entity name from the grouped collection"
    final String result = groupedCollection.entityName

    then: "we expect to get the queried entity name from the underlying collection"
    isTrue Mockito.verify(collection).getEntityName()
    result == "ENTITY NAME"
  }

  def "it allows to get the queried entity type from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getModelClass()).thenReturn(Object.class)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get the entity type from the grouped collection"
    final Class<Object> result = groupedCollection.modelClass

    then: "we expect to get the entity type from the underlying collection"
    isTrue Mockito.verify(collection).getModelClass()
    result == Object.class
  }

  def "it allows to get the ordering clause from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getOrderingClause()).thenReturn(Optional.of("ORDERING CLAUSE"))

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get the ordering clause from the grouped collection"
    final Optional<CharSequence> result = groupedCollection.orderingClause

    then: "we expect to get the ordering clause from the underlying collection"
    isTrue Mockito.verify(collection).getOrderingClause()
    result.isPresent()
    result.get() == "ORDERING CLAUSE"
  }

  def "it allows to get the filtering clause from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getFilteringClause()).thenReturn(Optional.of("FILTERING CLAUSE"))

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get the filtering clause from the grouped collection"
    final Optional<CharSequence> result = groupedCollection.filteringClause

    then: "we expect to get the filtering clause from the underlying collection"
    isTrue Mockito.verify(collection).getFilteringClause()
    result.isPresent()
    result.get() == "FILTERING CLAUSE"
  }

  def "it allows to get the count of groups of the collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression(":this.first"), Group.expression(":this.second")]
    )

    when: "we get the count of groups from the grouped collection"
    final int result = groupedCollection.groups.size()

    then: "we expect to get a valid count of group"
    result == 2
  }

  def "it allows to get the cursor from the underlying JPA entity collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getCursor()).thenReturn(Cursor.DEFAULT)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get the cursor from the grouped collection"
    final Cursor result = groupedCollection.cursor

    then: "we expect to get the cursor from the underlying collection"
    isTrue Mockito.verify(collection).getCursor()
    result == Cursor.DEFAULT
  }

  def "it allows to get a pre-configured aggregation query" () {
    given: "a JPA entity collection"
    final EntityManager manager = Mockito.mock(EntityManager.class)
    Mockito.when(manager.createQuery(Mockito.anyString())).thenReturn(Mockito.mock(Query.class))

    and: "a map of parameters"
    final Map<String, Object> parameters = [
      "first": "amadeus",
      "second": 6,
      "object": new Object()
    ]

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = Mockito.mock(GroupedJPAEntityCollection.class)

    Mockito.when(groupedCollection.aggregate(Mockito.anyString())).thenCallRealMethod()
    Mockito.when(groupedCollection.getQuery(Mockito.anyString())).thenReturn("QUERY")
    Mockito.when(groupedCollection.getParameters()).thenReturn(parameters)
    Mockito.when(groupedCollection.getEntityManager()).thenReturn(manager)

    when: "we build an aggregation query from the grouped collection"
    final Query query = groupedCollection.aggregate("COUNT(:this)")

    then: "we expect to get a valid query"
    isTrue Mockito.verify(groupedCollection).getQuery("COUNT(:this)")
    isTrue Mockito.verify(manager).createQuery("QUERY")

    for (final Map.Entry<String, Object> entry : parameters) {
      isTrue Mockito.verify(query).setParameter(entry.key, entry.value)
    }
  }

  def "it allows to get a pre-configured typed aggregation query" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)
    Mockito.when(manager.createQuery(
      Mockito.anyString(), Mockito.any(Class.class))
    ).thenReturn(Mockito.mock(TypedQuery.class))

    and: "a map of parameters"
    final Map<String, Object> parameters = [
      "first": "amadeus",
      "second": 6,
      "object": new Object()
    ]

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = Mockito.mock(GroupedJPAEntityCollection.class)

    Mockito.when(groupedCollection.aggregate(Mockito.anyString(), Mockito.any(Class.class))).thenCallRealMethod()
    Mockito.when(groupedCollection.getQuery(Mockito.anyString())).thenReturn("QUERY")
    Mockito.when(groupedCollection.getParameters()).thenReturn(parameters)
    Mockito.when(groupedCollection.getEntityManager()).thenReturn(manager)

    when: "we build an aggregation query from the grouped collection"
    final TypedQuery<List> query = groupedCollection.aggregate("COUNT(:this)", List.class)

    then: "we expect to get a valid query"
    isTrue Mockito.verify(groupedCollection).getQuery("COUNT(:this)")
    isTrue Mockito.verify(manager).createQuery("QUERY", List.class)

    for (final Map.Entry<String, Object> entry : parameters) {
      isTrue Mockito.verify(query).setParameter(entry.key, entry.value)
    }
  }

  def "it allows to get the grouping clause of the query if the query is grouped" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getEntityName()).thenReturn("object")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [
        Group.expression(":this.first"),
        Group.expression(":this.second"),
        Group.expression("third")
      ]
    )

    when: "we get the grouping clause of the collection"
    final Optional<CharSequence> clause = groupedCollection.groupingClause

    then: "we expect to get a valid grouping clause"
    clause.isPresent()
    clause.get().toString() == "object.first, object.second, third"
  }

  def "it returns an empty grouping clause if the collections isn't grouped" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getEntityName()).thenReturn("object")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [ ]
    )

    when: "we get the grouping clause of the collection"
    final Optional<CharSequence> clause = groupedCollection.groupingClause

    then: "we expect to get a valid grouping clause"
    !clause.isPresent()
  }

  def "it allow to get a query with a group by clause if the collection is grouped" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getQuery(Mockito.anyString())).thenReturn("SUB QUERY")
    Mockito.when(collection.getEntityName()).thenReturn("object")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [
        Group.expression(":this.first"),
        Group.expression(":this.second") ,
        Group.expression(":this.third")
      ]
    )

    expect: "to get a valid query from the collection"
    groupedCollection.getQuery("COUNT(:this)").toString() == String.join(
      "", "SUB QUERY GROUP BY ", groupedCollection.groupingClause.get()
    )

    isTrue Mockito.verify(collection).getQuery("COUNT(:this)")
  }

  def "it replace :groups by a list of groups in selection clause" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getQuery(Mockito.anyString())).thenReturn("SUB QUERY")
    Mockito.when(collection.getEntityName()).thenReturn("object")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [
      Group.expression(":this.first"),
      Group.expression(":this.second") ,
      Group.expression(":this.third")
    ]
    )

    expect: "to replace :groups by a list of groups when you use it in a selection clause"
    groupedCollection.getQuery("new Test(:groups, COUNT(:this))")
    isTrue Mockito.verify(collection).getQuery(
      "new Test(:this.first, :this.second, :this.third, COUNT(:this))"
    )
  }

  def "it replace :groups[\\d+] by the requested group in selection clause" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getQuery(Mockito.anyString())).thenReturn("SUB QUERY")
    Mockito.when(collection.getEntityName()).thenReturn("object")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [
      Group.expression(":this.first"),
      Group.expression(":this.second") ,
      Group.expression(":this.third")
    ]
    )

    expect: "to replace :groups[\\d] by the selected group when you use it in a selection clause"
    groupedCollection.getQuery("new Test(:groups[0], COUNT(:this), :groups[2])")
    isTrue Mockito.verify(collection).getQuery(
      "new Test(${groupedCollection.groups[0].expression}, COUNT(:this), ${groupedCollection.groups[2].expression})"
    )
  }

  def "it returns the underlying collection query if this collection is not grouped" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getQuery(Mockito.anyString())).thenReturn("SUB QUERY")
    Mockito.when(collection.getEntityName()).thenReturn("object")

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, []
    )

    expect: "to get a valid query from the collection"
    groupedCollection.getQuery("COUNT(:this)").toString() == "SUB QUERY"
    isTrue Mockito.verify(collection).getQuery("COUNT(:this)")
  }

  def "it allows you to get all parameters from the underlying collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    Mockito.when(collection.getParameters()).thenReturn([
      "first": 5,
      "second": [1, 2, 3]
    ])

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get the count of orders from the grouped collection"
    final Map<String, Object> parameters = groupedCollection.parameters

    then: "we expect to get the count of orders from the underlying collection"
    isTrue Mockito.verify(collection).getParameters()
    parameters == [
      "first": 5,
      "second": [1, 2, 3]
    ]
  }

  def "it allows you to change the grouped collection" () {
    given: "two collections"
    final JPAEntityCollection<Object> first = Mockito.mock(JPAEntityCollection.class)
    final JPAEntityCollection<Object> second = Mockito.mock(JPAEntityCollection.class)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      first, [Group.expression("")]
    )

    when: "we set the grouped collection to the second one"
    final GroupedJPAEntityCollection<Object> result = groupedCollection.setGroupedCollection(second)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == second
  }

  def "it allows you to update the underlying collection's cursor" () {
    given: "two collections"
    final JPAEntityCollection<Object> first = Mockito.mock(JPAEntityCollection.class)
    final JPAEntityCollection<Object> updated = Mockito.mock(JPAEntityCollection.class)

    Mockito.when(first.setCursor(Mockito.any(Cursor.class))).thenReturn(updated)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      first, [Group.expression("")]
    )

    when: "we set the cursor of the grouped collection"
    final Cursor cursor = new Cursor(10, 5)
    final GroupedJPAEntityCollection<Object> result = groupedCollection.setCursor(cursor)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == updated
    isTrue Mockito.verify(first).setCursor(cursor)
  }

  def "it allows you to add a filter to the underlying collection" () {
    given: "two collections"
    final JPAEntityCollection<Object> first = Mockito.mock(JPAEntityCollection.class)
    final JPAEntityCollection<Object> updated = Mockito.mock(JPAEntityCollection.class)

    Mockito.when(first.addFilter(Mockito.any(Filter.class))).thenReturn(updated)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      first, [Group.expression("")]
    )

    when: "we add a filter to the grouped collection"
    final Filter filter = Mockito.mock(Filter.class)
    final GroupedJPAEntityCollection<Object> result = groupedCollection.addFilter(filter)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == updated
    isTrue Mockito.verify(first).addFilter(filter)
  }

  def "it allows you to remove a filter from the underlying collection" () {
    given: "two collections"
    final JPAEntityCollection<Object> first = Mockito.mock(JPAEntityCollection.class)
    final JPAEntityCollection<Object> updated = Mockito.mock(JPAEntityCollection.class)

    Mockito.when(first.removeFilter(Mockito.any(Filter.class))).thenReturn(updated)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      first, [Group.expression("")]
    )

    when: "we remove a filter of the grouped collection"
    final Filter filter = Mockito.mock(Filter.class)
    final GroupedJPAEntityCollection<Object> result = groupedCollection.removeFilter(filter)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == updated
    isTrue Mockito.verify(first).removeFilter(filter)
  }

  def "it allows you to get all filters of the underlying collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    final Set<Filter> filters = [Mockito.mock(Filter.class), Mockito.mock(Filter.class)]
    Mockito.when(collection.getFilters()).thenReturn(filters)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get all filters of the grouped collection"
    final Set<Filter> result = groupedCollection.filters

    then: "we expect to get all filters of the underlying collection"
    isTrue Mockito.verify(collection).getFilters()
    result == filters
  }

  def "it allows you to group the collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression(":this.first")]
    )

    when: "we group the collection"
    final GroupedJPAEntityCollection<Object> result = groupedCollection.groupBy(Group.expression(":this.second"))

    then: "we expect to get an updated version of the collection"
    !result.is(groupedCollection)
    result.groups.size() == 2
    groupedCollection.groups.size() == 1
    groupedCollection.groups == [Group.expression(":this.first")]
    result.groups == [Group.expression(":this.first"), Group.expression(":this.second")]
  }

  def "it allows you to order the underlying query" () {
    given: "two collections"
    final JPAEntityCollection<Object> first = Mockito.mock(JPAEntityCollection.class)
    final JPAEntityCollection<Object> updated = Mockito.mock(JPAEntityCollection.class)

    Mockito.when(first.orderBy(Mockito.any(Order.class))).thenReturn(updated)

    and: "a grouped JPA entity collection that wrap the first entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      first, [Group.expression("")]
    )

    when: "we order the grouped collection"
    final Order order = Mockito.mock(Order.class)
    final GroupedJPAEntityCollection<Object> result = groupedCollection.orderBy(order)

    then: "we expect to get an updated copy of the source grouped collection"
    !result.is(groupedCollection)
    groupedCollection.groups == result.groups
    groupedCollection.groupedCollection == first
    result.groupedCollection == updated
    isTrue Mockito.verify(first).orderBy(order)
  }

  def "it allows you to get all orders from the underlying collection" () {
    given: "a JPA entity collection"
    final JPAEntityCollection<Object> collection = Mockito.mock(JPAEntityCollection.class)
    final List<Order> orders = [
      Mockito.mock(Order.class),
      Mockito.mock(Order.class),
      Mockito.mock(Order.class),
      Mockito.mock(Order.class)
    ]

    Mockito.when(collection.getOrderings()).thenReturn(orders)

    and: "a grouped JPA entity collection that wrap the given entity collection"
    final GroupedJPAEntityCollection<Object> groupedCollection = new GroupedJPAEntityCollection<>(
      collection, [Group.expression("")]
    )

    when: "we get all orders from the grouped collection"
    final List<Order> result = groupedCollection.orderings

    then: "we expect to get all orders from the underlying collection"
    isTrue Mockito.verify(collection).getOrderings()
    result == orders
  }

  def 'it define a custom equals method' () {
    given: "two JPA entities collection"
    final JPAEntityCollection<Object> first = Mockito.mock(JPAEntityCollection.class)
    final JPAEntityCollection<Object> second = Mockito.mock(JPAEntityCollection.class)

    and: "some groups operators"
    final List<Group> groups = [
      Group.expression(":this.first"),
      Group.expression(":this.second"),
      Group.expression(":this.third")
    ]

    expect: 'equals operator to behave accordingly with the standards'
    new GroupedJPAEntityCollection<>(first, groups) != null
    new GroupedJPAEntityCollection<>(first, groups) != new Object()
    new GroupedJPAEntityCollection<>(first, groups) != new GroupedJPAEntityCollection<>(second, groups)
    new GroupedJPAEntityCollection<>(first, groups) == new GroupedJPAEntityCollection<>(first, groups)

    final GroupedJPAEntityCollection<Object> base = new GroupedJPAEntityCollection<>(second, [])

    for (int x = 0; x < groups.size(); ++x) {
      for (int y = 0; y < groups.size(); ++y) {
        (groups[x].apply(base) == groups[y].apply(base)) == (x == y)
      }
    }
  }

  def 'it define a custom hashcode method' () {
    given: "two JPA entities collection"
    final JPAEntityCollection<Object> first = Mockito.mock(JPAEntityCollection.class)
    final JPAEntityCollection<Object> second = Mockito.mock(JPAEntityCollection.class)

    and: "some groups operators"
    final List<Group> groups = [
      Group.expression(":this.first"),
      Group.expression(":this.second"),
      Group.expression(":this.third")
    ]

    expect: 'hashcode operator to behave accordingly with the standards'
    new GroupedJPAEntityCollection<>(
      first, groups
    ).hashCode() != new GroupedJPAEntityCollection<>(second, groups).hashCode()

    new GroupedJPAEntityCollection<>(
      first, groups
    ).hashCode() == new GroupedJPAEntityCollection<>(first, groups).hashCode()

    final GroupedJPAEntityCollection<Object> base = new GroupedJPAEntityCollection<>(second, [])

    for (int x = 0; x < groups.size(); ++x) {
      for (int y = 0; y < groups.size(); ++y) {
        (groups[x].apply(base).hashCode() == groups[y].apply(base).hashCode()) == (x == y)
      }
    }
  }
}
