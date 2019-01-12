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

import java.util.Objects;

public class Embeddable<Related>
  implements Join<Related>
{
  @NonNull
  private final String _name;

  @NonNull
  private final Class<Related> _relatedClass;

  public Embeddable (
    @NonNull final Class<Related> relatedClass
  )
  {
    @NonNull final String relatedClassName = relatedClass.getSimpleName();

    _name = Character.toLowerCase(relatedClassName.charAt(0)) + relatedClassName.substring(1);
    _relatedClass = relatedClass;
  }

  public Embeddable (
    @NonNull final Class<Related> relatedClass, @NonNull final String name
  )
  {
    _name = name;
    _relatedClass = relatedClass;
  }

  public Embeddable (
    @NonNull final Embeddable<Related> toCopy
  )
  {
    _name = toCopy.getName();
    _relatedClass = toCopy.getRelatedClass();
  }

  @Override
  public @NonNull String getName () {
    return _name;
  }

  public @NonNull Embeddable<Related> setName (@NonNull final String name) {
    return new Embeddable<>(_relatedClass, name);
  }

  @Override
  public @NonNull String identifier () {
    return ":this." + getName();
  }

  @Override
  public @NonNull Class<Related> getRelatedClass () {
    return _relatedClass;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_name, _relatedClass);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Embeddable) {
      @NonNull final Embeddable otherJoin = (Embeddable) other;

      return Objects.equals(_relatedClass, otherJoin.getRelatedClass()) && Objects.equals(_name, otherJoin.getName());
    }

    return false;
  }
}
