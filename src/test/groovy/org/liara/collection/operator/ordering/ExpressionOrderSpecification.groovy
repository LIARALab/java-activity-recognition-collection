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

package org.liara.collection.operator.ordering

import org.liara.collection.Collection
import org.mockito.Mockito
import spock.lang.Specification

import javax.persistence.metamodel.Attribute

class ExpressionOrderSpecification
  extends Specification
{
  def "it instantiate an ascending ordering operator for a given field by default" () {
    when: "we create an ordering operator for a given expression"
    final ExpressionOrder order = new ExpressionOrder(":this.expression")

    then: "we expect to create an ascending operator for the given expression"
      order.direction == OrderingDirection.ASCENDING
    order.expression == ":this.expression"
  }

  def "it is instantiable from an attribute" () {
    given: "an attribute"
    final Attribute<?, ?> attribute = Mockito.mock(Attribute.class)
    Mockito.when(attribute.getName()).thenReturn('expression')

    when: "we create an ordering operator for a given expression"
    final ExpressionOrder order = new ExpressionOrder(attribute)
    final ExpressionOrder builtOrder = ExpressionOrder.expression(attribute)

    then: "we expect to create an ascending operator for the given expression"
    order.direction == OrderingDirection.ASCENDING
    order.expression == ":this.expression"
    builtOrder.direction == OrderingDirection.ASCENDING
    builtOrder.expression == ":this.expression"
  }

  def "it allow to instantiate a fully configured ordering operator for a given field" () {
    when: "we create an ordering operator with a given direction for a given expression"
    final ExpressionOrder order = new ExpressionOrder(":this.expression", OrderingDirection.DESCENDING)

    then: "we expect to get an ordering operator for the given expression with the given direction"
      order.direction == OrderingDirection.DESCENDING
    order.expression == ":this.expression"
  }

  def "it allow to instantiate a copy of another ordering operator" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(":this.expression", OrderingDirection.DESCENDING)

    when: "we instantiate a copy of a given ordering operator"
    final ExpressionOrder copy = new ExpressionOrder(source)

    then: "we expect to get a copy of the source operator"
      copy == source
      !copy.is(source)
  }

  def "it allow to instantiate an ordering operator from a JPA attribute" () {
    given: "a JPA attribute"
      final Attribute<?, ?> attribute = Mockito.mock(Attribute.class)
    Mockito.when(attribute.getName()).thenReturn("expression")

    when: "we create an ordering operator from the given attribute"
    final ExpressionOrder order = new ExpressionOrder(attribute, OrderingDirection.DESCENDING)

    then: "we expect to get an operator that order the given attribute name"
    order.expression == "expression"
      order.direction == OrderingDirection.DESCENDING
  }

  def "it allow to create a copy of an ordering operator that order another field" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(":this.expression", OrderingDirection.DESCENDING)

    when: "we update the ordered expression of this ordering operator"
    final ExpressionOrder copy = source.setExpression(":this.otherField")

    then: "we expect to get a copy of the source operator that order the given expression"
    copy.expression == ":this.otherField"
      copy.direction == OrderingDirection.DESCENDING
    source.expression == ":this.expression"
      source.direction == OrderingDirection.DESCENDING
      !copy.is(source)
  }

  def "it allow to create a copy of an ordering operator that order another field by using a JPA attribute" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(":this.expression", OrderingDirection.DESCENDING)

    and: "a JPA attribute"
    final Attribute<?, ?> attribute = Mockito.mock(Attribute.class)
    Mockito.when(attribute.getName()).thenReturn("otherField")

    when: "we update the ordered expression of this ordering operator"
    final ExpressionOrder copy = source.setField(attribute)

    then: "we expect to get a copy of the source operator that order the given expression"
    copy.expression == "otherField"
    copy.direction == OrderingDirection.DESCENDING
    source.expression == ":this.expression"
    source.direction == OrderingDirection.DESCENDING
    !copy.is(source)
  }

  def "it allow to create a copy of an ordering operator with a different ordering direction" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(":this.expression", OrderingDirection.DESCENDING)

    when: "we update the ordered expression of this ordering operator"
    final ExpressionOrder copy = source.setDirection(OrderingDirection.ASCENDING)

    then: "we expect to get a copy of the source operator that order the given expression"
    copy.expression == ":this.expression"
    copy.direction == OrderingDirection.ASCENDING
    source.expression == ":this.expression"
    source.direction == OrderingDirection.DESCENDING
    !copy.is(source)
  }

  def "it allow to get a copy with a specific ordering by calling ascending" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(":this.expression", OrderingDirection.DESCENDING)

    when: "we update the ordered expression of this ordering operator"
    final ExpressionOrder copy = source.ascending()

    then: "we expect to get a copy of the source operator that order the given expression"
    copy.expression == ":this.expression"
    copy.direction == OrderingDirection.ASCENDING
    source.expression == ":this.expression"
    source.direction == OrderingDirection.DESCENDING
    !copy.is(source)
  }

  def "it allow to get a copy with a specific ordering by calling descending" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(":this.expression", OrderingDirection.ASCENDING)

    when: "we update the ordered expression of this ordering operator"
    final ExpressionOrder copy = source.descending()

    then: "we expect to get a copy of the source operator that order the given expression"
    copy.expression == ":this.expression"
    copy.direction == OrderingDirection.DESCENDING
    source.expression == ":this.expression"
    source.direction == OrderingDirection.ASCENDING
    !copy.is(source)
  }

  def "it does nothing when it is applied to a unorderable collection" () {
    given: "an order"
    final ExpressionOrder order = new ExpressionOrder(":this.expression", OrderingDirection.ASCENDING)

    and: "an unorderable collection"
    final Collection collection = Mockito.mock(Collection.class)

    when: "we apply the order operator to the unorderable collection"
    order.apply(collection)

    then: "we expect that the operators does nothing to the collection"
    Mockito.verifyZeroInteractions(collection)
  }

  def "it update an orderable collection when it is applied to a orderable collection" () {
    given: "an order"
    final ExpressionOrder order = new ExpressionOrder(":this.expression", OrderingDirection.ASCENDING)

    and: "an orderable collection"
    final OrderableCollection collection = Mockito.mock(OrderableCollection.class)
    final OrderableCollection resultCollection = Mockito.mock(OrderableCollection.class)
    Mockito.when(collection.orderBy(order)).thenReturn(resultCollection)

    when: "we apply the order operator to the orderable collection"
    final OrderableCollection result = order.apply(collection)

    then: "we expect that the operator has updated the orderable collection"
    result == resultCollection
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    ExpressionOrder.expression(':this.other') != null
    final ExpressionOrder instance = ExpressionOrder.expression(':this.other')
    instance == instance
    ExpressionOrder.expression(':this.first') == ExpressionOrder.expression(':this.first')
    ExpressionOrder.expression(':this.first') != ExpressionOrder.expression(':this.other')
    ExpressionOrder.expression(':this.first').descending() != ExpressionOrder.expression(':this.first').ascending()
    ExpressionOrder.expression(':this.first') != new Object()
  }

  def 'it define a custom hashcode method' () {
    expect: 'hashcode operator to behave accordingly with the standards'
    ExpressionOrder.expression(':this.other').hashCode() == ExpressionOrder.expression(':this.other').hashCode()
    ExpressionOrder.expression(':this.other').hashCode() != ExpressionOrder.expression(':this.first').hashCode()
    ExpressionOrder.expression(':this.other').descending().hashCode() == ExpressionOrder.expression(
      ':this.other'
    ).descending().hashCode()
    ExpressionOrder.expression(':this.other').ascending().hashCode() != ExpressionOrder.expression(
      ':this.other'
    ).descending().hashCode()
  }
}
