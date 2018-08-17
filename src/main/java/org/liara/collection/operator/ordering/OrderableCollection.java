package org.liara.collection.operator.ordering;

import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;

import java.util.List;

public interface OrderableCollection extends Collection
{
  @NonNull OrderableCollection orderBy (@NonNull final Order order);

  @NonNull Order getOrdering (@NonNegative @LessThan("this.getOrderingCount()") final int index);

  @NonNegative int getOrderingCount ();

  @NonNull List<@NonNull Order> getOrderings ();

  @NonNull Iterable<@NonNull Order> orderings ();

  default boolean isOrdered () {
    return getOrderingCount() > 0;
  }
}
