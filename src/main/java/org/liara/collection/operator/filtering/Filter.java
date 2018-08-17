package org.liara.collection.operator.filtering;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;

import java.util.Map;

public interface Filter
       extends Operator
{
  static @NonNull Filter expression (@NonNull final String expression) {
    return new ExpressionFilter(expression);
  }

  @Override
  default @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof  FilterableCollection) {
      return FilterableCollection.class.cast(input).addFilter(this);
    }

    return input;
  }

  @NonNull String getExpression ();

  @NonNull Filter setParameter (@NonNull final String name, @Nullable final Object value);

  @NonNull Filter removeParameter (@NonNull final String name);

  @NonNull Map<@NonNull String, @NonNull Object> getParameters ();
}
