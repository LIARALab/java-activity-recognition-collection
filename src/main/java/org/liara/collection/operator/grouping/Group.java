package org.liara.collection.operator.grouping;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;

import java.util.Objects;

public final class Group implements Operator
{
  public static @NonNull Group expression (@NonNull final String expression) {
    return new Group(expression);
  }

  @NonNull
  private final String _expression;

  public Group (@NonNull final String expression) {
    _expression = expression;
  }

  public Group (@NonNull final Group toCopy) {
    _expression = toCopy.getExpression();
  }

  @Override
  public @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof GroupableCollection) {
      return ((GroupableCollection) input).groupBy(this);
    }

    return input;
  }

  public @NonNull String getExpression () {
    return _expression;
  }

  public @NonNull Group setExpression (@NonNull final String expression) {
    return new Group(expression);
  }

  @Override
  public int hashCode () {
    return Objects.hash(_expression);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Group) {
      @NonNull final Group otherGroup = (Group) other;
      return Objects.equals(_expression, otherGroup.getExpression());
    }

    return false;
  }
}
