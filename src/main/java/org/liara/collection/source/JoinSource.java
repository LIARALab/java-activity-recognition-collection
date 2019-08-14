/*
 * Copyright (C) 2019 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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

package org.liara.collection.source;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.data.graph.Column;
import org.liara.data.primitive.Primitive;
import org.liara.expression.Expression;
import org.liara.support.view.View;

import java.util.Objects;

public class JoinSource
  implements GraphSource
{
  @NonNull
  private final String _name;

  @NonNull
  private final JoinType _type;

  @NonNull
  private final Source _origin;

  @NonNull
  private final TableSource _joined;

  @NonNull
  private final Expression<@NonNull Boolean> _predicate;

  @NonNull
  private final JoinSourcePlaceholder<?>[] _ownPlaceholders;

  @NonNull
  private final SourcePlaceholder<?>[] _placeholders;

  @NonNull
  private final View<@NonNull SourcePlaceholder> _placeholdersView;

  public JoinSource (
    @NonNull final JoinType type,
    @NonNull final Source origin,
    @NonNull final TableSource joined,
    @NonNull final Expression<@NonNull Boolean> predicate,
    @NonNull final String name
  ) {
    _name = name;
    _type = type;
    _origin = origin;
    _joined = joined;

    _ownPlaceholders = new JoinSourcePlaceholder[_joined.getPlaceholders().getSize()];
    _placeholders = (
      new SourcePlaceholder[_ownPlaceholders.length + _origin.getPlaceholders().getSize()]
    );
    _placeholdersView = View.readonly(SourcePlaceholder.class, _placeholders);
    buildPlaceholders();

    _predicate = linkPredicate(Objects.requireNonNull(predicate));
  }

  public static GraphSource inner (
    @NonNull final GraphSource origin,
    @NonNull final TableSource joined,
    @NonNull final Expression<Boolean> predicate
  ) {
    return new JoinSource(
      JoinType.INNER_JOIN,
      origin,
      joined,
      predicate,
      joined.getName()
    );
  }

  public JoinSource (@NonNull final JoinSourceBuilder source) {
    _name = Objects.requireNonNull(source.getName());
    _joined = Objects.requireNonNull(source.getJoined());
    _origin = Objects.requireNonNull(source.getOrigin());
    _type = Objects.requireNonNull(source.getType());

    _ownPlaceholders = new JoinSourcePlaceholder[_joined.getPlaceholders().getSize()];
    _placeholders = (
      new SourcePlaceholder[_ownPlaceholders.length + _origin.getPlaceholders().getSize()]
    );
    _placeholdersView = View.readonly(SourcePlaceholder.class, _placeholders);
    buildPlaceholders();

    _predicate = linkPredicate(Objects.requireNonNull(source.getPredicate()));
  }

  private void buildPlaceholders () {
    for (int index = 0; index < _joined.getPlaceholders().getSize(); ++index) {
      _ownPlaceholders[index] = new JoinSourcePlaceholder<>(
        this,
        (Column<?>) _joined.getPlaceholders().get(index).getColumn()
      );

      _placeholders[_origin.getPlaceholders().getSize() + index] = _ownPlaceholders[index];
    }

    for (int index = 0; index < _origin.getPlaceholders().getSize(); ++index) {
      _placeholders[index] = _origin.getPlaceholders().get(index);
    }
  }

  private @NonNull Expression<Boolean> linkPredicate (
    @NonNull final Expression<Boolean> predicate
  ) {
    return JoinExpressionLinker.getInstance().link(this, predicate);
  }

  /**
   * Return a placeholder expression for the given column of this source.
   *
   * @param column Column from which getting a placeholder.
   *
   * @return A placeholder for the given column.
   */
  @SuppressWarnings("unchecked") // @see #instantiatePlaceholders()
  public <Type> @NonNull JoinSourcePlaceholder<Type> getOwnPlaceholder (
    @NonNull final Column<Type> column
  ) {
    return (JoinSourcePlaceholder<Type>) _ownPlaceholders[_joined.getTable()
                                                                 .getIndexOf(column)];
  }

  /**
   * Return a placeholder expression for the ith column of this source.
   *
   * @param index Index of the column from which getting a placeholder.
   *
   * @return A placeholder for the column at the given index.
   */
  public @NonNull JoinSourcePlaceholder<?> getOwnPlaceholder (@NonNegative final int index) {
    return _ownPlaceholders[index];
  }

  /**
   * Return a placeholder expression for the ith column of this source.
   *
   * @param expectedType Expected type of the column.
   * @param index        Index of the column from which getting a placeholder.
   *
   * @return A placeholder for the column at the given index.
   */
  @SuppressWarnings("unchecked") // Placeholder type test.
  public <Type> @NonNull JoinSourcePlaceholder<Type> getOwnPlaceholder (
    @NonNull final Class<Type> expectedType,
    @NonNegative final int index
  ) {
    @NonNull final JoinSourcePlaceholder<?> placeholder = getOwnPlaceholder(index);

    if (placeholder.getResultType().getJavaClass() == expectedType) {
      return (JoinSourcePlaceholder<Type>) placeholder;
    } else {
      throw new IllegalArgumentException(
        "Unable to get a placeholder for the column \"" + placeholder.getColumn().getName() +
        "\" of table \"" + placeholder.getColumn().getTable().getName() + "\" aliased as \"" +
        _name + "\" of graph \"" + placeholder.getColumn().getGraph().getName() +
        "\" of type " + expectedType.getName() +
        " because the given column was not of the expected type but of type " +
        placeholder.getColumn().getType().getJavaClass().getName() + "."
      );
    }
  }

  /**
   * @see GraphSource#getOwnPlaceholder(String)
   */
  @Override
  public @NonNull JoinSourcePlaceholder<?> getOwnPlaceholder (@NonNegative final String name) {
    return _ownPlaceholders[_joined.getTable().getIndexOf(_joined.getTable().getColumn(name))];
  }

  /**
   * @see GraphSource#getOwnPlaceholder(Primitive, String)
   */
  @SuppressWarnings("unchecked") // Placeholder type test.
  @Override
  public <Type> @NonNull JoinSourcePlaceholder<Type> getOwnPlaceholder (
    @NonNull final Primitive<Type> expectedType,
    @NonNegative final String name
  ) {
    @NonNull final JoinSourcePlaceholder<?> placeholder = getOwnPlaceholder(name);

    if (placeholder.getResultType() == expectedType) {
      return (JoinSourcePlaceholder<Type>) placeholder;
    } else {
      throw new IllegalArgumentException(
        "Unable to get a placeholder for the column \"" + placeholder.getColumn().getName() +
        "\" of table \"" + placeholder.getColumn().getTable().getName() + "\" aliased as \"" +
        _name + "\" of graph \"" + placeholder.getColumn().getGraph().getName() +
        "\" of type " + expectedType.getName() +
        " because the given column was not of the expected type but of type " +
        placeholder.getColumn().getType().getJavaClass().getName() + "."
      );
    }
  }

  /**
   * @see Source#getName()
   */
  @Override
  public @NonNull String getName () {
    return _name;
  }

  /**
   * @return The type of this join.
   */
  public @NonNull JoinType getType () {
    return _type;
  }

  /**
   * @return The origin source of this join source.
   */
  public @NonNull Source getOrigin () {
    return _origin;
  }

  /**
   * @return The joined source of this join source.
   */
  public @NonNull TableSource getJoined () {
    return _joined;
  }

  /**
   * @return The predicate used by this join.
   */
  public @NonNull Expression<@NonNull Boolean> getPredicate () {
    return _predicate;
  }

  /**
   * @see Source#contains(SourcePlaceholder)
   */
  @Override
  public boolean contains (@NonNull final SourcePlaceholder<?> placeholder) {
    return placeholder.getSource() == this || _origin.contains(placeholder);
  }

  /**
   * @see Source#getPlaceholders()
   */
  @Override
  public @NonNull View<? extends @NonNull SourcePlaceholder> getPlaceholders () {
    return _placeholdersView;
  }
}
