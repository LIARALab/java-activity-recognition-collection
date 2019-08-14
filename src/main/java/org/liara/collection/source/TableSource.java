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
import org.liara.data.graph.Table;
import org.liara.data.primitive.Primitive;
import org.liara.support.view.View;

/**
 * A source that is a data-graph table.
 */
public class TableSource
  implements GraphSource
{
  @NonNull
  private final String _name;

  @NonNull
  private final Table _table;

  @NonNull
  private final TableSourcePlaceholder<?>[] _placeholders;

  @NonNull
  private final View<@NonNull TableSourcePlaceholder> _placeholderView;

  /**
   * Instantiate a new source that is a data-graph table.
   *
   * @param table The data-graph table to use as a source.
   */
  public TableSource (@NonNull final Table table) {
    _table = table;
    _placeholders = new TableSourcePlaceholder[_table.getColumns().getSize()];
    _placeholderView = View.readonly(TableSourcePlaceholder.class, _placeholders);
    instantiatePlaceholders();
    _name = table.getName();
  }

  /**
   * Instantiate a new source that is a data-graph table with an alias.
   *
   * @param table The data-graph table to use as a source.
   * @param name  Alias to use as the table name.
   */
  public TableSource (@NonNull final Table table, @NonNull final String name) {
    _table = table;
    _placeholders = new TableSourcePlaceholder[_table.getColumns().getSize()];
    _placeholderView = View.readonly(TableSourcePlaceholder.class, _placeholders);
    instantiatePlaceholders();
    _name = name;
  }

  /**
   * Instantiate a new source that is a copy of another one.
   *
   * @param toCopy The source to copy.
   */
  public TableSource (@NonNull final TableSource toCopy) {
    _table = toCopy.getTable();
    _placeholders = new TableSourcePlaceholder[_table.getColumns().getSize()];
    _placeholderView = View.readonly(TableSourcePlaceholder.class, _placeholders);
    instantiatePlaceholders();
    _name = toCopy.getName();
  }

  /**
   * Instantiate this sources placeholders.
   */
  private void instantiatePlaceholders () {
    @NonNull final View<@NonNull Column> columns = _table.getColumns();

    for (int index = 0; index < columns.getSize(); ++index) {
      _placeholders[index] = new TableSourcePlaceholder<>(this, (Column<?>) columns.get(index));
    }
  }

  /**
   * Return a placeholder expression for the given column of this source.
   *
   * @param column Column from which getting a placeholder.
   *
   * @return A placeholder for the given column.
   */
  @SuppressWarnings("unchecked") // @see #instantiatePlaceholders()
  public <Type> @NonNull TableSourcePlaceholder<Type> getOwnPlaceholder (
    @NonNull final Column<Type> column
  ) { return (TableSourcePlaceholder<Type>) _placeholders[_table.getIndexOf(column)]; }

  /**
   * Return a placeholder expression for the ith column of this source.
   *
   * @param index Index of the column from which getting a placeholder.
   *
   * @return A placeholder for the column at the given index.
   */
  public @NonNull TableSourcePlaceholder<?> getOwnPlaceholder (@NonNegative final int index) {
    return _placeholders[index];
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
  public <Type> @NonNull TableSourcePlaceholder<Type> getOwnPlaceholder (
    @NonNull final Class<Type> expectedType,
    @NonNegative final int index
  ) {
    @NonNull final TableSourcePlaceholder<?> placeholder = getOwnPlaceholder(index);

    if (placeholder.getResultType().getJavaClass() == expectedType) {
      return (TableSourcePlaceholder<Type>) placeholder;
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
  public @NonNull TableSourcePlaceholder<?> getOwnPlaceholder (@NonNegative final String name) {
    return _placeholders[_table.getIndexOf(_table.getColumn(name))];
  }

  /**
   * @see GraphSource#getOwnPlaceholder(Primitive, String)
   */
  @Override
  @SuppressWarnings("unchecked") // Placeholder type test.
  public <Type> @NonNull TableSourcePlaceholder<Type> getOwnPlaceholder (
    @NonNull final Primitive<Type> expectedType,
    @NonNegative final String name
  ) {
    @NonNull final TableSourcePlaceholder<?> placeholder = getOwnPlaceholder(name);

    if (placeholder.getResultType() == expectedType) {
      return (TableSourcePlaceholder<Type>) placeholder;
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
   * @return The data-graph table used as a source.
   */
  public @NonNull Table getTable () {
    return _table;
  }

  /**
   * @see Source#getName()
   */
  @Override
  public @NonNull String getName () {
    return _name;
  }

  /**
   * @see Source#contains(SourcePlaceholder)
   */
  @Override
  public boolean contains (@NonNull final SourcePlaceholder<?> placeholder) {
    return placeholder.getSource() == this;
  }

  /**
   * @see Source#getPlaceholders()
   */
  @Override
  public @NonNull View<@NonNull TableSourcePlaceholder> getPlaceholders () {
    return _placeholderView;
  }
}
