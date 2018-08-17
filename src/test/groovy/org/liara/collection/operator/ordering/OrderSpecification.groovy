package org.liara.collection.operator.ordering

import org.liara.collection.Collection
import org.mockito.Mockito
import spock.lang.Specification

import javax.persistence.metamodel.Attribute

class OrderSpecification extends Specification {
  def "it instantiate an ascending ordering operator for a given field by default" () {
    when: "we create an ordering operator for a given field"
      final Order order = new Order(":this.field")

    then: "we expect to create an ascending operator for the given field"
      order.direction == OrderingDirection.ASCENDING
      order.field == ":this.field"
  }

  def "it allow to instantiate a fully configured ordering operator for a given field" () {
    when: "we create an ordering operator with a given direction for a given field"
      final Order order = new Order(":this.field", OrderingDirection.DESCENDING)

    then: "we expect to get an ordering operator for the given field with the given direction"
      order.direction == OrderingDirection.DESCENDING
      order.field == ":this.field"
  }

  def "it allow to instantiate a copy of another ordering operator" () {
    given: "an ordering operator"
      final Order source = new Order(":this.field", OrderingDirection.DESCENDING)

    when: "we instantiate a copy of a given ordering operator"
      final Order copy = new Order(source)

    then: "we expect to get a copy of the source operator"
      copy == source
      !copy.is(source)
  }

  def "it allow to instantiate an ordering operator from a JPA attribute" () {
    given: "a JPA attribute"
      final Attribute<?, ?> attribute = Mockito.mock(Attribute.class)
      Mockito.when(attribute.getName()).thenReturn("field")

    when: "we create an ordering operator from the given attribute"
      final Order order = new Order(attribute, OrderingDirection.DESCENDING)

    then: "we expect to get an operator that order the given attribute name"
      order.field == "field"
      order.direction == OrderingDirection.DESCENDING
  }

  def "it allow to create a copy of an ordering operator that order another field" () {
    given: "an ordering operator"
      final Order source = new Order(":this.field", OrderingDirection.DESCENDING)

    when: "we update the ordered field of this ordering operator"
      final Order copy = source.setField(":this.otherField")

    then: "we expect to get a copy of the source operator that order the given field"
      copy.field == ":this.otherField"
      copy.direction == OrderingDirection.DESCENDING
      source.field == ":this.field"
      source.direction == OrderingDirection.DESCENDING
      !copy.is(source)
  }

  def "it allow to create a copy of an ordering operator that order another field by using a JPA attribute" () {
    given: "an ordering operator"
    final Order source = new Order(":this.field", OrderingDirection.DESCENDING)

    and: "a JPA attribute"
    final Attribute<?, ?> attribute = Mockito.mock(Attribute.class)
    Mockito.when(attribute.getName()).thenReturn("otherField")

    when: "we update the ordered field of this ordering operator"
    final Order copy = source.setField(attribute)

    then: "we expect to get a copy of the source operator that order the given field"
    copy.field == "otherField"
    copy.direction == OrderingDirection.DESCENDING
    source.field == ":this.field"
    source.direction == OrderingDirection.DESCENDING
    !copy.is(source)
  }

  def "it allow to create a copy of an ordering operator with a different ordering direction" () {
    given: "an ordering operator"
    final Order source = new Order(":this.field", OrderingDirection.DESCENDING)

    when: "we update the ordered field of this ordering operator"
    final Order copy = source.setDirection(OrderingDirection.ASCENDING)

    then: "we expect to get a copy of the source operator that order the given field"
    copy.field == ":this.field"
    copy.direction == OrderingDirection.ASCENDING
    source.field == ":this.field"
    source.direction == OrderingDirection.DESCENDING
    !copy.is(source)
  }

  def "it allow to get a copy with a specific ordering by calling ascending" () {
    given: "an ordering operator"
    final Order source = new Order(":this.field", OrderingDirection.DESCENDING)

    when: "we update the ordered field of this ordering operator"
    final Order copy = source.ascending()

    then: "we expect to get a copy of the source operator that order the given field"
    copy.field == ":this.field"
    copy.direction == OrderingDirection.ASCENDING
    source.field == ":this.field"
    source.direction == OrderingDirection.DESCENDING
    !copy.is(source)
  }

  def "it allow to get a copy with a specific ordering by calling descending" () {
    given: "an ordering operator"
    final Order source = new Order(":this.field", OrderingDirection.ASCENDING)

    when: "we update the ordered field of this ordering operator"
    final Order copy = source.descending()

    then: "we expect to get a copy of the source operator that order the given field"
    copy.field == ":this.field"
    copy.direction == OrderingDirection.DESCENDING
    source.field == ":this.field"
    source.direction == OrderingDirection.ASCENDING
    !copy.is(source)
  }

  def "it does nothing when it is applied to a unorderable collection" () {
    given: "an order"
    final Order order = new Order(":this.field", OrderingDirection.ASCENDING)

    and: "an unorderable collection"
    final Collection collection = Mockito.mock(Collection.class)

    when: "we apply the order operator to the unorderable collection"
    order.apply(collection)

    then: "we expect that the operators does nothing to the collection"
    Mockito.verifyZeroInteractions(collection)
  }

  def "it update an orderable collection when it is applied to a orderable collection" () {
    given: "an order"
    final Order order = new Order(":this.field", OrderingDirection.ASCENDING)

    and: "an orderable collection"
    final OrderableCollection collection = Mockito.mock(OrderableCollection.class)
    final OrderableCollection resultCollection = Mockito.mock(OrderableCollection.class)
    Mockito.when(collection.orderBy(order)).thenReturn(resultCollection)

    when: "we apply the order operator to the orderable collection"
    final OrderableCollection result = order.apply(collection)

    then: "we expect that the operator has updated the orderable collection"
    result == resultCollection
  }
}
