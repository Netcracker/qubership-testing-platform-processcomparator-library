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

package org.qubership.automation.pc.comparator.impl;

import static org.qubership.automation.pc.compareresult.ResultType.EXTRA;
import static org.qubership.automation.pc.compareresult.ResultType.IDENTICAL;
import static org.qubership.automation.pc.compareresult.ResultType.MISSED;
import static org.qubership.automation.pc.compareresult.ResultType.MODIFIED;
import static org.qubership.automation.pc.compareresult.ResultType.SIMILAR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.comparator.enums.JsonComparatorParameters;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.JsonDiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class JsonComparatorTest extends AbstractComparatorTest{

    private JsonComparator jsonComparator;

    @BeforeEach
    public void setUp() throws Exception {
        jsonComparator = new JsonComparator();
    }

    @Test
    public void given_twoIdenticalObject_withoutParameters_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d8e\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef-4998-9974-93129df8eae6\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"ru\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry20190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        String ar = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d8e\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef-4998-9974-93129df8eae6\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"ru\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry20190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_twoIdenticalObjectAndErContainsRegexp_withoutParameters_resultListContains10DiffMessageTypeIdentical() throws ComparatorException {
        String er = "{\"id\":\"regexp:.*\",\"activeFrom\":\"regexp:.*\",\"contactMediums\":[],"
                + "\"partyRoleAssociations\":[],\"individualIdentifications\":[{\"id\":\"regexp:.*\","
                + "\"activeFrom\":\"regexp:.*\",\"identificationNumber\":\"regexp:.*\","
                + "\"identificationType\":\"Passport\"}],\"languageRefs\":[{\"id\":\"regexp:.*\","
                + "\"languageCode\":\"ru\",\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"regexp:.*\","
                + "\"activeFrom\":\"regexp:.*\",\"familyName\":\"regexp:Genry.*\",\"givenName\":\"regexp:Jennifer"
                + ".*\"}}";
        String ar = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d8e\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef-4998-9974-93129df8eae6\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"ru\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry20190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identicalCout = compare.stream()
                .filter(diffMessage -> diffMessage.getResult() == IDENTICAL)
                .filter(diffMessage -> diffMessage.getDescription().equals("Result is changed due to inline-regexp "
                        + "checking."))
                .count();
        long notIdenticalCout = compare.stream().filter(diffMessage -> diffMessage.getResult() != IDENTICAL).count();
        assertEquals(identicalCout, 10);
        assertEquals(notIdenticalCout, 0);
    }

    @Test
    public void given_twoNotIdenticalObject_withoutParameters_resultListContainsOneDiffMessageTypeSimilarForFieldId() throws ComparatorException {
        String er = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d8e\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef-4998-9974-93129df8eae6\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"ru\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry20190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        String ar = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d8\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef-4998-9974-93129df8eae6\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"ru\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry20190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        JsonDiffMessage expectedDiffMessage = new JsonDiffMessage(1, "/id", "/id", SIMILAR,
                "Node values are different.", "$['id']", "$['id']");
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(1, compare.size());
        assertEquals(expectedDiffMessage, compare.get(0));
    }

    @Test
    public void given_twoNotIdenticalObject_withoutParameters_resultListContains2DiffMessageTypeSimilarForFields_IdAndFamilyName() throws ComparatorException {
        String er = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d8e\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef-4998-9974-93129df8eae6\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"ru\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry20190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        String ar = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d8\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef-4998-9974-93129df8eae6\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"ru\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry0190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        List<DiffMessage> diffMessages = new ArrayList<>();
        JsonDiffMessage expectedDiffMessage1 = new JsonDiffMessage(1, "/id", "/id", SIMILAR,
                "Node values are different.", "$['id']", "$['id']");
        JsonDiffMessage expectedDiffMessage2 = new JsonDiffMessage(2, "/individualName/familyName", "/individualName/familyName", SIMILAR,
                "Node values are different.", "$['individualName']['familyName']", "$['individualName']['familyName']");
        diffMessages.add(expectedDiffMessage2);
        diffMessages.add(expectedDiffMessage1);
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(2, compare.size());
        assertThat(compare, containsInAnyOrder(diffMessages.toArray()));
    }

    @Test
    public void given_twoEmptyIdenticalObject_withoutParameters_resultEmptyListDiffMessage() throws ComparatorException {
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare("{}", "{}", parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_twoEmptyIdenticalArray_withoutParameters_resultEmptyListDiffMessage() throws ComparatorException {
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare("[]", "[]", parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_ErIsObjectAndArIsArray_withoutParameters_resultListContainsOneDiffMessageTypeModified() throws ComparatorException {
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare("{}", "[]", parameters);
        assertEquals(1, compare.size());
        assertEquals(MODIFIED, compare.get(0).getResult());
        assertEquals("Nodes have different types.", compare.get(0).getDescription());
    }

    @Test
    public void given_twoIdenticalArrayWithEmptyObjects_withoutParameters_resultEmptyListDiffMessage() throws ComparatorException {
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare("[{},{}]", "[{},{}]", parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_notIdenticalObjectAndErIsNull_withoutParameters_resultListContainsOneDiffMessageTypeExtra() throws ComparatorException {
        String er = null;
        String ar = "{\"test\":\"op\"}";
        JsonDiffMessage expectedDiffMessage = new JsonDiffMessage(1, "", "/test", EXTRA,
                "ar has extra node(s).", "", "$['test']");
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(1, compare.size());
        assertEquals(expectedDiffMessage, compare.get(0));
    }

    @Test
    public void given_twoIdenticalArrayWithTextAround_withoutParameters_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "som text[{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}]after text";
        String ar = "before text [{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}] and another text ";
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_notIdenticalArrayAndErIncorrect_withoutParameters_resultComparatorException() throws ComparatorException {
        String er = "[{{,{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}]";
        String ar = "[{},{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}]";
        String exceptionMessage = "Error while parsing input message er. Probably it is not valid JSON. Unexpected "
                + "character ('{' (code 123)): was expecting double-quote to start field name\n"
                + " at [Source: (String)\"[{{,{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}]\"; line: 1, column: 4]";
        Parameters parameters = new Parameters();

        ComparatorException ex = Assertions.assertThrows(ComparatorException.class,
                () ->jsonComparator.compare(er, ar, parameters),
                "Expected ComparatorException.");

        assertEquals(exceptionMessage, ex.getMessage());
        assertEquals(20000, ex.getStatusCode());
    }

    @Test
    public void given_notIdenticalArrayAndArIncorrect_withoutParameters_resultComparatorException() throws ComparatorException {
        String er = "[{},{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}]";
        String ar = "[}},{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}]";
        String exceptionMessage = "Error while parsing input message ar. Probably it is not valid JSON. Unexpected "
                + "close marker '}': expected ']' (for Array starting at [Source: (String)\"[}},"
                + "{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}]\"; line: 1, column: 1])\n"
                + " at [Source: (String)\"[}},{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}]\"; line: 1, column: 3]";
        Parameters parameters = new Parameters();

        ComparatorException ex = Assertions.assertThrows(ComparatorException.class,
                () ->jsonComparator.compare(er, ar, parameters),
                "Expected ComparatorException.");

        assertEquals(exceptionMessage, ex.getMessage());
        assertEquals(20000, ex.getStatusCode());
    }

    @Test
    public void given_ErAndARContainPairObjectNotInArray_firstObjectInPairsIdentical_withoutParameters_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "{},{\"activeFrom\":\"2019-02-28T11:38:0_9.082Z\"}";
        String ar = "{},{\"activeFrom\":\"2019-02-28T11:38:09.082Z\"}";
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_identicalArraysWithDifferentOrder_withoutParameters_resultListContains16DiffMessage() throws ComparatorException {
        String er = "[{\"id\":\"1\",\"name\":\"Alex Peters\",\"status\":\"Active\",\"statusReason\":\"none\","
                + "\"validFor\":{\"startDateTime\":\"2018-10-24T07:42:30.455Z\"},\"customerCategoryId\":\"1\","
                + "\"creditProfiles\":[{\"creditProfileDate\":\"2018-10-08T15:20:03.177Z\",\"creditRiskRating\":0,"
                + "\"creditScore\":0}],\"customerId\":\"1\"},{\"id\":\"3\",\"name\":\"I am the first\","
                + "\"validFor\":{\"startDateTime\":\"2018-10-24T08:26:05.332Z\"},\"customerCategoryId\":\"string\","
                + "\"creditProfiles\":[{\"creditProfileDate\":\"2018-10-24T12:25:55.055Z\",\"creditRiskRating\":0,"
                + "\"creditScore\":0}],\"customerId\":\"3\"}]";
        String ar = "[{\"id\":\"3\",\"name\":\"I am the first\",\"validFor\":{\"startDateTime\":\"2018-10-24T08:26:05"
                + ".332Z\"},\"customerCategoryId\":\"string\","
                + "\"creditProfiles\":[{\"creditProfileDate\":\"2018-10-24T12:25:55.055Z\",\"creditRiskRating\":0,"
                + "\"creditScore\":0}],\"customerId\":\"3\"},{\"id\":\"1\",\"name\":\"Alex Peters\","
                + "\"status\":\"Active\",\"statusReason\":\"none\","
                + "\"validFor\":{\"startDateTime\":\"2018-10-24T07:42:30.455Z\"},\"customerCategoryId\":\"1\","
                + "\"creditProfiles\":[{\"creditProfileDate\":\"2018-10-08T15:20:03.177Z\",\"creditRiskRating\":0,"
                + "\"creditScore\":0}],\"customerId\":\"1\"}]";
        Parameters parameters = new Parameters();
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long similarCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == SIMILAR).count();
        long missedCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == MISSED).count();
        long extraCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == EXTRA).count();
        assertEquals(16, compare.size());
        assertEquals(2, missedCount);
        assertEquals(12, similarCount);
        assertEquals(2, extraCount);
    }

    @Test
    public void given_identicalArraysWithDifferentOrder_withParameterIgnoreArraysOrder_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "[{\"id\":\"1\",\"name\":\"Alex Peters\",\"status\":\"Active\",\"statusReason\":\"none\","
                + "\"validFor\":{\"startDateTime\":\"2018-10-24T07:42:30.455Z\"},\"customerCategoryId\":\"1\","
                + "\"creditProfiles\":[{\"creditProfileDate\":\"2018-10-08T15:20:03.177Z\",\"creditRiskRating\":0,"
                + "\"creditScore\":0}],\"customerId\":\"1\"},{\"id\":\"3\",\"name\":\"I am the first\","
                + "\"validFor\":{\"startDateTime\":\"2018-10-24T08:26:05.332Z\"},\"customerCategoryId\":\"string\","
                + "\"creditProfiles\":[{\"creditProfileDate\":\"2018-10-24T12:25:55.055Z\",\"creditRiskRating\":0,"
                + "\"creditScore\":0}],\"customerId\":\"3\"}]";
        String ar = "[{\"id\":\"3\",\"name\":\"I am the first\",\"validFor\":{\"startDateTime\":\"2018-10-24T08:26:05"
                + ".332Z\"},\"customerCategoryId\":\"string\","
                + "\"creditProfiles\":[{\"creditProfileDate\":\"2018-10-24T12:25:55.055Z\",\"creditRiskRating\":0,"
                + "\"creditScore\":0}],\"customerId\":\"3\"},{\"id\":\"1\",\"name\":\"Alex Peters\","
                + "\"status\":\"Active\",\"statusReason\":\"none\","
                + "\"validFor\":{\"startDateTime\":\"2018-10-24T07:42:30.455Z\"},\"customerCategoryId\":\"1\","
                + "\"creditProfiles\":[{\"creditProfileDate\":\"2018-10-08T15:20:03.177Z\",\"creditRiskRating\":0,"
                + "\"creditScore\":0}],\"customerId\":\"1\"}]";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_identicalArraysWithDifferentOrder_withParameterIgnoreArraysOrderHasIncorrectValue_resultListContains4DiffMessage() throws ComparatorException {
        String er = "{\"ARRAY\":[{\"id\":\"3\"},{\"id\":\"2\"},{\"id\":\"1\"}],"
                + "\"ARRAY1\":[{\"id\":\"6\"},{\"id\":\"5\"},{\"id\":\"4\"}]}";
        String ar = "{\"ARRAY\":[{\"id\":\"1\"},{\"id\":\"2\"},{\"id\":\"3\"}],"
                + "\"ARRAY1\":[{\"id\":\"4\"},{\"id\":\"5\"},{\"id\":\"6\"}]}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "incorrectValue");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(4, compare.size());
    }

    @Test
    public void given_identicalArraysWithDifferentOrder_withParameterIgnoreArraysOrderHasEmptyValue_resultListContains4DiffMessage() throws ComparatorException {
        String er = "{\"ARRAY\":[{\"id\":\"3\"},{\"id\":\"2\"},{\"id\":\"1\"}],"
                + "\"ARRAY1\":[{\"id\":\"6\"},{\"id\":\"5\"},{\"id\":\"4\"}]}";
        String ar = "{\"ARRAY\":[{\"id\":\"1\"},{\"id\":\"2\"},{\"id\":\"3\"}],"
                + "\"ARRAY1\":[{\"id\":\"4\"},{\"id\":\"5\"},{\"id\":\"6\"}]}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(4, compare.size());
    }

    @Test
    public void given_objectsWithOneIdenticalField_withParameterIgnorePropertiesAllFieldsExceptId_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "{\"id\":\"207\"}";
        String ar = "{\"id\":\"207\",\"activeFrom\":\"2019-01-11T08:55:50.727Z\","
                + "\"customerCategoryId\":\"987654123265\",\"name\":\"I am the first\",\"customerNumber\":\"207\","
                + "\"engagedPartyId\":\"12365236256\",\"creditProfiles\":[{\"id\":\"208\","
                + "\"creditProfileDate\":\"2019-01-11T12:27:42.042Z\",\"creditRiskRating\":0,\"creditScore\":0,"
                + "\"activeFrom\":\"2019-01-11T08:55:50.729Z\"}],\"contactMediums\":[{\"id\":\"209\","
                + "\"emailAddress\":\"string\",\"typeOfContact\":\"home\",\"typeOfContactMethod\":\"email\","
                + "\"preferredNotification\":false,\"preferredContact\":false,\"isActive\":true,\"refId\":\"207\","
                + "\"refType\":\"Customer\",\"extendedAttributes\":{}},{\"id\":\"210\",\"number\":\"$tc"
                + ".ramdomNumber\",\"typeOfContact\":\"home\",\"typeOfContactMethod\":\"fax\","
                + "\"preferredNotification\":false,\"preferredContact\":false,\"isActive\":true,\"refId\":\"207\","
                + "\"refType\":\"Customer\",\"extendedAttributes\":{}}],\"extendedAttributes\":{},"
                + "\"partyRoleAssociations\":[]}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreProperties", "/activeFrom");
        parameters.put("ignoreProperties", "/customerCategoryId");
        parameters.put("ignoreProperties", "/name");
        parameters.put("ignoreProperties", "/customerNumber");
        parameters.put("ignoreProperties", "/engagedPartyId");
        parameters.put("ignoreProperties", "/creditProfiles");
        parameters.put("ignoreProperties", "/contactMediums");
        parameters.put("ignoreProperties", "/extendedAttributes");
        parameters.put("ignoreProperties", "/partyRoleAssociations");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_notIdenticalObjectWithOneIdenticalFieldId_withParameterReadByPathForId_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "{\"id\":\"8d42dfef-78b7-447e-8a39-b1bbcf2170cb\", \"des\":\"testDesc\", "
                + "\"accs\":[{\"accId\":\"1\"},{\"accId\":\"33\"}]}";
        String ar = "{\"id\":\"8d42dfef-78b7-447e-8a39-b1bbcf2170cb\",\"activeFrom\":\"2019-02-28T11:40:10.459Z\","
                + "\"customerCategoryId\":\"putch string\",\"name\":\"I am the first\",\"customerNumber\":\"29\","
                + "\"engagedPartyId\":\"fe2b669f-d0eb-4ff4-84c0-765b56f4b5b8\","
                + "\"engagedPartyRefType\":\"Individual\",\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"accountRefs\":[{\"id\":\"f69522c7-f37b-4264-b791-38da98e926fc\",\"name\":\"New account name\","
                + "\"href\":\"updated href\",\"description\":\"Updated description\","
                + "\"activeFrom\":\"2019-02-28T11:40:10.481Z\",\"referredAccountType\":\"Account\","
                + "\"accountId\":\"2802201906341228\",\"refType\":\"Customer\","
                + "\"refId\":\"8d42dfef-78b7-447e-8a39-b1bbcf2170cb\"},"
                + "{\"id\":\"c69522c7-f37b-4264-b791-38da98e926fc\",\"name\":\"1New account name\","
                + "\"href\":\"1updated href\",\"description\":\"1Updated description\","
                + "\"activeFrom\":\"1019-02-28T11:40:10.481Z\",\"referredAccountType\":\"1Account\","
                + "\"accountId\":\"1802201906341228\",\"refType\":\"1Customer\","
                + "\"refId\":\"cd42dfef-78b7-447e-8a39-b1bbcf2170cb\"}],\"agreementRefs\":[]}";
        Parameters parameters = new Parameters();
        parameters.put("readByPath", "$.id");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void given_emptyErAndCorrectAR_withParameterValidateSchemaDifferentInAr_resultListContainsOneDiffMessage() throws ComparatorException {
        String er = "";
        String ar = "{\"name\":\"juoij\", \"id\":\"5656814651\"}";
        Parameters parameters = new Parameters();
        parameters.put("validateSchema", "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"number\"},"
                + "\"name\":{\"type\":\"string\"}}}");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        DiffMessage expectedDiffMessage1 = new DiffMessage()
                .setOrderId(1)
                .setExpected("")
                .setActual("/id")
                .setResult(MODIFIED)
                .setDescription("$.id: string found, number expected");
        assertEquals(1, compare.size());
        assertEquals(expectedDiffMessage1, compare.get(0));
    }

    @Test
    public void given_notIdenticalArrays_withParameterDiffSummaryTemplate_resultListContains2DiffMessageWithModifyDescription() throws ComparatorException {
        String er = "[{\"id\":\"3\"},{\"id\":\"2\"}]";
        String ar = "[{\"id\":\"1\"},{\"id\":\"555\"}]";
        Parameters parameters = new Parameters();
        parameters.put("diffSummaryTemplate", "Some custom template with all placeholders! Founded different on ar "
                + "path {ERPATH} current value is '{VALUE}'. Summary: {SUMMARY}");
        String expectedDescription = "Some custom template with all placeholders! Founded different on ar path %s "
                + "current value is '%s'. Summary: Node values are different.";
        JsonDiffMessage expectedDiffMessage1 = new JsonDiffMessage(1, "/0/id", "/0/id", SIMILAR,
                String.format(expectedDescription, "/0/id", "1"), "$[0]['id']", "$[0]['id']");
        JsonDiffMessage expectedDiffMessage2 = new JsonDiffMessage(2, "/1/id", "/1/id", SIMILAR,
                String.format(expectedDescription, "/1/id", "555"), "$[1]['id']", "$[1]['id']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(2, compare.size());
        assertThat(compare, containsInAnyOrder(expectedDiffMessage1, expectedDiffMessage2));
    }

    @Test
    public void given_ArAndErHaveOneIdenticalFieldAndArHaveExtraFields_withParameterIgnoreExtra_resultListContains2DiffMessageTypeIdentical() throws ComparatorException {
        String er = "{\"id\":\"444\"}";
        String ar = "{\"id\":\"444\", \"name\":\"Some Name\", \"Address\":\"Spb\"}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreExtra", "true");
        JsonDiffMessage expectedDiffMessage1 = new JsonDiffMessage(1, "", "/name", IDENTICAL,
                "ar has extra node(s).", "", "$['name']");
        JsonDiffMessage expectedDiffMessage2 = new JsonDiffMessage(2, "", "/Address", IDENTICAL,
                "ar has extra node(s).", "", "$['Address']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(2, compare.size());
        assertThat(compare, containsInAnyOrder(expectedDiffMessage1, expectedDiffMessage2));
    }

    @Test
    public void given_identicalArray_withParametersObjectPrimaryKeyAndIgnoreArraysOrder_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "[{\n"
                + "        \"id_obj_A1\": \"1\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"4\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"regexp:.*\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"regexp:.*\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"regexp:.*\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }, {\n"
                + "        \"id_obj_A1\": \"2\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"5\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"some_value_251\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"some_value_252\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"some_value_253\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }, {\n"
                + "        \"id_obj_A1\": \"3\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"6\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"regexp:some_value_.*\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"regexp:some_value_.*\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"regexp:some_value_.*\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }\n"
                + "]";
        String ar = "[{\n"
                + "        \"id_obj_A1\": \"1\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"4\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"some_value_141\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"some_value_142\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"some_value_143\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }, {\n"
                + "        \"id_obj_A1\": \"3\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"6\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"some_value_361\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"some_value_362\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"some_value_363\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }, {\n"
                + "        \"id_obj_A1\": \"2\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"5\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"some_value_253\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"some_value_252\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"some_value_251\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }\n"
                + "]\n";
        Parameters parameters = new Parameters();
        parameters.put("objectPrimaryKey", "/*/obj_L2/targtArray/id_obj_A2");
        parameters.put("objectPrimaryKey", "//id_obj_A1");
        parameters.put("ignoreArraysOrder", "true");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long notIdentCount = compare.stream().filter(diffMessage -> diffMessage.getResult() != IDENTICAL).count();
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(6, identCount);
        assertEquals(0, notIdentCount);
    }

    /**
     * Test for compare with parameter objectPrimaryKey. Parameter objectPrimaryKey has several keys:
     * key for field of object in array first level - /orderItems/orderSpecificationId
     * wrong keys - like /this/is/wrong/key/for
     * keys for object which is at the level secondary array - like
     * /orderItems/'*'/targetProductInstance/characteristic/name
     * Also test has parameters ignoreProperties and ignoreArraysOrder.
     * Objects for compare from customer.
     */
    @Test
    public void given_identicalArray_withParametersObjectPrimaryKeyAndIgnoreArraysOrderAndIgnoreProperties_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "{\"id\":\"regexp:.*\",\"state\":\"completed\",\"externalId\":\"regexp:.*\","
                + "\"orderDate\":\"regexp:.*\",\"completionDate\":\"regexp:.*\",\"orderItems\":[{\"id\":\"regexp:"
                + ".*\",\"state\":\"completed\",\"genId\":\"regexp:.*\",\"orderSpecificationId\":\"INTERNAL_SG1\","
                + "\"orderSpecificationVersion\":\"1.0\",\"singleton\":true},{\"id\":\"top-item-2\","
                + "\"action\":\"add\",\"state\":\"completed\",\"genId\":\"regexp:.*\","
                + "\"orderSpecificationId\":\"PRODUCT_B_NEW_P2S_SG1\",\"orderSpecificationVersion\":\"1.0\","
                + "\"specificationId\":\"PRODUCT_B\",\"relations\":[{\"type\":\"reliesOn\",\"id\":\"regexp:.*\"}],"
                + "\"orderId\":\"regexp:.*\",\"targetProductInstance\":{\"id\":\"regexp:.*\",\"genId\":\"regexp:.*\","
                + "\"href\":\"tmfAPI/product/mobile_line_product.org\",\"status\":\"Active\","
                + "\"productSpecification\":{\"id\":\"PRODUCT_B\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product B\"},"
                + "\"realizingService\":[{\"genId\":\"regexp:.*\",\"id\":\"regexp:.*\",\"role\":\"Role_G1_Y2\","
                + "\"serviceSpecificationId\":\"SERVICE_Y\",\"serviceGroupId\":\"SG1\"},{\"genId\":\"regexp:.*\","
                + "\"id\":\"regexp:.*\",\"role\":\"Role_G1_X2\",\"serviceSpecificationId\":\"SERVICE_X\","
                + "\"serviceGroupId\":\"SG1\"}],\"relatedParty\":[{\"genId\":\"regexp:.*\",\"id\":\"regexp:.*\","
                + "\"href\":\"tmfAPI/customer/11.org\",\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"regexp:.*\",\"name\":\"Forced Order Spec\","
                + "\"productSpecCharacteristicId\":\"B4\",\"value\":[\"PRODUCT_B_NEW_P2S_SG1\"]},{\"genId\":\"regexp:"
                + ".*\",\"name\":\"action char\",\"productSpecCharacteristicId\":\"B10\",\"value\":[\"add\"]}]},"
                + "\"sourceProductInstance\":{\"id\":\"regexp:.*\",\"genId\":\"regexp:.*\","
                + "\"href\":\"tmfAPI/product/mobile_line_product.org\",\"status\":\"Created\","
                + "\"productSpecification\":{\"id\":\"PRODUCT_B\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product B\"},"
                + "\"relatedParty\":[{\"genId\":\"regexp:.*\",\"id\":\"regexp:.*\",\"href\":\"tmfAPI/customer/11"
                + ".org\",\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"regexp:.*\",\"name\":\"Forced Order Spec\","
                + "\"productSpecCharacteristicId\":\"B4\",\"value\":[\"PRODUCT_B_NEW_P2S_SG1\"]},{\"genId\":\"regexp:"
                + ".*\",\"name\":\"action char\",\"productSpecCharacteristicId\":\"B10\",\"value\":[\"add\"]}]},"
                + "\"p2s_operation\":\"provide\"},{\"id\":\"top-item-1\",\"action\":\"add\",\"state\":\"completed\","
                + "\"genId\":\"regexp:.*\",\"orderSpecificationId\":\"PRODUCT_A_NEW_P2S_SG1\","
                + "\"orderSpecificationVersion\":\"1.1\",\"specificationId\":\"PRODUCT_A\","
                + "\"relations\":[{\"type\":\"reliesOn\",\"id\":\"regexp:.*\"}],\"orderId\":\"regexp:.*\","
                + "\"targetProductInstance\":{\"id\":\"regexp:.*\",\"genId\":\"regexp:.*\","
                + "\"href\":\"tmfAPI/product/mobile_line_product.org\",\"status\":\"Active\","
                + "\"productSpecification\":{\"id\":\"PRODUCT_A\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product A\"},"
                + "\"realizingService\":[{\"genId\":\"regexp:.*\",\"id\":\"regexp:.*\",\"role\":\"Role_G1_X2\","
                + "\"serviceSpecificationId\":\"SERVICE_X\",\"serviceGroupId\":\"SG1\"}],"
                + "\"relatedParty\":[{\"genId\":\"regexp:.*\",\"id\":\"regexp:.*\",\"href\":\"tmfAPI/customer/11"
                + ".org\",\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"regexp:.*\",\"name\":\"Forced Order Spec\","
                + "\"productSpecCharacteristicId\":\"A4\",\"value\":[\"PRODUCT_A_NEW_P2S_SG1\"]},{\"genId\":\"regexp:"
                + ".*\",\"name\":\"action char\",\"productSpecCharacteristicId\":\"A9\",\"value\":[\"add\"]}]},"
                + "\"sourceProductInstance\":{\"id\":\"regexp:.*\",\"genId\":\"regexp:.*\","
                + "\"href\":\"tmfAPI/product/mobile_line_product.org\",\"status\":\"Created\","
                + "\"productSpecification\":{\"id\":\"PRODUCT_A\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product A\"},"
                + "\"relatedParty\":[{\"genId\":\"regexp:.*\",\"id\":\"regexp:.*\",\"href\":\"tmfAPI/customer/11"
                + ".org\",\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"regexp:.*\",\"name\":\"Forced Order Spec\","
                + "\"productSpecCharacteristicId\":\"A4\",\"value\":[\"PRODUCT_A_NEW_P2S_SG1\"]},{\"genId\":\"regexp:"
                + ".*\",\"name\":\"action char\",\"productSpecCharacteristicId\":\"A9\",\"value\":[\"add\"]}]},"
                + "\"p2s_operation\":\"provide\"},{\"id\":\"top-item-3\",\"action\":\"add\",\"state\":\"completed\","
                + "\"genId\":\"regexp:.*\",\"orderSpecificationId\":\"PRODUCT_C_NEW_P2S_Singl\","
                + "\"orderSpecificationVersion\":\"1.0\",\"specificationId\":\"PRODUCT_C\","
                + "\"relations\":[{\"type\":\"reliesOn\",\"id\":\"regexp:.*\"}],\"orderId\":\"regexp:.*\","
                + "\"targetProductInstance\":{\"id\":\"regexp:.*\",\"genId\":\"regexp:.*\","
                + "\"href\":\"tmfAPI/product/mobile_line_product.org\",\"status\":\"Active\","
                + "\"productSpecification\":{\"id\":\"PRODUCT_C\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product C\"},"
                + "\"relatedParty\":[{\"genId\":\"regexp:.*\",\"id\":\"regexp:.*\",\"href\":\"tmfAPI/customer/11"
                + ".org\",\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"regexp:.*\",\"name\":\"C-CHAR_Number\","
                + "\"productSpecCharacteristicId\":\"C2\",\"value\":[\"5\"]},{\"genId\":\"regexp:.*\","
                + "\"name\":\"Forced Order Spec\",\"productSpecCharacteristicId\":\"C4\","
                + "\"value\":[\"PRODUCT_C_NEW_P2S_Singl\"]},{\"genId\":\"regexp:.*\",\"name\":\"action char\","
                + "\"productSpecCharacteristicId\":\"C5\",\"value\":[\"add\"]}]},"
                + "\"sourceProductInstance\":{\"id\":\"regexp:.*\",\"genId\":\"regexp:.*\","
                + "\"href\":\"tmfAPI/product/mobile_line_product.org\",\"status\":\"Created\","
                + "\"productSpecification\":{\"id\":\"PRODUCT_C\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product C\"},"
                + "\"relatedParty\":[{\"genId\":\"regexp:.*\",\"id\":\"regexp:.*\",\"href\":\"tmfAPI/customer/11"
                + ".org\",\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"regexp:.*\",\"name\":\"C-CHAR_Number\","
                + "\"productSpecCharacteristicId\":\"C2\",\"value\":[\"5\"]},{\"genId\":\"regexp:.*\","
                + "\"name\":\"Forced Order Spec\",\"productSpecCharacteristicId\":\"C4\","
                + "\"value\":[\"PRODUCT_C_NEW_P2S_Singl\"]},{\"genId\":\"regexp:.*\",\"name\":\"action char\","
                + "\"productSpecCharacteristicId\":\"C5\",\"value\":[\"add\"]}]},\"p2s_operation\":\"provide\"}],"
                + "\"relatedParty\":[{\"genId\":\"regexp:.*\",\"id\":\"11\",\"href\":\"tmfAPI/customer/11.org\","
                + "\"role\":\"customer\",\"@referredType\":\"Customer\"}],\"orderProcess\":{\"olmProcess\":\"regexp:"
                + ".*\",\"provisioningProcess\":\"regexp:.*\"}}";
        String ar = "{\"id\":\"0a800307-6cd0-16b2-816c-d2283804024d\",\"state\":\"completed\","
                + "\"externalId\":\"9154979254713698496_singleton_new\",\"orderDate\":\"2019-08-27 08:19:06\","
                + "\"completionDate\":\"2019-08-27 08:19:34\",\"orderItems\":[{\"id\":\"5d64e77eaa7c070025de6a6c\","
                + "\"state\":\"completed\",\"genId\":\"0a800307-6cd0-16b2-816c-d2284698026f\","
                + "\"orderSpecificationId\":\"INTERNAL_SG1\",\"orderSpecificationVersion\":\"1.0\","
                + "\"singleton\":true},{\"id\":\"top-item-1\",\"action\":\"add\",\"state\":\"completed\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d2283805024e\","
                + "\"orderSpecificationId\":\"PRODUCT_A_NEW_P2S_SG1\",\"orderSpecificationVersion\":\"1.1\","
                + "\"specificationId\":\"PRODUCT_A\",\"relations\":[{\"type\":\"reliesOn\","
                + "\"id\":\"5d64e77eaa7c070025de6a6c\"}],\"orderId\":\"0a800307-6cd0-16b2-816c-d2283804024d\","
                + "\"targetProductInstance\":{\"id\":\"e4035119d98e448d96b0983d\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d2283805024f\",\"href\":\"tmfAPI/product/mobile_line_product"
                + ".org\",\"status\":\"Active\",\"productSpecification\":{\"id\":\"PRODUCT_A\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product A\"},"
                + "\"realizingService\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d22893b60270\","
                + "\"id\":\"6f6e7279-cda1-4cc5-a408-e5dfa1dccc62\",\"role\":\"Role_G1_X2\","
                + "\"serviceSpecificationId\":\"SERVICE_X\",\"serviceGroupId\":\"SG1\"}],"
                + "\"relatedParty\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d22838050252\","
                + "\"id\":\"b2efbfb7-e042-4eff-8f76-3b9dd85d7961\",\"href\":\"tmfAPI/customer/11.org\","
                + "\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d22838050250\",\"name\":\"Forced Order "
                + "Spec\",\"productSpecCharacteristicId\":\"A4\",\"value\":[\"PRODUCT_A_NEW_P2S_SG1\"]},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d22838050251\",\"name\":\"action char\","
                + "\"productSpecCharacteristicId\":\"A9\",\"value\":[\"add\"]}]},"
                + "\"sourceProductInstance\":{\"id\":\"e4035119d98e448d96b0983d\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d2283b09025f\",\"href\":\"tmfAPI/product/mobile_line_product"
                + ".org\",\"status\":\"Created\",\"productSpecification\":{\"id\":\"PRODUCT_A\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product A\"},"
                + "\"relatedParty\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283b0a0262\","
                + "\"id\":\"b2efbfb7-e042-4eff-8f76-3b9dd85d7961\",\"href\":\"tmfAPI/customer/11.org\","
                + "\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283b0a0260\",\"name\":\"Forced Order "
                + "Spec\",\"productSpecCharacteristicId\":\"A4\",\"value\":[\"PRODUCT_A_NEW_P2S_SG1\"]},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d2283b0a0261\",\"name\":\"action char\","
                + "\"productSpecCharacteristicId\":\"A9\",\"value\":[\"add\"]}]},\"p2s_operation\":\"provide\"},"
                + "{\"id\":\"top-item-3\",\"action\":\"add\",\"state\":\"completed\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d22838060258\","
                + "\"orderSpecificationId\":\"PRODUCT_C_NEW_P2S_Singl\",\"orderSpecificationVersion\":\"1.0\","
                + "\"specificationId\":\"PRODUCT_C\",\"relations\":[{\"type\":\"reliesOn\","
                + "\"id\":\"5d64e77eaa7c070025de6a6c\"}],\"orderId\":\"0a800307-6cd0-16b2-816c-d2283804024d\","
                + "\"targetProductInstance\":{\"id\":\"208a1ac1197046adad6acebb\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d22838060259\",\"href\":\"tmfAPI/product/mobile_line_product"
                + ".org\",\"status\":\"Active\",\"productSpecification\":{\"id\":\"PRODUCT_C\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product C\"},"
                + "\"relatedParty\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283806025d\","
                + "\"id\":\"b2efbfb7-e042-4eff-8f76-3b9dd85d7961\",\"href\":\"tmfAPI/customer/11.org\","
                + "\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283806025a\","
                + "\"name\":\"C-CHAR_Number\",\"productSpecCharacteristicId\":\"C2\",\"value\":[\"5\"]},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d2283806025b\",\"name\":\"Forced Order Spec\","
                + "\"productSpecCharacteristicId\":\"C4\",\"value\":[\"PRODUCT_C_NEW_P2S_Singl\"]},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d2283806025c\",\"name\":\"action char\","
                + "\"productSpecCharacteristicId\":\"C5\",\"value\":[\"add\"]}]},"
                + "\"sourceProductInstance\":{\"id\":\"208a1ac1197046adad6acebb\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d2283ba70263\",\"href\":\"tmfAPI/product/mobile_line_product"
                + ".org\",\"status\":\"Created\",\"productSpecification\":{\"id\":\"PRODUCT_C\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product C\"},"
                + "\"relatedParty\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283ba70267\","
                + "\"id\":\"b2efbfb7-e042-4eff-8f76-3b9dd85d7961\",\"href\":\"tmfAPI/customer/11.org\","
                + "\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283ba70264\","
                + "\"name\":\"C-CHAR_Number\",\"productSpecCharacteristicId\":\"C2\",\"value\":[\"5\"]},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d2283ba70265\",\"name\":\"Forced Order Spec\","
                + "\"productSpecCharacteristicId\":\"C4\",\"value\":[\"PRODUCT_C_NEW_P2S_Singl\"]},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d2283ba70266\",\"name\":\"action char\","
                + "\"productSpecCharacteristicId\":\"C5\",\"value\":[\"add\"]}]},\"p2s_operation\":\"provide\"},"
                + "{\"id\":\"top-item-2\",\"action\":\"add\",\"state\":\"completed\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d22838050253\","
                + "\"orderSpecificationId\":\"PRODUCT_B_NEW_P2S_SG1\",\"orderSpecificationVersion\":\"1.0\","
                + "\"specificationId\":\"PRODUCT_B\",\"relations\":[{\"type\":\"reliesOn\","
                + "\"id\":\"5d64e77eaa7c070025de6a6c\"}],\"orderId\":\"0a800307-6cd0-16b2-816c-d2283804024d\","
                + "\"targetProductInstance\":{\"id\":\"bf28f1529d4b4aa3a01c9e9f\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d22838050254\",\"href\":\"tmfAPI/product/mobile_line_product"
                + ".org\",\"status\":\"Active\",\"productSpecification\":{\"id\":\"PRODUCT_B\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product B\"},"
                + "\"realizingService\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d22893b70271\","
                + "\"id\":\"79511222-900a-4f1c-9187-75b53e8252db\",\"role\":\"Role_G1_X2\","
                + "\"serviceSpecificationId\":\"SERVICE_X\",\"serviceGroupId\":\"SG1\"},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d22893b70272\","
                + "\"id\":\"74214194-6855-49e8-9ae9-213392dc1a71\",\"role\":\"Role_G1_Y2\","
                + "\"serviceSpecificationId\":\"SERVICE_Y\",\"serviceGroupId\":\"SG1\"}],"
                + "\"relatedParty\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d22838060257\","
                + "\"id\":\"b2efbfb7-e042-4eff-8f76-3b9dd85d7961\",\"href\":\"tmfAPI/customer/11.org\","
                + "\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d22838050255\",\"name\":\"Forced Order "
                + "Spec\",\"productSpecCharacteristicId\":\"B4\",\"value\":[\"PRODUCT_B_NEW_P2S_SG1\"]},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d22838050256\",\"name\":\"action char\","
                + "\"productSpecCharacteristicId\":\"B10\",\"value\":[\"add\"]}]},"
                + "\"sourceProductInstance\":{\"id\":\"bf28f1529d4b4aa3a01c9e9f\","
                + "\"genId\":\"0a800307-6cd0-16b2-816c-d2283dd40268\",\"href\":\"tmfAPI/product/mobile_line_product"
                + ".org\",\"status\":\"Created\",\"productSpecification\":{\"id\":\"PRODUCT_B\","
                + "\"href\":\"tmfAPI/productSpecification/mobile_line_product.org\",\"name\":\"Product B\"},"
                + "\"relatedParty\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283dd4026b\","
                + "\"id\":\"b2efbfb7-e042-4eff-8f76-3b9dd85d7961\",\"href\":\"tmfAPI/customer/11.org\","
                + "\"role\":\"Customer\",\"@referredType\":\"Customer\"}],"
                + "\"characteristic\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283dd40269\",\"name\":\"Forced Order "
                + "Spec\",\"productSpecCharacteristicId\":\"B4\",\"value\":[\"PRODUCT_B_NEW_P2S_SG1\"]},"
                + "{\"genId\":\"0a800307-6cd0-16b2-816c-d2283dd4026a\",\"name\":\"action char\","
                + "\"productSpecCharacteristicId\":\"B10\",\"value\":[\"add\"]}]},\"p2s_operation\":\"provide\"}],"
                + "\"relatedParty\":[{\"genId\":\"0a800307-6cd0-16b2-816c-d2283806025e\",\"id\":\"11\","
                + "\"href\":\"tmfAPI/customer/11.org\",\"role\":\"customer\",\"@referredType\":\"Customer\"}],"
                + "\"orderProcess\":{\"olmProcess\":\"56a948ff-c8a3-11e9-b1b7-0a580a821769\","
                + "\"provisioningProcess\":\"58c1a1e5-c8a3-11e9-b1b7-0a580a821769\"}}";
        Parameters parameters = new Parameters();
        parameters.put("objectPrimaryKey", "/this/is/wrong/key/for/test/test");
        parameters.put("objectPrimaryKey", "/this/is/wrong/key/for");
        parameters.put("objectPrimaryKey", "/this/10/is/wrong/key");
        parameters.put("objectPrimaryKey", "/this/*/is/wrong/key");
        parameters.put("objectPrimaryKey", "/orderItems/orderSpecificationId");
        parameters.put("objectPrimaryKey", "/orderItems/*/targetProductInstance/characteristic/name");
        parameters.put("objectPrimaryKey", "/orderItems/*/targetProductInstance/realizingService"
                + "/serviceSpecificationId");
        parameters.put("objectPrimaryKey", "/orderItems/*/sourceProductInstance/realizingService"
                + "/serviceSpecificationId");
        parameters.put("objectPrimaryKey", "/orderItems/*/sourceProductInstance/characteristic/name");
        parameters.put("ignoreProperties", "/role");
        parameters.put("ignoreArraysOrder", "true");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long notIdentCount = compare.stream().filter(diffMessage -> diffMessage.getResult() != IDENTICAL).count();
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(62, identCount);
        assertEquals(0, notIdentCount);
    }

    @Test
    public void compareTwoJson_withoutRules_expectedResultIdentical() throws ComparatorException {
        String ear = "{\"json\": true}";
        Parameters parameters = new Parameters();
        List<DiffMessage> compareResult = jsonComparator.compare(ear, ear, parameters);
        assertEquals(0, compareResult.size());
    }

    @Test
    public void compareTwoJson_withoutRules_expectedResultSimilar() throws ComparatorException {
        String er = "{\"json\": true}";
        String ar = "{\"json\": false}";
        Parameters parameters = new Parameters();
        List<DiffMessage> compareResult = jsonComparator.compare(er, ar, parameters);
        assertEquals(1, compareResult.size());
        assertTrue(compareResult.stream().map(DiffMessage::getResult)
                .anyMatch(resultType -> resultType == ResultType.SIMILAR));
    }

    @Test
    public void compareTwoJson_withoutRules_expectedResultModified() throws ComparatorException {
        String er = "{\"json\": true}";
        String ar = "{\"json\": \"false\"}";
        Parameters parameters = new Parameters();
        List<DiffMessage> compareResult = jsonComparator.compare(er, ar, parameters);
        assertEquals(1, compareResult.size());
        assertTrue(compareResult.stream().map(DiffMessage::getResult)
                .anyMatch(resultType -> resultType == ResultType.MODIFIED));
    }

    @Test
    public void compareTwoJson_withoutRules_expectedResultMissed() throws ComparatorException {
        String er = "{\"json\": true}";
        String ar = "{}";
        Parameters parameters = new Parameters();
        List<DiffMessage> compareResult = jsonComparator.compare(er, ar, parameters);
        assertEquals(1, compareResult.size());
        assertTrue(compareResult.stream().map(DiffMessage::getResult)
                .anyMatch(resultType -> resultType == MISSED));
    }

    @Test
    public void compareTwoJson_withoutRules_expectedResultExtra() throws ComparatorException {
        String er = "{}";
        String ar = "{\"json\": false}";
        Parameters parameters = new Parameters();
        List<DiffMessage> compareResult = jsonComparator.compare(er, ar, parameters);
        assertEquals(1, compareResult.size());
        assertTrue(compareResult.stream().map(DiffMessage::getResult)
                .anyMatch(resultType -> resultType == ResultType.EXTRA));
    }

    @Test
    public void compareTwoJson_withSaveDiffValue_hasExpectedAndActualValues() throws ComparatorException {
        String er = "{\"json\": {\"param\":3}}";
        String ar = "{\"json\": {\"param\":2}}";
        Parameters parameters = new Parameters();
        parameters.put(JsonComparatorParameters.PARAMETER_SAVE_DIFF_VALUE.getParameterName(), "true");

        List<DiffMessage> compareResult = jsonComparator.compare(er, ar, parameters);

        assertEquals(1, compareResult.size());
        assertTrue(compareResult.stream().map(DiffMessage::getExpectedValue)
                .anyMatch(value -> "3".equals(value)));
        assertTrue(compareResult.stream().map(DiffMessage::getActualValue)
                .anyMatch(value -> "2".equals(value)));
    }

    @Test
    public void compareTwoJson_withRuleValidateSchema_expectedResultIdentical() throws ComparatorException {
        String er = "{\"id\": \"123456\"}";
        String ar = "[{\"id\": false}]";
        Parameters parameters = new Parameters();
        parameters.put("validateSchema", "{\"required\":[\"id\"]}");
        List<DiffMessage> compareResult = jsonComparator.compare(er, ar, parameters);
    }

    @Test
    public void compareTwoJsonArray_withRuleValidateSchema_expectedNullSizeCompareResultList() throws ComparatorException {
        String er = "[{\"id\":\"df\"}]";
        String ar = "[{\"id\":\"juoij\", \"name\":\"5656814651\"},{\"id\":\"lkjnj\", \"name\":\"5656814651\"}]";
        Parameters parameters = new Parameters();
        parameters.put("validateSchema", "{\"required\":[\"id\"]}");
        List<DiffMessage> compareResult = jsonComparator.compare(er, ar, parameters);
        assertEquals(0, compareResult.size());
    }

    @Test
    public void compareTwoJsonArray_withRuleValidateSchema_expectedOneInCompareResultListSize() throws ComparatorException {
        String er = "[{\"id\":\"df\"}]";
        String ar = "[{\"id\":\"juoij\", \"name\":\"5656814651\"},{\"idr\":\"lkjnj\", \"name\":\"5656814651\"}]";
        Parameters parameters = new Parameters();
        parameters.put("validateSchema","{\n" +
                "\"type\":\"array\",\n" +
                "\"items\":{\n" +
                "\"required\":[\"id\"]\n" +
                "}\n" +
                "}");
        List<DiffMessage> compareResult = jsonComparator.compare(er, ar, parameters);
        assertEquals(1, compareResult.size());
    }

    @Test
    public void compareTwoJsonArray_withRuleValidateSchema_ComplexSchema() throws ComparatorException {
        String er="";
        String ar="[\n" +
                "\t{\n" +
                "\t\t\"id\": \"95f5561a-49f2-4e92-ab90-dae64cf88fb8\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/95f5561a-49f2-4e92-ab90-dae64cf88fb8\",\n" +
                "\t\t\"chargingType\": \"PREPAID\",\n" +
                "\t\t\"isTopOffer\": true\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"85524d58-d542-4f6e-a0aa-316cde875ed9\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/85524d58-d542-4f6e-a0aa-316cde875ed9\",\n" +
                "\t\t\"chargingType\": \"PREPAID\",\n" +
                "\t\t\"isTopOffer\": true\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"27249f87-d6c5-4bd9-9c92-4ab729ed65f8\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/27249f87-d6c5-4bd9-9c92-4ab729ed65f8\",\n" +
                "\t\t\"chargingType\": \"PREPAID\",\n" +
                "\t\t\"isTopOffer\": true\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"3143813f-b0a1-4349-a0d7-3a3b1791eece\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/3143813f-b0a1-4349-a0d7-3a3b1791eece\",\n" +
                "\t\t\"isTopOffer\": true\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"7311268b-4b99-441c-a9c3-445c08dd1470\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/7311268b-4b99-441c-a9c3-445c08dd1470\",\n" +
                "\t\t\"chargingType\": \"PREPAID\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"97707c4f-b480-48d7-bf99-2ac6ac9a5f43\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/97707c4f-b480-48d7-bf99-2ac6ac9a5f43\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"84f3eb73-3a48-4a9c-8044-1e8c08ba4cfb\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/84f3eb73-3a48-4a9c-8044-1e8c08ba4cfb\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"d2359a02-5638-40a9-9d06-1364b8a3c3b0\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/d2359a02-5638-40a9-9d06-1364b8a3c3b0\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"7a8f5d8f-9ac2-4f18-8aeb-0c0578855b11\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/7a8f5d8f-9ac2-4f18-8aeb-0c0578855b11\",\n" +
                "\t\t\"chargingType\": \"PREPAID\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"be3d7069-7945-44cd-bfbd-142407a9e797\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/be3d7069-7945-44cd-bfbd-142407a9e797\",\n" +
                "\t\t\"isTopOffer\": true\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"9eba7d2b-d701-459b-bc19-90fd438e6f4c\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/9eba7d2b-d701-459b-bc19-90fd438e6f4c\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"512eeb67-2ba3-4752-ab04-76b41ebcfc63\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/512eeb67-2ba3-4752-ab04-76b41ebcfc63\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"f946b585-107f-4ff4-8597-bb4e0496dd71\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/f946b585-107f-4ff4-8597-bb4e0496dd71\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"30b611de-8ee5-4341-bd81-f5a26991bb02\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/30b611de-8ee5-4341-bd81-f5a26991bb02\",\n" +
                "\t\t\"chargingType\": \"PREPAID\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"f2c2fee0-27d5-4c7b-84b3-f4c9c0f79883\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/f2c2fee0-27d5-4c7b-84b3-f4c9c0f79883\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"071d92c1-720d-4a48-ba91-1cec86e8aa58\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/071d92c1-720d-4a48-ba91-1cec86e8aa58\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"0f568717-c8cf-4873-83be-c6b3d634ba5b\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/0f568717-c8cf-4873-83be-c6b3d634ba5b\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"4d342d86-ebba-4ad8-b90c-fb2f7a0ce832\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/4d342d86-ebba-4ad8-b90c-fb2f7a0ce832\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"a22eecfa-6e52-42d7-92b0-6609727f54a9\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/a22eecfa-6e52-42d7-92b0-6609727f54a9\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"3b601c9e-ffd5-42ff-8464-f04eea2ce54a\",\n" +
                "\t\t\"href\": \"https://public-gateway-gke-qa.project-gke-cluster1.openshift.sdntest.qubership.org/catalog-integration-tmf/catalogManagement/v2/productOffering/3b601c9e-ffd5-42ff-8464-f04eea2ce54a\",\n" +
                "\t\t\"isTopOffer\": false\n" +
                "\t}\n" +
                "]";
        Parameters parameters = new Parameters();
        parameters.put("validateSchema", "{\n" +
                "\t\"$schema\": \"http://json-schema.org/draft-07/schema\",\n" +
                "\t\"$id\": \"http://example.com/example.json\",\n" +
                "\t\"type\": \"array\",\n" +
                "\t\"additionalItems\": true,\n" +
                "\t\"items\": {\n" +
                "\t\t\"$id\": \"#/items\",\n" +
                "\t\t\"anyOf\": [{\n" +
                "\t\t\t\t\"$id\": \"#/items/anyOf\",\n" +
                "\t\t\t\t\"type\": \"object\",\n" +
                "\t\t\t\t\"required\": [\n" +
                "\t\t\t\t\t\"id\"\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"properties\": {\n" +
                "\t\t\t\t\t\"id\": {\n" +
                "\t\t\t\t\t\t\"$id\": \"#/items/anyOf/properties/id\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"href\": {\n" +
                "\t\t\t\t\t\t\"$id\": \"#/items/anyOf/properties/href\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"chargingType\": {\n" +
                "\t\t\t\t\t\t\"$id\": \"#/items/anyOf/properties/chargingType\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"isTopOffer\": {\n" +
                "\t\t\t\t\t\t\"$id\": \"#/items/anyOf/properties/isTopOffer\",\n" +
                "\t\t\t\t\t\t\"type\": \"boolean\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"version\": {\n" +
                "\t\t\t\t\t\t\"$id\": \"#/items/anyOf/properties/version\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t}\t\t\t\t\t\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"additionalProperties\": false\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}\n" +
                "}");
        List<DiffMessage> result = jsonComparator.compare(er,ar,parameters);
        assertEquals(new ArrayList<>(),result);

    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_SIMILARtoIDENTICAL() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        Parameters parameters = new Parameters();
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=$[*]['start_dtm']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(1, compare.size());
        assertEquals(1, identCount);
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_SIMILARtoMODIFIED() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        Parameters parameters = new Parameters();

        parameters.put("changeDiffResultJson", "SIMILAR=MODIFIED=$[*]['start_dtm']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long notIdentCount = compare.stream().filter(diffMessage -> diffMessage.getResult() != MODIFIED).count();
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == MODIFIED).count();
        assertEquals(1, compare.size());
        assertEquals(1, identCount);
        assertEquals(0, notIdentCount);
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_SIMILARtoIDENTICAL_WithThreeDiffs() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129444,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031677\"\n" +
                "}]";
        Parameters parameters = new Parameters();
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=$[*]['start_dtm']");
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=$[*]['domain_id']");
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=$[*]['discount_ref']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long notIdentCount = compare.stream().filter(diffMessage -> diffMessage.getResult() != IDENTICAL).count();
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(3, identCount);
        assertEquals(0, notIdentCount);
        assertEquals(3, compare.size());
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_EXTRAtoIDENTICAL() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129444,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"start_vtm\" : 1548288888888,\n" +
                "  \"domain_id\" : 732147129444,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}]";
        Parameters parameters = new Parameters();

        parameters.put("changeDiffResultJson", "EXTRA=IDENTICAL=$[*]['start_vtm']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long notIdentCount = compare.stream().filter(diffMessage -> diffMessage.getResult() != IDENTICAL).count();
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(1, identCount);
        assertEquals(0, notIdentCount);
        assertEquals(1, compare.size());
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_MISSEDtoIDENTICAL() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        Parameters parameters = new Parameters();

        parameters.put("changeDiffResultJson", "MISSED=IDENTICAL=$[*]['domain_id']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(1, identCount);
        assertEquals(1, compare.size());
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_withDifferntNesting() throws ComparatorException {
        String er = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d844e\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef-4998-9974-93129df8eae6\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"ru\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry20190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        String ar = "{\"id\":\"161de1c4-a8d0-4865-99f2-1403fbe06d8e\",\"activeFrom\":\"2019-02-28T11:38:09.066Z\","
                + "\"contactMediums\":[],\"partyRoleAssociations\":[],"
                + "\"individualIdentifications\":[{\"id\":\"63d36e37-5bef\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.332Z\",\"identificationNumber\":\"20190228063403580\","
                + "\"identificationType\":\"Passport\"}],"
                + "\"languageRefs\":[{\"id\":\"57ecc269-1daf-4aa0-a4ad-99612df29191\",\"languageCode\":\"eng\","
                + "\"role\":\"Speaking\"}],\"individualName\":{\"id\":\"6591f90b-e870-4f7f-bb25-bd8a80a1d38b\","
                + "\"activeFrom\":\"2019-02-28T11:38:09.082Z\",\"familyName\":\"Genry20190228063403617\","
                + "\"givenName\":\"Jennifer20190228063403569\"}}";
        Parameters parameters = new Parameters();

        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=$['individualIdentifications'][*]['id']");
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=$['languageRefs'][*]['languageCode']");
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=.id");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(3, compare.size());
        assertEquals(3, identCount);
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_withIncorrectResult() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000111,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        Parameters parameters = new Parameters();
        parameters.put("changeDiffResultJson", "MISSED=IDENTICAL=$[*][start_dtm]");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() != MODIFIED).count();
        assertEquals(1, compare.size());
        assertEquals(1, identCount);
    }

    @Test()
    public void compareTwoJson_withRuleChangeDiffResult_EmptyPath() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000111,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        Parameters parameters = new Parameters();
        parameters.put("changeDiffResultJson", " ");
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=$[*]['start_dtm']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(1, compare.size());
        assertEquals(1, identCount);
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_withIncorrectResultWithPoint() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000111,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        Parameters parameters = new Parameters();
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=.");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == SIMILAR).count();
        assertEquals(1, compare.size());
        assertEquals(1, identCount);
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_withEmptyResult() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        Parameters parameters = new Parameters();

        parameters.put("changeDiffResultJson", "MISSED=$[*]['domain_id']");
        parameters.put("changeDiffResultJson", "MISSED=IDENTICAL=$[*]['domain_id']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(1, compare.size());
        assertEquals(1, identCount);
    }

    @Test
    public void compareTwoJson_withRuleChangeDiffResult_withIncorrectPath() throws ComparatorException {
        String er = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String ar = "[{\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000000223232,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        Parameters parameters = new Parameters();
        parameters.put("changeDiffResultJson", "MISSED=IDENTICAL=====$[*]['start_dtm']");
        parameters.put("changeDiffResultJson", "SIMILAR=IDENTICAL=$[*]['start_dtm']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(1, compare.size());
        assertEquals(1, identCount);
    }

    @Test
    public void compareTwoJson_withRules_objectPrimaryKeyAndIgnoreOrder() throws ComparatorException {
        String input1 = "[ {\n" +
                "  \"event_discount_id\" : 1550,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 1,\n" +
                "  \"start_dtm\" : 1548288000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}, {\n" +
                "  \"event_discount_id\" : 1544,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 1,\n" +
                "  \"start_dtm\" : 1548288000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}, {\n" +
                "  \"event_discount_id\" : 746,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 1,\n" +
                "  \"start_dtm\" : 1548288000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}, {\n" +
                "  \"event_discount_id\" : 704,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 1,\n" +
                "  \"start_dtm\" : 1548288000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "},  {\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548288000000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}  ]";
        String input2 = "[ {\n" +
                "  \"event_discount_id\" : 704,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 1,\n" +
                "  \"start_dtm\" : 1548346621000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}, {\n" +
                "  \"event_discount_id\" : 746,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 1,\n" +
                "  \"start_dtm\" : 1548346621000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}, {\n" +
                "  \"event_discount_id\" : 1550,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 1,\n" +
                "  \"start_dtm\" : 1548346621000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "}, {\n" +
                "  \"event_discount_id\" : 1121,\n" +
                "  \"customer_ref\" : \"CA-XEnk7S3B20CNYEE8\",\n" +
                "  \"product_seq\" : 3,\n" +
                "  \"start_dtm\" : 1548346704000,\n" +
                "  \"domain_id\" : 732147129,\n" +
                "  \"account_num\" : \"BA-XEnk7S3B20CNYEE9\",\n" +
                "  \"invoicing_co_id\" : 2,\n" +
                "  \"aggregation_level\" : 8,\n" +
                "  \"currency_code\" : \"EUR\",\n" +
                "  \"discount_ref\" : \"321548346605031\"\n" +
                "} ]";
        Parameters parameters = new Parameters();
        parameters.put("objectPrimaryKey", "//event_discount_id");
        parameters.put("ignoreArraysOrder", "true");
        List<DiffMessage> compare = jsonComparator.compare(input1, input2, parameters);
    }

    @Test
    public void compareTwoJson_withRules_objectPrimaryKeyAndIgnoreOrder_twoPKeysInOneObject_ecspectedIdentical() throws ComparatorException {
        int expectedListSize = 0;
        String er = "[{\n" +
                "\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\"BALANCE\": 0,\n" +
                "\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t}, {\n" +
                "\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\"BALANCE\": 1000,\n" +
                "\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t}, {\n" +
                "\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t}]";
        String ar = "[{\n" +
                "\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\"BALANCE\": 0,\n" +
                "\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t}, {\n" +
                "\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\"BALANCE\": 1000,\n" +
                "\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t}, {\n" +
                "\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t}]\n";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKey", "//DISCOUNT_NAME");
        parameters.put("objectPrimaryKey", "//PRODUCT_SEQ");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(expectedListSize, compare.size());
    }

    @Test
    public void compareTwoJson_withRules_objectPrimaryKeyAndIgnoreOrder_twoPKeysInObjects_expectedIdentical() throws ComparatorException {
        int expectedListSize = 0;
        String er = "[{\n" +
                "\t\t\"Id\": 2,\n" +
                "\t\t\"Array\": [{\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 0,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1000,\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}, {\n" +
                "\t\t\"Id\": 1,\n" +
                "\t\t\"Array\": [{\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 0,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1000,\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}\n" +
                "]\n";
        String ar = "[{\n" +
                "\t\t\"Id\": 2,\n" +
                "\t\t\"Array\": [{\n" +
                "\t\t\t\t\"BALANCE\": 0,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1000,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}, {\n" +
                "\t\t\"Id\": 1,\n" +
                "\t\t\"Array\": [{\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 0,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1000,\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}\n" +
                "]\n";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKey", "//Id");
        parameters.put("objectPrimaryKey", "//Array/DISCOUNT_NAME");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(expectedListSize, compare.size());
    }

    @Test
    public void compareTwoJson_withRules_objectPrimaryKeyAndIgnoreOrder_oneKeyWithPath_expectedIdentical() throws ComparatorException {
        int expectedListSize = 0;
        String er = "{\n" +
                "\t\t\"Id\": 2,\n" +
                "\t\t\"Array\": [{\n" +
                "\t\t\t\t\"BALANCE\": 0,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1000,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}";
        String ar = "{\n" +
                "\t\t\"Id\": 2,\n" +
                "\t\t\"Array\": [{\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 0,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1000,\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKey", "/id/DISCOUNT_NAME");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(expectedListSize, compare.size());
    }

    @Test
    public void compareTwoJson_withRules_objectPrimaryKeyAndIgnoreOrder_oneKeyWithOneSlash_expectedIdentical() throws ComparatorException {
        int expectedListSize = 0;
        String er = "{\n" +
                "\t\t\"Id\": 2,\n" +
                "\t\t\"Array\": [{\n" +
                "\t\t\t\t\"BALANCE\": 0,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1000,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}";
        String ar = "{\n" +
                "\t\t\"Id\": 2,\n" +
                "\t\t\"Array\": [{\n" +
                "\t\t\t\t\"BALANCE\": 0,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10009,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"3600\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"1800000000\",\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Voice Allowance\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU Data Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1073741824,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10012,\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\",\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\"\n" +
                "\t\t\t}, {\n" +
                "\t\t\t\t\"DISCOUNT_NAME\": \"Reload National EU SMS Allowance\",\n" +
                "\t\t\t\t\"BALANCE\": 1000,\n" +
                "\t\t\t\t\"EVENT_DISCOUNT_ID\": 10011,\n" +
                "\t\t\t\t\"PRODUCT_SEQ\": \"1\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_MONEY\": \"0\",\n" +
                "\t\t\t\t\"CUSTOMERREF\": \"CUST922948950\",\n" +
                "\t\t\t\t\"TOTAL_DISCOUNTED_USAGE\": \"0\",\n" +
                "\t\t\t\t\"PERIOD_START_DTM\": \"2019-10-15 20:03:34\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKey", "/DISCOUNT_NAME");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(expectedListSize, compare.size());
    }

    @Test
    public void compareTwoJson_withRules_ignoreOrderAndObjectPrimaryKeyV2_ignoreProperties_expectedIdentical() throws ComparatorException {
        int expectedDiffsCount = 0;
        String er = "{\n" +
                "\t\"parentArray\": [{\n" +
                "\t\t\"fakeId\": \"1\",\n" +
                "\t\t\"objectName\": \"aaaaa\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"A\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"2\",\n" +
                "\t\t\"objectName\": \"bbbbb\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"B\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"3\",\n" +
                "\t\t\"objectName\": \"ccccc\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"C\"\n" +
                "\t\t}\n" +
                "\t}]\n" +
                "}";
        String ar = "{\n" +
                "\t\"parentArray\": [{\n" +
                "\t\t\"fakeId\": \"4\",\n" +
                "\t\t\"objectName\": \"ccccc\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"C\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"5\",\n" +
                "\t\t\"objectName\": \"bbbbb\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"B\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"6\",\n" +
                "\t\t\"objectName\": \"aaaaa\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"A\"\n" +
                "\t\t}\n" +
                "\t}]\n" +
                "}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreProperties", "/fakeId");
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKeyV2", "/parentArray[childObject/realId]");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(expectedDiffsCount, compare.size());
    }

    @Test
    public void compareTwoArrays_withRules_ignoreOrderAndObjectPrimaryKeyV2_PKinParentObject_expectedIdentical() throws ComparatorException {
        int expectedDiffsCount = 0;
        String er = "[\n" +
                "\t{\n" +
                "\t\t\"fakeId\": \"1\",\n" +
                "\t\t\"objectName\": \"aaaaa\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"A\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"fakeId\": \"2\",\n" +
                "\t\t\"objectName\": \"bbbbb\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"B\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"fakeId\": \"3\",\n" +
                "\t\t\"objectName\": \"ccccc\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"C\"\n" +
                "\t\t}\n" +
                "\t}\n" +
                "]";
        String ar = "[\n" +
                "\t{\n" +
                "\t\t\"fakeId\": \"3\",\n" +
                "\t\t\"objectName\": \"ccccc\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"C\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"fakeId\": \"2\",\n" +
                "\t\t\"objectName\": \"bbbbb\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"B\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"fakeId\": \"1\",\n" +
                "\t\t\"objectName\": \"aaaaa\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realId\": \"A\"\n" +
                "\t\t}\n" +
                "\t}\n" +
                "]";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKeyV2", "/[childObject/realId]");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(expectedDiffsCount, compare.size());
    }

    @Test
    public void given_identicalArray_bothObjectPrimaryKeyVersions_resultEmptyListDiffMessage() throws ComparatorException {
        String er = "[{\n"
                + "        \"id_obj_A1\": \"1\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"4\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"regexp:.*\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"regexp:.*\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"regexp:.*\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }, {\n"
                + "        \"id_obj_A1\": \"2\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"5\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"some_value_251\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"some_value_252\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"some_value_253\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }, {\n"
                + "        \"id_obj_A1\": \"3\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"6\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"regexp:some_value_.*\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"regexp:some_value_.*\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"regexp:some_value_.*\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }\n"
                + "]";
        String ar = "[{\n"
                + "        \"id_obj_A1\": \"1\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"4\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"some_value_141\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"some_value_142\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"some_value_143\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }, {\n"
                + "        \"id_obj_A1\": \"3\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"6\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"some_value_361\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"some_value_362\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"some_value_363\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }, {\n"
                + "        \"id_obj_A1\": \"2\",\n"
                + "        \"obj_L2\": {\n"
                + "            \"id_obj_L2\": \"5\",\n"
                + "            \"targtArray\": [{\n"
                + "                    \"id_obj_A2\": \"3\",\n"
                + "                    \"some_parametr\": \"some_value_253\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"2\",\n"
                + "                    \"some_parametr\": \"some_value_252\"\n"
                + "                }, {\n"
                + "                    \"id_obj_A2\": \"1\",\n"
                + "                    \"some_parametr\": \"some_value_251\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }\n"
                + "]\n";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKey", "//id_obj_A1");
        parameters.put("objectPrimaryKeyV2", "/*/obj_L2/targtArray[id_obj_A2]");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        long notIdentCount = compare.stream().filter(diffMessage -> diffMessage.getResult() != IDENTICAL).count();
        long identCount = compare.stream().filter(diffMessage -> diffMessage.getResult() == IDENTICAL).count();
        assertEquals(6, identCount);
        assertEquals(0, notIdentCount);
    }

    @Test
    public void compareTwoJson_ignorePropertiesV2_extraDiff_expectedEmptyDiffs_QABULKVAL_2895() throws ComparatorException {
        String er = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"msisdn\": \"9087656789\",\n" +
                "\t\"iccid\": \"0123654778965412365\",\n" +
                "\t\"accountId\": \"10236547789\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        String ar = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"msisdn\": \"9087656789\",\n" +
                "\t\"iccid\": \"0123654778965412365\",\n" +
                "\t\"accountId\": \"10236547789\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"GENS-0436\",\n" +
                "\t\t\t\"resultMsg\": \"Valid brand value\",\n" +
                "\t\t\t\"status\": \"WARNING\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        Parameters rules = new Parameters();
        rules.put("ignorePropertiesV2", "$.result.[?(@.result == 'GENS-0436')]");
        List<DiffMessage> diffs = jsonComparator.compare(er, ar, rules);
        assertEquals(0, diffs.size());
    }

    @Test
    public void compareTwoJson_ignorePropertiesV2_withRecursiceJsonPath_twoSimilarDiffs_expectedEmptyDiffs() throws ComparatorException {
        String er = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"blaBla\": \"bla\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"result\": \"500\",\n" +
                "\t\t\t\"status\": \"ERROR\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        String ar = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"blaBla\": \"bla\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"ERROR\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"result\": \"500\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        Parameters rules = new Parameters();
        rules.put("ignorePropertiesV2", "$.result..*");
        List<DiffMessage> diffs = jsonComparator.compare(er, ar, rules);
        assertEquals(0, diffs.size());
    }

    @Test
    public void compareTwoJson_multipleIgnorePropertiesV2_threeSimilarDiffs_expectedOneSimilarDiff() throws ComparatorException {
        String er = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"blaBla\": \"bla\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"result\": \"500\",\n" +
                "\t\t\t\"status\": \"ERROR\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        String ar = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"blaBla\": \"blabla\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"ERROR\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"result\": \"500\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        JsonDiffMessage expectedDiff = new JsonDiffMessage(3, "/result/1/status", "/result/1/status", SIMILAR,
                "Node values are different.", "$['result'][1]['status']", "$['result'][1]['status']");
        Parameters rules = new Parameters();
        rules.put("ignorePropertiesV2", "$.result.[?(@.result=='100')].*");
        rules.put("ignorePropertiesV2", "$.blaBla");
        List<DiffMessage> diffs = jsonComparator.compare(er, ar, rules);
        assertEquals(1, diffs.size());
        assertEquals(expectedDiff, diffs.get(0));
    }

    @Test
    public void compareTwoJson_ignorePropertiesV2_oneSimilarDiff_noMatchesForDiff_expectedOneSimilarDiff() throws ComparatorException {
        String er = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"blaBla\": \"bla\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"result\": \"500\",\n" +
                "\t\t\t\"status\": \"ERROR\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        String ar = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"blaBla\": \"CHANGED\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"result\": \"500\",\n" +
                "\t\t\t\"status\": \"ERROR\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        JsonDiffMessage expectedDiff = new JsonDiffMessage(1, "/blaBla", "/blaBla", SIMILAR,
                "Node values are different.", "$['blaBla']", "$['blaBla']");
        Parameters rules = new Parameters();
        rules.put("ignorePropertiesV2", "$.result.[?(@.result=='100')].*");
        List<DiffMessage> diffs = jsonComparator.compare(er, ar, rules);
        assertEquals(1, diffs.size());
        assertEquals(expectedDiff, diffs.get(0));
    }

    @Test
    public void compareTwoJson_ignorePropertiesV2_valueIsBroken_oneSimilarDiff_expectedOneSimilarDiff() throws ComparatorException {
        String er = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"blaBla\": \"bla\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"result\": \"500\",\n" +
                "\t\t\t\"status\": \"ERROR\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        String ar = "{\n" +
                "\t\"status\": \"SUCCESS\",\n" +
                "\t\"blaBla\": \"CHANGED\",\n" +
                "\t\"result\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"result\": \"100\",\n" +
                "\t\t\t\"status\": \"SUCCESS\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"result\": \"500\",\n" +
                "\t\t\t\"status\": \"ERROR\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        JsonDiffMessage expectedDiff = new JsonDiffMessage(1, "/blaBla", "/blaBla", SIMILAR,
                "Node values are different.", "$['blaBla']", "$['blaBla']");
        Parameters rules = new Parameters();
        rules.put("ignorePropertiesV2", "clearlyNotAJsonPath");
        List<DiffMessage> diffs = jsonComparator.compare(er, ar, rules);
        assertEquals(1, diffs.size());
        assertEquals(expectedDiff, diffs.get(0));
    }

    @Test
    public void compareTwoJson_objectPrimaryKey_multipleKeys_expectTwoDiffs_TASUP_10117() throws ComparatorException {
        String er = "[\n" +
                "\t{\n" +
                "\t\t\"RTYPE\": \"A\",\n" +
                "\t\t\"Charge Type\": \"X\",\n" +
                "\t\t\"Item Number\": \"G\",\n" +
                "\t\t\"Diff\": \"A\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"RTYPE\": \"A\",\n" +
                "\t\t\"Charge Type\": \"Y\",\n" +
                "\t\t\"Item Number\": \"H\",\n" +
                "\t\t\"Diff\": \"B\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"RTYPE\": \"B\",\n" +
                "\t\t\"Charge Type\": \"X\",\n" +
                "\t\t\"Item Number\": \"H\",\n" +
                "\t\t\"Diff\": \"C\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"RTYPE\": \"B\",\n" +
                "\t\t\"Charge Type\": \"Y\",\n" +
                "\t\t\"Item Number\": \"G\",\n" +
                "\t\t\"Diff\": \"D\"\n" +
                "\t}\n" +
                "]";
        String ar = "[\n" +
                "\t{\n" +
                "\t\t\"RTYPE\": \"B\",\n" +
                "\t\t\"Charge Type\": \"X\",\n" +
                "\t\t\"Item Number\": \"H\",\n" +
                "\t\t\"Diff\": \"C\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"RTYPE\": \"A\",\n" +
                "\t\t\"Charge Type\": \"Y\",\n" +
                "\t\t\"Item Number\": \"H\",\n" +
                "\t\t\"Diff\": \"BB\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"RTYPE\": \"B\",\n" +
                "\t\t\"Charge Type\": \"Y\",\n" +
                "\t\t\"Item Number\": \"G\",\n" +
                "\t\t\"Diff\": \"D\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"RTYPE\": \"A\",\n" +
                "\t\t\"Charge Type\": \"X\",\n" +
                "\t\t\"Item Number\": \"G\",\n" +
                "\t\t\"Diff\": \"AA\"\n" +
                "\t}\n" +
                "]";
        Parameters rules = new Parameters();
        rules.put("ignoreArraysOrder", "true");
        rules.put("objectPrimaryKey", "//RTYPE&&Charge Type&&Item Number");
        List<DiffMessage> diffs = jsonComparator.compare(er, ar, rules);
        assertEquals(2, diffs.size());
        JsonDiffMessage expectedDiff = new JsonDiffMessage(1, "/0/Diff", "/3/Diff", SIMILAR,
                "Node values are different.", "$[0]['Diff']", "$[3]['Diff']");
        JsonDiffMessage expectedDiff2 = new JsonDiffMessage(2, "/1/Diff", "/1/Diff", SIMILAR,
                "Node values are different.", "$[1]['Diff']", "$[1]['Diff']");
        assertEquals(expectedDiff, diffs.get(0));
        assertEquals(expectedDiff2, diffs.get(1));
    }

    @Test
    public void compareTwoJson_objectPrimaryKeyV2_multipleKeys_expectIdentical_TASUP_10117() throws ComparatorException {
        String er = "{\n" +
                "\t\"parentArray\": [{\n" +
                "\t\t\"fakeId\": \"1\",\n" +
                "\t\t\"objectName\": \"aaaaa\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"A\",\n" +
                "\t\t\t\"realIdB\": \"C\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"2\",\n" +
                "\t\t\"objectName\": \"bbbbb\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"B\",\n" +
                "\t\t\t\"realIdB\": \"C\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"3\",\n" +
                "\t\t\"objectName\": \"ccccc\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"C\",\n" +
                "\t\t\t\"realIdB\": \"A\"\n" +
                "\t\t}\n" +
                "\t}]\n" +
                "}";
        String ar = "{\n" +
                "\t\"parentArray\": [{\n" +
                "\t\t\"fakeId\": \"4\",\n" +
                "\t\t\"objectName\": \"ccccc\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"C\",\n" +
                "\t\t\t\"realIdB\": \"A\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"5\",\n" +
                "\t\t\"objectName\": \"bbbbb\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"B\",\n" +
                "\t\t\t\"realIdB\": \"C\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"6\",\n" +
                "\t\t\"objectName\": \"aaaaa\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"A\",\n" +
                "\t\t\t\"realIdB\": \"C\"\n" +
                "\t\t}\n" +
                "\t}]\n" +
                "}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("ignoreProperties", "/fakeId");
        parameters.put("objectPrimaryKeyV2", "/parentArray[childObject/realIdA&&childObject/realIdB]");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        assertEquals(0, compare.size());
    }

    @Test
    public void compareTwoJson_objectPrimaryKeyV2_multipleKeys_expectTwoDiffs_twoNodesHaveNoPair_TASUP_10117() throws ComparatorException {
        String er = "{\n" +
                "\t\"parentArray\": [{\n" +
                "\t\t\"fakeId\": \"1\",\n" +
                "\t\t\"objectName\": \"aaaaa\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"A\",\n" +
                "\t\t\t\"realIdB\": \"B\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"2\",\n" +
                "\t\t\"objectName\": \"bbbbb\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"B\",\n" +
                "\t\t\t\"realIdB\": \"C\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"3\",\n" +
                "\t\t\"objectName\": \"ccccc\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"C\",\n" +
                "\t\t\t\"realIdB\": \"A\"\n" +
                "\t\t}\n" +
                "\t}]\n" +
                "}";
        String ar = "{\n" +
                "\t\"parentArray\": [{\n" +
                "\t\t\"fakeId\": \"4\",\n" +
                "\t\t\"objectName\": \"ccccc\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"C\",\n" +
                "\t\t\t\"realIdB\": \"A\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"5\",\n" +
                "\t\t\"objectName\": \"bbbbb\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"B\",\n" +
                "\t\t\t\"realIdB\": \"C\"\n" +
                "\t\t}\n" +
                "\t},{\n" +
                "\t\t\"fakeId\": \"6\",\n" +
                "\t\t\"objectName\": \"aaaaa\",\n" +
                "\t\t\"childObject\": {\n" +
                "\t\t\t\"realIdA\": \"A\",\n" +
                "\t\t\t\"realIdB\": \"C\"\n" +
                "\t\t}\n" +
                "\t}]\n" +
                "}";
        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("ignoreProperties", "/fakeId");
        parameters.put("objectPrimaryKeyV2", "/parentArray[childObject/realIdA&&childObject/realIdB]");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        JsonDiffMessage expectedDiff = new JsonDiffMessage(3, "/parentArray/0", "", MISSED,
                "er node is missed.", "$['parentArray'][0]", "");
        JsonDiffMessage expectedDiff2 = new JsonDiffMessage(4, "", "/parentArray/2", EXTRA,
                "ar has extra node(s).", "", "$['parentArray'][0]");

        assertEquals(2, compare.size());
        assertEquals(expectedDiff, compare.get(0));
        assertEquals(expectedDiff2, compare.get(1));
    }

    @Test
    public void compareTwoJson_withRuleIgnoreValue_missedAndExtraOnly() throws ComparatorException {
        String ar = "{\n" +
                "    \"name\":\"BVtool\",\n" +
                "    \"result\":\"X3\",\n" +
                "    \"ticket\":27893,\n" +
                "    \"extra\":\"text\"\n" +
                "}";
        String er = "{\n" +
                "    \"name\":\"BV\",\n" +
                "    \"result\":\"regexp:\\\\d\",\n" +
                "    \"rule\":\"ignoreValue\",\n" +
                "    \"ticket\":27893\n" +
                "}\n";
        Parameters parameters = new Parameters();
        parameters.put("ignoreValue", "$.*");
        JsonDiffMessage expectedDiff = new JsonDiffMessage(3, "/rule", "", MISSED,
                "er node is missed.", "$['rule']", "");
        JsonDiffMessage expectedDiff2 = new JsonDiffMessage(4, "", "/extra", EXTRA,
                "ar has extra node(s).", "", "$['extra']");

        List<DiffMessage> diffMessages = jsonComparator.compare(er, ar, parameters);

        assertEquals(2, diffMessages.size());
        assertEquals(expectedDiff, diffMessages.get(0));
        assertEquals(expectedDiff2, diffMessages.get(1));
    }

    @Test
    public void compare_primaryKeyV2WithIndexNumberToInnerMassiveElement_matchingElementsByInnerMassiveValue() throws  IOException, ComparatorException {
        String ar = getStringFromFile("src/test/resources/json/primaryKeyV2_innerMassiveWithOneObject/ar.json");
        String er = getStringFromFile("src/test/resources/json/primaryKeyV2_innerMassiveWithOneObject/er.json");

        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKeyV2", "/problems[objects/0/objectName]");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        JsonDiffMessage expectedDiff1 = new JsonDiffMessage(1, "/problems/0/id", "/problems/1/id", IDENTICAL,
                "Result is changed due to inline-regexp checking.", "$['problems'][0]['id']", "$['problems'][1]['id']");
        JsonDiffMessage expectedDiff2 = new JsonDiffMessage(2, "/problems/0/objects/0/cellName", "", MISSED,
                "er node is missed.", "$['problems'][0]['objects'][0]['cellName']", "");
        JsonDiffMessage expectedDiff3 = new JsonDiffMessage(3, "/problems/1/id", "/problems/2/id", IDENTICAL,
                "Result is changed due to inline-regexp checking.", "$['problems'][1]['id']", "$['problems'][2]['id']");
        JsonDiffMessage expectedDiff4 = new JsonDiffMessage(4, "/problems/2/id", "/problems/0/id", IDENTICAL,
                "Result is changed due to inline-regexp checking.", "$['problems'][2]['id']", "$['problems'][0]['id']");

        assertEquals(4, compare.size());
        assertEquals(expectedDiff1, compare.get(0));
        assertEquals(expectedDiff2, compare.get(1));
        assertEquals(expectedDiff3, compare.get(2));
        assertEquals(expectedDiff4, compare.get(3));
    }
    @Test
    public void compare_primaryKeyV2WithCertainPropertyValue_matchingElementsByInnerMassiveValue() throws  IOException, ComparatorException {
        String ar = getStringFromFile("src/test/resources/json/primaryKeyV2_innerMassiveWithCertainPropValue/ar.json");
        String er = getStringFromFile("src/test/resources/json/primaryKeyV2_innerMassiveWithCertainPropValue/er.json");

        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKeyV2", "/[children/*/name='ee']");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        JsonDiffMessage expectedDiff1 = new JsonDiffMessage(1, "/0", "", MISSED,
                "er node is missed.", "$[0]", "");
        JsonDiffMessage expectedDiff2 = new JsonDiffMessage(2, "/1/children/0/name", "/0/children/1/name", SIMILAR,
                "Node values are different.", "$[1]['children'][0]['name']", "$[0]['children'][1]['name']");
        JsonDiffMessage expectedDiff3 = new JsonDiffMessage(3, "", "/1", EXTRA,
                "ar has extra node(s).", "", "$[1]");

        assertEquals(3, compare.size());
        assertEquals(expectedDiff1, compare.get(0));
        assertEquals(expectedDiff2, compare.get(1));
        assertEquals(expectedDiff3, compare.get(2));
    }

    @Test
    public void compare_primaryKeyV2WithAsteriskToInnerMassive_matchingElementsByInnerMassiveValues() throws  IOException, ComparatorException {
        String ar = getStringFromFile("src/test/resources/json/primaryKeyV2_innerMassiveWithSeveralObjects/ar.json");
        String er = getStringFromFile("src/test/resources/json/primaryKeyV2_innerMassiveWithSeveralObjects/er.json");

        Parameters parameters = new Parameters();
        parameters.put("ignoreArraysOrder", "true");
        parameters.put("objectPrimaryKeyV2", "/problems[objects/*/objectName]");
        parameters.put("objectPrimaryKeyV2", "problems/*/objects[objectName]");
        List<DiffMessage> compare = jsonComparator.compare(er, ar, parameters);
        JsonDiffMessage expectedDiff1 = new JsonDiffMessage(1, "/problems/0/id", "/problems/0/id", IDENTICAL,
                "Result is changed due to inline-regexp checking.", "$['problems'][0]['id']", "$['problems'][0]['id']");
        JsonDiffMessage expectedDiff2 = new JsonDiffMessage(2, "/problems/0/objects/0/cellName", "/problems/0/objects/0/cellName", SIMILAR,
                "Node values are different.", "$['problems'][0]['objects'][0]['cellName']", "$['problems'][0]['objects'][0]['cellName']");
        JsonDiffMessage expectedDiff3 = new JsonDiffMessage(3, "/problems/0/objects/1/cellName", "/problems/0/objects/1/cellName", SIMILAR,
                "Node values are different.", "$['problems'][0]['objects'][1]['cellName']", "$['problems'][0]['objects'][1]['cellName']");
        JsonDiffMessage expectedDiff4 = new JsonDiffMessage(4, "/problems/1/id", "/problems/1/id", IDENTICAL,
                "Result is changed due to inline-regexp checking.", "$['problems'][1]['id']", "$['problems'][1]['id']");
        JsonDiffMessage expectedDiff5 = new JsonDiffMessage(5, "/problems/1/objects/1/cellName", "/problems/1/objects/2/cellName", SIMILAR,
                "Node values are different.", "$['problems'][1]['objects'][1]['cellName']", "$['problems'][1]['objects'][2]['cellName']");
        JsonDiffMessage expectedDiff6 = new JsonDiffMessage(6, "/problems/1/objects/2/objectType", "/problems/1/objects/0/objectType", SIMILAR,
                "Node values are different.", "$['problems'][1]['objects'][2]['objectType']", "$['problems'][1]['objects'][0]['objectType']");

        assertEquals(6, compare.size());
        assertEquals(expectedDiff1, compare.get(0));
        assertEquals(expectedDiff2, compare.get(1));
        assertEquals(expectedDiff3, compare.get(2));
        assertEquals(expectedDiff4, compare.get(3));
        assertEquals(expectedDiff5, compare.get(4));
        assertEquals(expectedDiff6, compare.get(5));
    }

    @Test
    public void compareWithKeysCaseInsensitive_givenJsonWithArrayAndObject_canCompare() throws ComparatorException {
        String er = "{\n"
                + "    \"ARRAY1\":[{\n"
                + "        \"CHILD1.1\":\"Value1\",\n"
                + "        \"cHild1.2\":\"Value2\"\n"
                + "    },{\n"
                + "        \"chILD2\":\"VALUE 3\",\n"
                + "        \"ChIlD2\":\"VAlue4\"\n"
                + "    }],\n"
                + "    \"NODE2**\":{\n"
                + "        \"node_3\":{\n"
                + "            \"NOde?4\":{\n"
                + "                \"node5\":\"value 3 !A#B@C$D#E@F!\",\n"
                + "                \"Node5\":\"Value2\",\n"
                + "                \"NODE5\" : \"VALUE 4 A$B%C&?D*E/F\",\n"
                + "                \"NodE5\":\"value\"\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}";
        String ar = "{\n"
                + "    \"ARRAY1\":[\n"
                + "    {\n"
                + "        \"chILD2\":\"VALUE 10\",\n"
                + "        \"ChIlD2\":\"VAlue4\"\n"
                + "    },{\n"
                + "        \"CHILD1.1\":\"Value1\",\n"
                + "        \"cHild1.2\":\"Value2\"\n"
                + "    }],\n"
                + "    \"NODE2**\":{\n"
                + "        \"NOde_3\":{\n"
                + "            \"NodE?4\":{\n"
                + "                \"NodE5\":\"Different value\",\n"
                + "                \"Node5\":\"Different Value2\",\n"
                + "                \"node5\":\"value 3 !A#B@C$D#E@F!\",\n"
                + "                \"NODE5\" : \"VALUE 4 A$B%C&?D*E/F\"\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("ignoreArraysOrder", "true");
        List<DiffMessage> diffs = Arrays.asList(
                new DiffMessage(1, "/node2**/node_3/node?4/node5", "/node2**/node_3/node?4/node5", SIMILAR)
        );

        List<DiffMessage> result = jsonComparator.compare(er, ar, params);

        compareJsonDiffs(diffs, result);
    }

    @Test
    public void compareWithKeysCaseInsensitive_with2versionOfObjectPrimaryKey_canApplyBothVersion() throws ComparatorException {
        String er = "{\n"
                + "    \"ROOT\":{\n"
                + "        \"FOR ObjectPrimaryKey\":[{\n"
                + "            \"id\":\"1\",\n"
                + "            \"name\":\"regexp:[A-Z_]+\"\n"
                + "        },{\n"
                + "            \"Id\":\"2\",\n"
                + "            \"Name\":\"regexp:[A-Z_]+\",\n"
                + "            \"Addition\":\"It is missed\"\n"
                + "        },{\n"
                + "            \"ID\":\"3\",\n"
                + "            \"NAME\":\"Name\"\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        String ar = "{\n"
                + "    \"ROOT\":{\n"
                + "        \"FOR ObjectPrimaryKey\":[\n"
                + "        {\n"
                + "            \"ID\":\"3\",\n"
                + "            \"NAME\":\"similar\"\n"
                + "        },{\n"
                + "            \"id\":\"1\",\n"
                + "            \"extraField\":\"Extra\",\n"
                + "            \"name\":\"Modified\"\n"
                + "        },{\n"
                + "            \"Id\":\"2\",\n"
                + "            \"Name\":\"IDENTICAL\"\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        Parameters paramsV1 = new Parameters();
        paramsV1.put("keysCaseInsensitive", "true");
        paramsV1.put("ignoreArraysOrder", "true");
        paramsV1.put("objectPrimaryKey", "/RooT/for objectPrimaryKey/id");
        Parameters paramsV2 = new Parameters();
        paramsV2.put("keysCaseInsensitive", "true");
        paramsV2.put("ignoreArraysOrder", "true");
        paramsV2.put("objectPrimaryKeyV2", "/RooT/for objectPrimaryKey[id]");

        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "/root/for objectprimarykey/0/name", "/root/for objectprimarykey/1/name",
                        MODIFIED, "Result is changed due to inline-regexp checking."),
                new DiffMessage(2, "", "/root/for objectprimarykey/1/extrafield",
                        EXTRA, "ar has extra node(s)."),
                new DiffMessage(3, "/root/for objectprimarykey/1/name", "/root/for objectprimarykey/2/name",
                        IDENTICAL, "Result is changed due to inline-regexp checking."),
                new DiffMessage(4, "/root/for objectprimarykey/1/addition", "",
                        MISSED, "er node is missed."),
                new DiffMessage(5, "/root/for objectprimarykey/2/name", "/root/for objectprimarykey/0/name",
                        SIMILAR, "Node values are different.")
        );

        List<DiffMessage> resultV1 = jsonComparator.compare(er, ar, paramsV1);
        List<DiffMessage> resultV2 = jsonComparator.compare(er, ar, paramsV2);

        compareJsonDiffs(diffMessages, resultV1);
        compareJsonDiffs(diffMessages, resultV2);
    }

    private void compareJsonDiffs(List<DiffMessage> expectedDiffs, List<DiffMessage> actualDiffs) {
        assertEquals(expectedDiffs.size(), actualDiffs.size(), "Number of differences should be the same");
        for (int i=0; i<expectedDiffs.size(); i++) {
            assertEquals(expectedDiffs.get(i).getResult(), actualDiffs.get(i).getResult(), "Result should be the same");
            assertEquals(expectedDiffs.get(i).getExpected(), actualDiffs.get(i).getExpected(), "Expected should be the same");
            assertEquals(expectedDiffs.get(i).getActual(), actualDiffs.get(i).getActual(), "Actual should be the same");
        }
    }

    @Test
    public void compareWithKeysCaseInsensitive_withIgnorePropertiesV2WithExpression_canApplyRules() throws ComparatorException {
        String er = "{\n"
                + "    \"ROOT\":{\n"
                + "        \"FOR IgnoreProperties\":[{\n"
                + "            \"STATUS\":\"to_ignore\"\n"
                + "        },{\n"
                + "            \"Status\":\"TO_IGNORE\",\n"
                + "            \"missed\":\"also ignored\"\n"
                + "        },{\n"
                + "            \"StAtUs\":123\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        String ar = "{\n"
                + "    \"ROOT\":{\n"
                + "        \"FOR IgnoreProperties\":[{\n"
                + "            \"sTaTus\":\"to_ignore different\"\n"
                + "        },{\n"
                + "            \"sTATUS\":\"different TO_IGNORE\",\n"
                + "            \"extra\":\"also ignored because of ignoreProperties rule\"\n"
                + "        },{\n"
                + "            \"StatuS\":\"Different type\"\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("ignoreArraysOrder", "true");
        params.put("ignorePropertiesV2", "$..RooT.['for IGNOREPROPERTIES'].*[?(@.status =~ /.*TO_IGNORE.*/)].*");

        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "/root/for ignoreproperties/0/status", "/root/for ignoreproperties/0/status",
                        SIMILAR, "Node values are different."),
                new DiffMessage(2, "/root/for ignoreproperties/2/status", "/root/for ignoreproperties/2/status",
                        MODIFIED, "Nodes have different types.")
        );

        List<DiffMessage> result = jsonComparator.compare(er, ar, params);

        compareJsonDiffs(diffMessages, result);
    }

    @Test
    public void compareWithKeysCaseInsensitive_withIgnoreProperties_canApplyRules() throws ComparatorException {
        String er = "{\n"
                + "    \"ROOT\":{\n"
                + "        \"FOR IgnoreProperties\":[{\n"
                + "            \"STATUS\":\"to_ignore\"\n"
                + "        },{\n"
                + "            \"Status\":\"TO_IGNORE\"\n"
                + "        },{\n"
                + "            \"StAtUs\":123\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        String ar = "{\n"
                + "    \"rooT\":{\n"
                + "        \"for ignoreProperties\":[{\n"
                + "            \"sTaTus\":\"to_ignore different\"\n"
                + "        },{\n"
                + "            \"sTATUS\":\"different TO_IGNORE\"\n"
                + "        },{\n"
                + "            \"StatuS\":\"Different type\"\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("ignoreProperties", "/StatuS");

        List<DiffMessage> result = jsonComparator.compare(er, ar, params);

        assertEquals(0, result.size(), "There should be no difference in the result.");
    }

    @Test
    public void compareWithKeysCaseInsensitive_withIgnoreValue_canApplyRules() throws ComparatorException {
        String er = "{\n"
                + "\"namE\":[{\n"
                + "    \"obj\":\"1\",\n"
                + "    \"OBJ\":\"2\"\n"
                + "    }]\n"
                + "}";
        String ar = "{\n"
                + "\"NamE\":[{\n"
                + "        \"obj\":\"1\",\n"
                + "        \"OBJ\":\"3\"\n"
                + "    },{\n"
                + "        \"obj\":\"4\",\n"
                + "        \"Obj\":\"5\"\n"
                + "    }]\n"
                + "}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("ignoreValue", "$..oBJ");

        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "", "/name/1",
                        EXTRA, "ar has extra node(s)."));

        List<DiffMessage> result = jsonComparator.compare(er, ar, params);

        compareJsonDiffs(diffMessages, result);
    }

    @Test
    public void compareWithKeysCaseInsensitive_withMandatoryAttribute_canApplyRules() throws ComparatorException {
        String er = "{\n"
                + "    \"namE\":[{\n"
                + "        \"obj\":\"1\",\n"
                + "        \"OBJ\":2,\n"
                + "        \"Status\":\"true\"\n"
                + "    },{\n"
                + "        \"obj\":3,\n"
                + "        \"OBJ\":\"4\",\n"
                + "        \"Status\":\"TruE\"\n"
                + "    }]\n"
                + "} ";
        String ar = "{\n"
                + "    \"NamE\":[{\n"
                + "            \"obj\":\"5\",\n"
                + "            \"OBJ\":\"6\",\n"
                + "            \"StaTuS\":\"TruE\"\n"
                + "        },{\n"
                + "            \"obj\":\"7\",\n"
                + "            \"Obj\":\"8\",\n"
                + "            \"Status\":\"false\"\n"
                + "        }]\n"
                + "}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("mandatoryAttribute", "$.name.*.staTus");
        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "/name/0/status", "/name/0/status",
                        SIMILAR, "Node values are different."),
                new DiffMessage(2, "/name/1/status", "/name/1/status",
                        SIMILAR, "Node values are different.")
        );

        List<DiffMessage> result = jsonComparator.compare(er, ar, params);

        compareJsonDiffs(diffMessages, result);
    }

    @Test
    public void compareWithKeysCaseInsensitive_withReadByPath_canApplyRules() throws ComparatorException {
        String er = "{\n"
                + "    \"root\":{\n"
                + "        \"Missed\":\"Should not present in result\",\n"
                + "        \"FOR READBYPATH\":[{\n"
                + "            \"STATUS\":\"similar\"\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        String ar = "{\n"
                + "    \"ROOT\":{\n"
                + "        \"FOR ReadByPath\":[{\n"
                + "            \"sTaTus\":\"different\"\n"
                + "        }],\n"
                + "        \"extraElement\":\"Should not present in result\"\n"
                + "    }\n"
                + "}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("readByPath", "$.RooT.['for readBYpath']");

        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "/0/status", "/0/status",
                        SIMILAR, "Node values are different."));

        List<DiffMessage> result = jsonComparator.compare(er, ar, params);

        compareJsonDiffs(diffMessages, result);
    }

    @Test
    public void compareWithKeysCaseInsensitive_withCheckArray_canApplyRules() throws ComparatorException {
        String er = "{\n"
                + "    \"namE\":[{\n"
                + "        \"obj\":\"1\",\n"
                + "        \"OBJ\":2,\n"
                + "        \"Status\":\"true\"\n"
                + "    },{\n"
                + "        \"obj\":3,\n"
                + "        \"OBJ\":\"4\",\n"
                + "        \"Status\":\"TruE\"\n"
                + "    }]\n"
                + "}";
        String ar = "{\n"
                + "    \"NamE\":[{\n"
                + "            \"obj\":\"5\",\n"
                + "            \"OBJ\":\"6\",\n"
                + "            \"StaTuS\":\"TruE\"\n"
                + "        },{\n"
                + "            \"obj\":\"7\",\n"
                + "            \"Obj\":\"8\",\n"
                + "            \"Status\":\"false\"\n"
                + "        }]\n"
                + "}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("ignorePropertiesV2", "$..*");
        params.put("checkArray", "$.nAmE.*.OBj");

        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "/name/0/obj", "/name/0/obj",
                        MODIFIED, "Node values are different."),
        new DiffMessage(3, "/name/1/obj", "/name/1/obj",
                        SIMILAR, "Node values are different.")
        );

        List<DiffMessage> result = jsonComparator.compare(er, ar, params);

        compareJsonDiffs(diffMessages, result);
    }

    @Test
    public void compareWithKeysCaseInsensitive_withChangeDiffResultJson_canApplyRules() throws ComparatorException {
        String er = "{\n"
                + "\"namE\":[]\n"
                + "} ";
        String ar = "{\n"
                + "\"NamE\":[{\n"
                + "        \"OBJ\":\"extra\"\n"
                + "    },{\n"
                + "        \"Obj\":\"EXTRA\"\n"
                + "    }]\n"
                + "} ";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("changeDiffResultJson", "EXTRA=IDENTICAL=$.NaMe.*[?(@.OBJ == 'EXTRA')]");

        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "", "/name/0",
                        EXTRA, "ar has extra node(s)."),
                new DiffMessage(3, "", "/name/1",
                        IDENTICAL, "ar has extra node(s).")
        );

        List<DiffMessage> result = jsonComparator.compare(er, ar, params);

        compareJsonDiffs(diffMessages, result);
    }

    @Test
    public void compareWithKeysCaseInsensitive_withJsonSchema_canApplyRules() throws ComparatorException {
        String schema = "{\n"
                + "    \"type\": \"object\",\n"
                + "    \"required\": [\"salesuserslandline\", \"relatedparty\", \"status\"],\n"
                + "    \"properties\": {\n"
                + "        \"salesuserslandline\": {\n"
                + "            \"type\": \"string\"\n"
                + "        },\n"
                + "        \"relatedparty\": {\n"
                + "            \"type\": \"array\",\n"
                + "            \"items\": {\n"
                + "                \"type\": \"string\",\n"
                + "                \"enum\": [\n"
                + "                    \"one\",\n"
                + "                    \"two\",\n"
                + "                    \"three\"\n"
                + "                ]\n"
                + "            }\n"
                + "        },\n"
                + "        \"status\": {\n"
                + "            \"type\": \"boolean\"\n"
                + "        }\n"
                + "    }\n"
                + "}";
        String ar = "{\n"
                + "    \"SalesUsersLandline\": \"Phone\",\n"
                + "    \"relatedParty\": [\"one\", \"four\"],\n"
                + "    \"status\": 1\n"
                + "}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("validateSchema", schema);

        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "", "/relatedparty/1",
                        MODIFIED, "$.relatedparty[1]: does not have a value in the enumeration [one, two, three]"),
                new DiffMessage(2, "", "/status",
                        MODIFIED, "$.status: integer found, boolean expected")
        );

        List<DiffMessage> result = jsonComparator.compare("", ar, params);

        compareJsonDiffs(diffMessages, result);
    }

    @Test
    public void compareWithKeysCaseInsensitive_withSimpleJsonSchema_canApplyRules() throws ComparatorException {
        String erSchema = "{\n"
                + "    \"salesUsersLandline\": \"Phone\",\n"
                + "    \"relatedParty[1..1]\": [\n"
                + "        {\n"
                + "            \"role\": \"Customer\",\n"
                + "            \"id\": \"91601\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"overrideMode?\": \"gross\"\n"
                + "}";
        String ar = "{\n"
                + "    \"salesUsersLandline\": \"Phone\",\n"
                + "    \"RelatedParty\": [],\n"
                + "    \"OVERRIDEMODE\":\"different value\"\n"
                + "}}";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("validateAsSimpleSchema", "true");

        List<DiffMessage> diffMessages = Arrays.asList(
                new DiffMessage(1, "/relatedparty[1..1]", "/relatedparty",
                        MODIFIED, "there must be a minimum of 1 items in the 'relatedparty' array"),
                new DiffMessage(2, "/overridemode?", "/overridemode",
                        SIMILAR, "Node values are different")
        );

        List<DiffMessage> result = jsonComparator.compare(erSchema, ar, params);

        compareJsonDiffs(diffMessages, result);
    }

}
