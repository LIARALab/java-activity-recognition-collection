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

package org.liara.collection.operator.grouping

import org.liara.collection.Collection
import org.mockito.Mockito
import spock.lang.Specification

class ExpressionGroupSpecification
  extends Specification
{
  def 'it can be instantiated with an expression' () {
    expect: 'to be instantiable with an expression'
    new ExpressionGroup(':this.name').expression == ':this.name'
    new ExpressionGroup(':this').expression == ':this'
    ExpressionGroup.expression(':this.name').expression == ':this.name'
    ExpressionGroup.expression(':this').expression == ':this'
  }

  def 'it can be instantiated as a copy of another group' () {
    given: 'a group operator'
    final ExpressionGroup source = new ExpressionGroup(':this.name')

    when: 'we instantiate a copy of the source group'
    final ExpressionGroup copy = new ExpressionGroup(source)

    then: 'we expect to get a copy of the source group'
    copy == source
    !copy.is(source)
  }

  def 'it return a new updated group when you mutate the group expression' () {
    given: 'a group operator'
    final ExpressionGroup source = new ExpressionGroup(':this.source')

    when: 'we mutate the group operator'
    final ExpressionGroup other = source.setExpression(':this.other')

    then: 'we expect to get an updated copy of the source group'
      other.expression == ':this.other'
      source.expression == ':this.source'
      !other.is(source)
  }

  def "it does nothing when it is applied to an ungroupable collection" () {
    given: "a group operator"
    final ExpressionGroup group = new ExpressionGroup(":this.name")

    and: "an ungroupable collection"
    final Collection collection = Mockito.mock(Collection.class)

    when: "we apply the group operator to the ungroupable collection"
    group.apply(collection)

    then: "we expect that the operator does nothing to the collection"
    Mockito.verifyZeroInteractions(collection)
  }

  def "it update a groupable collection when it is applied to a groupable collection" () {
    given: "a group"
    final ExpressionGroup group = new ExpressionGroup(":this.name")

    and: "a groupable collection"
    final GroupableCollection collection = Mockito.mock(GroupableCollection.class)
    final GroupableCollection resultCollection = Mockito.mock(GroupableCollection.class)
    Mockito.when(collection.groupBy(group)).thenReturn(resultCollection)

    when: "we apply the group operator to the groupable collection"
    final Collection result = group.apply(collection)

    then: "we expect that the operator has updated the groupable collection"
    result == resultCollection
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    ExpressionGroup.expression(':this.other') != null
    final ExpressionGroup instance = ExpressionGroup.expression(':this.other')
    instance == instance
    ExpressionGroup.expression(':this.first') == ExpressionGroup.expression(':this.first')
    ExpressionGroup.expression(':this.first') != ExpressionGroup.expression(':this.other')
    ExpressionGroup.expression(':this.first') != new Object()
  }

  def 'it define a custom hashcode method' () {
    expect: 'hashcode operator to behave accordingly with the standards'
    ExpressionGroup.expression(':this.other').hashCode() == ExpressionGroup.expression(':this.other').hashCode()
    ExpressionGroup.expression(':this.other').hashCode() != ExpressionGroup.expression(':this.first').hashCode()
  }
}
