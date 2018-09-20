/*
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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

package org.liara.collection.operator

import org.liara.collection.Collection as LIARACollection
import org.mockito.Mockito
import spock.lang.Specification

class IdentitySpecification extends Specification
{
  def "it return its target collection instance when it is applied" () {
    given: "an identity operator and a collection"
      final Identity identity = Identity.INSTANCE
      final LIARACollection collection = Mockito.mock(LIARACollection.class)

    when: "we apply the operator to a collection"
      final LIARACollection result = identity.apply(collection)

    then: "we expect that the result of the transformation is its given collection"
      result == collection
  }
  
  def "it returns its given operator when it is applied to another operator" () {
    given: "an identity operator and a lambda operator"
      final Identity identity = Identity.INSTANCE
      final Operator operator = Mockito.mock(Operator.class)

    when: "we compose the identity operator with another one"
      final Operator result = identity.apply(operator)
      
    then: "we expect that the result of the application is the original transformation"
      result == operator
  }
}
