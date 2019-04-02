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

import java.util.Objects;

public class ModelAggregationBuilder
{
  @Nullable
  private Groups _groups;

  @Nullable
  private Aggregates _aggregates;

  public ModelAggregationBuilder () {
    _groups = null;
    _aggregates = null;
  }

  public ModelAggregationBuilder (@NonNull final ModelAggregationBuilder toCopy) {
    _groups = toCopy.getGroups();
    _aggregates = toCopy.getAggregates();
  }

  public <Entity> @NonNull ModelAggregation<Entity> aggregate (
    @NonNull final ModelCollection<Entity> collection
  ) {
    return new ModelAggregation<>(collection, this);
  }

  public @Nullable Groups getGroups () {
    return _groups;
  }

  public void setGroups (@Nullable final Groups groups) {
    _groups = groups;
  }

  public @Nullable Aggregates getAggregates () {
    return _aggregates;
  }

  public void setAggregates (@Nullable final Aggregates aggregates) {
    _aggregates = aggregates;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof ModelAggregationBuilder) {
      @NonNull
      final ModelAggregationBuilder otherModelAggregationBuilder = (ModelAggregationBuilder) other;

      return Objects.equals(
        _groups,
        otherModelAggregationBuilder.getGroups()
      ) && Objects.equals(
        _aggregates,
        otherModelAggregationBuilder.getAggregates()
      );
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_groups, _aggregates);
  }
}
