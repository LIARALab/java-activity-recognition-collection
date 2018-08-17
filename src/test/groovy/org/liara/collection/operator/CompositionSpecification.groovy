package org.liara.collection.operator

import org.liara.collection.Collection as LIARACollection
import org.mockito.InOrder
import org.mockito.Mockito

import spock.lang.Specification

class CompositionSpecification extends Specification
{
  def "it can be instantiated from an iterable of operators" () {
    given: "a list of operators"
    final List<Operator> operators = [
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class)
    ]

    when: "we instantiate a composition from the given list of operators"
    final Composition composition = new Composition(operators as Iterable<Operator>)

    then: "we expect that we get a valid composition"
    composition.size == operators.size()
    for (int index = 0; index < operators.size(); ++index) {
      composition[index] == operators[index]
    }
    composition.operators.is(operators) == false
  }

  def "it can be instantiated from an iterator of operators" () {
    given: "a list of operators"
    final List<Operator> operators = [
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class)
    ]

    when: "we instantiate a composition from the given list of operators"
    final Composition composition = new Composition(operators.iterator())

    then: "we expect that we get a valid composition"
    composition.size == operators.size()
    for (int index = 0; index < operators.size(); ++index) {
      composition[index] == operators[index]
    }
    composition.operators.is(operators) == false
  }

  def "it can be instantiated from an array of operators" () {
    given: "a list of operators"
    final Operator[] operators = [
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class)
    ]

    when: "we instantiate a composition from the given list of operators"
    final Composition composition = new Composition(operators)

    then: "we expect that we get a valid composition"
    composition.size == operators.length
    for (int index = 0; index < operators.length; ++index) {
      composition[index] == operators[index]
    }
    composition.operators.is(operators) == false
  }

  def "it can be instantiated from another composition" () {
    given: "a list of operators"
    final Operator[] operators = [
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class)
    ]

    when: "we instantiate a composition from the given list of operators"
    final Composition composition = new Composition(new Composition(operators))

    then: "we expect that we get a valid composition"
    composition.size == operators.length
    for (int index = 0; index < operators.length; ++index) {
      composition[index] == operators[index]
    }
    composition.operators.is(operators) == false
  }

  def "it flatten composition of compositions" () {
    given: "a list of operators"
    final Operator[] operators = [
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class),
      Mockito.mock(Operator.class)
    ]

    when: "we instantiate a composition from the given list of operators"
    final Composition composition = new Composition([
      operators[0],
      new Composition(Arrays.copyOfRange(operators, 1, 3)),
      operators[3],
      new Composition(Arrays.copyOfRange(operators, 4, 7))
    ])

    then: "we expect that we get a valid composition"
    composition.size == operators.length
    for (int index = 0; index < operators.length; ++index) {
      composition[index] == operators[index]
    }
    composition.operators.is(operators) == false
  }

  def "it apply each of its child operators sequentially when it is applied to a collection" () {
    given: "a list of operators"
    final LIARACollection[] collections = new LIARACollection[10]
    final Operator[] operators = new Operator[collections.length]

    for (int index = 0; index < collections.length; ++index) {
      collections[index] = Mockito.mock(LIARACollection.class)
      operators[index] = Mockito.mock(Operator.class)
      Mockito.when(
        operators[index].apply((LIARACollection) Mockito.any(LIARACollection.class))
      ).thenReturn(collections[index])
    }

    and: "a composition of each of its operators"
    final Composition composition = new Composition(operators)

    when: "we apply the composition to a collection"
    final LIARACollection inputCollection = Mockito.mock(LIARACollection.class)
    final LIARACollection result = composition.apply(inputCollection)

    then: "we expect that all composed operators are applied sequentially to the collection"
    result == collections[0]
    final InOrder order = Mockito.inOrder(operators)
    for (int index = 0; index < operators.length; ++index) {
      order.verify(operators[operators.length - index - 1]).apply(
        index == 0 ? inputCollection : collections[operators.length - index]
      )
    }
  }
}
