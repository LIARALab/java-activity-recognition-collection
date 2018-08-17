package org.liara.collection.operator.filtering;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;

import java.util.Set;

public interface FilterableCollection extends Collection
{
  @NonNull FilterableCollection addFilter (@NonNull final Filter filter);

  @NonNull FilterableCollection removeFilter (@NonNull final Filter filter);

  @NonNegative int getFilterCount ();

  @NonNull Set<@NonNull Filter> getFilters ();

  @NonNull Iterable<@NonNull Filter> filters ();

  default boolean isFiltered () {
    return getFilterCount() > 0;
  }
}
