package org.liara.collection.operator

import org.checkerframework.checker.nullness.qual.NonNull
import org.liara.collection.Collection
import org.liara.collection.Collection as LIARACollection
import org.liara.collection.operator.cursoring.Cursor
import org.mockito.InOrder
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
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

  def "it return the empty composition if you trying to compose operators of an empty array" () {
    expect: "to return the empty composition if we try to compose operators of an empty array"
    Composition.of() == Composition.EMPTY
    Composition.of([] as Operator[]) == Composition.EMPTY
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

  def mockOperatorWithHashAndEquals (final int identifier) {
    final Operator operator = new Operator() {
      @Override
      Collection apply (Collection input) {
        return input
      }

      @Override
      int hashCode () {
        return identifier
      }

      @Override
      boolean equals (Object obj) {
        return obj != null && obj.hashCode() == identifier
      }
    }

    return operator
  }

  def 'it define a custom equals method' () {
    given: 'some mocked operators'
    final Operator[] operators = [
      mockOperatorWithHashAndEquals(0),
      mockOperatorWithHashAndEquals(1),
      mockOperatorWithHashAndEquals(2),
      mockOperatorWithHashAndEquals(3),
      mockOperatorWithHashAndEquals(4)
    ] as Operator[]

    final Operator[] shuffled = Arrays.copyOf(operators, operators.length)
    Collections.shuffle(Arrays.asList(shuffled), new Random(559))

    expect: 'equal operator to behave accordingly with the standards'
    Composition.of(operators) != null
    Composition.of(operators) == Composition.of(operators)
    Composition.of(operators) != Composition.of(shuffled)
    Composition.of(Arrays.copyOfRange(operators, 0, 3)) != Composition.of(Arrays.copyOfRange(operators, 2, 3))
    Composition.of(operators) != new Object()
  }

  def 'it define a custom hashcode method' () {
    given: 'some mocked operators'
    final Operator[] operators = [
      mockOperatorWithHashAndEquals(0),
      mockOperatorWithHashAndEquals(1),
      mockOperatorWithHashAndEquals(2),
      mockOperatorWithHashAndEquals(3),
      mockOperatorWithHashAndEquals(4)
    ] as Operator[]

    final Operator[] shuffled = Arrays.copyOf(operators, operators.length)
    Collections.shuffle(Arrays.asList(shuffled), new Random(559))

    expect: 'hashcode operator to behave accordingly with the standards'
    Composition.of(operators).hashCode() == Composition.of(operators).hashCode()
    Composition.of(operators).hashCode() != Composition.of(shuffled).hashCode()
    Composition.of(Arrays.copyOfRange(operators, 0, 3)).hashCode() != Composition.of(Arrays.copyOfRange(operators, 2, 3)).hashCode()
  }
}
