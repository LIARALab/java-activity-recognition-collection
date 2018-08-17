package org.liara.collection.operator.filtering

import spock.lang.Specification

class ExpressionFilterSpecification extends Specification {
  def "it can be instantiated with an expression" () {
    when: "we instantiate a new expression filter with an expression"
      final ExpressionFilter filter = new ExpressionFilter(":this.name = 'plopl'")

    then: "we expect to get a valid filter"
      filter.expression == ":this.name = 'plopl'"
      filter.parameters.size() == 0
  }

  def "it can be instantiated with an expression and a map of parameters" () {
    when: "we instantiate a new expression filter with an expression and a map of parameters"
      final Map<String, Object> parameters = [
        "name": "plopl"
      ]
      final ExpressionFilter filter = new ExpressionFilter(":this.name = :name", parameters)

    then: "we expect to get a valid filter"
      filter.expression == ":this.name = :name"
      filter.parameters == parameters
      !filter.parameters.is(parameters)
  }

  def "it can be instantiated as a copy of another filter" () {
    given: "a filter"
    final ExpressionFilter source = new ExpressionFilter(":this.name = :name", [
      "name": "plopl"
    ])

    when: "we instantiate a new expression filter as a copy of the other one"
    final ExpressionFilter copy = new ExpressionFilter(source)

    then: "we expect to get a copy of the given filter"
    copy == source
    !copy.is(source)
    !copy.parameters.is(source)
  }

  def "it can return a new instance of another filter with a new parameter in it" () {
    given: "a filter"
    final ExpressionFilter source = new ExpressionFilter(":this.name = :name", [
      "name": "plopl"
    ])

    when: "we put a new parameter into the source filter"
    final ExpressionFilter copy = source.setParameter("type", "banana")

    then: "we expect to get a copy of the given filter with the new parameter in it"
    copy.parameters == [
      "name": "plopl",
      "type": "banana"
    ]
    source.parameters == [
      "name": "plopl"
    ]
    copy.expression == source.expression
    !copy.is(source)
  }

  def "it can return a new instance of another filter with an updated parameter" () {
    given: "a filter"
    final ExpressionFilter source = new ExpressionFilter(":this.name = :name", [
      "name": "plopl"
    ])

    when: "we put a new parameter into the source filter"
    final ExpressionFilter copy = source.setParameter("name", "banana")

    then: "we expect to get a copy of the given filter with the new parameter in it"
    copy.parameters == [
      "name": "banana"
    ]
    source.parameters == [
      "name": "plopl"
    ]
    copy.expression == source.expression
    !copy.is(source)
  }

  def "it can return a new instance of another filter with a parameter removed from it" () {
    given: "a filter"
    final ExpressionFilter source = new ExpressionFilter(":this.name = :name", [
      "name": "plopl"
    ])

    when: "we put a new parameter into the source filter"
    final ExpressionFilter copy = source.setParameter("name", null)

    then: "we expect to get a copy of the given filter with the new parameter in it"
    copy.parameters == [:]
    source.parameters == [
      "name": "plopl"
    ]
    copy.expression == source.expression
    !copy.is(source)
  }
}
