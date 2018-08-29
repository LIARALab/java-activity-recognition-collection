package org.liara.collection.operator.cursoring

import org.liara.collection.operator.filtering.Filter
import org.mockito.Mockito
import org.liara.collection.Collection as LIARACollection
import spock.lang.Specification

class CursorSpecification extends Specification
{
  def "it initialize an unlimited cursor with zero offset by default" () {
    expect: "to initialize an unlimited cursor with zero offset by default"
      new Cursor().hasLimit() == false
      new Cursor().offset == 0
  }

  def "it can be instantiated with a limit" () {
    expect: "to be instantiable with a limit"
      new Cursor(100).limit == 100
      new Cursor(25).limit == 25
  }

  def "it can be instantiable with an offset with limit" () {
    expect: "to be instantiable with a limit"
    new Cursor(10,100).limit == 100
    new Cursor(10,100).offset == 10
    new Cursor(5,25).limit == 25
    new Cursor(5,25).offset == 5
  }

  def "it can be instantiable from another cursor" () {
    given: "a source cursor"
    final Cursor source = new Cursor(10, 150)

    when: "we instantiate a cursor from another cursor"
    final Cursor copy = new Cursor(source)

    then: "we expect to get a copy of the source cursor"
    copy == source
    !copy.is(source)
  }

  def "it allows you to instantiate a copy of a cursor with a different offset" () {
    given: "a source cursor"
    final Cursor source = new Cursor(10, 150)

    when: "we assign to the source cursor a new offset"
    final Cursor copy = source.setOffset(20)

    then: "we expect to get a copy of the source cursor with the new offset"
    source.offset == 10
    source.limit == 150
    copy.offset == 20
    copy.limit == 150
  }

  def "it allows you to instantiate a copy of a cursor with a different limit" () {
    given: "a source cursor"
    final Cursor source = new Cursor(10, 150)

    when: "we assign to the source cursor a new limit"
    final Cursor copy = source.setLimit(40)

    then: "we expect to get a copy of the source cursor with the new limit"
    source.offset == 10
    source.limit == 150
    copy.offset == 10
    copy.limit == 40
  }

  def "it allows you to instantiate a copy of a cursor without limit" () {
    given: "a source cursor"
    final Cursor source = new Cursor(10, 150)

    when: "we unlimit the source cursor"
    final Cursor copy = source.unlimit()

    then: "we expect to get a copy of the source cursor without limit"
    source.offset == 10
    source.limit == 150
    copy.offset == 10
    !copy.hasLimit()
  }

  def "it allows you to instantiate a copy of a cursor without offset" () {
    given: "a source cursor"
    final Cursor source = new Cursor(10, 150)

    when: "we unskip the source cursor"
    final Cursor copy = source.unskip()

    then: "we expect to get a copy of the source cursor without offset"
    source.offset == 10
    source.limit == 150
    copy.offset == 0
    copy.limit == 150
  }

  def "it does nothing when it is applied to a uncursorable collection" () {
    given: "a source cursor"
    final Cursor cursor = new Cursor(10, 150)

    and: "an uncursorable collection"
    final LIARACollection collection = Mockito.mock(LIARACollection.class)

    when: "we apply the cursor operator to the uncursorable collection"
    cursor.apply(collection)

    then: "we expect that the operators does nothing to the collection"
    Mockito.verifyZeroInteractions(collection)
  }

  def "it update a cursorable collection when it is applied to a cursorable collection" () {
    given: "a source cursor"
    final Cursor cursor = new Cursor(10, 150)

    and: "an cursorable collection"
    final CursorableCollection collection = Mockito.mock(CursorableCollection.class)
    final CursorableCollection resultCollection = Mockito.mock(CursorableCollection.class)
    Mockito.when(collection.setCursor(cursor)).thenReturn(resultCollection)

    when: "we apply the cursor operator to the cursorable collection"
    final CursorableCollection result = cursor.apply(collection)

    then: "we expect that the operator has updated the cursorable collection"
    result == resultCollection
  }

  def 'it define a custom equals method' () {
    expect: 'equal operator to behave accordingly with the standards'
    Cursor.DEFAULT != null
    Cursor.DEFAULT == Cursor.DEFAULT
    Cursor.NONE.setLimit(20).setOffset(10) == Cursor.NONE.setLimit(20).setOffset(10)
    Cursor.NONE.setLimit(20).setOffset(10) != Cursor.NONE.setLimit(20).setOffset(15)
    Cursor.NONE.setLimit(20).setOffset(10) != Cursor.NONE.setLimit(15).setOffset(10)
    Cursor.DEFAULT != new Object()
  }

  def 'it define a custom hashcode method' () {
    expect: 'hashcode operator to behave accordingly with the standards'
    Cursor.DEFAULT.hashCode() == Cursor.DEFAULT.hashCode()
    Cursor.NONE.setLimit(20).setOffset(10).hashCode() == Cursor.NONE.setLimit(20).setOffset(10).hashCode()
    Cursor.NONE.setLimit(20).setOffset(10).hashCode() != Cursor.NONE.setLimit(20).setOffset(15).hashCode()
    Cursor.NONE.setLimit(20).setOffset(10).hashCode() != Cursor.NONE.setLimit(15).setOffset(10).hashCode()
  }
}
