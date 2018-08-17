package org.liara.collection.operator.aggregating;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;

import javax.persistence.metamodel.Attribute;

public final class Aggregate implements Operator
{
  @NonNull
  private final String _expression;

  @NonNull
  private final String _name;

  public Aggregate (@NonNull final String expression, @NonNull final String name) {
    _expression = expression;
    _name = name;
  }

  public Aggregate (@NonNull final Attribute<?, ?> attribute) {
    _expression = ":this." + attribute.getName();
    _name = attribute.getName();
  }

  public Aggregate (@NonNull final Attribute<?, ?> attribute, @NonNull final String name) {
    _expression = ":this." + attribute.getName();
    _name = name;
  }

  public Aggregate (@NonNull final Aggregate toCopy) {
    _expression = toCopy.getExpression();
    _name = toCopy.getName();
  }

  @Override
  public @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof  AggregableCollection) {
      return ((AggregableCollection) input).aggregate(this);
    }

    return input;
  }

  public @NonNull String getExpression () {
    return _expression;
  }

  public @NonNull String getExpression (@NonNull final String entityName) {
    return _expression.replaceAll(":this", entityName + ".");
  }

  public @NonNull Aggregate setExpression (@NonNull final String expression) {
    return new Aggregate(
      expression, _name
    );
  }

  public @NonNull Aggregate setExpression (@NonNull final Attribute<?, ?> expression) {
    return new Aggregate(
      expression, _name
    );
  }

  public @NonNull String getName () {
    return _name;
  }

  public @NonNull Aggregate setName (@NonNull final String name) {
    return new Aggregate(_expression, name);
  }
}
