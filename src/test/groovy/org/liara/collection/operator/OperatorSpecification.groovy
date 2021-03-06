/*
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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

package org.liara.collection.operator

import org.liara.collection.Collection as LiaraCollection
import org.liara.collection.Specification
import org.mockito.Mockito

class OperatorSpecification extends Specification
{
  def "it can be defined as a lambda" () {
    given: "a lambda operator and a mocked collection"
      final Operator operator = {
        final LiaraCollection collection -> Mockito.mock(LiaraCollection.class)
      }
      
    expect: "to be appliable"
      operator.apply(Mockito.mock(LiaraCollection.class)) != null
  }

  def "it can be composed with other operators" () {
    given: "three lambda operators"
      final Operator first = Mockito.mock(Operator.class)
      final Operator second = Mockito.mock(Operator.class)
      final Operator last = Mockito.mock(Operator.class)
      Mockito.when(first.apply(Mockito.any(Operator.class) as Operator)).thenCallRealMethod()
      Mockito.when(second.apply(Mockito.any(Operator.class) as Operator)).thenCallRealMethod()
      Mockito.when(last.apply(Mockito.any(Operator.class) as Operator)).thenCallRealMethod()
      
    when: "we compose each operators as a chain"
      final Operator t = second.apply(last as Operator)
      final Composition result = first.apply(t) as Composition
      
    then: "we expect to get a valid composition of the operators"
      result[0] == first
      result[1] == second
      result[2] == last
  }
}
