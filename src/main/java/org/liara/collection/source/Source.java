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
import org.liara.data.graph.Table;
import org.liara.support.view.View;

public interface Source
{
  static @NonNull TableSource from (@NonNull final Table table) {
    return new TableSource(table);
  }

  static @NonNull TableSource from (@NonNull final Table table, @NonNull final String alias) {
    return new TableSource(table, alias);
  }

  /**
   * @return This source's name.
   */
  @NonNull String getName ();

  /**
   * Returns true if this source contains the given placeholder.
   *
   * @param placeholder A placeholder to search.
   *
   * @return True if this source contains the given placeholder.
   */
  boolean contains (@NonNull final SourcePlaceholder<?> placeholder);

  /**
   * @return A view over each placeholder of this source.
   */
  @NonNull View<? extends @NonNull SourcePlaceholder> getPlaceholders ();
}
