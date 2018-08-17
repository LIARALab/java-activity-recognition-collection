package org.liara.collection.jpa

import org.checkerframework.checker.nullness.qual.NonNull
import org.liara.collection.operator.Composition
import org.liara.collection.operator.aggregating.AggregableCollection
import org.liara.collection.operator.cursoring.Cursor
import org.liara.collection.operator.filtering.Filter
import org.liara.collection.operator.grouping.GroupableCollection
import org.liara.collection.operator.ordering.Order
import org.mockito.Mockito
import spock.lang.Specification

import javax.persistence.EntityManager

class JPAEntityCollectionSpecification extends Specification {
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
    new JPAEntityCollection(manager, AggregableCollection.class).entityName == "aggregableCollection"
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

  def "it return a null ordering query when the collection is not ordered" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "an unordered collection"
    final JPAEntityCollection<Object> collection = new JPAEntityCollection<>(manager, Object.class)

    when: "we get the ordering query"
    final String query = collection.orderingQuery

    then: "we expect to get null"
    query == null
  }

  def "it return a valid ordering query when the collection is ordered" () {
    given: "an entity manager"
    final EntityManager manager = Mockito.mock(EntityManager.class)

    and: "an ordered collection"
    final JPAEntityCollection<Object> collection = Composition.of(
      Order.field(":this.third").descending(),
      Order.field(":this.second").ascending(),
      Order.field(":this.first").descending()
    ).apply(new JPAEntityCollection<>(manager, Object.class))

    when: "we get the ordering query"
    final String query = collection.orderingQuery

    then: "we expect to get a valid ordering query"
    query == String.join(
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
    int index = 0
    final Iterator<Filter> collectionFilters = collection.filters().iterator()

    while (collectionFilters.hasNext()) {
      final Filter filter = collectionFilters.next()

      for (final Map.Entry<String, Object> entry : filter.parameters) {
        collection.parameters["filter${index}_${entry.key}"] == entry.value
      }

      index += 1
    }

    collection.parameters.size() == 2
  }
}
