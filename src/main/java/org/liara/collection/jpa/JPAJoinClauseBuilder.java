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

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.joining.DeepJoin;
import org.liara.collection.operator.joining.InnerJoin;
import org.liara.collection.operator.joining.Join;

import java.util.*;
import java.util.stream.Collectors;

public class JPAJoinClauseBuilder
{
  @NonNull
  private final Map<@NonNull String, @NonNull Join> _joins;

  @NonNull
  private final Set<@NonNull String> _builtJoins;

  public JPAJoinClauseBuilder () {
    _joins = new HashMap<>();
    _builtJoins = new HashSet<>();
  }

  public void setJoins (@NonNull final Map<@NonNull String, @NonNull Join> joins) {
    _joins.clear();
    _joins.putAll(joins);
  }

  public @NonNull CharSequence build () {
    _builtJoins.clear();

    @NonNull final List<@NonNull CharSequence> sequences = new LinkedList<>();

    for (@NonNull final String name : _joins.keySet()) {
      sequences.addAll(build(name));
    }

    return sequences.stream()
             .collect(Collectors.joining(" "));
  }

  private @NonNull List<@NonNull CharSequence> build (@NonNull final String name) {
    if (_builtJoins.contains(name)) return Collections.emptyList();

    @NonNull final List<CharSequence> result;
    @NonNull final Join               join = _joins.get(name);

    if (join instanceof DeepJoin) {
      @NonNull final DeepJoin deepJoin = (DeepJoin) join;

      result = new LinkedList<>();

      result.addAll(build(deepJoin.getBase()
                            .getName()));
      result.addAll(build(deepJoin.getBase()
                            .getName(), deepJoin.getNext()));
    } else {
      result = build(":super", join);
    }

    _builtJoins.add(name);
    return result;
  }

  private @NonNull List<@NonNull CharSequence> build (
    @NonNull final String superIdentifier, @NonNull final Join join
  )
  {
    if (join instanceof InnerJoin) {
      return Collections.singletonList(getInnerJoinClause(superIdentifier, (InnerJoin) join));
    } else {
      return Collections.emptyList();
    }
  }

  private @NonNull CharSequence getInnerJoinClause (
    @NonNull final String superIdentifier, @NonNull final InnerJoin join
  )
  {
    @NonNull final StringBuilder clause = new StringBuilder();

    clause.append("INNER JOIN ");
    clause.append(join.getRelatedClass()
                    .getName());
    clause.append(" ");
    clause.append(join.getName());

    if (join.getFilters()
          .size() > 0) {
      clause.append(" ON ");

      @NonNull final Iterator<@NonNull Filter> filters = join.filters()
                                                           .iterator();
      @NonNegative int                         index   = 0;

      while (filters.hasNext()) {
        @NonNull final Filter filter = filters.next();

        clause.append(filter.getExpression()
                        .replaceAll(":super", superIdentifier)
                        .replaceAll(":this", join.getName())
                        .replaceAll(":([a-zA-Z0-9_]+)", String.join("", ":filter", String.valueOf(index), "_$1")));

        if (filters.hasNext()) {
          clause.append(" AND ");
        }

        index += 1;
      }
    }

    return clause;
  }
}
