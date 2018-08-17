package org.liara.collection.operator

import org.checkerframework.checker.nullness.qual.NonNull
import org.liara.collection.Collection
import org.mockito.Mockito
import spock.lang.Specification
import org.liara.collection.Collection as LiaraCollection

class OperatorSpecification extends Specification
{
  def "it can be defined as a lambda" () {
    given: "a lambda operator and a mocked collection"
      final LiaraCollection baseCollection = Mockito.mock(LiaraCollection.class)
      final LiaraCollection resultCollection = Mockito.mock(LiaraCollection.class)
      final Operator operator = { final LiaraCollection collection -> resultCollection }
      
    when: "we apply the operator to a collection"
      final LiaraCollection operationResult = operator.apply(baseCollection)

    then: "we expect that the result view was transformed accordingly"
      operationResult == resultCollection
      operationResult != baseCollection
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
