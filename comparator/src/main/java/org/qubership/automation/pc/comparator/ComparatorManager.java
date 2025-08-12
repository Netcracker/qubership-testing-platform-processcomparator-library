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

package org.qubership.automation.pc.comparator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.CompareResult;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultData;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.ComparatorConfiguration;
import org.qubership.automation.pc.configuration.ComparatorConfigurationSet;
import org.qubership.automation.pc.configuration.Rule;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.ComparatorFactory;
import org.qubership.automation.pc.core.enums.CompareResultType;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.exceptions.ComparatorManagerException;
import org.qubership.automation.pc.core.exceptions.ComparatorNotFoundException;
import org.qubership.automation.pc.core.exceptions.FactoryInstatiationException;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.helpers.ThreadUtils;
import org.qubership.automation.pc.core.interfaces.IComparator;
import org.qubership.automation.pc.core.threads.CompareSession;
import org.qubership.automation.pc.core.threads.CompareSessionStatus;
import org.qubership.automation.pc.core.threads.CompareSessionsManager;
import org.qubership.automation.pc.core.threads.MultiThreadsQueue;
import org.qubership.automation.pc.core.utils.ComparatorUtils;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Core component responsible for orchestrating the data comparison process.
 * <p>
 * Handles comparison logic for different data types (e.g., SIMPLE, PROCESS),
 * supports multithreaded execution for large datasets, and manages transformation,
 * filtering, and evaluation of comparison results.
 * <p>
 * Provides entry points for comparing structured or unstructured data, calculating
 * summary results, and applying substitution and filtering rules.
 */
public class ComparatorManager {

    public static final String COMPARE_STEPS_COUNT_NAME = "compareStepsCount";
    public static final String INCLUDE_DATA_CONTENT_NAME = "includeDataContent";
    public static final String COMPARE_PARAMETERS_COUNT_NAME = "compareParametersCount";
    public static final String SKIP_STEP_WITH_DUBLICATES_NAME = "skipStepWithDublicates";

    public static final String COMPARE_AS = "compareAs";
    public static final String CHANGE_COMPARE_RESULT = "changeResult";
    public static final String EXCLUDE_DIFF_WITH_STATUS = "excludeDiffWithStatus";
    public static final String PARAMETER_ER_SUBSTITUTION = "erSubstitution";

    private static final Logger log = LoggerFactory.getLogger(ComparatorManager.class);
    private static final int PARALLEL_THRESHOLD = 10;
    private static final int MAX_THREADS = 100;
    private boolean parallelCompare = false;

    public List<CompareResult> compare(List<DataPackage> dataPackages,
                                       ComparatorConfiguration configuration)
            throws ComparatorManagerException, InterruptedException {
        List<CompareResult> results = new ArrayList<>();
        List<DataPackage> contextPackages = new ArrayList<>();
        MultiThreadsQueue queue = null;
        log.debug("[ComparatorManager] multiThread start");
        if (!dataPackages.isEmpty()) {
            // 1st loop - collect 'CONTEXT_PARAMETER's if exist
            for (DataPackage contextPackage : dataPackages) {
                if (contextPackage.getEr().getDataType() == DataType.CONTEXT_PARAMETER) {
                    // decode er & ar of context parameters
                    DataPackage dpack = contextPackage;
                    String erValue = dpack.getEr().getContent();

                    if (!erValue.isEmpty()) {
                        dpack.getEr().setContent(erValue);
                    }
                    for (Data thisAR : dpack.getAr()) {
                        String arValue = thisAR.getContent();
                        if (!StringUtils.isBlank(arValue)) {
                            thisAR.setContent(DataContentConverter.toString(arValue));
                        }
                    }
                    contextPackages.add(dpack);
                }
            }

            if (dataPackages.size() >= PARALLEL_THRESHOLD) {
                parallelCompare = true;
                queue = new MultiThreadsQueue();
                queue.setSize(MAX_THREADS);
            }
            for (final DataPackage dataPackage : dataPackages) {
                switch (dataPackage.getEr().getDataType()) {
                    case SIMPLE:
                        dataPackage.getEr().setContent(useSubstitutionRuleOnErContent(dataPackage));
                        if (parallelCompare) {
                            Map<String, String> mdcMap = MDC.getCopyOfContextMap();
                            CompareSession bs = new CompareSession() {
                                @Override
                                public void run() throws ComparatorException {
                                    ThreadUtils.setMdcContextMap(mdcMap);
                                    setStarted(new Date());
                                    setStatus(CompareSessionStatus.IN_PROGRESS);
                                    CompareResult parameterResult;
                                    try {
                                        parameterResult = simpleCompare(dataPackage.getEr(), dataPackage.getAr(),
                                                dataPackage.getConfiguration().getGlobal().getParameters(true));
                                        setCompareResult(parameterResult);
                                    } catch (ComparatorException ex) {
                                        parameterResult = new CompareResult();
                                        parameterResult.setId(dataPackage.getEr().getExternalId());
                                        parameterResult.setType(CompareResultType.SIMPLE);
                                        parameterResult.setSummaryResult(ResultType.ERROR);
                                        parameterResult.setSummaryMessage(new DiffMessage(0,
                                                ResponseMessages.msg(20002, ex.getMessage()),
                                                ResponseMessages.msg(20002, "null"), ResultType.ERROR));
                                        setCompareResult(parameterResult);
                                    } finally {
                                        markAsCompleted();
                                        this.getParent().cdl.countDown();
                                    }
                                }
                            };
                            bs.setSessionId(dataPackage.getEr().getExternalId());
                            assert queue != null;
                            queue.add(bs);
                        } else {
                            try {
                                results.add(simpleCompare(
                                        dataPackage.getEr(),
                                        dataPackage.getAr(),
                                        dataPackage.getConfiguration().getGlobal().getParameters(true)));
                            } catch (Exception ex) {
                                CompareResult parameterResult = new CompareResult();
                                parameterResult.setId(dataPackage.getEr().getExternalId());
                                parameterResult.setType(CompareResultType.SIMPLE);
                                parameterResult.setSummaryResult(ResultType.ERROR);
                                parameterResult.setSummaryMessage(
                                        new DiffMessage(
                                                0,
                                                ResponseMessages.msg(20002, ex.getMessage()),
                                                ResponseMessages.msg(20002, "null"),
                                                ResultType.ERROR));
                                results.add(parameterResult);
                            }
                        }
                        break;
                    case PROCESS_STEP:
                    case PROCESS:
                        results.add(processCompare(
                                dataPackage.getEr(),
                                dataPackage.getAr(),
                                dataPackage.getConfiguration()));
                        break;
                    default:
                }
            }

            if (parallelCompare) {
                CompareSessionsManager.getInstance().runQueue(queue);
                while (!queue.queueIsCompleted()) {
                    Thread.sleep(250);
                }
                for (CompareSession bs : queue.getQueueSessions()) {
                    results.add(bs.getCompareResult());
                }
                CompareSessionsManager.getInstance().releaseQueue(queue.getId());
            }
        } else {
            log.error(ResponseMessages.msg(20105));
            throw new ComparatorManagerException(ResponseMessages.msg(20105), 20105);
        }
        //This method does not allow content to be passed to the response, to form the change mapping on the
        //clearContent(results) side
        log.debug("[ComparatorManager] multiThread returnresult");
        return results;
    }

    private String useSubstitutionRuleOnErContent(DataPackage contextPackage) {
        //get er replacement rules from comparator config
        Parameters parameterRules = contextPackage.getConfiguration().getGlobal().getParameters();
        String erValue = contextPackage.getEr().getContent();

        if (parameterRules.containsKey(PARAMETER_ER_SUBSTITUTION) && !erValue.isEmpty()) {
            List<String> erRules = parameterRules.getParameters(PARAMETER_ER_SUBSTITUTION);
            String decodedEr = DataContentConverter.toString(erValue);
            erValue = ComparatorUtils.applyErSubstitutionRule(decodedEr, erRules);
            erValue = DataContentConverter.fromString(erValue);
        }
        return erValue;
    }

    public CompareResult simpleCompare(Data er, List<Data> ar, Parameters parameters) throws ComparatorException {
        DataContentType targetType = null;

        if (parameters.has(COMPARE_AS) && EnumUtils.isValidEnum(DataContentType.class, parameters.get(COMPARE_AS))) {
            try {
                targetType = DataContentType.valueOf(parameters.get(COMPARE_AS));
            } catch (IllegalArgumentException ex) {
                log.warn("Parameter contains illegal data content type value. {}", ex);
            }
        }


        IComparator comparator;
        CompareResult result = new CompareResult();
        result.setType(CompareResultType.SIMPLE);
        result.setData(er);
        result.setId(er.getExternalId());
        if (targetType != null) {
            DataContentConverter.convertContent(er, targetType); //prepare data if rule 'compareAs' is set
        }
        try {
            comparator = ComparatorFactory.getComparator(er.getContentType());
        } catch (FactoryInstatiationException | ComparatorNotFoundException ex) {
            log.error(ResponseMessages.msg(20001));
            throw new ComparatorException(ex);
        }
        List<ResultData> arData = new ArrayList<>();
        ResultType fromResultType = null;
        ResultType toResultType = null;
        for (Data data : ar) {
            ResultData resultData = new ResultData();
            resultData.setAr(data);
            if (data.getContent() == null) {
                resultData.setDifferences(arMissedResultList());
            } else {
                if (targetType != null) {
                    DataContentConverter.convertContent(data, targetType); //prepare data if rule 'compareAs' is set
                }
                List<DiffMessage> compareResults = comparator.compare(er, data, parameters);
                if (parameters.has(EXCLUDE_DIFF_WITH_STATUS)) {
                    List<String> excludes = parameters.getParameters(EXCLUDE_DIFF_WITH_STATUS);
                    compareResults = compareResults.stream()
                            .filter(el -> !excludes.contains(el.getResult().toString().toUpperCase()))
                            .collect(Collectors.toList());
                }
                resultData.setDifferences(compareResults);
            }
            arData.add(resultData);
        }
        result.setAr(arData);
        calculateSummaryResultForParameter(result, fromResultType, toResultType);
        try {
            if (parameters.has(CHANGE_COMPARE_RESULT)) {
                List<String> changeResultValues = parameters.getParameters(CHANGE_COMPARE_RESULT);
                for (String changeResultValue : changeResultValues) {
                    if (changeResultValue.contains("=")) {
                        String[] changeResultMap = changeResultValue.split("=");
                        String fromResult = changeResultMap[0].trim().toUpperCase();
                        String toResult = changeResultMap[1].trim().toUpperCase();
                        if (EnumUtils.isValidEnum(ResultType.class, fromResult)
                                && EnumUtils.isValidEnum(ResultType.class, toResult)) {
                            fromResultType = ResultType.valueOf(fromResult);
                            toResultType = ResultType.valueOf(toResult);
                            if (result.getSummaryResult() != null && fromResultType == result.getSummaryResult()) {
                                calculateSummaryResultForParameter(result, fromResultType, toResultType);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    public CompareResult simpleCompare(Data er, Data ar, Parameters parameters) throws ComparatorException {
        List<Data> arData = new ArrayList<>();
        arData.add(ar);
        return simpleCompare(er, arData, parameters);
    }

    private void clearContent(List<CompareResult> results) {
        if (results != null) {
            for (CompareResult item : results) {
                if (item != null) {
                    if (item.getData() != null) {
                        item.getData().setContent("");
                    }
                    if (item.getAr() != null) {
                        for (ResultData arData : item.getAr()) {
                            if (arData.getAr() != null) {
                                arData.getAr().setContent("");
                            }
                        }
                    }
                }
            }
        }
    }

    // This method constructs resultList in case when ar is null
    private static List<DiffMessage> arMissedResultList() {
        List<DiffMessage> resultList = new ArrayList<>();
        resultList.add(new DiffMessage(0, "", "", ResultType.AR_MISSED));
        return resultList;
    }

    public CompareResult processCompare(Data er, List<Data> ar, ComparatorConfiguration configuration) {
        boolean compareStepsCount = false;
        CompareResult result = new CompareResult();
        result.setType(CompareResultType.TESTCASE);
        result.setChilds(new ArrayList<CompareResult>());
        for (Data arTestCase : ar) {
            CompareResult arTestCaseResult = new CompareResult();
            arTestCaseResult.setType(CompareResultType.PROCESS);
            arTestCaseResult.setData(arTestCase);
            if (compareStepsCount) {
                if (er.getChilds().size() != arTestCase.getChilds().size()) {
                    log.debug(ResponseMessages.msg(20107, arTestCase.getName()));
                    arTestCaseResult.setSummaryMessage(
                            new DiffMessage(
                                    0,
                                    ResponseMessages.msg(10102, String.valueOf(er.getChilds().size())),
                                    ResponseMessages.msg(10103, String.valueOf(arTestCase.getChilds().size())),
                                    ResultType.FAILED));
                    arTestCaseResult.setSummaryResult(ResultType.FAILED);
                    continue;
                }
            }
            arTestCaseResult.setChilds(new ArrayList<CompareResult>());
            ComparatorConfigurationSet tcConfSet
                    = configuration.getComparatorConfigurationSet(arTestCase.getExternalId());

            //Create StepMap
            /*
             * StepMap - ArrayList of StepMapItem objects
             * StepMap is used to create combined map of er & ar steps.
             * The KEY is NAME of a STEP in an er. The VALUE is NAME of a STEP in an ar.
             * If step missed KEY or VALUE is NULL string
             */
            List<StepMapItem> stepMap = new ArrayList<>();
            fillStepMap(stepMap, er, arTestCase);

            for (StepMapItem stepMapItem : stepMap) {
                CompareResult stepResult = new CompareResult();
                stepResult.setType(CompareResultType.STEP);

                if (stepMapItem.erStepName == null) {
                    stepResult.setSummaryResult(ResultType.ER_MISSED);
                    List<ResultData> arResultList = new ArrayList<>();
                    ResultData arResult = new ResultData();
                    arResult.setAr(arTestCase.getChilds().get(stepMapItem.arStepIndex));
                    arResultList.add(arResult);
                    stepResult.setAr(arResultList);
                    arTestCaseResult.getChilds().add(stepResult);
                    continue;
                }

                Data erStep = er.getChilds().get(stepMapItem.erStepIndex);
                stepResult.setId(erStep.getExternalId());

                if (stepMapItem.arStepName == null) {
                    stepResult.setSummaryResult(ResultType.AR_MISSED);
                    stepResult.setData(er.getChilds().get(stepMapItem.erStepIndex));
                    arTestCaseResult.getChilds().add(stepResult);
                    continue;
                }

                Data arStep = arTestCase.getChilds().get(stepMapItem.arStepIndex);

                String stepKey = String.valueOf(stepMapItem.erStepIndex + 1);
                Rule stepRule = tcConfSet.getStepRule(stepKey);
                if (stepRule.isSkip()) {
                    stepResult.setSummaryResult(ResultType.SKIPPED);
                    stepResult.setData(erStep);
                } else {
                    stepResult.setChilds(new ArrayList<CompareResult>());
                    for (int paramIndex = 0; paramIndex < erStep.getChilds().size(); paramIndex++) {
                        Data erParameter = erStep.getChilds().get(paramIndex);
                        Data arParameter = getParameterByName(erParameter.getName(), arStep);
                        CompareResult parameterResult = new CompareResult();
                        parameterResult.setType(CompareResultType.SIMPLE);
                        parameterResult.setData(erParameter);
                        if (arParameter == null) { //ar parameter not found. do some action
                            parameterResult.setSummaryResult(ResultType.MISSED);
                            parameterResult.setSummaryMessage(
                                    new DiffMessage(
                                            0,
                                            ResponseMessages.msg(10106, erParameter.getName()),
                                            ResponseMessages.msg(10107, "null"),
                                            ResultType.MISSED));
                        } else {
                            try {
                                parameterResult = simpleCompare(
                                        erParameter, arParameter, stepRule.getParameters(erParameter.getName()));
                                calculateSummaryResultForParameter(parameterResult, null, null);
                            } catch (ComparatorException ex) {
                                parameterResult.setSummaryResult(ResultType.ERROR);
                                parameterResult.setSummaryMessage(
                                        new DiffMessage(
                                                0,
                                                ResponseMessages.msg(20002, ex.getMessage()),
                                                ResponseMessages.msg(20002, "null"),
                                                ResultType.ERROR));
                            }
                        }
                        stepResult.getChilds().add(parameterResult);
                    }
                    calculateSummaryResult(stepResult);
                }
                if (stepMapItem.erStepIndex != stepMapItem.arStepIndex) {
                    stepResult.setSummaryMessage(
                            new DiffMessage(
                                    0,
                                    String.valueOf(stepMapItem.erStepIndex + 1),
                                    String.valueOf(stepMapItem.arStepIndex + 1),
                                    ResultType.BROKEN_STEP_INDEX));
                }
                arTestCaseResult.getChilds().add(stepResult);
            }
            calculateSummaryResult(arTestCaseResult); //set summary result
            result.getChilds().add(arTestCaseResult); //set testcase result
        }
        return result;
    }

    //process helper functions
    private Data getParameterByName(String parameterName, Data stepData) {
        if (!stepData.getChilds().isEmpty()) {
            for (Data parameter : stepData.getChilds()) {
                if (parameter.getName().equals(parameterName)) {
                    return parameter;
                }
            }
        }
        return null;
    }

    private void calculateSummaryResultForParameter(CompareResult crObject,
                                                    ResultType fromResult,
                                                    ResultType toResult) {
        boolean changeResult = false;
        if (fromResult != null && toResult != null && !fromResult.equals(toResult)) {
            changeResult = true;
        }
        ResultType summaryResult = ResultType.IDENTICAL;
        int crObjectOrdinal = -1;
        if (crObject.getAr() != null) {
            for (ResultData arData : crObject.getAr()) {
                ResultType arSummaryResult = ResultType.IDENTICAL;
                int ordinal = -1;
                    for (DiffMessage diff : arData.getDifferences()) {
                        int currOrdinal = diff.getResult().ordinal();
                        if (currOrdinal > ordinal) {
                            ordinal = currOrdinal;
                            arSummaryResult = diff.getResult();
                        }
                    }
                if (!changeResult || crObject.getSummaryResult() == arSummaryResult) {
                    if (changeResult && arSummaryResult.equals(fromResult)) {
                        arSummaryResult = toResult;
                        ordinal = arSummaryResult.ordinal();
                    }
                    arData.setSummaryResult(arSummaryResult);
                    if (ordinal > crObjectOrdinal) {
                        crObjectOrdinal = ordinal;
                        summaryResult = arSummaryResult;
                    }
                } else {
                    summaryResult = crObject.getSummaryResult();
                }

            }
        }
        crObject.setSummaryResult(summaryResult);
    }

    private void calculateSummaryResult(CompareResult crObject) {
        ResultType summaryResult = ResultType.IDENTICAL;
        int ordinal = -1;
        for (CompareResult childCompareResult : crObject.getChilds()) {
            ResultType currResultType = childCompareResult.getSummaryResult();
            if (currResultType.ordinal() > ordinal) {
                summaryResult = currResultType;
                ordinal = currResultType.ordinal();
            }
        }
        crObject.setSummaryResult(summaryResult);
    }

    private void fillStepMap(List<StepMapItem> stepMap, Data er, Data ar) {
        List<String> foundErSteps = new ArrayList<>();
        List<String> foundArSteps = new ArrayList<>();
        int counter = 0;
        for (Data erChild : er.getChilds()) {
            StepMapItem stepMapItem = new StepMapItem();
            stepMapItem.setEr(erChild.getName(), counter);
            foundErSteps.add(stepMapItem.erStepName);

            Data arChild = ar.getChildByName(stepMapItem.erStepName);

            if (arChild == null) {
                stepMapItem.setAr(null, -1);
            } else {
                stepMapItem.setAr(arChild.getName(), ar.getChildIndexByName(arChild.getName()));
                foundArSteps.add(stepMapItem.arStepName);
            }
            stepMap.add(stepMapItem);
            counter++;
        }
        if (ar.getChilds().size() > er.getChilds().size()) {
            counter = 0;
            for (Data arChild : ar.getChilds()) {
                if (!foundArSteps.contains(arChild.getName())) {
                    StepMapItem stepMapItem = new StepMapItem(null, -1, arChild.getName(), counter);
                    stepMap.add(stepMapItem);
                }
                counter++;
            }
        }
    }

    public class StepMapItem {

        public String erStepName;
        public String arStepName;
        public int erStepIndex;
        public int arStepIndex;

        public StepMapItem() {

        }

        public StepMapItem(String erStepName, int erStepIndex, String arStepName, int arStepIndex) {
            this.erStepName = erStepName;
            this.erStepIndex = erStepIndex;
            this.arStepName = arStepName;
            this.arStepIndex = arStepIndex;
        }

        public void setEr(String stepName, int stepIndex) {
            this.erStepName = stepName;
            this.erStepIndex = stepIndex;
        }

        public void setAr(String stepName, int stepIndex) {
            this.arStepName = stepName;
            this.arStepIndex = stepIndex;
        }
    }
}
