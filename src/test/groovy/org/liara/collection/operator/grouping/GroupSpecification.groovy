package org.liara.collection.operator.grouping

import org.liara.collection.Collection
import org.mockito.Mockito
import spock.lang.Specification

class GroupSpecification extends Specification {
  def 'it can be instantiated with an expression' () {
    expect: 'to be instantiable with an expression'
    new Group(':this.name').expression == ':this.name'
    new Group(':this').expression == ':this'
    Group.expression(':this.name').expression == ':this.name'
    Group.expression(':this').expression == ':this'
  }

  def 'it can be instantiated as a copy of another group' () {
    given: 'a group operator'
      final Group source = new Group(':this.name')

    when: 'we instantiate a copy of the source group'
      final Group copy = new Group(source)

    then: 'we expect to get a copy of the source group'
    copy == source
    !copy.is(source)
  }

  def 'it return a new updated group when you mutate the group expression' () {
    given: 'a group operator'
      final Group source = new Group(':this.source')

    when: 'we mutate the group operator'
      final Group other = source.setExpression(':this.other')

    then: 'we expect to get an updated copy of the source group'
      other.expression == ':this.other'
      source.expression == ':this.source'
      !other.is(source)
  }

  def "it does nothing when it is applied to an ungroupable collection" () {
    given: "a group operator"
    final Group group = new Group(":this.name")

    and: "an ungroupable collection"
    final Collection collection = Mockito.mock(Collection.class)

    when: "we apply the group operator to the ungroupable collection"
    group.apply(collection)

    then: "we expect that the operator does nothing to the collection"
    Mockito.verifyZeroInteractions(collection)
  }

  def "it update a groupable collection when it is applied to a groupable collection" () {
    given: "a group"
    final Group group = new Group(":this.name")

    and: "a groupable collection"
    final GroupableCollection collection = Mockito.mock(GroupableCollection.class)
    final GroupableCollection resultCollection = Mockito.mock(GroupableCollection.class)
    Mockito.when(collection.groupBy(group)).thenReturn(resultCollection)

    when: "we apply the group operator to the groupable collection"
    final GroupableCollection result = group.apply(collection)

    then: "we expect that the operator has updated the groupable collection"
    result == resultCollection
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    Group.expression(':this.other') != null
    final Group instance = Group.expression(':this.other')
    instance == instance
    Group.expression(':this.first') == Group.expression(':this.first')
    Group.expression(':this.first') != Group.expression(':this.other')
    Group.expression(':this.first') != new Object()
  }

  def 'it define a custom hashcode method' () {
    expect: 'hashcode operator to behave accordingly with the standards'
    Group.expression(':this.other').hashCode() == Group.expression(':this.other').hashCode()
    Group.expression(':this.other').hashCode() != Group.expression(':this.first').hashCode()
  }
}
