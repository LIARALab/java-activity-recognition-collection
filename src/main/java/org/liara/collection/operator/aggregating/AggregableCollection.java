package org.liara.collection.operator.aggregating;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;

import java.util.Set;

public interface AggregableCollection extends Collection
{
  @NonNull AggregableCollection aggregate (@NonNull final Aggregate aggregation);

  @NonNegative int getAggregationCount ();

  @NonNull Set<@NonNull Aggregate> getAggregations ();

  @NonNull Iterable<@NonNull Aggregate> aggregations ();

  default boolean isAggregated () {
    return getAggregationCount() > 0;
  }
}
