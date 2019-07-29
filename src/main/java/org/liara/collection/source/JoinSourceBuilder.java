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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.expression.Expression;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class JoinSourceBuilder
{
  @NonNull
  private static final Map<@NonNull Thread, @NonNull JoinSourceBuilder> SINGLETONS = (
    Collections.synchronizedMap(new WeakHashMap<>())
  );
  @Nullable
  private              String                                           _name;
  @Nullable
  private              TableSource                                      _joined;
  @Nullable
  private              Source                                           _origin;
  @Nullable
  private              JoinType                                         _type;
  @Nullable
  private              Expression<@NonNull Boolean>                     _predicate;

  public JoinSourceBuilder () {
    _name = null;
    _joined = null;
    _origin = null;
    _type = null;
    _predicate = null;
  }

  public JoinSourceBuilder (@NonNull final JoinSourceBuilder builder) {
    _name = builder.getName();
    _joined = builder.getJoined();
    _origin = builder.getOrigin();
    _type = builder.getType();
    _predicate = builder.getPredicate();
  }

  public static @NonNull JoinSourceBuilder getInstance () {
    @NonNull final Thread thread = Thread.currentThread();

    if (!SINGLETONS.containsKey(thread)) {
      SINGLETONS.put(thread, new JoinSourceBuilder());
    }

    return SINGLETONS.get(thread);
  }

  public void clear () {
    _name = null;
    _joined = null;
    _origin = null;
    _type = null;
    _predicate = null;
  }

  public @NonNull JoinSource build () {
    return new JoinSource(this);
  }

  public @Nullable String getName () {
    return _name;
  }

  public void setName (@Nullable final String name) {
    _name = name;
  }

  public @Nullable TableSource getJoined () {
    return _joined;
  }

  public void setJoined (@Nullable final TableSource joined) {
    _joined = joined;
  }

  public @Nullable Source getOrigin () {
    return _origin;
  }

  public void setOrigin (@Nullable final Source origin) {
    _origin = origin;
  }

  public @Nullable JoinType getType () {
    return _type;
  }

  public void setType (@Nullable final JoinType type) {
    _type = type;
  }

  public @Nullable Expression<@NonNull Boolean> getPredicate () {
    return _predicate;
  }

  public void setPredicate (@Nullable final Expression<@NonNull Boolean> predicate) {
    _predicate = predicate;
  }
}
