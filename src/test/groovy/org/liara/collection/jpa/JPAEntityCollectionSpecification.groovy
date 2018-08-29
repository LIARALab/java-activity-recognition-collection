package org.liara.collection.jpa

import org.liara.collection.operator.Composition
import org.liara.collection.operator.Operator
import org.liara.collection.operator.cursoring.Cursor
import org.liara.collection.operator.filtering.Filter
import org.liara.collection.operator.grouping.GroupableCollection
import org.liara.collection.operator.ordering.Order
import org.mockito.Mockito
import spock.lang.Specification

import javax.persistence.EntityManager

class JPAEntityCollectionSpecification extends Specification {
  def <Entity> Map<Filter, Map<String, String>> getNamespacedParametersOf (
    final JPAEntityCollection<Entity> collection
  ) {
    final Map<Filter, Map<String, String>> result = [:]
    final Iterator<Filter> collectionFilters = collection.filters().iterator()
    int index = 0

    while (collectionFilters.hasNext()) {
      final Filter filter = collectionFilters.next()

      if (filter.parameters.size() != 0) {
        result[filter] = [:]
      }

      for (final Map.Entry<String, Object> entry : filter.parameters) {
        result[filter][entry.key] = "filter${index}_${entry.key}"
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
      collection.entityType == Object.class
      collection.cursor == Cursor.ALL
      !collection.ordered
      !collection.filtered
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
        Order.field(":this.third").descending(),
        Order.field(":this.second").ascending(),
        Order.field(":this.first").descending()
      ]

    when: "we order the collection"
      final JPAEntityCollection<Object> orderedCollection = Composition.of(orderings).apply(collection)

    then: "we expect to get a fully ordered copy of the original collection"
      orderedCollection.ordered
      orderedCollection.orderingCount == 3
      orderedCollection.getOrdering(0) == orderings[2]
      orderedCollection.getOrdering(1) == orderings[1]
      orderedCollection.getOrdering(2) == orderings[0]
      !orderedCollection.getOrderings().is(orderings)
      !orderedCollection.is(collection)
      !collection.ordered
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
      Order.field(":this.third").descending(),
      Order.field(":this.second").ascending(),
      Order.field(":this.first").descending()
    ).apply(new JPAEntityCollection<>(manager, Object.class))

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
    final JPAEntityCollection<Object> orderedCollection = cursor.apply(collection)

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
    final JPAEntityCollection<Object> result = Composition.of(filters).apply(collection)

    then: "we expect that the collection was updated accordingly"
    !collection.filtered
    result.filtered
    collection.filterCount == 0
    result.filterCount == 3
    collection.getFilters() == new HashSet<Filter>()
    result.getFilters() == new HashSet<Filter>(Arrays.asList(filters))
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
    final JPAEntityCollection<Object> collection = Composition.of(filters).apply(
      new JPAEntityCollection<>(manager, Object.class)
    )

    when: "we get the parameters map of the request"
    final Map<String, Object> parameters = collection.parameters

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
    final JPAEntityCollection<Object> collection = Composition.of(filters).apply(
      new JPAEntityCollection<>(manager, Object.class)
    )

    when: "we get the parameters map of the request"
    final Map<String, Object> parameters = collection.parameters

    then: "we expect that the collection returns a valid parameter map"
    final Map<Filter, Map<String, String>> namespacedParameters = getNamespacedParametersOf(collection)

    for (final Filter filter : collection.filters()) {
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
    final JPAEntityCollection<Object> collection = Composition.of(filters).apply(
      new JPAEntityCollection<>(manager, Object.class)
    )

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

    for (final Filter filter : collection.filters()) {
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
    from == "${collection.entityType.getName()} ${collection.entityName}"
  }

  def "it can return a complete JPQL query" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "a list of operators"
    final Operator[] operators = [
      Filter.expression(":this.first = 5"),
      Order.field("second").descending(),
      Order.field("first").ascending(),
      Filter.expression(":this.last > :value").setParameter("value", 10),
      Filter.expression(":this.third IN :list").setParameter("list", ["banana", "apple", "pineapple"]),
      Cursor.DEFAULT
    ]

    and: "a collection"
    final JPAEntityCollection<Object> collection = Composition.of(operators).apply(
      new JPAEntityCollection<>(manager, Object.class)
    )

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
    )

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
      Order.field("second").descending(),
      Order.field("first").ascending(),
      Cursor.DEFAULT
    ]

    and: "a collection"
    final JPAEntityCollection<Object> collection = Composition.of(operators).apply(
      new JPAEntityCollection<>(manager, Object.class)
    )

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
    )

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

    expect: "the collection to not have any grouped field"
    collection.groupCount == 0
    collection.groups.empty
  }
}
