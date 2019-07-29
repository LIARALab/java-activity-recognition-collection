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
import org.liara.expression.Expression
import org.mockito.Mockito
import spock.lang.Specification

class ExpressionOrderSpecification
  extends Specification
{
  def "it instantiate an ascending ordering operator for a given field by default" () {
    given: "an expression"
    final Expression expression = Mockito.mock(Expression.class)

    when: "we create an ordering operator for a given expression"
    final ExpressionOrder order = new ExpressionOrder(expression)

    then: "we expect to create an ascending operator for the given expression"
    order.direction == OrderingDirection.ASCENDING
    order.expression == expression
  }

  def "it allow to instantiate a fully configured ordering operator for a given field" () {
    given: "an expression"
    final Expression expression = Mockito.mock(Expression.class)

    when: "we create an ordering operator with a given direction for a given expression"
    final ExpressionOrder order = new ExpressionOrder(expression, OrderingDirection.DESCENDING)

    then: "we expect to get an ordering operator for the given expression with the given direction"
    order.direction == OrderingDirection.DESCENDING
    order.expression == expression
  }

  def "it allow to instantiate a copy of another ordering operator" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(Mockito.mock(Expression.class), OrderingDirection.DESCENDING)

    when: "we instantiate a copy of a given ordering operator"
    final ExpressionOrder copy = new ExpressionOrder(source)

    then: "we expect to get a copy of the source operator"
    copy == source
    !copy.is(source)
  }

  def "it allow to create a copy of an ordering operator with a different ordering direction" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(Mockito.mock(Expression.class), OrderingDirection.DESCENDING)

    when: "we update the ordered expression of this ordering operator"
    final ExpressionOrder copy = source.setDirection(OrderingDirection.ASCENDING)

    then: "we expect to get a copy of the source operator that order the given expression"
    copy.direction == OrderingDirection.ASCENDING
    source.direction == OrderingDirection.DESCENDING
    copy.expression == source.expression
    !copy.is(source)
  }

  def "it allow to get a copy with a specific ordering by calling ascending" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(Mockito.mock(Expression.class), OrderingDirection.DESCENDING)

    when: "we update the ordered expression of this ordering operator"
    final ExpressionOrder copy = source.ascending()

    then: "we expect to get a copy of the source operator that order the given expression"
    copy.direction == OrderingDirection.ASCENDING
    source.expression == copy.expression
    source.direction == OrderingDirection.DESCENDING
    !copy.is(source)
  }

  def "it allow to get a copy with a specific ordering by calling descending" () {
    given: "an ordering operator"
    final ExpressionOrder source = new ExpressionOrder(Mockito.mock(Expression.class), OrderingDirection.ASCENDING)

    when: "we update the ordered expression of this ordering operator"
    final ExpressionOrder copy = source.descending()

    then: "we expect to get a copy of the source operator that order the given expression"
    copy.direction == OrderingDirection.DESCENDING
    source.expression == copy.expression
    source.direction == OrderingDirection.ASCENDING
    !copy.is(source)
  }

  def "it does nothing when it is applied to a unorderable collection" () {
    given: "an order"
    final ExpressionOrder order = new ExpressionOrder(Mockito.mock(Expression.class), OrderingDirection.ASCENDING)

    and: "an unorderable collection"
    final Collection collection = Mockito.mock(Collection.class)

    when: "we apply the order operator to the unorderable collection"
    order.apply(collection)

    then: "we expect that the operators does nothing to the collection"
    Mockito.verifyZeroInteractions(collection)
  }

  def "it update an orderable collection when it is applied to a orderable collection" () {
    given: "an order"
    final ExpressionOrder order = new ExpressionOrder(Mockito.mock(Expression.class), OrderingDirection.ASCENDING)

    and: "an orderable collection"
    final OrderableCollection collection = Mockito.mock(OrderableCollection.class)
    final OrderableCollection resultCollection = Mockito.mock(OrderableCollection.class)
    Mockito.when(collection.orderBy(order)).thenReturn(resultCollection)

    when: "we apply the order operator to the orderable collection"
    final Collection result = order.apply(collection)

    then: "we expect that the operator has updated the orderable collection"
    result == resultCollection
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    final Expression expression = Mockito.mock(Expression.class)

    final ExpressionOrder instance = new ExpressionOrder(expression)
    instance == instance

    ExpressionOrder.expression(expression) != null
    ExpressionOrder.expression(expression) == ExpressionOrder.expression(expression)
    ExpressionOrder.expression(expression) != ExpressionOrder.expression(Mockito.mock(Expression.class))
    ExpressionOrder.expression(expression).descending() != ExpressionOrder.expression(expression).ascending()
    ExpressionOrder.expression(expression) != new Object()
  }

  def 'it define a custom hashcode method' () {
    expect: 'hashcode operator to behave accordingly with the standards'
    final Expression expression = Mockito.mock(Expression.class)

    ExpressionOrder.expression(expression).hashCode() == ExpressionOrder.expression(expression).hashCode()
    ExpressionOrder.expression(expression).hashCode() != ExpressionOrder.expression(
      Mockito.mock(Expression.class)
    ).hashCode()
    ExpressionOrder.expression(expression).descending().hashCode() == ExpressionOrder.expression(
      expression
    ).descending().hashCode()
    ExpressionOrder.expression(expression).ascending().hashCode() != ExpressionOrder.expression(
      expression
    ).descending().hashCode()
  }
}
