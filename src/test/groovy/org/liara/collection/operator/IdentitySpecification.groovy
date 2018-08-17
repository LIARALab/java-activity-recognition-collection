package org.liara.collection.operator

import org.liara.collection.Collection as LIARACollection

import org.mockito.Mockito
import spock.lang.Specification

class IdentitySpecification extends Specification
{
  def "it return its target collection instance when it is applied" () {
    given: "an identity operator and a collection"
      final Identity identity = Identity.INSTANCE
      final LIARACollection collection = Mockito.mock(LIARACollection.class)

    when: "we apply the operator to a collection"
      final LIARACollection result = identity.apply(collection)

    then: "we expect that the result of the transformation is its given collection"
      result == collection
  }
  
  def "it returns its given operator when it is applied to another operator" () {
    given: "an identity operator and a lambda operator"
      final Identity identity = Identity.INSTANCE
      final Operator operator = Mockito.mock(Operator.class)

    when: "we compose the identity operator with another one"
      final Operator result = identity.apply(operator)
      
    then: "we expect that the result of the application is the original transformation"
      result == operator
  }
}
