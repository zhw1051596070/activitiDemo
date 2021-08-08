package com.activiti.test;

import com.google.common.collect.Maps;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * Created by asus on 2021/8/7.
 */
public class DemoMain {
    private  static final Logger LOGGER= LoggerFactory.getLogger(DemoMain.class);
    public static void main(String[] args) {
        LOGGER.info("启动程序");
        //创建流程引擎
        ProcessEngine processEngine = getProcessEngine();


        //部署流程定义文件
        ProcessDefinition processDefinition = getProcessDefinition(processEngine);

        //启动运行流程

        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
        LOGGER.info("启动流程{}",processInstance.getProcessDefinitionKey());


        //处理流程任务
        Scanner scanner = new Scanner(System.in);
        while (processInstance!=null&& !processInstance.isEnded())
        {
            TaskService taskService = processEngine.getTaskService();
            List<Task> list = taskService.createTaskQuery().list();
            for (Task task : list) {
                LOGGER.info("待处理任务{}",task.getName());
                FormService formService = processEngine.getFormService();
                TaskFormData taskFormData = formService.getTaskFormData(task.getId());
                List<FormProperty> formProperties = taskFormData.getFormProperties();
                Map<String,Object> variables= Maps.newHashMap();
                for (FormProperty formProperty : formProperties)
                {
                    String line =null;

                   if(StringFormType.class.isInstance(formProperty.getType()))
                   {
                    LOGGER.info("请输入 {} ?",formProperty.getName());
                     line=scanner.nextLine();
                    variables.put(formProperty.getId(),line);

                   }else if(DateFormType.class.isInstance(formProperty.getType()))
                   {
                         LOGGER.info("请输入{}？ 格式（yyyy-MM-dd）",formProperty.getName());
                         line=scanner.nextLine();
                         SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
                          try {
                              Date date = dateFormat.parse(line);
                              variables.put(formProperty.getId(),date);

                          }catch (Exception e)
                          {

                          }
                   }else
                   {
                       LOGGER.info("类型暂不支持{}",formProperty.getType());
                   }
                }
                 taskService.complete(task.getId(),variables);
                 processInstance = processEngine.getRuntimeService()
                        .createProcessInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .singleResult();

            }
        }

        LOGGER.info("结束程序。");

    }

    private static ProcessDefinition getProcessDefinition(ProcessEngine processEngine) {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addClasspathResource("MyProcess002.bpmn20.xml");
        Deployment deployment = deploymentBuilder.deploy();
        String deploymentId = deployment.getId();
        ProcessDefinition processDefinition =  repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .singleResult();
        LOGGER.info("流程定义文件{}，流程ID{}",processDefinition.getName(),processDefinition.getId());
        return processDefinition;
    }

    private static ProcessEngine getProcessEngine() {
        ProcessEngineConfiguration cfg = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        ProcessEngine processEngine = cfg.buildProcessEngine();
        String name = processEngine.getName();
        String version = processEngine.VERSION;
        LOGGER.info("流程引擎名称{}，版本{}",name,version);
        return processEngine;
    }
}
