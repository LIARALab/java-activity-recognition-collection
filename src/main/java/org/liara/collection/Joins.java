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

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.joining.Join;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class Joins
  implements Iterable<@NonNull Join>
{
  @NonNull
  public static final Joins EMPTY = new Joins();

  @NonNull
  private final ImmutableMap<@NonNull String, @NonNull Join> _joins;

  public Joins (@NonNull final Join... joins) {
    final ImmutableMap.@NonNull Builder<@NonNull String, @NonNull Join> builder =
      ImmutableMap.builder();

    for (@NonNull final Join join : joins) {
      builder.put(join.getName(), join);
    }

    _joins = builder.build();
  }

  public Joins (@NonNull final Iterator<@NonNull Join> joins) {
    final ImmutableMap.@NonNull Builder<@NonNull String, @NonNull Join> builder =
      ImmutableMap.builder();

    while (joins.hasNext()) {
      @NonNull final Join join = joins.next();
      builder.put(join.getName(), join);
    }

    _joins = builder.build();
  }

  public Joins (final java.util.@NonNull Collection<@NonNull Join> joins) {
    final ImmutableMap.@NonNull Builder<@NonNull String, @NonNull Join> builder =
      ImmutableMap.builder();

    for (@NonNull final Join join : joins) {
      builder.put(join.getName(), join);
    }

    _joins = builder.build();
  }

  public Joins (@NonNull final ImmutableMap<@NonNull String, @NonNull Join> joins) {
    _joins = joins;
  }

  public Joins (@NonNull final Map<@NonNull String, @NonNull Join> joins) {
    _joins = ImmutableMap.copyOf(joins);
  }

  public Joins (@NonNull final Joins joins) {
    _joins = joins.getJoins();
  }

  public @NonNull Joins join (@NonNull final Join join) {
    if (_joins.containsKey(join.getName()) && !_joins.get(join.getName()).equals(join)) {
      throw new Error(
        "Unable to join to a collection of " + join.getClass() + " as " + join.getName() +
        " because another join" +
        " already exists with the same name. Please use different names for your joins.");
    } else if (_joins.containsKey(join.getName())) {
      return this;
    } else {
      final ImmutableMap.@NonNull Builder<@NonNull String, @NonNull Join> builder =
        ImmutableMap.builder();
      builder.putAll(_joins);
      builder.put(join.getName(), join);

      return new Joins(builder.build());
    }
  }

  public @NonNull Joins disjoin (@NonNull final Join join) {
    if (_joins.get(join.getName()).equals(join)) {
      final ImmutableMap.@NonNull Builder<@NonNull String, @NonNull Join> builder =
        ImmutableMap.builder();

      for (@NonNull final Join toAdd : this) {
        if (!toAdd.equals(join)) {
          builder.put(toAdd.getName(), toAdd);
        }
      }

      return new Joins(builder.build());
    } else {
      return this;
    }
  }

  public @NonNull Joins disjoin (@NonNull final String name) {
    if (_joins.containsKey(name)) {
      final ImmutableMap.@NonNull Builder<@NonNull String, @NonNull Join> builder =
        ImmutableMap.builder();

      for (@NonNull final Join toAdd : this) {
        if (!toAdd.getName().equals(name)) {
          builder.put(toAdd.getName(), toAdd);
        }
      }

      return new Joins(builder.build());
    } else {
      return this;
    }
  }

  public @NonNull ImmutableMap<@NonNull String, @NonNull Join> getJoins () {
    return _joins;
  }

  @Override
  public @NonNull Iterator<@NonNull Join> iterator () {
    return _joins.values().iterator();
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Joins) {
      @NonNull final Joins otherJoins = (Joins) other;

      return Objects.equals(_joins, otherJoins.getJoins());
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_joins);
  }
}
