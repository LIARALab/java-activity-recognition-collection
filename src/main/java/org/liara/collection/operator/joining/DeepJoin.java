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

package org.liara.collection.operator.joining;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;

import java.util.Objects;

public class DeepJoin<Related>
  implements Join<Related>
{
  @NonNull
  private final Join<?> _base;

  @NonNull
  private final Join<Related> _next;

  public DeepJoin (
    @NonNull final Join<?> base,
    @NonNull final Join<Related> next
  )
  {
    if (next instanceof DeepJoin) {
      _base = base.join(((DeepJoin<Object>) next).getBase());
      _next = ((DeepJoin<Related>) next).getNext();
    } else {
      _base = base;
      _next = next;
    }
  }

  public DeepJoin (
    @NonNull final DeepJoin<Related> toCopy
  )
  {
    _base = toCopy.getBase();
    _next = toCopy.getNext();
  }

  public @NonNull Join<?> getBase () {
    return _base;
  }

  public @NonNull Join<Related> getNext () {
    return _next;
  }

  @Override
  public @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof JoinableCollection) {
      return _base.apply(((JoinableCollection) input).join(this));
    }

    return input;
  }

  @Override
  public @NonNull String getName () {
    return _base.getName() + "_" + _next.getName();
  }

  @Override
  public @NonNull Class<Related> getRelatedClass () {
    return _next.getRelatedClass();
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof DeepJoin) {
      @NonNull final DeepJoin otherDeepJoin = (DeepJoin) other;

      return Objects.equals(_base, otherDeepJoin.getBase()) && Objects.equals(_next, otherDeepJoin.getNext());
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_base, _next);
  }
}
