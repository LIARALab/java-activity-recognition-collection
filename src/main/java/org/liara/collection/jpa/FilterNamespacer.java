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

package org.liara.collection.jpa;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class FilterNamespacer
  implements Function<@NonNull MatchResult, @NonNull String>
{
  @NonNull
  public static final Pattern PATTERN = Pattern.compile(":([a-zA-Z0-9_]+)");
  @NonNull
  private final       String  _entityName;
  private             int     _index  = 0;

  public FilterNamespacer (@NonNull final String entityName) {
    _entityName = entityName;
  }

  public void next () {
    _index += 1;
  }

  @Override
  public @NonNull String apply (@NonNull final MatchResult result) {
    if (result.group().startsWith(":this")) {
      return _entityName + result.group().substring(5);
    } else if (result.group().startsWith(":super")) {
      return result.group();
    } else {
      return ":filter" + String.valueOf(_index) + "_" + result.group(1);
    }
  }
}
