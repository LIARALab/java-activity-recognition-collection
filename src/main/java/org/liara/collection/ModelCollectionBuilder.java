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

package org.liara.collection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.cursoring.Cursor;

import java.util.Objects;

public class ModelCollectionBuilder
{
  @Nullable
  private Filters _filters;

  @Nullable
  private Cursor _cursor;

  @Nullable
  private Orderings _orderings;

  @Nullable
  private Joins _joins;

  public ModelCollectionBuilder () {
    _filters = null;
    _cursor = null;
    _orderings = null;
    _joins = null;
  }

  public ModelCollectionBuilder (@NonNull final ModelCollectionBuilder toCopy) {
    _filters = toCopy.getFilters();
    _cursor = toCopy.getCursor();
    _orderings = toCopy.getOrderings();
    _joins = toCopy.getJoins();
  }

  public <Entity> @NonNull ModelCollection<Entity> build (@NonNull final Class<Entity> modelClass) {
    return new ModelCollection<>(modelClass, this);
  }

  public @Nullable Joins getJoins () {
    return _joins;
  }

  public void setJoins (@Nullable final Joins joins) {
    _joins = joins;
  }

  public @Nullable Filters getFilters () {
    return _filters;
  }

  public void setFilters (@Nullable final Filters filters) {
    _filters = filters;
  }

  public @Nullable Cursor getCursor () {
    return _cursor;
  }

  public void setCursor (@Nullable final Cursor cursor) {
    _cursor = cursor;
  }

  public @Nullable Orderings getOrderings () {
    return _orderings;
  }

  public void setOrderings (@Nullable final Orderings orderings) {
    _orderings = orderings;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ModelCollectionBuilder) {
      @NonNull
      final ModelCollectionBuilder otherEntityCollectionBuilder = (ModelCollectionBuilder) other;

      return Objects.equals(
        _filters,
        otherEntityCollectionBuilder.getFilters()
      ) && Objects.equals(
        _cursor,
        otherEntityCollectionBuilder.getCursor()
      ) && Objects.equals(
        _orderings,
        otherEntityCollectionBuilder.getOrderings()
      ) && Objects.equals(
        _joins,
        otherEntityCollectionBuilder.getJoins()
      );
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_filters, _cursor, _orderings, _joins);
  }
}
