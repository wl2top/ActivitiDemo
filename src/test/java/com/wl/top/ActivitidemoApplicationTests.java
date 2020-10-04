package com.wl.top;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@SpringBootTest
class ActivitidemoApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivitidemoApplicationTests.class);

    @Resource
    private ProcessEngine processEngine;

    @Test
    void contextLoads() {
    }

    /**
     * 一、创建流程引擎的两种方式
     */
    @Test
    void createActivitiProcessEngine1() {

        /*  - 1.通过代码形式创建
         *  - 取得ProcessEngineConfiguration对象
         *  - 设置数据库连接属性
         *  - 设置创建表的策略 （当没有表时，自动创建表）
         *  - 通过ProcessEngineConfiguration对象创建 ProcessEngine 对象*/

        //取得ProcessEngineConfiguration对象
        ProcessEngineConfiguration engineConfiguration = ProcessEngineConfiguration.
                createStandaloneProcessEngineConfiguration();
        //设置数据库连接属性
        engineConfiguration.setJdbcDriver("com.mysql.cj.jdbc.Driver");
        engineConfiguration.setJdbcUrl("jdbc:mysql://localhost:3306/act_demo?createDatabaseIfNotExist=true"
                + "&nullCatalogMeansCurrent=true&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&serverTimezone=GMT%2B8&useSSL=true");
        engineConfiguration.setJdbcUsername("root");
        engineConfiguration.setJdbcPassword("root");

        // 设置创建表的策略 （当没有表时，自动创建表）
        // public static final java.lang.String DB_SCHEMA_UPDATE_FALSE = "false";//不会自动创建表，没有表，则抛异常
        // public static final java.lang.String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";//先删除，再创建表
        // public static final java.lang.String DB_SCHEMA_UPDATE_TRUE = "true";//假如没有表，则自动创建
        engineConfiguration.setDatabaseSchemaUpdate("true");
        //通过ProcessEngineConfiguration对象创建 ProcessEngine 对象
        ProcessEngine processEngine = engineConfiguration.buildProcessEngine();
        System.out.println("流程引擎创建成功!");
    }

    @Test
    void createActivitiProcessEngine2() {
        //2. 通过加载配置文件 activiti.xml 自动创建数据库表并获得工作流核心--processEngine
        ProcessEngineConfiguration engineConfiguration =
                ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.xml");
        //从类加载路径中查找资源  activiti.xml 文件名可以自定义
        ProcessEngine processEngine = engineConfiguration.buildProcessEngine();
        System.out.println("使用配置文件Activiti.xml获取流程引擎");
    }

    /**
     * 二、定义工作流流程（画流程图）
     * 三、部署工作流流程（流程部署）
     */
    @Test
    void deployProcess() {
        //部署方式一：加载 同名的BPMN和PNG文件 部署流程
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy1 = repositoryService.createDeployment()//创建一个部署的构建器
                .addClasspathResource("diagram/LeaveActiviti.bpmn")//从类路径中添加资源,一次只能添加一个资源
                .name("请求单流程")//设置部署的名称
                .category("办公类别")//设置部署的类别
                .deploy();
        System.out.println("部署的id" + deploy1.getId());
        System.out.println("部署的名称" + deploy1.getName());

        //部署方式二：加载 包含同名BPMN和PNG文件的zip压缩包 部署流程
        InputStream in = getClass().getClassLoader().getResourceAsStream("LeaveActiviti.zip");
        Deployment deploy2 = processEngine.getRepositoryService()
                .createDeployment()
                .name("采购流程")
                .addZipInputStream(new ZipInputStream(in))
                .deploy();
        System.out.println("部署名称:" + deploy2.getName());
        System.out.println("部署id:" + deploy2.getId());
    }

    /**
     * 四、执行工作流流程（流程开始执行） --- 每开始（开跑）一条流程就会创建一个流程实例
     */
    @Test
    void startProcess() {
        // 流程定义Key -- bpmn文件的id属性
        String processDefinitionKey = "LeaveActiviti";
        // 根据 流程定义Key 开跑一条流程  生成对应的流程实例
        ProcessInstance pi = processEngine.getRuntimeService()
                .startProcessInstanceByKey(processDefinitionKey);

        System.out.println("流程执行对象的id：" + pi.getId());//Execution 对象
        System.out.println("流程实例的id：" + pi.getProcessInstanceId());//ProcessInstance 对象
        System.out.println("流程定义的id：" + pi.getProcessDefinitionId());//默认执行的是最新版本的流程定义
    }

    /**
     * 五、查看当前流程所处环节/查看代理人任务列表
     */
    @Test
    void queryTask() {
        //任务的办理人
        String assignee = "钟福成";
        //取得任务服务
        TaskService taskService = processEngine.getTaskService();
        //创建一个任务查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //办理人的任务列表
        List<Task> list = taskQuery.taskAssignee(assignee)//指定办理人
                .list();
        //遍历任务列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务的id：" + task.getId());
                System.out.println("任务的名称：" + task.getName());
            }
        }
    }

    /**
     * 办理任务
     */
    @Test
    public void completeTask() {
        //获取任务ID
        String taskId = "304";
        //根据任务ID办理当前任务
        processEngine.getTaskService().complete(taskId);
        System.out.println("当前任务执行完毕");
    }

    /**
     * 查看程定义
     */
    @Test
    public void queryProcessDefinition() {
        /*
          1.通过 流程部署ID 获取流程定义列表
            流程部署ID 参考 act_re_deployment中的ID
         */
        String deploymentId = "1";
        List<ProcessDefinition> proDefList0 = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                // 最新版本
                .latestVersion()
                .list();

        /*
          2.通过 流程定义key 获取流程定义列表
            流程定义key 由 bpmn文件的id属性 决定
         */
        String processDefinitionKey = "LeaveProcess";
        //根据 流程定义key 精准查询
        List<ProcessDefinition> proDefList1 = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .list();
        //根据 流程定义key 模糊查询
        List<ProcessDefinition> proDefList2 = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKeyLike(processDefinitionKey)
                .latestVersion()
                .list();

        /*
          3.通过 流程定义名称 获取流程定义列表
            流程定义名称 由 bpmn文件的name属性 决定
         */
        String processDefinitionName = "";
        List<ProcessDefinition> proDefList3 = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionName(processDefinitionName)
                .latestVersion()
                .list();

        /*
          4.通过 流程定义ID 获取流程定义列表
          流程定义ID：LeaveProcess:1:4  组成元素：流程定义Key + 流程定义版本 + 自动生成id
         */
        String processDefinitionId = processDefinitionKey + ":1:4";
        List<ProcessDefinition> proDefList4 = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                // 按流程定义的版本号降序排序（默认升序）
                .orderByProcessDefinitionVersion().desc()
                // 对流程定义列表的处理一：统计结果数量
                //.count();
                // 对流程定义列表的处理二：分页查询
                //.listPage(1, 3);
                // 对流程定义列表的处理三：展示列表中所有元素
                .list();
    }

    /**
     * 删除流程定义
     */
    @Test
    public void deleteProcessDef() {
        //通过流程部署ID来删除流程定义
        String deploymentId = "1";
        processEngine.getRepositoryService().deleteDeployment(deploymentId);
    }

    /**
     * 查询所有正在执行的任务
     */
    @Test
    public void queryAllTasks() {
        //获取任务服务
        TaskService taskService = processEngine.getTaskService();
        //创建任务查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //获取所有正在执行的任务列表
        List<Task> list = taskQuery.list();
        //遍历任务列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务的id：" + task.getId());
                System.out.println("任务的名称：" + task.getName());
            }
        }
    }

    /**
     * 获取流程实例的状态
     */
    @Test
    public void getProcessInstanceStatus() {
        // 根据流程实例ID查询获取流程实例
        String processInstanceId = "123";
        ProcessInstance pi = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();//返回的数据要么是单行，要么是空 ，其他情况报错
        //判断流程实例的状态
        if (pi != null) {
            System.out.println("该流程实例" + processInstanceId + "正在运行...  " + "当前活动的任务:" + pi.getActivityId());
        } else {
            System.out.println("当前的流程实例" + processInstanceId + " 已经结束！");
        }
    }

    /**
     * 查看历史流程实例信息 --- 查询act_hi_procinst表
     */
    @Test
    public void queryHistoryProcessInstance() {
        List<HistoricProcessInstance> list = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .list();
        if (list != null && list.size() > 0) {
            for (HistoricProcessInstance temp : list) {
                System.out.println("历史流程实例id:" + temp.getId());
                System.out.println("历史流程定义的id:" + temp.getProcessDefinitionId());
                System.out.println("历史流程实例开始时间--结束时间:" + temp.getStartTime() + "-->" + temp.getEndTime());
            }
        }
    }

    /**
     * 查查看某流程实例的历史任务信息 --- 查询act_hi_taskinst表（根据流程实例ID）
     */
    @Test
    public void queryHistoryTask() {
        String processInstanceId = "123";
        List<HistoricTaskInstance> list = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if (list != null && list.size() > 0) {
            for (HistoricTaskInstance temp : list) {
                System.out.print("历史流程实例任务id:" + temp.getId());
                System.out.print("历史流程定义的id:" + temp.getProcessDefinitionId());
                System.out.print("历史流程实例任务名称:" + temp.getName());
                System.out.println("历史流程实例任务处理人:" + temp.getAssignee());
            }
        }
    }

    /**
     * 设置流程变量
     */
    @Test
    public void setAndGetVariables() {
        // 获取 taskService 和 runtimeService 服务
        TaskService taskService = processEngine.getTaskService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        /*
          1.通过 runtimeService 来设置流程变量
            executionId: 执行对象
		    variableName：变量名
		    values：变量值
         */
        String executionId = "111";
        String variableName = "count";
        int values = 2;
        runtimeService.setVariable(executionId, variableName, values);
        // 设置该执行对象的变量，变量的作用域只在当前的execution对象
        runtimeService.setVariableLocal(executionId, variableName, values);
        // 可以设置多个变量，将变量放在一个Map<String,Object>中
        Map<String, Object> variables = new HashMap<>();
        variables.put("thisUser", "WL");
        variables.put("nextUser", "WL");
        runtimeService.setVariables(executionId, variables);


        /*
          2.通过 TaskService 设置流程变量
            taskId：任务Id
         */
        String taskId = "123";
        taskService.setVariable(taskId, variableName, values);
        // 设置该任务环节的变量，变量的作用域只在当前任务环节
        taskService.setVariableLocal(taskId, variableName, values);
        taskService.setVariables(taskId, variables);


        /*
          3.在流程启动时，设置流程变量
            processDefinitionKey: 流程定义key
		    variables： 设置多个变量  Map<String,Object>
         */
        String processDefinitionKey = "LeaveActiviti";
        runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);


        /*
          4.在当前任务完成时，可以设置流程变量
         */
        taskService.complete(taskId, variables);


        /*
          5.通过RuntimeService取变量值
         */
        // 根据流程变量名获取变量值
        runtimeService.getVariable(executionId, variableName);
        // 获取当前执行对象的流程变量
        runtimeService.getVariableLocal(executionId, variableName);
        runtimeService.getVariables(variableName);


        /*
          6.通过TaskService取变量值
         */
        // 根据流程变量名获取变量值
        taskService.getVariable(taskId, variableName);
        // 获取当前任务环节的流程变量
		taskService.getVariableLocal(taskId, variableName);
		taskService.getVariables(taskId);

    }
}
