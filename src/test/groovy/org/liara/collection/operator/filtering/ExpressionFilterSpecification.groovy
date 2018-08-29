package org.liara.collection.operator.filtering

import org.liara.collection.operator.grouping.Group
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

  def "it can return a copy of an existing instance with an updated expression" () {
    given: "a filter instance"
      final ExpressionFilter source = new ExpressionFilter(":this.name = :name", [
        "name": "plopl"
      ])

    when: "we update the expression of the given filter"
      final ExpressionFilter updated = source.setExpression(":this.name != :name")

    then: "we expect to get an updated copy of the source filter"
    updated.parameters == source.parameters
    updated.expression != source.expression
    !updated.is(source)
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

  def "it returns itself if you trying to remove a parameter that does not exists" () {
    given: "a filter"
    final ExpressionFilter source = new ExpressionFilter(":this.name = :name", [
      "name": "plopl"
    ])

    when: "we trying to remove a parameter that does not exists from the filter"
    final ExpressionFilter copy = source.removeParameter("plopl")

    then: "we expect to get the current filter instance as a result"
    copy.is(source)
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    Filter.expression(':this.other = 5') != null
    final Filter instance = Filter.expression(':this.other = 5')
    instance == instance
    Filter.expression(':this.first = 5') == Filter.expression(':this.first = 5')
    Filter.expression(':this.first = :value').setParameter("value", 8) != Filter.expression(
      ':this.first = :value'
    ).setParameter("value", 10)
    Filter.expression(':this.first = :value').setParameter("value", 8) == Filter.expression(
      ':this.first = :value'
    ).setParameter("value", 8)
    Filter.expression(':this.first = 5') != Filter.expression(':this.other = 8')
    Filter.expression(':this.first = 5') != new Object()
  }

  def 'it define a custom hashcode method' () {
    expect: 'hashcode operator to behave accordingly with the standards'
    Filter.expression(':this.other = 5').hashCode() == Filter.expression(':this.other = 5').hashCode()
    Filter.expression(':this.other = 8').hashCode() != Filter.expression(':this.other = 5').hashCode()
    Filter.expression(':this.first = :value').setParameter("value", 8).hashCode() != Filter.expression(
      ':this.first = :value'
    ).setParameter("value", 10).hashCode()
    Filter.expression(':this.first = :value').setParameter("value", 8).hashCode() == Filter.expression(
      ':this.first = :value'
    ).setParameter("value", 8).hashCode()
  }
}
