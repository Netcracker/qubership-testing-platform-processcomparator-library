/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.automation.pc.comparator.impl.table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.qubership.automation.pc.core.helpers.TextHelpers;

import lombok.Getter;

/* Expected config format is (check rule description):
<check>
# Table = er-table name
# This checking is performed via this er-table.
    Name of er-table is set in Reader configuration (or can be read from Excel)
Table = Cat1

# Relation = ar-table column name = er-table column name
# This is condition for joining er & ar rows
Relation = Offering  = Offering (German)

# Columns = ar-table column1 name = er-table column1 name, ar-table column2 name = er-table column2 name
# This is conditions for checking attributes' values
Columns = Offering Range Min=Offering Range Min, Offering Range Max=Offering Range Max

# Filter=ar-table column1 name = Value1
# Filter=ar-table column2 name = Value2
# Filter=ar-table column3 name = Value3
# Filter for ar-table rows

Filter=Category = 'Internet'
Filter=Offering = '-'
Filter=Next Level Entity =
Filter=Next Level Entity Type = 'Offering'
</check>

<alias>
# Name = Name of placeholder, i.e. <Top Offer>
Name = <Top Offer>

# Table = Table-name
# Table name of lookup table
Table = Cat1

# Colname = Column name
# Column name of lookup table
Colname = Offering (German)

# Filter=er-table column name = Value
# Filter for er-table (lookup table) rows
Filter = Category = 'Internet'
</alias>

<replace>
# Replace substring in ar to this substring, i.e. "Surf-Flat 25^1" ~ "Surf-Flat 25 ver. 1"
'^'=' ver. '
</replace>
     */

@Getter
public class CheckPocRule {

    private List<CheckPocSection> checkPocSections = new ArrayList<>();

    public CheckPocRule(List<String> rows) {
        if (rows != null) {
            List<String> splitRows = rows.stream()
                    .flatMap(str -> TextHelpers.stringToList(str).stream())
                    .collect(Collectors.toList());
            List<String> sectionRows = new ArrayList<>();
            boolean checkSectionOpened = false;
            boolean replaceSectionOpened = false;
            boolean aliasSectionOpened = false;
            for (String ruleLine : splitRows) {
                String str = ruleLine.trim();
                if (str.isEmpty() || str.startsWith("#")) {
                    continue;
                }
                switch (str.toLowerCase()) {
                    case "<alias>":
                        aliasSectionOpened = true;
                        checkSectionOpened = false;
                        replaceSectionOpened = false;
                        sectionRows = new ArrayList<>();
                        break;
                    case "<check>":
                        checkSectionOpened = true;
                        aliasSectionOpened = false;
                        replaceSectionOpened = false;
                        sectionRows = new ArrayList<>();
                        break;
                    case "<replace>":
                        replaceSectionOpened = true;
                        aliasSectionOpened = false;
                        checkSectionOpened = false;
                        sectionRows = new ArrayList<>();
                        break;
                    case "</replace>":
                        if (replaceSectionOpened) {
                            replaceSectionOpened = false;
                            CheckPocSection cfgItem = new CheckPocSection(sectionRows, CheckPocSectionType.REPLACE);
                            if (!cfgItem.replacements.isEmpty()) {
                                checkPocSections.add(cfgItem);
                            }
                        }
                        break;
                    case "</check>":
                        if (checkSectionOpened) {
                            checkSectionOpened = false;
                            CheckPocSection cfgItem = new CheckPocSection(sectionRows, CheckPocSectionType.CHECK);
                            if (!cfgItem.erTable.isEmpty()
                                    && !cfgItem.relations.isEmpty()
                                    && !cfgItem.columns.isEmpty()) {
                                checkPocSections.add(cfgItem);
                            }
                        }
                        break;
                    case "</alias>":
                        if (aliasSectionOpened) {
                            aliasSectionOpened = false;
                            CheckPocSection cfgItem = new CheckPocSection(sectionRows, CheckPocSectionType.ALIAS);
                            if (!cfgItem.erTable.isEmpty()
                                    && !cfgItem.cfgName.isEmpty()
                                    && !cfgItem.colName.isEmpty()) {
                                checkPocSections.add(cfgItem);
                            }
                        }
                        break;
                    default:
                        if (aliasSectionOpened || checkSectionOpened || replaceSectionOpened) {
                            sectionRows.add(str);
                        }
                        break;
                }
            }
        }
    }
}
