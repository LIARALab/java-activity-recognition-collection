package org.liara.collection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.operator.Operator;

public interface Collection {
  /**
   * Apply an operator on this collection and return the result.
   *
   * @param operator An operator to apply.
   *
   * @return The result of this operation over this collection.
   */
  default @NonNull Collection apply (@NonNull final Operator operator) {
    return operator.apply(this);
  }
}