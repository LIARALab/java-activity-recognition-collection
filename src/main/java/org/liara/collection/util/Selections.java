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

package org.liara.collection.util;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.selection.Select;

import java.util.Iterator;
import java.util.Objects;

public class Selections
  implements Iterable<@NonNull Select>
{
  @NonNull
  public static final Selections EMPTY = new Selections();

  @NonNull
  private final ImmutableList<@NonNull Select> _selects;

  public Selections (@NonNull final Select... selects) {
    _selects = ImmutableList.copyOf(selects);
  }

  public Selections (@NonNull final Iterator<@NonNull Select> selections) {
    _selects = ImmutableList.copyOf(selections);
  }

  public Selections (final java.util.@NonNull Collection<@NonNull Select> selects) {
    _selects = ImmutableList.copyOf(selects);
  }

  public Selections (@NonNull final ImmutableList<@NonNull Select> selects) {
    _selects = selects;
  }

  public Selections (@NonNull final Selections toCopy) {
    _selects = toCopy.getSelects();
  }

  public @NonNull Selections select (@NonNull final Select select) {
    if (_selects.contains(select)) {
      return this;
    } else {
      final ImmutableList.@NonNull Builder<@NonNull Select> builder = ImmutableList.builder();
      builder.addAll(_selects);
      builder.add(select);

      return new Selections(builder.build());
    }
  }

  public @NonNull Selections remove (@NonNull final Select select) {
    if (_selects.contains(select)) {
      final ImmutableList.@NonNull Builder<@NonNull Select> builder = ImmutableList.builder();

      for (@NonNull final Select toAdd : _selects) {
        if (!select.equals(toAdd)) {
          builder.add(toAdd);
        }
      }

      return new Selections(builder.build());
    } else {
      return this;
    }
  }

  public @NonNull Selections clear () {
    return Selections.EMPTY;
  }

  @Override
  public Iterator<@NonNull Select> iterator () {
    return _selects.iterator();
  }

  public @NonNull ImmutableList<@NonNull Select> getSelects () {
    return _selects;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Selections) {
      @NonNull final Selections otherGroups = (Selections) other;

      return Objects.equals(_selects, otherGroups.getSelects());
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_selects);
  }
}
