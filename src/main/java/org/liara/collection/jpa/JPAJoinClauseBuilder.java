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
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.joining.DeepJoin;
import org.liara.collection.operator.joining.InnerJoin;
import org.liara.collection.operator.joining.Join;

import java.util.*;

public class JPAJoinClauseBuilder
{
  @NonNull
  private final Map<@NonNull String, @NonNull Join> _joins;

  @NonNull
  private final Set<@NonNull Join> _builtJoins;

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
      sequences.addAll(build(_joins.get(name)));
    }

    return String.join(" ", sequences);
  }

  private @NonNull List<@NonNull CharSequence> build (@NonNull final Join join) {
    if (_builtJoins.contains(join)) return Collections.emptyList();

    @NonNull final List<CharSequence> result;

    if (join instanceof DeepJoin) {
      @NonNull final DeepJoin deepJoin = (DeepJoin) join;

      result = new LinkedList<>();

      result.addAll(build(deepJoin.getBase()));
      result.addAll(getClause(deepJoin, deepJoin.getBase().getName()));
    } else {
      result = getClause(join, ":super");
    }

    _builtJoins.add(join);
    return result;
  }

  private @NonNull List<@NonNull CharSequence> getClause (
    @NonNull final Join join,
    @NonNull final String superIdentifier
  )
  {
    if (join instanceof InnerJoin) {
      return Collections.singletonList(
        getInnerJoinClause((InnerJoin) join, join.getName(), superIdentifier)
      );
    } else if (join instanceof DeepJoin && ((DeepJoin) join).getNext() instanceof InnerJoin) {
      return Collections.singletonList(
        getInnerJoinClause(
          (InnerJoin) (((DeepJoin) join).getNext()),
          join.getName(),
          superIdentifier
        )
      );
    }

    return Collections.emptyList();
  }

  private @NonNull CharSequence getInnerJoinClause (
    @NonNull final InnerJoin join,
    @NonNull final String alias,
    @NonNull final String superIdentifier
  )
  {
    @NonNull final StringBuilder clause = new StringBuilder();

    clause.append("INNER JOIN ");
    clause.append(join.getRelatedClass().getName());
    clause.append(" ");
    clause.append(alias);

    if (join.getFilters().size() > 0) {
      clause.append(" ON ");

      @NonNull final Iterator<@NonNull Filter> filters = join.filters().iterator();
      @NonNull final FilterNamespacer          namespacer = new FilterNamespacer(alias);

      while (filters.hasNext()) {
        @NonNull final Filter filter = filters.next();

        clause.append(
          FilterNamespacer.PATTERN.matcher(filter.getExpression()).replaceAll(namespacer)
            .replaceAll(":super", superIdentifier)
        );

        if (filters.hasNext()) clause.append(" AND ");

        namespacer.next();
      }
    }

    return clause;
  }
}
