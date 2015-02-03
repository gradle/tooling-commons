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
