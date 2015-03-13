/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.testing

import spock.lang.Specification

class CombinationsTest extends Specification {

  def "getCombinations_empty"() {
    when:
    def combinations = Combinations.getCombinations()

    then:
    combinations.size() == 0
  }

  def "getCombinations_two_by_two"() {
    when:
    def combinations = Combinations.getCombinations(["Foo", "Bar"], ["One", "Two"])

    then:
    combinations.size() == 4
    combinations == [["Foo", "One"], ["Foo", "Two"], ["Bar", "One"], ["Bar", "Two"]]
  }

  def "getCombinations_two_by_four_by_three"() {
    when:
    def combinations = Combinations.getCombinations(["Foo", "Bar"], ["One", "Two", "Three", "Four"], ["Rock", "Paper", "Scissors"])

    then:
    combinations.size() == 24
  }

  def "getCombinations_supports_null_elements"() {
    when:
    def combinations = Combinations.getCombinations(["Foo", "Bar"], ["One", null])

    then:
    combinations.size() == 4
    combinations == [["Foo", "One"], ["Foo", null], ["Bar", "One"], ["Bar", null]]
  }

}
