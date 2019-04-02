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

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.grouping.Group;

import java.util.Iterator;
import java.util.Objects;

public class Groups
  implements Iterable<@NonNull Group>
{
  @NonNull
  public static final Groups EMPTY = new Groups();

  @NonNull
  private final ImmutableList<@NonNull Group> _groups;

  public Groups (@NonNull final Group... groups) {
    _groups = ImmutableList.copyOf(groups);
  }

  public Groups (@NonNull final Iterator<@NonNull Group> groups) {
    _groups = ImmutableList.copyOf(groups);
  }

  public Groups (final java.util.@NonNull Collection<@NonNull Group> groups) {
    _groups = ImmutableList.copyOf(groups);
  }

  public Groups (@NonNull final ImmutableList<@NonNull Group> groups) {
    _groups = groups;
  }

  public Groups (@NonNull final Groups toCopy) {
    _groups = toCopy.getGroups();
  }

  public @NonNull Groups groupBy (@NonNull final Group group) {
    if (_groups.contains(group)) {
      return this;
    } else {
      final ImmutableList.@NonNull Builder<@NonNull Group> builder = ImmutableList.builder();
      builder.addAll(_groups);
      builder.add(group);

      return new Groups(builder.build());
    }
  }

  public @NonNull Groups remove (@NonNull final Group group) {
    if (_groups.contains(group)) {
      final ImmutableList.@NonNull Builder<@NonNull Group> builder = ImmutableList.builder();

      for (@NonNull final Group toAdd : _groups) {
        if (!group.equals(toAdd)) {
          builder.add(toAdd);
        }
      }

      return new Groups(builder.build());
    } else {
      return this;
    }
  }

  public @NonNull Groups clear () {
    return Groups.EMPTY;
  }

  @Override
  public Iterator<@NonNull Group> iterator () {
    return _groups.iterator();
  }

  public @NonNull ImmutableList<@NonNull Group> getGroups () {
    return _groups;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Groups) {
      @NonNull final Groups otherGroups = (Groups) other;

      return Objects.equals(_groups, otherGroups.getGroups());
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_groups);
  }
}
