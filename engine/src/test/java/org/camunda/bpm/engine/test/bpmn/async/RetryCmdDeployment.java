package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class RetryCmdDeployment {

  public static final String FAILING_EVENT = "failingEvent";
  public static final String PROCESS_ID = "failedIntermediateThrowingEventAsync";
  private static final String SCHEDULE = "R5/PT5M";
  public static final String MESSAGE = "start";
  private BpmnModelInstance[] bpmnModelInstances;

  public static RetryCmdDeployment deployment() {
    return new RetryCmdDeployment();
  }

  public static BpmnModelInstance prepareSignalEventProcess() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .camundaFailedJobRetryTimeCycle(SCHEDULE)
            .signal(MESSAGE)
          .serviceTask()
            .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
    return modelInstance;
  }

  public static BpmnModelInstance prepareMessageEventProcess() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
              .camundaFailedJobRetryTimeCycle(SCHEDULE)
              .message(MESSAGE)
            .serviceTask()
              .camundaClass(FailingDelegate.class.getName())
        .done();
  }

  public static BpmnModelInstance prepareEscalationEventProcess() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .camundaFailedJobRetryTimeCycle(SCHEDULE)
            .escalation(MESSAGE)
          .serviceTask()
            .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }


  public static BpmnModelInstance prepareCompensationEventProcess() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .subProcess("subProcess")
            .embeddedSubProcess()
              .startEvent()
              .endEvent()
          .subProcessDone()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .camundaFailedJobRetryTimeCycle(SCHEDULE)
            .compensateEventDefinition()
            .compensateEventDefinitionDone()
          .serviceTask()
          .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }

  public static BpmnModelInstance prepareSignalEventProcessWithoutRetry() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .signal(MESSAGE)
          .serviceTask()
            .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
    return modelInstance;
  }

  public static BpmnModelInstance prepareMessageEventProcessWithoutRetry() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
              .message(MESSAGE)
            .serviceTask()
              .camundaClass(FailingDelegate.class.getName())
        .done();
  }

  public static BpmnModelInstance prepareEscalationEventProcessWithoutRetry() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .escalation(MESSAGE)
          .serviceTask()
            .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }


  public static BpmnModelInstance prepareCompensationEventProcessWithoutRetry() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .subProcess("subProcess")
            .embeddedSubProcess()
              .startEvent()
              .endEvent()
          .subProcessDone()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .compensateEventDefinition()
            .compensateEventDefinitionDone()
          .serviceTask()
          .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }


  public RetryCmdDeployment withEventProcess(BpmnModelInstance... bpmnModelInstances) {
    this.bpmnModelInstances = bpmnModelInstances;
    return this;
  }

  public static Collection<RetryCmdDeployment[]> asParameters(RetryCmdDeployment... deployments) {
    List<RetryCmdDeployment[]> deploymentList = new ArrayList<RetryCmdDeployment[]>();
    for (RetryCmdDeployment deployment : deployments) {
      deploymentList.add(new RetryCmdDeployment[]{ deployment });
    }

    return deploymentList;
  }

  public BpmnModelInstance[] getBpmnModelInstances() {
    return bpmnModelInstances;
  }

  public void setBpmnModelInstances(BpmnModelInstance[] bpmnModelInstances) {
    this.bpmnModelInstances = bpmnModelInstances;
  }
}
