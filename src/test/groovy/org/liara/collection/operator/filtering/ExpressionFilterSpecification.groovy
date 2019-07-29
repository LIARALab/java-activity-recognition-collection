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

package org.liara.collection.operator.filtering

import org.liara.expression.Expression
import org.mockito.Mockito
import spock.lang.Specification

class ExpressionFilterSpecification extends Specification {
  def "it can be instantiated with an expression" () {
    given: "an expression"
    final Expression expression = Mockito.mock(Expression.class)

    when: "we instantiate a new expression filter with an expression"
    final ExpressionFilter filter = new ExpressionFilter(expression)

    then: "we expect to get a valid filter"
    filter.expression == expression
  }

  def "it can be instantiated as a copy of another filter" () {
    given: "a filter"
    final ExpressionFilter source = new ExpressionFilter(Mockito.mock(Expression.class))

    when: "we instantiate a new expression filter as a copy of the other one"
    final ExpressionFilter copy = new ExpressionFilter(source)

    then: "we expect to get a copy of the given filter"
    copy == source
    !copy.is(source)
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    final Expression expression = Mockito.mock(Expression.class)

    Filter.expression(Mockito.mock(Expression.class)) != null
    final Filter instance = Filter.expression(expression)
    instance == instance

    Filter.expression(expression) == Filter.expression(expression)
    Filter.expression(expression) != Filter.expression(Mockito.mock(Expression.class))
    Filter.expression(expression) != new Object()
  }

  def 'it define a custom hashcode method' () {
    expect: 'hashcode operator to behave accordingly with the standards'
    final Expression expression = Mockito.mock(Expression.class)

    Filter.expression(expression).hashCode() == Filter.expression(expression).hashCode()
    Filter.expression(expression).hashCode() != Filter.expression(Mockito.mock(Expression.class)).hashCode()
  }
}
