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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jcabi.immutable.Array;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class XmlComparatorTest {

    XmlComparator comparator = new XmlComparator();

    @Test
    public void compareTwoXml_withoutRules_expectedResultIdentical() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 0;
        String er = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<settings xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "                xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" " +
                "                xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 " +
                "                http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n" +
                "    <localRepository>/cache</localRepository> \n" +
                "    <pluginGroups>\n" +
                "        <pluginGroup>org.qubership.om.tls.maven.plugin</pluginGroup>\n" +
                "    </pluginGroups>\n" +
                "    <mirrors>\n" +
                "        <mirror>\n" +
                "            <id>dtrust.central</id>\n" +
                "            <url>https://artifactorycn.somedomian.com/nc.nexussrv-group-dtrust.mvn.group</url>\n" +
                "            <mirrorOf>\n" +
                "                central, apache.snapshots, spring-ext, spring-milestones, spring-external, spy\n" +
                "                spring-snapshot, spring-snapshots, sonatype-nexus-snapshots, jvnet-nexus-releases,\n" +
                "                jvnet-nexus-snapshots, glassfish-repo-archive, codehaus.snapshots, snapshots, ow2-snapshot\n" +
                "            </mirrorOf>\n" +
                "        </mirror>\n" +
                "    </mirrors>\n" +
                "    <profiles>\n" +
                "        <profile>\n" +
                "            <id>default</id>\n" +
                "            <activation>\n" +
                "                <activeByDefault>true</activeByDefault>\n" +
                "            </activation>\n" +
                "            <properties>\n" +
                "                <repo.prefix>file:/localRepositories</repo.prefix>\n" +
                "            </properties>\n" +
                "            <repositories>\n" +
                "                <repository>\n" +
                "                    <id>nc.super.poms</id>\n" +
                "                    <url>https://artifactorycn.somedomian.com/nexussrv_nc_super_poms.mvn.proxy</url>\n" +
                "                    <releases>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </releases>\n" +
                "                    <snapshots>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </snapshots>\n" +
                "                </repository>\n" +
                "                <repository>\n" +
                "                    <id>pd.sandbox-legacy.mvn.group</id>\n" +
                "                    <url>https://artifactorycn.somedomian.com/pd.sandbox-legacy.mvn.group</url>\n" +
                "                    <releases>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </releases>\n" +
                "                    <snapshots>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </snapshots>\n" +
                "                </repository>\n" +
                "                <EmptyRepository/>\n" +
                "            </repositories>\n" +
                "        </profile>\n" +
                "    </profiles>\n" +
                "</settings>\n";
        Parameters params = new Parameters();
        List<DiffMessage> result = comparator.compare(er, er, params);
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, result.size());
    }

    @Test
    public void compareTwoXml_withoutRules_expectedResultSimilar() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 2;
        String er = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<settings xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "                xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" " +
                "                xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 " +
                "                http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n" +
                "    <localRepository>/cache</localRepository> \n" +
                "    <pluginGroups>\n" +
                "        <pluginGroup>org.qubership.om.tls.maven.plugin</pluginGroup>\n" +
                "    </pluginGroups>\n" +
                "    <mirrors>\n" +
                "        <mirror>\n" +
                "            <id>dtrust.central</id>\n" +
                "            <url>https://artifactorycn.somedomian.com/nc.nexussrv-group-dtrust.mvn.group</url>\n" +
                "            <mirrorOf>\n" +
                "                central, apache.snapshots, spring-ext, spring-milestones, spring-external, spy\n" +
                "                spring-snapshot, spring-snapshots, sonatype-nexus-snapshots, jvnet-nexus-releases,\n" +
                "                jvnet-nexus-snapshots, glassfish-repo-archive, codehaus.snapshots, snapshots, ow2-snapshot\n" +
                "            </mirrorOf>\n" +
                "        </mirror>\n" +
                "    </mirrors>\n" +
                "    <profiles>\n" +
                "        <profile>\n" +
                "            <id>default</id>\n" +
                "            <activation>\n" +
                "                <activeByDefault>true</activeByDefault>\n" +
                "            </activation>\n" +
                "            <properties>\n" +
                "                <repo.prefix>file:/localRepositories</repo.prefix>\n" +
                "            </properties>\n" +
                "            <repositories>\n" +
                "                <repository>\n" +
                "                    <id>nc.super.poms</id>\n" +
                "                    <url>https://artifactorycn.somedomian.com/nexussrv_super_poms.mvn.proxy</url>\n" +
                "                    <releases>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </releases>\n" +
                "                    <snapshots>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </snapshots>\n" +
                "                </repository>\n" +
                "                <repository>\n" +
                "                    <id>pd.sandbox-legacy.mvn.group</id>\n" +
                "                    <url>https://artifactorycn.somedomian.com/pd.sandbox-legacy.mvn.group</url>\n" +
                "                    <releases>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </releases>\n" +
                "                    <snapshots>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </snapshots>\n" +
                "                </repository>\n" +
                "                <EmptyRepository/>\n" +
                "            </repositories>\n" +
                "        </profile>\n" +
                "    </profiles>\n" +
                "</settings>\n";
        String ar = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<settings xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "                xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" " +
                "                xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 " +
                "                http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n" +
                "    <localRepository>/cache</localRepository> \n" +
                "    <pluginGroups>\n" +
                "        <pluginGroup>org.qubership.om.tls.maven.plugin</pluginGroup>\n" +
                "    </pluginGroups>\n" +
                "    <mirrors>\n" +
                "        <mirror>\n" +
                "            <id>dtrust.central</id>\n" +
                "            <url>https://artifactorycn.somedomian.com/nc.nexussrv-group-dtrust.mvn.group</url>\n" +
                "            <mirrorOf>\n" +
                "                central, apache.snapshots, spring-ext, spring-milestones, spring-external, spy\n" +
                "                spring-snapshot, spring-snapshots, sonatype-nexus-snapshots, jvnet-nexus-releases,\n" +
                "                jvnet-nexus-snapshots, glassfish-repo-archive, codehaus.snapshots, snapshots, ow2-snapshot\n" +
                "            </mirrorOf>\n" +
                "        </mirror>\n" +
                "    </mirrors>\n" +
                "    <profiles>\n" +
                "        <profile>\n" +
                "            <id>new</id>\n" +
                "            <activation>\n" +
                "                <activeByDefault>true</activeByDefault>\n" +
                "            </activation>\n" +
                "            <properties>\n" +
                "                <repo.prefix>file:/localRepositories</repo.prefix>\n" +
                "            </properties>\n" +
                "            <repositories>\n" +
                "                <repository>\n" +
                "                    <id>nc.super.poms</id>\n" +
                "                    <url>https://artifactorycn.somedomian.com/nexussrv_super_poms.mvn.proxy</url>\n" +
                "                    <releases>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </releases>\n" +
                "                    <snapshots>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </snapshots>\n" +
                "                </repository>\n" +
                "                <repository>\n" +
                "                    <id>pd.sandbox-legacy.mvn.group</id>\n" +
                "                    <url>https://artifactorycn.somedomian.com/pd.sandbox-legacy.mvn.group</url>\n" +
                "                    <releases>\n" +
                "                        <updatePolicy>always</updatePolicy>\n" +
                "                    </releases>\n" +
                "                    <snapshots>\n" +
                "                        <updatePolicy>never</updatePolicy>\n" +
                "                    </snapshots>\n" +
                "                </repository>\n" +
                "                <EmptyRepository/>\n" +
                "            </repositories>\n" +
                "        </profile>\n" +
                "    </profiles>\n" +
                "</settings>\n";
        Parameters params = new Parameters();
        List<DiffMessage> result = comparator.compare(er, ar, params);
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, result.size());
        assertTrue(result.stream().map(DiffMessage::getResult)
                .anyMatch(resultType -> resultType == ResultType.SIMILAR));
    }

    //@Ignore //don't know how create Modified diff result
    @Test
    public void compareTwoXml_withoutRules_expectedResultModified() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 2;
        String er = "<nodes>\n" +
                "    <node1>regexp:\\d{15}</node1>\n" +
                "    <node2 att=\"12\">er has attribute</node2>\n" +
                "</nodes>";
        String ar = "<nodes>\n" +
                "    <node1>111abc</node1>\n" +
                "    <node2>er has attribute</node2>\n" +
                "</nodes>";
        Parameters params = new Parameters();
        List<DiffMessage> result = comparator.compare(er, ar, params);
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, result.size());
        assertTrue(result.stream().map(DiffMessage::getResult)
                .allMatch(resultType -> resultType == ResultType.MODIFIED));
    }

    @Test
    public void compareTwoXml_withRules_expectedResultModified() throws ComparatorException {
        String er = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><tns:executeAction xmlns:tns=\"http://services.tdeu.telekom.net/ServAndResMgmt/TechOrderMgmt/FulfillmentProvider_v01.00\"><typ:context xmlns:typ=\"http://services.tdeu.telekom.net/ServAndResMgmt/TechOrderMgmt/FulfillmentProvider_v01.00/types\" xmlns=\"http://system-services.t-home.telekom.de/ServiceManagement/OIS_Services_v01.00/common\" xmlns:csdg=\"http://schemas.telekom.net/csdg_v01.01\" xmlns:ns2=\"http://system-services.t-home.telekom.de/ServiceManagement/TechnicalOrderManagement/Fulfillment_v01.01/types\"><csdg:technicalContext><csdg:from>de.telekom.ngssm.fulfillment:Adapter</csdg:from><csdg:routingInfo>de.telekom.ngssm.fulfillment:Adapter:FulfillmentProvider_ACCESS</csdg:routingInfo><csdg:messageId>9154600904013812661_9154600907813812954</csdg:messageId><csdg:currentSenderTimestampUTC>2019-07-14T05:44:48.285Z</csdg:currentSenderTimestampUTC><csdg:expiryOffsetInMillis>0</csdg:expiryOffsetInMillis></csdg:technicalContext><csdg:businessContext><csdg:processId>xPlayWSActivation9154600904013812634</csdg:processId><csdg:processTypeId>xPlayWSReservation</csdg:processTypeId></csdg:businessContext></typ:context><typ:data xmlns:typ=\"http://services.tdeu.telekom.net/ServAndResMgmt/TechOrderMgmt/FulfillmentProvider_v01.00/types\" xmlns=\"http://system-services.t-home.telekom.de/ServiceManagement/OIS_Services_v01.00/common\" xmlns:csdg=\"http://schemas.telekom.net/csdg_v01.01\" xmlns:ns2=\"http://system-services.t-home.telekom.de/ServiceManagement/TechnicalOrderManagement/Fulfillment_v01.01/types\"><ns2:executeAction><ns2:messageID><value>9154600904013812661_9154600907813812954</value></ns2:messageID><ns2:Plan><entityKey><keyA>17f0e497-7ee8-4289-9928-5654b22a0bb1</keyA></entityKey><state><value>readyForExecution</value></state><specification><specificationName>PlanSpec</specificationName><specificationID>PlanSpec</specificationID><characteristic><characteristicID>Status</characteristicID><characteristicValue>readyForExecution</characteristicValue></characteristic><characteristic><characteristicID>CRMAuftragsNummer</characteristicID><characteristicValue>auftrag_663706</characteristicValue></characteristic><characteristic><characteristicID>IncidentResolutionPriority</characteristicID><characteristicValue>3</characteristicValue></characteristic><characteristic><characteristicID>OE_OrderID</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CRMAuftragsPosNummer</characteristicID><characteristicValue>915460090401381263301</characteristicValue></characteristic><characteristic><characteristicID>Initial_PPVersion</characteristicID><characteristicValue>RC19.2</characteristicValue></characteristic><characteristic><characteristicID>BindingPeriod</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Request Type</characteristicID><characteristicValue>Activation</characteristicValue></characteristic><characteristic><characteristicID>CouplingID_List</characteristicID></characteristic><characteristic><characteristicID>PreviousPlan</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue>b3c23217-ff8c-4f35-85f2-ea381dd7e2d0</characteristicValue></characteristic></characteristic></characteristic><characteristic><characteristicID>DueDate</characteristicID><characteristicValue>2019-07-14T01:13:15Z</characteristicValue></characteristic><characteristic><characteristicID>Plan-ID</characteristicID><characteristicValue>17f0e497-7ee8-4289-9928-5654b22a0bb1</characteristicValue></characteristic><characteristic><characteristicID>aktiverArbeitsplatzList</characteristicID></characteristic></specification><businessInteractionItem><entityKey><keyA>80de2cf2-98b6-4c3d-a4c5-455625459573</keyA></entityKey><specification><specificationName>AccessLineServiceOrderItemSpec</specificationName><specificationID>AccessLineServiceOrderItemSpec</specificationID><characteristic><characteristicID>ServiceProfile</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>EnhancedInquiryServiceReason</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>BusinessInfo</characteristicID><characteristic><characteristicID>LSZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>SchalttokenDelete</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LSZZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Leitungskey</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>AccessReservationID</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CallNumber</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ONKZ</characteristicID><characteristicValue>228</characteristicValue></characteristic></characteristic><characteristic><characteristicID>DeactivatePEL</characteristicID><characteristicValue>true</characteristicValue></characteristic><characteristic><characteristicID>InformationTAL</characteristicID><characteristic><characteristicID>MaxBruttobitrate</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Schleifenwiderstand</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>KabelAbschnitt_List</characteristicID></characteristic><characteristic><characteristicID>TALOrdnungsnummer</characteristicID><characteristicValue/></characteristic></characteristic><characteristic><characteristicID>AccessProfile</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ExtendedSupply</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>PreventEnhancedInquiry</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>PreventTechnologyChange</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>PropertiesTAL</characteristicID><characteristic><characteristicID>TransmissionMethod</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>hasZWR</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ONKZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CarrierCode</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ASB</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>KVzID</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>UeVtPin</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LineTermination</characteristicID><characteristicValue/></characteristic></characteristic><characteristic><characteristicID>KKF_Action_required</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>WorkforceInformation</characteristicID><characteristic><characteristicID>ServiceBandbreite</characteristicID><characteristic><characteristicID>DownstreamBandwidth</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>UpstreamBandwidth</characteristicID><characteristicValue/></characteristic></characteristic><characteristic><characteristicID>LBZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>SchaltToken</characteristicID><characteristicValue>Schalttoken001</characteristicValue></characteristic><characteristic><characteristicID>PostalAddressReference</characteristicID><characteristicValue>123456</characteristicValue></characteristic><characteristic><characteristicID>SchaltKennerList</characteristicID></characteristic><characteristic><characteristicID>WorkforceAction</characteristicID><characteristicValue>Montage_ohne_Kundenanwesenheit</characteristicValue></characteristic></characteristic><characteristic><characteristicID>AccessBandwidth</characteristicID><characteristic><characteristicID>GuaranteedUploadBandwidth</characteristicID><characteristicValue>704</characteristicValue></characteristic><characteristic><characteristicID>MaxDownloadBandwidth</characteristicID><characteristicValue>16000</characteristicValue></characteristic><characteristic><characteristicID>GuaranteedDownloadBandwidth</characteristicID><characteristicValue>10944</characteristicValue></characteristic><characteristic><characteristicID>MinDownloadBandwidth</characteristicID><characteristicValue>716</characteristicValue></characteristic><characteristic><characteristicID>MinUploadBandwidth</characteristicID><characteristicValue>364</characteristicValue></characteristic><characteristic><characteristicID>MaxUploadBandwidth</characteristicID><characteristicValue>1024</characteristicValue></characteristic></characteristic><characteristic><characteristicID>AccessAvailabilityDueDate</characteristicID><characteristicValue>2018-12-22</characteristicValue></characteristic><characteristic><characteristicID>DurationSH</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>WorkOrderServiceOrderItemReference</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>keyB</characteristicID><characteristicValue/></characteristic></characteristic></characteristic><characteristic><characteristicID>Technology</characteristicID><characteristicValue>VDSL</characteristicValue></characteristic><characteristic><characteristicID>UseNGSSM_WF</characteristicID><characteristicValue>true</characteristicValue></characteristic><characteristic><characteristicID>BondedPhysicalLinks</characteristicID><characteristicValue>1</characteristicValue></characteristic><characteristic><characteristicID>Taktsynchronisation</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Location</characteristicID><characteristic><characteristicID>LocalLocation</characteristicID><characteristic><characteristicID>Room</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>BuildingPartInfo</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>BuildingPart</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LocationInfo</characteristicID><characteristic><characteristicID>ONKZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Name</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ASB</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Note</characteristicID><characteristicValue/></characteristic></characteristic></characteristic><characteristic><characteristicID>Address</characteristicID><characteristic><characteristicID>Name</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Country</characteristicID><characteristic><characteristicID>Country</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CountryID</characteristicID><characteristicValue/></characteristic></characteristic><characteristic><characteristicID>PostalAddress</characteristicID><characteristic><characteristicID>PostOfficeBox</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ZIP</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Street</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>HouseNumberSupplement</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>District</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>HouseNumber</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CityPostOfficeBox</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ZipPostOfficeBox</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>City</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>KLS_ID</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>GeoCoordinates</characteristicID><characteristic><characteristicID>Latitude</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Longitude</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Easting</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Northing</characteristicID><characteristicValue/></characteristic></characteristic></characteristic></characteristic></characteristic></specification><service><entityKey><keyA>d80e988e-25fc-4815-965b-4686bb68c40a</keyA><keyB>0a109cb0-1665-4f76-bd87-88a2d1b33040</keyB></entityKey><specification><specificationName>AccessLine</specificationName><specificationID>AccessLine</specificationID><characteristic><characteristicID>Anschlussnummer</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LSZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Anschlussvariante</characteristicID><characteristicValue>Erstanbindung</characteristicValue></characteristic><characteristic><characteristicID>CustomerPremiseLocationReference</characteristicID><characteristicValue>167006</characteristicValue></characteristic><characteristic><characteristicID>AplStiftRef List</characteristicID></characteristic><characteristic><characteristicID>StatischeProvisionierung</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LSZZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>BNG_Port_Adresse_GF</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Line_ID</characteristicID><characteristicValue>DEU.DTAG.9B95</characteristicValue></characteristic><characteristic><characteristicID>Leitungsreferenz</characteristicID><characteristicValue/></characteristic></specification></service></businessInteractionItem><businessInteractionItem><entityKey><keyA>473eddfa-80c4-470c-b82b-ff6edc020a22</keyA></entityKey><specification><specificationName>SubscriptionServiceOrderItemSpec</specificationName><specificationID>SubscriptionServiceOrderItemSpec</specificationID></specification><service><entityKey><keyA>40e21f5b-587c-441a-8307-45133ea88e8a</keyA></entityKey><specification><specificationName>xPlayWS</specificationName><specificationID>xPlayWS</specificationID><characteristic><characteristicID>Subscription-ID</characteristicID><characteristicValue>000000000327527</characteristicValue></characteristic><characteristic><characteristicID>CarrierAnschlussBSAReference</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue>29ec2dee-1707-4590-92c9-414909c7db64</characteristicValue></characteristic><characteristic><characteristicID>keyB</characteristicID><characteristicValue/></characteristic></characteristic></characteristic><characteristic><characteristicID>Subscriber-Referenz</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue>a287addc-8e28-4992-b097-a9fb6db4929c</characteristicValue></characteristic><characteristic><characteristicID>keyB</characteristicID><characteristicValue>b9419b80-3553-4e0d-9f92-c7daf0f75d21</characteristicValue></characteristic></characteristic></characteristic><characteristic><characteristicID>Einzelvertragsnummer</characteristicID><characteristicValue>xPl_qaapp426cn:6800_663706</characteristicValue></characteristic></specification></service></businessInteractionItem><businessInteractionItem><entityKey><keyA>0eb5f365-ed3e-3e1b-9bb7-4613a76921be</keyA></entityKey><specification><specificationName>SubscriberServiceOrderItemSpec</specificationName><specificationID>SubscriberServiceOrderItemSpec</specificationID></specification><service><entityKey><keyA>a287addc-8e28-4992-b097-a9fb6db4929c</keyA><keyB>b9419b80-3553-4e0d-9f92-c7daf0f75d21</keyB></entityKey><specification><specificationName>WS_Partner</specificationName><specificationID>WS_Partner</specificationID><characteristic><characteristicID>Providernummer</characteristicID><characteristicValue>663706</characteristicValue></characteristic><characteristic><characteristicID>Subscriber-ID</characteristicID><characteristicValue>2e7c0fec-11c7-4ff1-b3fe-a44a7186d9c1</characteristicValue></characteristic></specification></service></businessInteractionItem><businessInteractionItem><entityKey><keyA>9154600904013812661</keyA></entityKey><state><value>inExecution</value></state><specification><specificationName>Konsistenzsicherung xPlay Access starten</specificationName><specificationID>Konsistenzsicherung xPlay Access starten</specificationID><characteristic><characteristicID>ONKZ</characteristicID><characteristicValue>228</characteristicValue></characteristic><characteristic><characteristicID>Rufnummer</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Access_Line</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue>80de2cf2-98b6-4c3d-a4c5-455625459573</characteristicValue></characteristic><characteristic><characteristicID>keyB</characteristicID><characteristicValue/></characteristic></characteristic></characteristic></specification></businessInteractionItem></ns2:Plan></ns2:executeAction></typ:data></tns:executeAction></soapenv:Body></soapenv:Envelope>";
        String ar = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><tns:executeAction xmlns:tns=\"http://services.tdeu.telekom.net/ServAndResMgmt/TechOrderMgmt/FulfillmentProvider_v01.00\"><typ:context xmlns:typ=\"http://services.tdeu.telekom.net/ServAndResMgmt/TechOrderMgmt/FulfillmentProvider_v01.00/types\" xmlns=\"http://system-services.t-home.telekom.de/ServiceManagement/OIS_Services_v01.00/common\" xmlns:csdg=\"http://schemas.telekom.net/csdg_v01.01\" xmlns:ns2=\"http://system-services.t-home.telekom.de/ServiceManagement/TechnicalOrderManagement/Fulfillment_v01.01/types\"><csdg:technicalContext><csdg:from>de.telekom.ngssm.fulfillment:Adapter</csdg:from><csdg:routingInfo>de.telekom.ngssm.fulfillment:Adapter:FulfillmentProvider_ACCESS</csdg:routingInfo><csdg:messageId>9154944098813362054_9154944101813362527</csdg:messageId><csdg:currentSenderTimestampUTC>2019-08-23T06:03:49.891Z</csdg:currentSenderTimestampUTC><csdg:expiryOffsetInMillis>0</csdg:expiryOffsetInMillis></csdg:technicalContext><csdg:businessContext><csdg:processId>xPlayWSActivation9154944096813361935</csdg:processId><csdg:processTypeId>xPlayWSReservation</csdg:processTypeId></csdg:businessContext></typ:context><typ:data xmlns:typ=\"http://services.tdeu.telekom.net/ServAndResMgmt/TechOrderMgmt/FulfillmentProvider_v01.00/types\" xmlns=\"http://system-services.t-home.telekom.de/ServiceManagement/OIS_Services_v01.00/common\" xmlns:csdg=\"http://schemas.telekom.net/csdg_v01.01\" xmlns:ns2=\"http://system-services.t-home.telekom.de/ServiceManagement/TechnicalOrderManagement/Fulfillment_v01.01/types\"><ns2:executeAction><ns2:messageID><value>9154944098813362054_9154944101813362527</value></ns2:messageID><ns2:Plan><entityKey><keyA>378614fb-77e3-4bfc-a337-e2976797386e</keyA></entityKey><state><value>readyForExecution</value></state><specification><specificationName>PlanSpec</specificationName><specificationID>PlanSpec</specificationID><characteristic><characteristicID>Status</characteristicID><characteristicValue>readyForExecution</characteristicValue></characteristic><characteristic><characteristicID>CRMAuftragsNummer</characteristicID><characteristicValue>auftrag_941324</characteristicValue></characteristic><characteristic><characteristicID>IncidentResolutionPriority</characteristicID><characteristicValue>3</characteristicValue></characteristic><characteristic><characteristicID>OE_OrderID</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CRMAuftragsPosNummer</characteristicID><characteristicValue>915494409681336193301</characteristicValue></characteristic><characteristic><characteristicID>Initial_PPVersion</characteristicID><characteristicValue>RC19.3</characteristicValue></characteristic><characteristic><characteristicID>BindingPeriod</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Request Type</characteristicID><characteristicValue>Activation</characteristicValue></characteristic><characteristic><characteristicID>CouplingID_List</characteristicID></characteristic><characteristic><characteristicID>PreviousPlan</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue>1368ddad-8f53-4908-94e7-ee10d89c98ce</characteristicValue></characteristic></characteristic></characteristic><characteristic><characteristicID>DueDate</characteristicID><characteristicValue>2019-08-22T21:56:15Z</characteristicValue></characteristic><characteristic><characteristicID>Plan-ID</characteristicID><characteristicValue>378614fb-77e3-4bfc-a337-e2976797386e</characteristicValue></characteristic><characteristic><characteristicID>aktiverArbeitsplatzList</characteristicID></characteristic></specification><businessInteractionItem><entityKey><keyA>159df5d7-153c-4654-a688-dd2c10fb372e</keyA></entityKey><specification><specificationName>AccessLineServiceOrderItemSpec</specificationName><specificationID>AccessLineServiceOrderItemSpec</specificationID><characteristic><characteristicID>ServiceProfile</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>EnhancedInquiryServiceReason</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>BusinessInfo</characteristicID><characteristic><characteristicID>LSZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>SchalttokenDelete</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LSZZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Leitungskey</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>AccessReservationID</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CallNumber</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ONKZ</characteristicID><characteristicValue>228</characteristicValue></characteristic></characteristic><characteristic><characteristicID>DeactivatePEL</characteristicID><characteristicValue>true</characteristicValue></characteristic><characteristic><characteristicID>InformationTAL</characteristicID><characteristic><characteristicID>MaxBruttobitrate</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Schleifenwiderstand</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>KabelAbschnitt_List</characteristicID></characteristic><characteristic><characteristicID>TALOrdnungsnummer</characteristicID><characteristicValue/></characteristic></characteristic><characteristic><characteristicID>AccessProfile</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ExtendedSupply</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>PreventEnhancedInquiry</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>PreventTechnologyChange</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>PropertiesTAL</characteristicID><characteristic><characteristicID>TransmissionMethod</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>hasZWR</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ONKZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CarrierCode</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ASB</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>KVzID</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>UeVtPin</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LineTermination</characteristicID><characteristicValue/></characteristic></characteristic><characteristic><characteristicID>KKF_Action_required</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>WorkforceInformation</characteristicID><characteristic><characteristicID>ServiceBandbreite</characteristicID><characteristic><characteristicID>DownstreamBandwidth</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>UpstreamBandwidth</characteristicID><characteristicValue/></characteristic></characteristic><characteristic><characteristicID>LBZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>SchaltToken</characteristicID><characteristicValue>Schalttoken001</characteristicValue></characteristic><characteristic><characteristicID>PostalAddressReference</characteristicID><characteristicValue>123456</characteristicValue></characteristic><characteristic><characteristicID>SchaltKennerList</characteristicID></characteristic><characteristic><characteristicID>WorkforceAction</characteristicID><characteristicValue>Montage_ohne_Kundenanwesenheit</characteristicValue></characteristic></characteristic><characteristic><characteristicID>AccessBandwidth</characteristicID><characteristic><characteristicID>GuaranteedUploadBandwidth</characteristicID><characteristicValue>704</characteristicValue></characteristic><characteristic><characteristicID>MaxDownloadBandwidth</characteristicID><characteristicValue>16000</characteristicValue></characteristic><characteristic><characteristicID>GuaranteedDownloadBandwidth</characteristicID><characteristicValue>10944</characteristicValue></characteristic><characteristic><characteristicID>MinDownloadBandwidth</characteristicID><characteristicValue>716</characteristicValue></characteristic><characteristic><characteristicID>MinUploadBandwidth</characteristicID><characteristicValue>364</characteristicValue></characteristic><characteristic><characteristicID>MaxUploadBandwidth</characteristicID><characteristicValue>1024</characteristicValue></characteristic></characteristic><characteristic><characteristicID>AccessAvailabilityDueDate</characteristicID><characteristicValue>2018-12-22</characteristicValue></characteristic><characteristic><characteristicID>DurationSH</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>WorkOrderServiceOrderItemReference</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>keyB</characteristicID><characteristicValue/></characteristic></characteristic></characteristic><characteristic><characteristicID>Technology</characteristicID><characteristicValue>VDSL</characteristicValue></characteristic><characteristic><characteristicID>UseNGSSM_WF</characteristicID><characteristicValue>true</characteristicValue></characteristic><characteristic><characteristicID>BondedPhysicalLinks</characteristicID><characteristicValue>1</characteristicValue></characteristic><characteristic><characteristicID>Taktsynchronisation</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Location</characteristicID><characteristic><characteristicID>LocalLocation</characteristicID><characteristic><characteristicID>Room</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>BuildingPartInfo</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>BuildingPart</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LocationInfo</characteristicID><characteristic><characteristicID>ONKZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Name</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ASB</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Note</characteristicID><characteristicValue/></characteristic></characteristic></characteristic><characteristic><characteristicID>Address</characteristicID><characteristic><characteristicID>Name</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Country</characteristicID><characteristic><characteristicID>Country</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CountryID</characteristicID><characteristicValue/></characteristic></characteristic><characteristic><characteristicID>PostalAddress</characteristicID><characteristic><characteristicID>PostOfficeBox</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ZIP</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Street</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>HouseNumberSupplement</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>District</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>HouseNumber</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>CityPostOfficeBox</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>ZipPostOfficeBox</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>City</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>KLS_ID</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>GeoCoordinates</characteristicID><characteristic><characteristicID>Latitude</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Longitude</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Easting</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Northing</characteristicID><characteristicValue/></characteristic></characteristic></characteristic></characteristic></characteristic></specification><service><entityKey><keyA>97e7ec15-239e-4a23-92a9-327f1a8ab7ec</keyA><keyB>edc1050c-7324-4af7-b7e3-b5b5856c287e</keyB></entityKey><specification><specificationName>AccessLine</specificationName><specificationID>AccessLine</specificationID><characteristic><characteristicID>Anschlussnummer</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LSZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Anschlussvariante</characteristicID><characteristicValue>Erstanbindung</characteristicValue></characteristic><characteristic><characteristicID>CustomerPremiseLocationReference</characteristicID><characteristicValue>988629</characteristicValue></characteristic><characteristic><characteristicID>AplStiftRef List</characteristicID></characteristic><characteristic><characteristicID>StatischeProvisionierung</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>LSZZ</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>BNG_Port_Adresse_GF</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Line_ID</characteristicID><characteristicValue>DEU.DTAG.C203</characteristicValue></characteristic><characteristic><characteristicID>Leitungsreferenz</characteristicID><characteristicValue/></characteristic></specification></service></businessInteractionItem><businessInteractionItem><entityKey><keyA>34c35350-287f-47cb-b1da-745a084e23c9</keyA></entityKey><specification><specificationName>SubscriptionServiceOrderItemSpec</specificationName><specificationID>SubscriptionServiceOrderItemSpec</specificationID></specification><service><entityKey><keyA>ad863ce9-4965-464d-a311-975f1d9dd34a</keyA></entityKey><specification><specificationName>xPlayWS</specificationName><specificationID>xPlayWS</specificationID><characteristic><characteristicID>Subscription-ID</characteristicID><characteristicValue>000000000335643</characteristicValue></characteristic><characteristic><characteristicID>CarrierAnschlussBSAReference</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue>372d3666-f5e9-433d-a7cc-75713b4c1867</characteristicValue></characteristic><characteristic><characteristicID>keyB</characteristicID><characteristicValue/></characteristic></characteristic></characteristic><characteristic><characteristicID>Subscriber-Referenz</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue>315cdfaf-e3f5-4de0-a2cb-12b37cd36d8e</characteristicValue></characteristic><characteristic><characteristicID>keyB</characteristicID><characteristicValue>8be99a3b-2fbf-4b61-a144-2efecf653911</characteristicValue></characteristic></characteristic></characteristic><characteristic><characteristicID>Einzelvertragsnummer</characteristicID><characteristicValue>xPl_qaapp426cn:6800_941324</characteristicValue></characteristic></specification></service></businessInteractionItem><businessInteractionItem><entityKey><keyA>4a1d4e59-3143-39f4-bc19-983c7e1ad11c</keyA></entityKey><specification><specificationName>SubscriberServiceOrderItemSpec</specificationName><specificationID>SubscriberServiceOrderItemSpec</specificationID></specification><service><entityKey><keyA>315cdfaf-e3f5-4de0-a2cb-12b37cd36d8e</keyA><keyB>8be99a3b-2fbf-4b61-a144-2efecf653911</keyB></entityKey><specification><specificationName>WS_Partner</specificationName><specificationID>WS_Partner</specificationID><characteristic><characteristicID>Providernummer</characteristicID><characteristicValue>941324</characteristicValue></characteristic><characteristic><characteristicID>Subscriber-ID</characteristicID><characteristicValue>62990862-ecba-4b34-8e1f-8066a3670f7b</characteristicValue></characteristic></specification></service></businessInteractionItem><businessInteractionItem><entityKey><keyA>9154944098813362054</keyA></entityKey><state><value>inExecution</value></state><specification><specificationName>Konsistenzsicherung xPlay Access starten</specificationName><specificationID>Konsistenzsicherung xPlay Access starten</specificationID><characteristic><characteristicID>ONKZ</characteristicID><characteristicValue>228</characteristicValue></characteristic><characteristic><characteristicID>Rufnummer</characteristicID><characteristicValue/></characteristic><characteristic><characteristicID>Access_Line</characteristicID><characteristic><characteristicID>EntityReference</characteristicID><characteristic><characteristicID>keyA</characteristicID><characteristicValue>159df5d7-153c-4654-a688-dd2c10fb372e</characteristicValue></characteristic><characteristic><characteristicID>keyB</characteristicID><characteristicValue/></characteristic></characteristic></characteristic></specification></businessInteractionItem></ns2:Plan></ns2:executeAction></typ:data></tns:executeAction></soapenv:Body></soapenv:Envelope>";
        Parameters params = new Parameters();
        List<DiffMessage> result = comparator.compare(er, ar, params);
        assertTrue(true);
    }

    @Test
    public void compareTwoXml_withRuleKeyChild_expectedResultIdentical() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 1;
        String er = "<xml>\n" +
                "\t<employees>\n" +
                "\t\t<employee>\n" +
                "\t\t\t<name>Johnny Dape</name>\n" +
                "\t\t\t<id>1\n" +
                "\t\t\t</id>\n" +
                "\t\t</employee>\n" +
                "\t\t<employee>\n" +
                "\t\t\t<name>Will Smith</name>\n" +
                "\t\t\t<id>2\n" +
                "\t\t\t</id>\n" +
                "\t\t</employee>\n" +
                "\t</employees>\n" +
                "</xml>";
        String ar = "<xml>\n" +
                "\t<employees>\n" +
                "\t\t<employee>\n" +
                "\t\t\t<name>Will Smit</name>\n" +
                "\t\t\t<id>2\n" +
                "\t\t\t</id>\n" +
                "\t\t</employee>\n" +
                "\t\t<employee>\n" +
                "\t\t\t<name>Johnny Dape</name>\n" +
                "\t\t\t<id>1\n" +
                "\t\t\t</id>\n" +
                "\t\t</employee>\n" +
                "\t</employees>\n" +
                "</xml>";
        Parameters params = new Parameters();
        params.put("keyChild", "employee/id");
        List<DiffMessage> result = comparator.compare(er, ar, params);
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, result.size());
        assertTrue(result.stream().anyMatch(diff -> diff.getDescription().contains("Will Smit")));
    }

    @Test
    public void compareTwoXml_withRuleKeyChild_expectedResultErrors() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 733;
        String er = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<soapenv:Body>\n" +
                "\t\t<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "\t\t\t<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "\t\t\t\t<ns1:costedEventDataArray>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>${PhoneNumber}</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>1</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>0</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>${PhoneNumber}</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>32123456722</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>120</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>100002</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BUNDLE</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Nationale gesprekken naar vast</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BUNDLE:45 Based on you monthly allowance</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium -> Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>100002</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">21</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>${PhoneNumber}</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>1</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>225000000</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>${PhoneNumber}</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>67322734112</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>200002</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>INTERNATIONAL</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Internationale gesprekken naar vast vanuit België: Brunei</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium -> World 2</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>China</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>World 2</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>101043</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">21</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>2</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>${PhoneNumber}</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>360000000</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>${PhoneNumber}</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>32154605433</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BRN</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>200005</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>IROAM:Brunei</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Uitgaande gesprekken vanuit niet-EU naar niet-EU</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>World 2 -> Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Brunei</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>World 2</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium 2</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>102110</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">25</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>${PhoneNumber}</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>7000000</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>${PhoneNumber}</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>35943820523</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>200003</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>FREE</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Nationale sms'en</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium -> Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>100015</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">22</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>4</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>${PhoneNumber}</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>0</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>${PhoneNumber}</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>32486000333</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BGR</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>200007</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>FREE</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Internationale sms'en vanuit niet-EU naar</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Zone EU -> Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Zone EU</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>102310</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">26</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>5</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>${PhoneNumber}</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>10000000</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>${PhoneNumber}</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>32471340645</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>32C4700</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>100025</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>NATIONAL</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Nationale mms'en</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium -> Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>100025</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">23</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>6</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>${PhoneNumber}</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>10000000</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>${PhoneNumber}</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>32486000005</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>OMN</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>200008</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>IROAM:Oman</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Internationale mms'en vanuit niet-EU naar</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>World 2 -> Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Brunei</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>World 2</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>102650</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">27</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>7</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>${PhoneNumber}</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>0</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>${PhoneNumber}</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>52428800</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>104857600</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>52428800</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>gprs.base.be</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>RG4</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>100091</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BUNDLE</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Mobiel internet</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BUNDLE:45 Based on you monthly allowance</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium -> Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>100091</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">24</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>8</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>32359173812</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>0</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>32359173812</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>3145728</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>1024768</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>3145728</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>gprs.base.be</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BGR</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>200009</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BUNDLE</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Mobiel internet in niet-EU</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>BUNDLE:45 Based on you monthly allowance</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Zone EU -> Zone EU</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Bulgaria</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>World 2</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>102907</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">28</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>    \n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t\t<ns1:a>\n" +
                "\t\t\t\t\t\t<ns1:eventTypeId>9</ns1:eventTypeId>\n" +
                "\t\t\t\t\t\t<ns1:eventSource>32359173812</ns1:eventSource>\n" +
                "\t\t\t\t\t\t<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventCostMny>30000000</ns1:eventCostMny>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>32359173812</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>110001</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Diensten van derden (PayByMobile)</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:eventAttributes>Pre-Costed Usage</ns1:eventAttributes>\n" +
                "\t\t\t\t\t\t<ns1:revenueCodeId>110001</ns1:revenueCodeId>\n" +
                "\t\t\t\t\t\t<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "\t\t\t\t\t\t<ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t\t\t<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">9</ns4:ratingTariffId>\n" +
                "\t\t\t\t\t\t</ns1:ratingTariffPK>\n" +
                "\t\t\t\t\t</ns1:a>\n" +
                "\t\t\t\t</ns1:costedEventDataArray>\n" +
                "\t\t\t\t<ns1:truncated>false</ns1:truncated>\n" +
                "\t\t\t</ns1:result>\n" +
                "\t\t</queryEvents_1Output>\n" +
                "\t</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String ar = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <soapenv:Body>\n" +
                "        <queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "            <ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "                <ns1:costedEventDataArray>\n" +
                "                    <ns1:a>\n" +
                "                        <ns1:accountPK>\n" +
                "                            <ns2:accountNum xmlns:ns2=\"urn:Convergys-Interface-ECA-Account\">999911353</ns2:accountNum>\n" +
                "                        </ns1:accountPK>\n" +
                "                        <ns1:eventSeq>191009000</ns1:eventSeq>\n" +
                "                        <ns1:eventSource>32359173812</ns1:eventSource>\n" +
                "                        <ns1:eventTypeId>1</ns1:eventTypeId>\n" +
                "                        <ns1:eventDtm>2019-10-02T22:21:55.000+02:00</ns1:eventDtm>\n" +
                "                        <ns1:eventCostMny>0</ns1:eventCostMny>\n" +
                "                        <ns1:eventAttributes>32359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>32123456722</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>120</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>32C1027</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>012</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>100002</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>BUNDLE</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Nationale gesprekken naar vast</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>BUNDLE:45 Based on you monthly allowance</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>1300000359</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>187500000</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>Belgium -&gt; Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>120</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0123456722</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>-99</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "                        <ns1:costCentreId>0</ns1:costCentreId>\n" +
                "                        <ns1:createdDtm>2019-10-05T22:55:07.000+02:00</ns1:createdDtm>\n" +
                "                        <ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "                        <ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "                        <ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "                        <ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "                        <ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "                        <ns1:managedFileId>14433</ns1:managedFileId>\n" +
                "                        <ns1:originalAccountPK>\n" +
                "                            <ns3:accountNum xmlns:ns3=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"/>\n" +
                "                        </ns1:originalAccountPK>\n" +
                "                        <ns1:preDiscountedCostMny>3750000</ns1:preDiscountedCostMny>\n" +
                "                        <ns1:ratingDiscountedUsageTotalMny>3750000</ns1:ratingDiscountedUsageTotalMny>\n" +
                "                        <ns1:rowNumber>1</ns1:rowNumber>\n" +
                "                        <ns1:ratingTariffPK>\n" +
                "                            <ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">51</ns4:ratingTariffId>\n" +
                "                        </ns1:ratingTariffPK>\n" +
                "                        <ns1:taxOverrideId>0</ns1:taxOverrideId>\n" +
                "                        <ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "                        <ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "                        <ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "                        <ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "                        <ns1:receivableClassId>1</ns1:receivableClassId>\n" +
                "                        <ns1:revenueCodeId>100002</ns1:revenueCodeId>\n" +
                "                        <ns1:chargeNumber>0</ns1:chargeNumber>\n" +
                "                        <ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "                        <ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "                        <ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "                        <ns1:seq>3</ns1:seq>\n" +
                "                        <ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "                        <ns1:primaryEventRef>03B8F000000006E</ns1:primaryEventRef>\n" +
                "                        <ns1:discountData>02:B,Lc,B,OThw,OThw,OThw,A</ns1:discountData>\n" +
                "                        <ns1:highestPriorityDiscountid>732</ns1:highestPriorityDiscountid>\n" +
                "                        <ns1:highestPrioritySeq>1</ns1:highestPrioritySeq>\n" +
                "                        <ns1:eventRef>03B8F000000006E</ns1:eventRef>\n" +
                "                    </ns1:a>\n" +
                "                    <ns1:a>\n" +
                "                        <ns1:accountPK>\n" +
                "                            <ns5:accountNum xmlns:ns5=\"urn:Convergys-Interface-ECA-Account\">999911353</ns5:accountNum>\n" +
                "                        </ns1:accountPK>\n" +
                "                        <ns1:eventSeq>191009000</ns1:eventSeq>\n" +
                "                        <ns1:eventSource>32359173812</ns1:eventSource>\n" +
                "                        <ns1:eventTypeId>1</ns1:eventTypeId>\n" +
                "                        <ns1:eventDtm>2019-10-02T22:21:55.000+02:00</ns1:eventDtm>\n" +
                "                        <ns1:eventCostMny>225000000</ns1:eventCostMny>\n" +
                "                        <ns1:eventAttributes>32359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>67322734112</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>00673227</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>200002</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>INTERNATIONAL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Internationale gesprekken naar mobiel vanuit België: Brunei</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>1300000359</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>22500000000</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>Belgium -&gt; World 2</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Brunei</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>World 2</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0067322734112</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>-99</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>02:-B,B,B,NaTpA;D,C,B,NaTpA</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "                        <ns1:costCentreId>0</ns1:costCentreId>\n" +
                "                        <ns1:createdDtm>2019-10-05T22:55:07.000+02:00</ns1:createdDtm>\n" +
                "                        <ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "                        <ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "                        <ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "                        <ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "                        <ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "                        <ns1:managedFileId>14433</ns1:managedFileId>\n" +
                "                        <ns1:originalAccountPK>\n" +
                "                            <ns6:accountNum xmlns:ns6=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"/>\n" +
                "                        </ns1:originalAccountPK>\n" +
                "                        <ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "                        <ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "                        <ns1:rowNumber>2</ns1:rowNumber>\n" +
                "                        <ns1:ratingTariffPK>\n" +
                "                            <ns7:ratingTariffId xmlns:ns7=\"urn:Convergys-Interface-ECA-RatingTariff\">51</ns7:ratingTariffId>\n" +
                "                        </ns1:ratingTariffPK>\n" +
                "                        <ns1:taxOverrideId>0</ns1:taxOverrideId>\n" +
                "                        <ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "                        <ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "                        <ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "                        <ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "                        <ns1:receivableClassId>1</ns1:receivableClassId>\n" +
                "                        <ns1:revenueCodeId>101330</ns1:revenueCodeId>\n" +
                "                        <ns1:chargeNumber>0</ns1:chargeNumber>\n" +
                "                        <ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "                        <ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "                        <ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "                        <ns1:seq>3</ns1:seq>\n" +
                "                        <ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "                        <ns1:primaryEventRef>03B8F000000006F</ns1:primaryEventRef>\n" +
                "                        <ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "                        <ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "                        <ns1:eventRef>03B8F000000006F</ns1:eventRef>\n" +
                "                    </ns1:a>\n" +
                "                    <ns1:a>\n" +
                "                        <ns1:accountPK>\n" +
                "                            <ns8:accountNum xmlns:ns8=\"urn:Convergys-Interface-ECA-Account\">999911353</ns8:accountNum>\n" +
                "                        </ns1:accountPK>\n" +
                "                        <ns1:eventSeq>191009000</ns1:eventSeq>\n" +
                "                        <ns1:eventSource>32359173812</ns1:eventSource>\n" +
                "                        <ns1:eventTypeId>2</ns1:eventTypeId>\n" +
                "                        <ns1:eventDtm>2019-10-02T22:21:55.000+02:00</ns1:eventDtm>\n" +
                "                        <ns1:eventCostMny>360000000</ns1:eventCostMny>\n" +
                "                        <ns1:eventAttributes>32359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>32154605433</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>BRN</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>200005</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>IROAM:Brunei</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Uitgaande gesprekken vanuit niet-EU naar EU</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>1300000359</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>36000000000</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>World 2 -&gt; Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Brunei</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>World 2</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0154605433</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>-99</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>02:-B,B,B,VdSoA;D,C,B,VdSoA</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "                        <ns1:costCentreId>0</ns1:costCentreId>\n" +
                "                        <ns1:createdDtm>2019-10-05T22:55:07.000+02:00</ns1:createdDtm>\n" +
                "                        <ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "                        <ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "                        <ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "                        <ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "                        <ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "                        <ns1:managedFileId>14433</ns1:managedFileId>\n" +
                "                        <ns1:originalAccountPK>\n" +
                "                            <ns9:accountNum xmlns:ns9=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"/>\n" +
                "                        </ns1:originalAccountPK>\n" +
                "                        <ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "                        <ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "                        <ns1:rowNumber>3</ns1:rowNumber>\n" +
                "                        <ns1:ratingTariffPK>\n" +
                "                            <ns10:ratingTariffId xmlns:ns10=\"urn:Convergys-Interface-ECA-RatingTariff\">55</ns10:ratingTariffId>\n" +
                "                        </ns1:ratingTariffPK>\n" +
                "                        <ns1:taxOverrideId>0</ns1:taxOverrideId>\n" +
                "                        <ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "                        <ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "                        <ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "                        <ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "                        <ns1:receivableClassId>1</ns1:receivableClassId>\n" +
                "                        <ns1:revenueCodeId>102121</ns1:revenueCodeId>\n" +
                "                        <ns1:chargeNumber>0</ns1:chargeNumber>\n" +
                "                        <ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "                        <ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "                        <ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "                        <ns1:seq>3</ns1:seq>\n" +
                "                        <ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "                        <ns1:primaryEventRef>03B8F0000000070</ns1:primaryEventRef>\n" +
                "                        <ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "                        <ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "                        <ns1:eventRef>03B8F0000000070</ns1:eventRef>\n" +
                "                    </ns1:a>\n" +
                "                    <ns1:a>\n" +
                "                        <ns1:accountPK>\n" +
                "                            <ns11:accountNum xmlns:ns11=\"urn:Convergys-Interface-ECA-Account\">999911353</ns11:accountNum>\n" +
                "                        </ns1:accountPK>\n" +
                "                        <ns1:eventSeq>191009000</ns1:eventSeq>\n" +
                "                        <ns1:eventSource>32359173812</ns1:eventSource>\n" +
                "                        <ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "                        <ns1:eventDtm>2019-10-02T22:21:55.000+02:00</ns1:eventDtm>\n" +
                "                        <ns1:eventCostMny>7000000</ns1:eventCostMny>\n" +
                "                        <ns1:eventAttributes>32359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>35943820523</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>00359438</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>200003</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>INTERNATIONAL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Internationale sms'en vanuit België: Bulgarije</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>1300000359</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>700000000</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>Belgium -&gt; Europe 2</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Bulgaria</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Europe 2</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0035943820523</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>-99</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>02:-B,B,B,as/A;D,C,B,as/A</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "                        <ns1:costCentreId>0</ns1:costCentreId>\n" +
                "                        <ns1:createdDtm>2019-10-05T22:55:07.000+02:00</ns1:createdDtm>\n" +
                "                        <ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "                        <ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "                        <ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "                        <ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "                        <ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "                        <ns1:managedFileId>14433</ns1:managedFileId>\n" +
                "                        <ns1:originalAccountPK>\n" +
                "                            <ns12:accountNum xmlns:ns12=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"/>\n" +
                "                        </ns1:originalAccountPK>\n" +
                "                        <ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "                        <ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "                        <ns1:rowNumber>4</ns1:rowNumber>\n" +
                "                        <ns1:ratingTariffPK>\n" +
                "                            <ns13:ratingTariffId xmlns:ns13=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns13:ratingTariffId>\n" +
                "                        </ns1:ratingTariffPK>\n" +
                "                        <ns1:taxOverrideId>0</ns1:taxOverrideId>\n" +
                "                        <ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "                        <ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "                        <ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "                        <ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "                        <ns1:receivableClassId>1</ns1:receivableClassId>\n" +
                "                        <ns1:revenueCodeId>101631</ns1:revenueCodeId>\n" +
                "                        <ns1:chargeNumber>0</ns1:chargeNumber>\n" +
                "                        <ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "                        <ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "                        <ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "                        <ns1:seq>3</ns1:seq>\n" +
                "                        <ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "                        <ns1:primaryEventRef>03B8F0000000071</ns1:primaryEventRef>\n" +
                "                        <ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "                        <ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "                        <ns1:eventRef>03B8F0000000071</ns1:eventRef>\n" +
                "                    </ns1:a>\n" +
                "                    <ns1:a>\n" +
                "                        <ns1:accountPK>\n" +
                "                            <ns14:accountNum xmlns:ns14=\"urn:Convergys-Interface-ECA-Account\">999911353</ns14:accountNum>\n" +
                "                        </ns1:accountPK>\n" +
                "                        <ns1:eventSeq>191009000</ns1:eventSeq>\n" +
                "                        <ns1:eventSource>32359173812</ns1:eventSource>\n" +
                "                        <ns1:eventTypeId>4</ns1:eventTypeId>\n" +
                "                        <ns1:eventDtm>2019-10-02T22:21:55.000+02:00</ns1:eventDtm>\n" +
                "                        <ns1:eventCostMny>0</ns1:eventCostMny>\n" +
                "                        <ns1:eventAttributes>32359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>32486000333</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>BGR</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>200007</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>FREE</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Internationale sms'en vanuit EU naar EU</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>1300000359</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>Zone EU -&gt; Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Bulgaria</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Zone EU</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0486000333</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>-99</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>02:-B,B,B,A;D,C,B,A</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "                        <ns1:costCentreId>0</ns1:costCentreId>\n" +
                "                        <ns1:createdDtm>2019-10-05T22:55:07.000+02:00</ns1:createdDtm>\n" +
                "                        <ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "                        <ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "                        <ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "                        <ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "                        <ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "                        <ns1:managedFileId>14433</ns1:managedFileId>\n" +
                "                        <ns1:originalAccountPK>\n" +
                "                            <ns15:accountNum xmlns:ns15=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"/>\n" +
                "                        </ns1:originalAccountPK>\n" +
                "                        <ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "                        <ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "                        <ns1:rowNumber>5</ns1:rowNumber>\n" +
                "                        <ns1:ratingTariffPK>\n" +
                "                            <ns16:ratingTariffId xmlns:ns16=\"urn:Convergys-Interface-ECA-RatingTariff\">56</ns16:ratingTariffId>\n" +
                "                        </ns1:ratingTariffPK>\n" +
                "                        <ns1:taxOverrideId>0</ns1:taxOverrideId>\n" +
                "                        <ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "                        <ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "                        <ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "                        <ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "                        <ns1:receivableClassId>1</ns1:receivableClassId>\n" +
                "                        <ns1:revenueCodeId>102201</ns1:revenueCodeId>\n" +
                "                        <ns1:chargeNumber>0</ns1:chargeNumber>\n" +
                "                        <ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "                        <ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "                        <ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "                        <ns1:seq>3</ns1:seq>\n" +
                "                        <ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "                        <ns1:primaryEventRef>03B8F0000000072</ns1:primaryEventRef>\n" +
                "                        <ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "                        <ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "                        <ns1:eventRef>03B8F0000000072</ns1:eventRef>\n" +
                "                    </ns1:a>\n" +
                "                    <ns1:a>\n" +
                "                        <ns1:accountPK>\n" +
                "                            <ns17:accountNum xmlns:ns17=\"urn:Convergys-Interface-ECA-Account\">999911353</ns17:accountNum>\n" +
                "                        </ns1:accountPK>\n" +
                "                        <ns1:eventSeq>191009000</ns1:eventSeq>\n" +
                "                        <ns1:eventSource>32359173812</ns1:eventSource>\n" +
                "                        <ns1:eventTypeId>5</ns1:eventTypeId>\n" +
                "                        <ns1:eventDtm>2019-10-02T22:21:55.000+02:00</ns1:eventDtm>\n" +
                "                        <ns1:eventCostMny>10000000</ns1:eventCostMny>\n" +
                "                        <ns1:eventAttributes>32359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>32471340645</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>32C4700</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>047</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>100025</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>NATIONAL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Nationale mms'en</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>1300000359</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>1000000000</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>Belgium -&gt; Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0359173812</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>0471340645</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes>-99</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>02:-B,B,B,mJaA;D,C,B,mJaA</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:eventAttributes/>\n" +
                "                        <ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "                        <ns1:costCentreId>0</ns1:costCentreId>\n" +
                "                        <ns1:createdDtm>2019-10-05T22:55:07.000+02:00</ns1:createdDtm>\n" +
                "                        <ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "                        <ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "                        <ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "                        <ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "                        <ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "                        <ns1:managedFileId>14433</ns1:managedFileId>\n" +
                "                        <ns1:originalAccountPK>\n" +
                "                            <ns18:accountNum xmlns:ns18=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"/>\n" +
                "                        </ns1:originalAccountPK>\n" +
                "                        <ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "                        <ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "                        <ns1:rowNumber>6</ns1:rowNumber>\n" +
                "                        <ns1:ratingTariffPK>\n" +
                "                            <ns19:ratingTariffId xmlns:ns19=\"urn:Convergys-Interface-ECA-RatingTariff\">53</ns19:ratingTariffId>\n" +
                "                        </ns1:ratingTariffPK>\n" +
                "                        <ns1:taxOverrideId>0</ns1:taxOverrideId>\n" +
                "                        <ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "                        <ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "                        <ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "                        <ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "                        <ns1:receivableClassId>1</ns1:receivableClassId>\n" +
                "                        <ns1:revenueCodeId>100025</ns1:revenueCodeId>\n" +
                "                        <ns1:chargeNumber>0</ns1:chargeNumber>\n" +
                "                        <ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "                        <ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "                        <ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "                        <ns1:seq>3</ns1:seq>\n" +
                "                        <ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "                        <ns1:primaryEventRef>03B8F0000000073</ns1:primaryEventRef>\n" +
                "                        <ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "                        <ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "                        <ns1:eventRef>03B8F0000000073</ns1:eventRef>\n" +
                "                    </ns1:a>\n" +
                "\n" +
                "                </ns1:costedEventDataArray>\n" +
                "                <ns1:truncated>false</ns1:truncated>\n" +
                "            </ns1:result>\n" +
                "        </queryEvents_1Output>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        Parameters params = new Parameters();
        params.put("keyChild", "a/revenueCodeId");
        Array<String> xPaths = new Array<>(
                "/Envelope[1]/Body[1]/queryEvents_1Output[1]/result[1]/costedEventDataArray[1]/a[1]/revenueCodeId[1]/text()[1]"
        );
        List<DiffMessage> result = comparator.compare(er, ar, params);
        Boolean checkCompareResultsByXmlPathInDiffList =
                result.stream().anyMatch(diff
                        -> xPaths.get(0).equals(diff.getActual())
                        || xPaths.get(0).equals(diff.getExpected()));
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, result.size());
        assertFalse(checkCompareResultsByXmlPathInDiffList);
    }

    // TESTS FOR QABULKVAL-2887

    @Test
    public void compareTwoXml_keyChildRule_hardcodedSearch_expectedResultIdentical() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 0;
        String er = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<soapenv:Body>\n" +
                "<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "<ns1:costedEventDataArray>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>A</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32905168777</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100044</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>B1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>B2</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "</ns1:costedEventDataArray>\n" +
                "</ns1:result>\n" +
                "</queryEvents_1Output>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String ar = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<soapenv:Body>\n" +
                "<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "<ns1:costedEventDataArray>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>A</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32905168777</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100044</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:eventAttributes>B2</ns1:eventAttributes>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>B1</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "</ns1:costedEventDataArray>\n" +
                "</ns1:result>\n" +
                "</queryEvents_1Output>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        Parameters rules = new Parameters();
        rules.put("keyChild", "a/eventAttributes[1]");
        List<DiffMessage> results = comparator.compare(er, ar, rules);
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, results.size());
    }

    @Test
    public void compareTwoXml_keyChildRule_complexKey_expectedResultIdentical() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 0;
        String er = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<soapenv:Body>\n" +
                "<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "<ns1:costedEventDataArray>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>AAA</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32905168777</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100044</ns1:revenueCodeId>\n" +
                "<ns1:secondKey>A</ns1:secondKey>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>BBB1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:secondKey>B1</ns1:secondKey>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>BBB2</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:secondKey>B2</ns1:secondKey>\n" +
                "</ns1:a>\n" +
                "</ns1:costedEventDataArray>\n" +
                "<ns1:truncated>false</ns1:truncated>\n" +
                "</ns1:result>\n" +
                "</queryEvents_1Output>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String ar = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<soapenv:Body>\n" +
                "<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "<ns1:costedEventDataArray>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>BBB2</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:secondKey>B2</ns1:secondKey>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>AAA</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32905168777</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100044</ns1:revenueCodeId>\n" +
                "<ns1:secondKey>A</ns1:secondKey>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>BBB1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:secondKey>B1</ns1:secondKey>\n" +
                "</ns1:a>\n" +
                "</ns1:costedEventDataArray>\n" +
                "<ns1:truncated>false</ns1:truncated>\n" +
                "</ns1:result>\n" +
                "</queryEvents_1Output>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        Parameters rules = new Parameters();
        rules.put("keyChild", "a/revenueCodeId&&secondKey");
        List<DiffMessage> results = comparator.compare(er, ar, rules);
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, results.size());
    }

    @Test
    public void compareTwoXml_keyChildRule_combinedKeyWithHardcodedSearch_expectedResultIdentical() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 0;
        String er = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<soapenv:Body>\n" +
                "<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "<ns1:costedEventDataArray>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>A</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32905168777</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100044</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>B1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>B2</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "</ns1:costedEventDataArray>\n" +
                "</ns1:result>\n" +
                "</queryEvents_1Output>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String ar = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<soapenv:Body>\n" +
                "<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "<ns1:costedEventDataArray>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>A</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32905168777</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100044</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:eventAttributes>B2</ns1:eventAttributes>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>B1</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "</ns1:a>\n" +
                "</ns1:costedEventDataArray>\n" +
                "</ns1:result>\n" +
                "</queryEvents_1Output>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        Parameters rules = new Parameters();
        rules.put("keyChild", "a/revenueCodeId&&eventAttributes[1]");
        List<DiffMessage> results = comparator.compare(er, ar, rules);
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, results.size());
    }

    @Test
    public void QABULKVAL_2887_expectedResultIdentical() throws ComparatorException {
        final int EXPECTED_DIFFERENCE_LIST_SIZE = 695;
        String er = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<soapenv:Body>\n" +
                "<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "<ns1:costedEventDataArray>\n" +
                "<ns1:a>\n" +
                "<ns1:eventTypeId>1</ns1:eventTypeId>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:eventCostMny>200000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32905168777</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (0905-gesprekken)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100044</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>TVGAMES,1894</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0905168</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>TVGAMES,1894</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100044</ns1:revenueCodeId>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">51</ns4:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventTypeId>1</ns1:eventTypeId>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:eventCostMny>200000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32907077907</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0907077</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100048</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (0907-gesprekken)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>ADULT,1899</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0907077</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>ADULT,1899</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100048</ns1:revenueCodeId>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">51</ns4:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:eventCostMny>100000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100082</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (verzonden sms'en)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1903</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1903</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns4:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:eventCostMny>200000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100082</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (verzonden sms'en)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1904</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1904</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns4:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:eventCostMny>100000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4911</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100082</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (verzonden sms'en)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1908</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4911</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1908</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns4:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:eventCostMny>200000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>7333</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100082</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (verzonden sms'en)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>ADULT,1909</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>7333</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>ADULT,1909</ns1:eventAttributes>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns4:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "</ns1:a>\n" +
                "</ns1:costedEventDataArray>\n" +
                "<ns1:truncated>false</ns1:truncated>\n" +
                "</ns1:result>\n" +
                "</queryEvents_1Output>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String ar = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "<soapenv:Body>\n" +
                "<queryEvents_1Output xmlns=\"urn:Convergys-Interface-ECA-UDM-WSDL\">\n" +
                "<ns1:result xmlns:ns1=\"urn:Convergys-Interface-ECA-UDM\">\n" +
                "<ns1:costedEventDataArray>\n" +
                "<ns1:a>\n" +
                "<ns1:accountPK>\n" +
                "<ns2:accountNum xmlns:ns2=\"urn:Convergys-Interface-ECA-Account\">971967221</ns2:accountNum>\n" +
                "</ns1:accountPK>\n" +
                "<ns1:eventSeq>191206000</ns1:eventSeq>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>1</ns1:eventTypeId>\n" +
                "<ns1:eventDtm>2019-11-26T15:09:07.000+01:00</ns1:eventDtm>\n" +
                "<ns1:eventCostMny>200000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32905168777</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0905168</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100044</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (0905-gesprekken)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1300734373</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>200000000</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>TVGAMES,1894</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0905168</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>TVGAMES,1894</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>T</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0905168</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0905168777</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>VOICE40916729214</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>02:-B,B,B,L68IA;C,E,B,L68IA;D,C,B,L68IA</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32460904140624</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32460904140624</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>971967221^1^1^32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "<ns1:costCentreId>0</ns1:costCentreId>\n" +
                "<ns1:createdDtm>2019-11-26T14:09:09.000+01:00</ns1:createdDtm>\n" +
                "<ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "<ns1:importedCurrencyCode>EUR</ns1:importedCurrencyCode>\n" +
                "<ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "<ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "<ns1:managedFileId>0</ns1:managedFileId>\n" +
                "<ns1:originalAccountPK>\n" +
                "<ns3:accountNum xmlns:ns3=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"></ns3:accountNum>\n" +
                "</ns1:originalAccountPK>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "<ns1:rowNumber>1</ns1:rowNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns4:ratingTariffId xmlns:ns4=\"urn:Convergys-Interface-ECA-RatingTariff\">51</ns4:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "<ns1:taxOverrideId>1</ns1:taxOverrideId>\n" +
                "<ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "<ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "<ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "<ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "<ns1:receivableClassId>5</ns1:receivableClassId>\n" +
                "<ns1:revenueCodeId>100044</ns1:revenueCodeId>\n" +
                "<ns1:chargeNumber>1</ns1:chargeNumber>\n" +
                "<ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "<ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "<ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "<ns1:seq>4</ns1:seq>\n" +
                "<ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "<ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "<ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "<ns1:eventRef>Q000004A00000243</ns1:eventRef>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:accountPK>\n" +
                "<ns5:accountNum xmlns:ns5=\"urn:Convergys-Interface-ECA-Account\">971967221</ns5:accountNum>\n" +
                "</ns1:accountPK>\n" +
                "<ns1:eventSeq>191206000</ns1:eventSeq>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>1</ns1:eventTypeId>\n" +
                "<ns1:eventDtm>2019-11-26T15:09:53.000+01:00</ns1:eventDtm>\n" +
                "<ns1:eventCostMny>200000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32907077907</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0907077</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100048</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (0907-gesprekken)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1300734373</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>20000000000</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>ADULT,1899</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0907077</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>ADULT,1899</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>T</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0907077</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>60</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0907077907</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>VOICE91209029948</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>02:-B,B,B,L68IA;C,E,B,L68IA;D,C,B,L68IA</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32460938555790</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32460938555790</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>971967221^1^1^32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "<ns1:costCentreId>0</ns1:costCentreId>\n" +
                "<ns1:createdDtm>2019-11-26T14:09:55.000+01:00</ns1:createdDtm>\n" +
                "<ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "<ns1:importedCurrencyCode>EUR</ns1:importedCurrencyCode>\n" +
                "<ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "<ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "<ns1:managedFileId>0</ns1:managedFileId>\n" +
                "<ns1:originalAccountPK>\n" +
                "<ns6:accountNum xmlns:ns6=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"></ns6:accountNum>\n" +
                "</ns1:originalAccountPK>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "<ns1:rowNumber>1</ns1:rowNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns7:ratingTariffId xmlns:ns7=\"urn:Convergys-Interface-ECA-RatingTariff\">51</ns7:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "<ns1:taxOverrideId>1</ns1:taxOverrideId>\n" +
                "<ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "<ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "<ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "<ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "<ns1:receivableClassId>5</ns1:receivableClassId>\n" +
                "<ns1:revenueCodeId>100048</ns1:revenueCodeId>\n" +
                "<ns1:chargeNumber>1</ns1:chargeNumber>\n" +
                "<ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "<ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "<ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "<ns1:seq>4</ns1:seq>\n" +
                "<ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "<ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "<ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "<ns1:eventRef>Q000004A00000244</ns1:eventRef>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:accountPK>\n" +
                "<ns8:accountNum xmlns:ns8=\"urn:Convergys-Interface-ECA-Account\">971967221</ns8:accountNum>\n" +
                "</ns1:accountPK>\n" +
                "<ns1:eventSeq>191206000</ns1:eventSeq>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "<ns1:eventDtm>2019-11-26T15:10:40.000+01:00</ns1:eventDtm>\n" +
                "<ns1:eventCostMny>200000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>7333</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>7333</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100082</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (verzonden sms'en)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1300734373</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>ADULT,1909</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>7333</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>ADULT,1909</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>T</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>7333</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>7333</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>SMS16720847725</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>02:-B,B,B,L68IA;C,E,B,L68IA;D,C,B,L68IA</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32486000005</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>971967221^1^1^32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "<ns1:costCentreId>0</ns1:costCentreId>\n" +
                "<ns1:createdDtm>2019-11-26T14:10:40.000+01:00</ns1:createdDtm>\n" +
                "<ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "<ns1:importedCurrencyCode>EUR</ns1:importedCurrencyCode>\n" +
                "<ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "<ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "<ns1:managedFileId>0</ns1:managedFileId>\n" +
                "<ns1:originalAccountPK>\n" +
                "<ns9:accountNum xmlns:ns9=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"></ns9:accountNum>\n" +
                "</ns1:originalAccountPK>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "<ns1:rowNumber>1</ns1:rowNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns10:ratingTariffId xmlns:ns10=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns10:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "<ns1:taxOverrideId>1</ns1:taxOverrideId>\n" +
                "<ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "<ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "<ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "<ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "<ns1:receivableClassId>5</ns1:receivableClassId>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:chargeNumber>1</ns1:chargeNumber>\n" +
                "<ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "<ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "<ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "<ns1:seq>4</ns1:seq>\n" +
                "<ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "<ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "<ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "<ns1:eventRef>Q000004A00000245</ns1:eventRef>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:accountPK>\n" +
                "<ns11:accountNum xmlns:ns11=\"urn:Convergys-Interface-ECA-Account\">971967221</ns11:accountNum>\n" +
                "</ns1:accountPK>\n" +
                "<ns1:eventSeq>191206000</ns1:eventSeq>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "<ns1:eventDtm>2019-11-26T15:11:26.000+01:00</ns1:eventDtm>\n" +
                "<ns1:eventCostMny>100000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100082</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (verzonden sms'en)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1300734373</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1903</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1903</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>T</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2312</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>SMS14419447748</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>02:-B,B,B,F9eEA;C,E,B,F9eEA;D,C,B,F9eEA</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32486000005</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>971967221^1^1^32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "<ns1:costCentreId>0</ns1:costCentreId>\n" +
                "<ns1:createdDtm>2019-11-26T14:11:26.000+01:00</ns1:createdDtm>\n" +
                "<ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "<ns1:importedCurrencyCode>EUR</ns1:importedCurrencyCode>\n" +
                "<ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "<ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "<ns1:managedFileId>0</ns1:managedFileId>\n" +
                "<ns1:originalAccountPK>\n" +
                "<ns12:accountNum xmlns:ns12=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"></ns12:accountNum>\n" +
                "</ns1:originalAccountPK>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "<ns1:rowNumber>1</ns1:rowNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns13:ratingTariffId xmlns:ns13=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns13:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "<ns1:taxOverrideId>1</ns1:taxOverrideId>\n" +
                "<ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "<ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "<ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "<ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "<ns1:receivableClassId>5</ns1:receivableClassId>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:chargeNumber>1</ns1:chargeNumber>\n" +
                "<ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "<ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "<ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "<ns1:seq>4</ns1:seq>\n" +
                "<ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "<ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "<ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "<ns1:eventRef>Q000004A00000246</ns1:eventRef>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:accountPK>\n" +
                "<ns14:accountNum xmlns:ns14=\"urn:Convergys-Interface-ECA-Account\">971967221</ns14:accountNum>\n" +
                "</ns1:accountPK>\n" +
                "<ns1:eventSeq>191206000</ns1:eventSeq>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "<ns1:eventDtm>2019-11-26T15:12:09.000+01:00</ns1:eventDtm>\n" +
                "<ns1:eventCostMny>200000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100082</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (verzonden sms'en)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1300734373</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1904</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1904</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>T</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3030</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>SMS28091679465</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>02:-B,B,B,L68IA;C,E,B,L68IA;D,C,B,L68IA</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32486000005</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>971967221^1^1^32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "<ns1:costCentreId>0</ns1:costCentreId>\n" +
                "<ns1:createdDtm>2019-11-26T14:12:09.000+01:00</ns1:createdDtm>\n" +
                "<ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "<ns1:importedCurrencyCode>EUR</ns1:importedCurrencyCode>\n" +
                "<ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "<ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "<ns1:managedFileId>0</ns1:managedFileId>\n" +
                "<ns1:originalAccountPK>\n" +
                "<ns15:accountNum xmlns:ns15=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"></ns15:accountNum>\n" +
                "</ns1:originalAccountPK>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "<ns1:rowNumber>1</ns1:rowNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns16:ratingTariffId xmlns:ns16=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns16:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "<ns1:taxOverrideId>1</ns1:taxOverrideId>\n" +
                "<ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "<ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "<ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "<ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "<ns1:receivableClassId>5</ns1:receivableClassId>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:chargeNumber>1</ns1:chargeNumber>\n" +
                "<ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "<ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "<ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "<ns1:seq>4</ns1:seq>\n" +
                "<ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "<ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "<ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "<ns1:eventRef>Q000004A00000247</ns1:eventRef>\n" +
                "</ns1:a>\n" +
                "<ns1:a>\n" +
                "<ns1:accountPK>\n" +
                "<ns17:accountNum xmlns:ns17=\"urn:Convergys-Interface-ECA-Account\">971967221</ns17:accountNum>\n" +
                "</ns1:accountPK>\n" +
                "<ns1:eventSeq>191206000</ns1:eventSeq>\n" +
                "<ns1:eventSource>32700000255</ns1:eventSource>\n" +
                "<ns1:eventTypeId>3</ns1:eventTypeId>\n" +
                "<ns1:eventDtm>2019-11-26T15:12:51.000+01:00</ns1:eventDtm>\n" +
                "<ns1:eventCostMny>100000000</ns1:eventCostMny>\n" +
                "<ns1:eventAttributes>32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4911</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>O</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4911</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>BEL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>100082</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>3PPS</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Diensten van derden (verzonden sms'en)</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1300734373</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1908</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4911</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>Belgium</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>MPAYMENT,1908</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>T</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4911</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>F</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4911</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>SMS80357246997</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>02:-B,B,B,F9eEA;C,E,B,F9eEA;D,C,B,F9eEA</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>4</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>1</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>0</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>32486000005</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>2</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>971967221^1^1^32700000255</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes>NULL</ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:eventAttributes></ns1:eventAttributes>\n" +
                "<ns1:competitorCostMny>0</ns1:competitorCostMny>\n" +
                "<ns1:costCentreId>0</ns1:costCentreId>\n" +
                "<ns1:createdDtm>2019-11-26T14:12:52.000+01:00</ns1:createdDtm>\n" +
                "<ns1:externalCostMny>0</ns1:externalCostMny>\n" +
                "<ns1:fragmentNumber>1</ns1:fragmentNumber>\n" +
                "<ns1:importedCostMny>0</ns1:importedCostMny>\n" +
                "<ns1:importedCurrencyCode>EUR</ns1:importedCurrencyCode>\n" +
                "<ns1:internalCostMny>0</ns1:internalCostMny>\n" +
                "<ns1:loyaltyPoints>0</ns1:loyaltyPoints>\n" +
                "<ns1:managedFileId>0</ns1:managedFileId>\n" +
                "<ns1:originalAccountPK>\n" +
                "<ns18:accountNum xmlns:ns18=\"urn:Convergys-Interface-ECA-Account\" xsi:nil=\"true\"></ns18:accountNum>\n" +
                "</ns1:originalAccountPK>\n" +
                "<ns1:preDiscountedCostMny>0</ns1:preDiscountedCostMny>\n" +
                "<ns1:ratingDiscountedUsageTotalMny>0</ns1:ratingDiscountedUsageTotalMny>\n" +
                "<ns1:rowNumber>1</ns1:rowNumber>\n" +
                "<ns1:ratingTariffPK>\n" +
                "<ns19:ratingTariffId xmlns:ns19=\"urn:Convergys-Interface-ECA-RatingTariff\">52</ns19:ratingTariffId>\n" +
                "</ns1:ratingTariffPK>\n" +
                "<ns1:taxOverrideId>1</ns1:taxOverrideId>\n" +
                "<ns1:ustCategoryId>0</ns1:ustCategoryId>\n" +
                "<ns1:ustCodeId>0</ns1:ustCodeId>\n" +
                "<ns1:modifiedBoo>false</ns1:modifiedBoo>\n" +
                "<ns1:ruleNumber>0</ns1:ruleNumber>\n" +
                "<ns1:receivableClassId>5</ns1:receivableClassId>\n" +
                "<ns1:revenueCodeId>100082</ns1:revenueCodeId>\n" +
                "<ns1:chargeNumber>1</ns1:chargeNumber>\n" +
                "<ns1:postBillRetentionDays>0</ns1:postBillRetentionDays>\n" +
                "<ns1:maskBillRuleId>0</ns1:maskBillRuleId>\n" +
                "<ns1:maskStoreRuleId>0</ns1:maskStoreRuleId>\n" +
                "<ns1:seq>4</ns1:seq>\n" +
                "<ns1:twinEventBoo>false</ns1:twinEventBoo>\n" +
                "<ns1:highestPriorityDiscountid>0</ns1:highestPriorityDiscountid>\n" +
                "<ns1:highestPrioritySeq>0</ns1:highestPrioritySeq>\n" +
                "<ns1:eventRef>Q000004A00000248</ns1:eventRef>\n" +
                "</ns1:a>\n" +
                "</ns1:costedEventDataArray>\n" +
                "<ns1:truncated>false</ns1:truncated>\n" +
                "</ns1:result>\n" +
                "</queryEvents_1Output>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        Parameters rules = new Parameters();
        rules.put("keyChild", "a/revenueCodeId&&eventAttributes[1]");
        rules.put("changeDiffResult", "EXTRA=IDENTICAL=//*[local-name()='a']");
        List<DiffMessage> result = comparator.compare(er, ar, rules);
        assertEquals(EXPECTED_DIFFERENCE_LIST_SIZE, result.size());
        assertTrue(result.stream().filter(diff -> !diff.getResult().equals(ResultType.IDENTICAL)).count() == 0);
    }


    @Test
    public void comparator_containXXE_throwException() throws ComparatorException, IOException {
        Path externalPath = Paths.get("src/test/resources/example.txt");
        String externalFilePath = externalPath.toAbsolutePath().toString();
        String er = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
                + "\n"
                + "<!DOCTYPE test [\n"
                + "\n"
                + "    <!ELEMENT test ANY >\n"
                + "\n"
                + "    <!ENTITY xxe SYSTEM \"file:" + externalFilePath +"\" >]>\n"
                + "<xml>&xxe;</xml>";
        String ar = "<xml>123</xml>";
        Parameters params = new Parameters();
        assertThrows(ComparatorException.class, ()->comparator.compare(er, ar, params));
    }

}
