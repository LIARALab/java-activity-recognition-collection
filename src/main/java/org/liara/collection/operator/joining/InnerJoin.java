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
import org.liara.collection.operator.filtering.Filter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class InnerJoin<Related>
  implements Join<Related>
{
  @NonNull
  private final String _name;

  @NonNull
  private final Class<Related> _relatedClass;

  @NonNull
  private final Set<@NonNull Filter> _filters;

  public InnerJoin (
    @NonNull final Class<Related> relatedClass
  )
  {
    @NonNull final String relatedClassName = relatedClass.getSimpleName();

    _name = Character.toLowerCase(relatedClassName.charAt(0)) + relatedClassName.substring(1);
    _relatedClass = relatedClass;
    _filters = new HashSet<>();
  }

  public InnerJoin (
    @NonNull final Class<Related> relatedClass, final java.util.@NonNull Collection<@NonNull Filter> filters
  )
  {
    @NonNull final String relatedClassName = relatedClass.getSimpleName();

    _name = Character.toLowerCase(relatedClassName.charAt(0)) + relatedClassName.substring(1);
    _relatedClass = relatedClass;
    _filters = new HashSet<>(filters);
  }

  public InnerJoin (
    @NonNull final Class<Related> relatedClass, @NonNull final String name
  )
  {
    _name = name;
    _relatedClass = relatedClass;
    _filters = new HashSet<>();
  }

  public InnerJoin (
    @NonNull final Class<Related> relatedClass,
    @NonNull final String name,
    final java.util.@NonNull Collection<@NonNull Filter> filters
  )
  {
    _name = name;
    _relatedClass = relatedClass;
    _filters = new HashSet<>(filters);
  }

  public InnerJoin (
    @NonNull final InnerJoin<Related> toCopy
  )
  {
    _name = toCopy.getName();
    _relatedClass = toCopy.getRelatedClass();
    _filters = toCopy.getFilters();
  }

  public @NonNull InnerJoin<Related> filter (@NonNull final Filter filter) {
    @NonNull final Set<@NonNull Filter> filters = new HashSet<>(_filters);
    filters.add(filter);

    return new InnerJoin<>(_relatedClass, _name, filters);
  }

  @Override
  public @NonNull String getName () {
    return _name;
  }

  public @NonNull InnerJoin<Related> setName (@NonNull final String name) {
    return new InnerJoin<>(_relatedClass, name, _filters);
  }

  @Override
  public @NonNull Class<Related> getRelatedClass () {
    return _relatedClass;
  }

  public @NonNull Set<@NonNull Filter> getFilters () {
    return _filters;
  }

  public @NonNull Iterable<@NonNull Filter> filters () { return _filters; }

  @Override
  public int hashCode () {
    return Objects.hash(_name, _relatedClass, _filters);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof InnerJoin) {
      @NonNull final InnerJoin otherJoin = (InnerJoin) other;

      return Objects.equals(_relatedClass, otherJoin.getRelatedClass()) && Objects.equals(_name, otherJoin.getName()) &&
             Objects.equals(_filters, otherJoin.getFilters());
    }

    return false;
  }
}
