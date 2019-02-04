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

package org.liara.collection.operator.filtering;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.jpa.JPAEntityCollection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExistsFilter
  implements Filter
{
  @NonNull
  private final JPAEntityCollection _collection;

  @NonNull
  private final String _expression;

  @NonNull
  private final Map<@NonNull String, @NonNull Object> _parameters;

  public ExistsFilter (@NonNull final JPAEntityCollection collection) {
    _collection = collection;
    _expression = ":count > 0";
    _parameters = new HashMap<>();
  }

  public ExistsFilter (
    @NonNull final JPAEntityCollection collection,
    @NonNull final String expression
  )
  {
    _collection = collection;
    _expression = expression;
    _parameters = new HashMap<>();
  }

  public ExistsFilter (
    @NonNull final ExistsFilter toCopy,
    @NonNull final String expression
  )
  {
    _collection = toCopy.getCollection();
    _expression = expression;
    _parameters = new HashMap<>();
  }

  public ExistsFilter (
    @NonNull final ExistsFilter toCopy,
    @NonNull final Map<String, Object> parameters
  )
  {
    _collection = toCopy.getCollection();
    _expression = toCopy.getExpression();
    _parameters = new HashMap<>(parameters);
  }

  public ExistsFilter (
    @NonNull final ExistsFilter toCopy,
    @NonNull final JPAEntityCollection collection
  )
  {
    _collection = collection;
    _expression = toCopy.getExpression();
    _parameters = new HashMap<>(toCopy.getParameters());
  }

  public ExistsFilter (@NonNull final ExistsFilter toCopy) {
    _collection = toCopy.getCollection();
    _expression = toCopy.getExpression();
    _parameters = new HashMap<>(toCopy.getParameters());
  }

  @Override
  public @NonNull String getExpression () {
    return _expression.replace(
      ":count",
      "(" +
      _collection.getQuery("COUNT(:this)", ":this_" + _collection.getEntityName())
        .toString().replace(":super", ":this") +
      ")"
    );
  }

  @Override
  public @NonNull Filter setParameter (
    @NonNull final String name, @Nullable final Object value
  )
  {
    @NonNull final Map<String, Object> result = new HashMap<>(_parameters);

    if (value == null) {
      result.remove(name);
    } else {
      result.put(name, value);
    }

    return new ExistsFilter(this, result);
  }

  @Override
  public @NonNull Filter setParameters (
    @NonNull final Map<@NonNull String, @NonNull Object> parameters
  )
  {
    return new ExistsFilter(this, parameters);
  }

  @Override
  public @NonNull Filter removeParameter (@NonNull final String name) {
    @NonNull final Map<String, Object> result = new HashMap<>(_parameters);
    result.remove(name);

    return new ExistsFilter(this, result);
  }

  @Override
  public @NonNull Map<@NonNull String, @NonNull Object> getParameters () {
    @NonNull final Map<@NonNull String, @NonNull Object> parameters = new HashMap<>(_parameters);
    parameters.putAll(_collection.getParameters());


    return Collections.unmodifiableMap(parameters);
  }

  public @NonNull JPAEntityCollection getCollection () {
    return _collection;
  }
}
