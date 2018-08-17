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
