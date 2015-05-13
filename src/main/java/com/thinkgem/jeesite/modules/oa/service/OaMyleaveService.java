/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.oa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thinkgem.jeesite.common.persistence.Page;
import com.thinkgem.jeesite.common.service.CrudService;
import com.thinkgem.jeesite.common.utils.StringUtils;
import com.thinkgem.jeesite.modules.act.utils.ActUtils;
import com.thinkgem.jeesite.modules.oa.entity.Leave;
import com.thinkgem.jeesite.modules.oa.entity.OaMyleave;
import com.thinkgem.jeesite.modules.oa.dao.OaMyleaveDao;

/**
 * 单表生成Service
 * @author haop
 * @version 2015-05-12
 */
@Service
@Transactional(readOnly = true)
public class OaMyleaveService extends CrudService<OaMyleaveDao, OaMyleave> {

	@Autowired
	private OaMyleaveDao oaMyleaveDao;
	@Autowired
	private IdentityService identityService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	protected TaskService taskService;
	@Autowired
	protected HistoryService historyService;
	@Autowired
	protected RepositoryService repositoryService;
	public OaMyleave get(String id) {
		return super.get(id);
	}
	
	public List<OaMyleave> findList(OaMyleave oaMyleave) {
		return super.findList(oaMyleave);
	}
	
	public Page<OaMyleave> findPage(Page<OaMyleave> page, OaMyleave oaMyleave) {
		page=super.findPage(page, oaMyleave);
		List<OaMyleave> list=page.getList();
		for(OaMyleave leave:list){
			String processInstanceId = leave.getProcessInstanceId();
			ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();
			
			if(null==processInstance){
				HistoricProcessInstance historicProcessInstance=historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
				leave.setHistoricProcessInstance(historicProcessInstance);
				continue;
			}
			
			Task task=taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
			ProcessDefinition processDefinition=repositoryService.createProcessDefinitionQuery().processDefinitionId((processInstance.getProcessDefinitionId())).singleResult();
			
			leave.setTask(task);
			leave.setProcessInstance(processInstance);
			leave.setProcessDefinition(processDefinition);
			
		}
		return page;
	}
	
	@Transactional(readOnly = false)
	public void save(OaMyleave oaMyleave, Map<String, Object> variables) {
		// 保存业务数据
				if (StringUtils.isBlank(oaMyleave.getId())){
					oaMyleave.preInsert();
					oaMyleaveDao.insert(oaMyleave);
				}else{
					oaMyleave.preUpdate();
					oaMyleaveDao.update(oaMyleave);
				}
				logger.debug("save entity: {}", oaMyleave);
				
				// 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
				identityService.setAuthenticatedUserId(oaMyleave.getCurrentUser().getLoginName());
				
				// 启动流程
				String businessKey = oaMyleave.getId().toString();
				variables.put("type", "leave");
				variables.put("busId", businessKey);
				ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myleave",businessKey, variables);
				oaMyleave.setProcessInstance(processInstance);
				
				// 更新流程实例ID
				oaMyleave.setProcessInstanceId(processInstance.getId());
				oaMyleaveDao.updateProcessInstanceId(oaMyleave);
				
				logger.debug("start process of {key={}, bkey={}, pid={}, variables={}}", new Object[] { 
						ActUtils.PD_LEAVE[0], businessKey, processInstance.getId(), variables });
	}
	
	/**
	 * 查询待办任务
	 * @param userId 用户ID
	 * @return
	 */
	public List<OaMyleave> findTodoTasks(String userId) {
		
		List<OaMyleave> results = new ArrayList<OaMyleave>();
		 // 根据当前人的ID查询
//        TaskQuery taskQuery = taskService.createTaskQuery().processDefinitionKey("myleave").taskCandidateOrAssigned(userId);
//        List<Task> tasks = taskQuery.list();
		
		// 根据当前人的ID查询
				List<Task> todoList = taskService.createTaskQuery().processDefinitionKey("myleave").taskAssignee(userId).active().orderByTaskPriority().desc().orderByTaskCreateTime().desc().list();
				// 根据当前人未签收的任务
				List<Task> unsignedTasks = taskService.createTaskQuery().processDefinitionKey("myleave").taskCandidateUser(userId).active().orderByTaskPriority().desc().orderByTaskCreateTime().desc().list();
				
				
				// 合并
				todoList.addAll(unsignedTasks);
		
		// 根据流程的业务ID查询实体并关联
		for (Task task : todoList) {
			String processInstanceId = task.getProcessInstanceId();
			ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();
			String businessKey = processInstance.getBusinessKey();
			OaMyleave leave = oaMyleaveDao.get(businessKey);
			if(null==leave){
				continue;
			}
			leave.setTask(task);
			leave.setProcessInstance(processInstance);
			leave.setProcessDefinition(repositoryService.createProcessDefinitionQuery().processDefinitionId((processInstance.getProcessDefinitionId())).singleResult());
			results.add(leave);
		}
		return results;
	}
	
	@Transactional(readOnly = false)
	public void delete(OaMyleave oaMyleave) {
		super.delete(oaMyleave);
	}
	
}