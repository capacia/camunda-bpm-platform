/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.bpmn.parse;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.bpmn.async.FailingDelegate;
import org.camunda.bpm.engine.test.bpmn.async.RetryCmdDeployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class GlobalRetryConfigurationTest {

  public static final String FAILING_EVENT = "failingEvent";
  public static final String PROCESS_ID = "failedIntermediateThrowingEventAsync";
  private static final String SCHEDULE = "R5/PT5M";
  public static final String MESSAGE = "start";

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setFailedJobRetryTimeCycle(SCHEDULE);
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  private Deployment currentDeployment;

  @Before
  public void setUp() {
    BpmnModelInstance instance = prepareSignalEventProcessWithoutRetry();
    currentDeployment = testRule.deploy(instance);
  }

  @After
  public void tearDown() {
    engineRule.getRepositoryService().deleteDeployment(currentDeployment.getId(), true, true);
  }

  @Test
  public void testFailedIntermediateThrowingSignalEventAsync() {
    String failedJobRetryTimeCycle = engineRule.getProcessEngineConfiguration().getFailedJobRetryTimeCycle();
    assertEquals(SCHEDULE, failedJobRetryTimeCycle);
    ProcessInstance pi = engineRule.getRuntimeService().startProcessInstanceByKey(RetryCmdDeployment.PROCESS_ID);
    assertJobRetries(pi);
  }

  private void assertJobRetries(ProcessInstance pi) {
    assertThat(pi, is(notNullValue()));

    Job job = fetchJob(pi.getProcessInstanceId());

    try {
      engineRule.getManagementService().executeJob(job.getId());
    } catch (Exception e) {
    }

    // update job
    job = fetchJob(pi.getProcessInstanceId());
    assertThat(job.getRetries(), is(4));
  }

  private Job fetchJob(String processInstanceId) {
    return engineRule.getManagementService().createJobQuery().processInstanceId(processInstanceId).singleResult();
  }

  private BpmnModelInstance prepareSignalEventProcessWithoutRetry() {
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
}