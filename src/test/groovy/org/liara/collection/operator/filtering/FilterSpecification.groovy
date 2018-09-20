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

package org.liara.collection.operator.filtering

import org.liara.collection.Collection
import org.mockito.Mockito
import spock.lang.Specification

class FilterSpecification extends Specification {
  def "it does nothing when it is applied to an unfilterable collection" () {
    given: "a filter"
    final Filter filter = Mockito.mock(Filter.class)
    Mockito.when(filter.apply(Mockito.any(Collection.class) as Collection)).thenCallRealMethod()

    and: "an unfilterable collection"
    final Collection collection = Mockito.mock(Collection.class)

    when: "we apply the filter operator to the unfilterable collection"
    filter.apply(collection)

    then: "we expect that the operator does nothing to the collection"
    Mockito.verifyZeroInteractions(collection)
  }

  def "it update a filterable collection when it is applied to a filterable collection" () {
    given: "a filter"
    final Filter filter = Mockito.mock(Filter.class)
    Mockito.when(filter.apply(Mockito.any(Collection.class) as Collection)).thenCallRealMethod()

    and: "a filterable collection"
    final FilterableCollection collection = Mockito.mock(FilterableCollection.class)
    final FilterableCollection resultCollection = Mockito.mock(FilterableCollection.class)
    Mockito.when(collection.addFilter(filter)).thenReturn(resultCollection)

    when: "we apply the filter operator to the filterable collection"
    final FilterableCollection result = filter.apply(collection)

    then: "we expect that the operator has updated the filterable collection"
    result == resultCollection
  }
}
