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
import org.liara.collection.operator.Composition
import org.liara.collection.operator.Operator
import org.liara.collection.operator.cursoring.Cursor
import org.liara.collection.operator.filtering.Filter
import org.liara.collection.operator.grouping.Group
import org.liara.collection.operator.grouping.GroupableCollection
import org.liara.collection.operator.ordering.Order
import org.mockito.Mockito

import javax.persistence.EntityManager
import javax.persistence.TypedQuery

class JPAEntityCollectionSpecification extends Specification {
  def <Entity> Map<Filter, Map<String, String>> getNamespacedParametersOf (
    final JPAEntityCollection<Entity> collection
  ) {
    final Map<Filter, Map<String, String>> result = [:]
    final Iterator<Filter> collectionFilters = collection.filters.iterator()
    int index = 0

    while (collectionFilters.hasNext()) {
      final Filter filter = collectionFilters.next()

      if (filter.parameters.size() != 0) {
        result[filter] = [:]
      }

      for (final Map.Entry<String, Object> entry : filter.parameters) {
        result[filter].put(entry.key, "filter${index}_${entry.key}")
      }

      index += 1
    }

    return result
  }

  def "it can be instantiated from an entity manager and an entity type" () {
    given: "an entity manager"
      final EntityManager manager = Mockito.mock(EntityManager.class)

    when: "we create a collection from a given entity manager and a given entity type"
      final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    then: "we expect to get a well-configured collection"
      collection.entityManager == manager
    collection.modelClass == Object.class
      collection.cursor == Cursor.ALL
      !collection.ordered
      !collection.filtered
  }

  def "it can be instantiated as a copy of another collection" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a composition of operators"
    final Operator operators = Composition.of(
      Cursor.ALL,
      Filter.expression(':this.first = :value').setParameter("value", 10),
      Order.expression(':this.second').descending(),
      Filter.expression(':this.second > :value').setParameter('value', 65)
    )

    and: "a source collection"
    final JPAEntityCollection<Object> source = operators.apply(
      new JPAEntityCollection(manager, Object.class)
    ) as JPAEntityCollection<Object>

    when: "we instantiate a copy of the source collection"
    final JPAEntityCollection<Object> copy = new JPAEntityCollection<>(source)

    then: "we expect to get a valid copy of the source collection"
    copy == source
    !copy.is(source)
  }

  def "it generate a simple readable name for the queried entity" () {
    given:
    final EntityManager manager = Mockito.mock(EntityManager.class)

    expect: "to get a simple readable name for each collection's queried entity"
    new JPAEntityCollection(manager, Object.class).entityName == "object"
    new JPAEntityCollection(manager, Number.class).entityName == "number"
    new JPAEntityCollection(manager, GroupableCollection.class).entityName == "groupableCollection"
  }

  def "it can be ordered" () {
    given: "an entity manager"
      final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a collection"
      final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    and: "a chain of ordering operators"
    final Order[] orderings = [
      Order.expression(":this.first").descending(),
      Order.expression(":this.second").ascending(),
      Order.expression(":this.third").descending()
    ]

    when: "we order the collection"
    JPAEntityCollection<Object> orderedCollection = collection

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

  def "it return an empty ordering clause when the collection is not ordered" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "an unordered collection"
    final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    when: "we get the ordering clause"
    final Optional<CharSequence> orderingClause = collection.orderingClause

    then: "we expect to get null"
    !orderingClause.present
  }

  def "it return a valid ordering clause when the collection is ordered" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "an ordered collection"
    final JPAEntityCollection<Object> collection = Composition.of(
      Order.expression(":this.third").descending(),
      Order.expression(":this.second").ascending(),
      Order.expression(":this.first").descending()
    ).apply(new JPAEntityCollection<>(manager, Object.class)) as JPAEntityCollection<Object>

    when: "we get the ordering clause"
    final Optional<CharSequence> orderingClause = collection.orderingClause

    then: "we expect to get a valid ordering clause"
    orderingClause.present
    orderingClause.get().toString() == String.join(
      "",
      "${collection.entityName}.first DESC, ",
      "${collection.entityName}.second ASC, ",
      "${collection.entityName}.third DESC"
    )
  }

  def "it can be cursored" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a collection"
    final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    and: "a cursor"
    final Cursor cursor = Cursor.DEFAULT

    when: "we apply the cursor to the collection"
    final JPAEntityCollection<Object> orderedCollection = cursor.apply(collection) as JPAEntityCollection<Object>

    then: "we expect to get a cursored copy of the original collection"
    orderedCollection.cursor == cursor
    collection.cursor == Cursor.ALL
    !orderedCollection.is(collection)
  }

  def "it can be filtered" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a collection"
    final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    and: "some filters"
    final Filter[] filters = [
      Filter.expression(":this.first = 5"),
      Filter.expression(":this.second = :name").setParameter("name", "plopl"),
      Filter.expression(":this.third IN :types").setParameter("types", ["banana", "apple"])
    ]

    when: "we add all filters to the collection"
    JPAEntityCollection<Object> result = collection

    for (final Filter filter : filters) {
      result = result.addFilter(filter)
    }

    then: "we expect that the collection was updated accordingly"
    collection.filters == new HashSet<Filter>()
    result.filters == new HashSet<Filter>(Arrays.asList(filters))
    !result.is(collection)
  }

  def "it return an empty parameters map when the collection does not have any filter parameters" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "some filters without parameters"
    final Filter[] filters = [
      Filter.expression(":this.first = 5"),
      Filter.expression(":this.second = 'plopl'"),
      Filter.expression(":this.third IN ('banana', 'apple')")
    ]

    and: "a filtered collection without any parameters"
    JPAEntityCollection<Object> result = new JPAEntityCollection<>(manager, Object.class)

    for (final Filter filter : filters) {
      result = result.addFilter(filter)
    }

    when: "we get the parameters map of the request"
    final Map<String, Object> parameters = result.parameters

    then: "we expect that the collection returns an empty map"
    parameters == [:]
  }

  def "it return a parameter map when the collection does have filters with parameters" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "some filters with some parameters"
    final Filter[] filters = [
      Filter.expression(":this.first = 5"),
      Filter.expression(":this.second = :name").setParameter("name", "plopl"),
      Filter.expression(":this.third IN :types").setParameter("types", ["banana", "apple"])
    ]

    and: "a filtered collection with parameters"
    JPAEntityCollection<Object> result = new JPAEntityCollection<>(manager, Object.class)

    for (final Filter filter : filters) {
      result = result.addFilter(filter)
    }

    when: "we get the parameters map of the request"
    final Map<String, Object> parameters = result.parameters

    then: "we expect that the collection returns a valid parameter map"
    final Map<Filter, Map<String, String>> namespacedParameters = getNamespacedParametersOf(result)

    for (final Filter filter : result.filters) {
      for (final Map.Entry<String, Object> entry : filter.parameters) {
        parameters[namespacedParameters[filter][entry.key]] == entry.value
      }
    }

    parameters.size() == 2
  }

  def "it return an empty filtering clause when the collection does not have filters" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a collection without filters"
    final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    when: "we get the filtering clause of the request"
    final Optional<CharSequence> filteringClause = collection.filteringClause

    then: "we expect that the collection returns an empty filtering clause"
    !filteringClause.present
  }

  def "it return a conjunction of its filters as a filtering clause when the collection does have filters" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "some filters with some parameters"
    final Filter[] filters = [
      Filter.expression(":this.first = 5"),
      Filter.expression(":this.second = :name").setParameter("name", "plopl"),
      Filter.expression(":this.third IN :types").setParameter("types", ["banana", "apple"])
    ]

    and: "a filtered collection"
    JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    for (final Filter filter : filters) {
      collection = collection.addFilter(filter)
    }

    when: "we get the filtering clause of the request"
    final Optional<CharSequence> filteringClause = collection.filteringClause

    then: "we expect that the collection returns a conjunction of its filters"
    filteringClause.present

    final Map<Filter, Map<String, String>> namespacedParameters = getNamespacedParametersOf(collection)
    final Map<Filter, String> resultsByFilters = [
      (filters[0]): "${collection.entityName}.first = 5",
      (filters[1]): "${collection.entityName}.second = :${namespacedParameters[filters[1]]["name"]}",
      (filters[2]): "${collection.entityName}.third IN :${namespacedParameters[filters[2]]["types"]}"
    ]
    final List<String> result = []

    for (final Filter filter : collection.filters) {
      result.add(resultsByFilters[filter])
    }

    filteringClause.get().toString() == String.join(" AND ", result)
  }

  def "it can return a JPQL from clause" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a collection"
    final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    when: "we get the collection from clause"
    final String from = collection.fromClause

    then: "we expect that the collection return a valid from clause"
    from == "${collection.modelClass.getName()} ${collection.entityName}"
  }

  def "it can return a complete JPQL query" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a list of operators"
    final Operator[] operators = [
      Filter.expression(":this.first = 5"),
      Order.expression("second").descending(),
      Order.expression("first").ascending(),
      Filter.expression(":this.last > :value").setParameter("value", 10),
      Filter.expression(":this.third IN :list").setParameter("list", ["banana", "apple", "pineapple"]),
      Cursor.DEFAULT
    ]

    and: "a collection"
    final JPAEntityCollection<Object> collection = Composition.of(operators).apply(
      new JPAEntityCollection<>(manager, Object.class)
    ) as JPAEntityCollection<Object>

    when: "we get the result query"
    final String query = collection.getQuery("COUNT(:this)")

    then: "we expect to get a valid query"
    query == String.join(
      "",
      "SELECT COUNT(${collection.entityName}) ",
      "FROM ${collection.fromClause} ",
      "WHERE ${collection.filteringClause.get()} ",
      "ORDER BY ${collection.orderingClause.get()}"
    )
  }

  def "it can return a partial JPQL query if the collection is not ordered" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a list of operators"
    final Operator[] operators = [
      Filter.expression(":this.first = 5"),
      Filter.expression(":this.last > :value").setParameter("value", 10),
      Filter.expression(":this.third IN :list").setParameter("list", ["banana", "apple", "pineapple"]),
      Cursor.DEFAULT
    ]

    and: "a collection"
    final JPAEntityCollection<Object> collection = Composition.of(operators).apply(
      new JPAEntityCollection<>(manager, Object.class)
    ) as JPAEntityCollection<Object>

    when: "we get the result query"
    final String query = collection.getQuery("COUNT(:this)")

    then: "we expect to get a valid query"
    query == String.join(
      "",
      "SELECT COUNT(${collection.entityName}) ",
      "FROM ${collection.fromClause} ",
      "WHERE ${collection.filteringClause.get()}"
    )
  }

  def "it can return a partial JPQL query if the collection is not filtered" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a list of operators"
    final Operator[] operators = [
      Order.expression("second").descending(),
      Order.expression("first").ascending(),
      Cursor.DEFAULT
    ]

    and: "a collection"
    final JPAEntityCollection<Object> collection = Composition.of(operators).apply(
      new JPAEntityCollection<>(manager, Object.class)
    ) as JPAEntityCollection<Object>

    when: "we get the result query"
    final String query = collection.getQuery("COUNT(:this)")

    then: "we expect to get a valid query"
    query == String.join(
      "",
      "SELECT COUNT(${collection.entityName}) ",
      "FROM ${collection.fromClause} ",
      "ORDER BY ${collection.orderingClause.get()}"
    )
  }

  def "it can return a partial JPQL query if the collection is nor filtered nor ordered" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a list of operators"
    final Operator[] operators = [
      Cursor.DEFAULT
    ]

    and: "a collection"
    final JPAEntityCollection<Object> collection = Composition.of(operators).apply(
      new JPAEntityCollection<>(manager, Object.class)
    ) as JPAEntityCollection<Object>

    when: "we get the result query"
    final String query = collection.getQuery("COUNT(:this)")

    then: "we expect to get a valid query"
    query == String.join(
      "",
      "SELECT COUNT(${collection.entityName}) ",
      "FROM ${collection.fromClause}"
    )
  }

  def "it does not have grouped fields by default" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a collection"
    final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    expect: "the collection to not have any grouped expression"
    collection.groups.empty
    !collection.grouped
  }

  def "it can create a typed query and return it" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)
    Mockito.when(manager.createQuery(Mockito.anyString(), Mockito.eq(Object.class)))
           .thenReturn(Mockito.mock(TypedQuery.class))

    and: "a collection"
    final JPAEntityCollection<Object> collection = Composition.of(
      Filter.expression(":this.first = 5"),
      Order.expression("second").descending(),
      Order.expression("first").ascending(),
      Filter.expression(":this.last > :value").setParameter("value", 10),
      Filter.expression(":this.third IN :list").setParameter("list", ["banana", "apple", "pineapple"]),
      Cursor.NONE.setOffset(10).setLimit(20)
    ).apply(new JPAEntityCollection<Object>(manager, Object.class)) as JPAEntityCollection<Object>

    when: "we get a selection query from the collection"
    final TypedQuery<Object> query = collection.select(":this", Object.class)

    then: "we expect to get a valid query"
    isTrue Mockito.verify(manager, Mockito.times(1)).createQuery(
      collection.getQuery(":this").toString(),
      Object.class
    )

    for (final Map.Entry<String, Object> parameter : collection.parameters.entrySet()) {
      isTrue Mockito.verify(query, Mockito.times(1)).setParameter(parameter.key, parameter.value)
    }

    isTrue Mockito.verify(query, Mockito.times(1)).setFirstResult(collection.cursor.offset)
    isTrue Mockito.verify(query, Mockito.times(1)).setMaxResults(collection.cursor.limit)

    isTrue Mockito.verifyNoMoreInteractions(query)
  }

  def "it can find the size of the collection and return it" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a collection"
    final JPAEntityCollection<Object> collection = Mockito.spy(
      Composition.of(
        Filter.expression(":this.first = 5"),
        Order.expression("second").descending(),
        Cursor.NONE.setOffset(10).setLimit(20)
      ).apply(
        new JPAEntityCollection<Object>(manager, Object.class)
      ) as JPAEntityCollection<Object>
    )

    final TypedQuery<Integer> query = Mockito.mock(TypedQuery.class)
    Mockito.when(query.getSingleResult()).thenReturn(13L)
    Mockito.doReturn(query).when(collection).select("COUNT(:this)", Long.class)

    when: "we get the size of the collection"
    final long size = collection.count()

    then: "we expect to get the size of the collection"
    isTrue Mockito.verify(collection, Mockito.times(1)).select(
      "COUNT(:this)", Long.class
    )

    size == 13
  }

  def "it can find all elements of the collection and return them" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a collection"
    final JPAEntityCollection<Object> collection = Mockito.spy(
      Composition.of(
        Filter.expression(":this.first = 5"),
        Order.expression("second").descending(),
        Cursor.NONE.setOffset(10).setLimit(20)
      ).apply(
        new JPAEntityCollection<Object>(manager, Object.class)
      ) as JPAEntityCollection<Object>
    )

    final TypedQuery<Object> query = Mockito.mock(TypedQuery.class)
    final List<Object> queryResult = Arrays.asList(
      new Object(),
      new Object(),
      new Object()
    )
    Mockito.when(query.getResultList()).thenReturn(queryResult)
    Mockito.doReturn(query).when(collection).select(":this", Object.class)

    when: "we get the content of the collection"
    final List<Object> content = collection.fetch()

    then: "we expect to get the content of the collection"
    isTrue Mockito.verify(collection, Mockito.times(1)).select(
      ":this", Object.class
    )

    content == queryResult
  }

  def "it allows to remove a filter of the collection" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "some filters"
    final Filter[] filters = [
      Filter.expression(':this.first = 5'),
      Filter.expression(':this.second LIKE :value').setParameter('value', 'value'),
      Filter.expression(':this.third IN :values').setParameter('values', [1, 2, 3])
    ]

    and: "a collection"
    JPAEntityCollection<Object> collection = new JPAEntityCollection<Object>(manager, Object.class)
    for (final Filter filter : filters) {
      collection = collection.addFilter(filter)
    }

    when: "we remove a filter from the collection"
    final JPAEntityCollection<Object> result = collection.removeFilter(filters[1])

    then: "to get a copy of the collection without the given filter"
    !result.is(collection)

    collection.filters.size() == 3
    collection.filters.containsAll(Arrays.asList(filters))

    result.filters.size() == 2
    result.filters.containsAll([filters[0], filters[2]])
  }

  def 'it define a custom equals method' () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    expect: 'equal operator to behave accordingly with the standards'
    new JPAEntityCollection<>(manager, Object.class) != null
    new JPAEntityCollection<>(manager, Object.class) != new Object()
    new JPAEntityCollection<>(manager, Object.class) != new JPAEntityCollection<>(manager, Integer.class)
    new JPAEntityCollection<>(manager, Object.class) == new JPAEntityCollection<>(manager, Object.class)

    final JPAEntityCollection<Object> base = new JPAEntityCollection<>(manager, Object.class)
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
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    expect: 'hashcode operator to behave accordingly with the standards'
    new JPAEntityCollection<>(manager, Object.class).hashCode() != new JPAEntityCollection<>(
      manager, Integer.class
    ).hashCode()

    new JPAEntityCollection<>(manager, Object.class).hashCode() == new JPAEntityCollection<>(
      manager, Object.class
    ).hashCode()

    final JPAEntityCollection<Object> base = new JPAEntityCollection<>(manager, Object.class)
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

  def "it return a grouped JPA entity collection when you trying to group the collection" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: 'an entity collection'
    final JPAEntityCollection<Object> collection = Composition.of(
      Cursor.DEFAULT,
      Filter.expression(":this.first = 5"),
      Order.expression("plopl").ascending()
    ).apply(new JPAEntityCollection<>(manager, Object.class)) as JPAEntityCollection<Object>

    and: 'a group operator'
    final Group group = Group.expression(":this.type")

    when: 'we apply a grouping operator to the collection'
    final org.liara.collection.Collection result = group.apply(collection)

    then: 'we expect to get a valid grouped collection instance'
    result instanceof GroupedJPAEntityCollection
    (result as GroupedJPAEntityCollection).groupedCollection.is(collection)
    (result as GroupedJPAEntityCollection).groups[0] == group
    (result as GroupedJPAEntityCollection).groups.size() == 1
  }
}
