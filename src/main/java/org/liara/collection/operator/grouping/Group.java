package org.liara.collection.operator.grouping;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;

public final class Group implements Operator
{
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
    return null;
  }

  public @NonNull String getExpression () {
    return _expression;
  }

  public @NonNull String getExpression (@NonNull final String entity) {
    return entity.replaceAll(":this", entity + ".");
  }

  public @NonNull Group setExpression (@NonNull final String expression) {
    return new Group(expression);
  }
}
