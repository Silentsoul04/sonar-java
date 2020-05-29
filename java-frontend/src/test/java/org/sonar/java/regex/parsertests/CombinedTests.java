/*
 * SonarQube Java
 * Copyright (C) 2012-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.regex.parsertests;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.java.regex.ast.DisjunctionTree;
import org.sonar.java.regex.ast.GroupTree;
import org.sonar.java.regex.ast.Quantifier;
import org.sonar.java.regex.ast.RegexTree;
import org.sonar.java.regex.ast.RepetitionTree;
import org.sonar.java.regex.ast.SequenceTree;
import org.sonar.java.regex.ast.SimpleQuantifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sonar.java.regex.parsertests.RegexParserTestUtils.assertLocation;
import static org.sonar.java.regex.parsertests.RegexParserTestUtils.assertPlainCharacter;
import static org.sonar.java.regex.parsertests.RegexParserTestUtils.assertPlainString;
import static org.sonar.java.regex.parsertests.RegexParserTestUtils.assertSuccessfulParse;
import static org.sonar.java.regex.parsertests.RegexParserTestUtils.assertType;

class CombinedTests {

  @Test
  void testNonTrivialRegex() {
    RegexTree regex = assertSuccessfulParse("(ab|b)*(||)");
    assertLocation(0, 11, regex);
    SequenceTree sequence = assertType(SequenceTree.class, regex);
    List<RegexTree> items = sequence.getItems();
    assertEquals(2, items.size(), "The sequence should have two elements.");

    RepetitionTree firstPart = assertType(RepetitionTree.class, items.get(0));
    assertLocation(0, 7, firstPart);
    assertLocation(0, 6, firstPart.getElement());
    assertLocation(6, 7, firstPart.getQuantifier());
    SimpleQuantifier quantifier = assertType(SimpleQuantifier.class, firstPart.getQuantifier());
    assertEquals(SimpleQuantifier.Kind.STAR, quantifier.getKind(), "Quantifier should be a star.");
    assertEquals(Quantifier.Modifier.GREEDY, quantifier.getModifier(), "Quantifier should be greedy.");
    GroupTree repeatedGroup = assertType(GroupTree.class, firstPart.getElement());
    DisjunctionTree repeatedDisjunction = assertType(DisjunctionTree.class, repeatedGroup.getElement());
    List<RegexTree> repeatedAlternatives = repeatedDisjunction.getAlternatives();
    assertEquals(2, repeatedAlternatives.size(), "First disjunction should have two alternatives.");
    assertPlainString("ab", repeatedAlternatives.get(0));
    assertPlainCharacter('b', repeatedAlternatives.get(1));

    GroupTree secondPart = assertType(GroupTree.class, items.get(1));
    assertLocation(7, 11, secondPart);
    DisjunctionTree disjunction = assertType(DisjunctionTree.class, secondPart.getElement());
    List<RegexTree> alternatives = disjunction.getAlternatives();
    assertEquals(3, alternatives.size(), "Second disjunction should have three alternatives");
    for (RegexTree alternative : alternatives) {
      SequenceTree empty = assertType(SequenceTree.class, alternative);
      assertEquals(0, empty.getItems().size(), "Second disjunction should contain only empty sequences.");
    }
  }

}