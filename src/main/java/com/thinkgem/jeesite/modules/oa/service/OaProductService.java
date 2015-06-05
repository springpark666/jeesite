/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.oa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thinkgem.jeesite.common.persistence.Page;
import com.thinkgem.jeesite.common.service.CrudService;
import com.thinkgem.jeesite.common.utils.StringUtils;
import com.thinkgem.jeesite.modules.act.utils.ActUtils;
import com.thinkgem.jeesite.modules.oa.entity.OaMyleave;
import com.thinkgem.jeesite.modules.oa.entity.OaProduct;
import com.thinkgem.jeesite.modules.oa.dao.OaProductDao;

/**
 * 商品Service
 * @author haop
 * @version 2015-06-04
 */
@Service
@Transactional(readOnly = true)
public class OaProductService extends CrudService<OaProductDao, OaProduct> {
	@Autowired
	private OaProductDao oaProductDao;
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
	public OaProduct get(String id) {
		return super.get(id);
	}
	
	public List<OaProduct> findList(OaProduct oaProduct) {
		return super.findList(oaProduct);
	}
	
	public Page<OaProduct> findPage(Page<OaProduct> page, OaProduct oaProduct) {
		return super.findPage(page, oaProduct);
	}
	
	@Transactional(readOnly = false)
	public void save(OaProduct oaProduct,Map<String, Object> variables) {
		// 保存业务数据
		if (StringUtils.isBlank(oaProduct.getId())){
			oaProduct.preInsert();
			oaProductDao.insert(oaProduct);
		}else{
			oaProduct.preUpdate();
			oaProductDao.update(oaProduct);
		}
		logger.debug("save entity: {}", oaProduct);
		
		// 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
		
		identityService.setAuthenticatedUserId(oaProduct.getCurrentUser().getLoginName());
		
		// 启动流程[新开一个流程]
		if(StringUtils.isEmpty(oaProduct.getProcessInstanceId())){
			String businessKey = oaProduct.getId().toString();
			variables.put("busId", businessKey);
			variables.put("productuser",oaProduct.getCurrentUser().getLoginName());
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("productup",businessKey, variables);
			oaProduct.setProcessInstance(processInstance);
			
			// 更新流程实例ID
			oaProduct.setProcessInstanceId(processInstance.getId());
			oaProductDao.updateProcessInstanceId(oaProduct);
			
			logger.debug("start process of {key={}, bkey={}, pid={}, variables={}}", new Object[] { 
					"productup", businessKey, processInstance.getId(), variables });
		}else{//【请假修改保持一个流程】
			Task task=taskService.createTaskQuery().processInstanceId(oaProduct.getProcessInstanceId()).singleResult();
			Map<String,Object> var=new HashMap<String, Object>();
			var.put("edityes",true);
			taskService.complete(task.getId(),var);
		}
	}
	
	public List<OaProduct> findFinishedTasks(){
		 List<OaProduct> results = new ArrayList<OaProduct>();
	        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("productup").finished().orderByProcessInstanceEndTime().desc();
	        List<HistoricProcessInstance> list = query.list();

	        // 关联业务实体
	        for (HistoricProcessInstance historicProcessInstance : list) {
	            String businessKey = historicProcessInstance.getBusinessKey();
	            OaProduct oaProduct = oaProductDao.get(businessKey);
	            if(null==oaProduct){
	            	continue;
	            }
	            oaProduct.setProcessDefinition(getProcessDefinition(historicProcessInstance.getProcessDefinitionId()));
	            oaProduct.setHistoricProcessInstance(historicProcessInstance);
	            results.add(oaProduct);
	        }
	        return results;
	}
	
	protected ProcessDefinition getProcessDefinition(String processDefinitionId) {
       ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
       return processDefinition;
   }
	
	
	/**
	 * 查询待办任务
	 * @param userId 用户ID
	 * @return
	 */
	public List<OaProduct> findTodoTasks(String userId) {
		
		List<OaProduct> results = new ArrayList<OaProduct>();
		 // 根据当前人的ID查询
//       TaskQuery taskQuery = taskService.createTaskQuery().processDefinitionKey("myleave").taskCandidateOrAssigned(userId);
//       List<Task> tasks = taskQuery.list();
		
		// 根据当前人的ID查询
				List<Task> todoList = taskService.createTaskQuery().processDefinitionKey("productup").taskAssignee(userId).active().orderByTaskPriority().desc().orderByTaskCreateTime().desc().list();
				// 根据当前人未签收的任务
				List<Task> unsignedTasks = taskService.createTaskQuery().processDefinitionKey("productup").taskCandidateUser(userId).active().orderByTaskPriority().desc().orderByTaskCreateTime().desc().list();
				
				
				// 合并
				todoList.addAll(unsignedTasks);
		
		// 根据流程的业务ID查询实体并关联
		for (Task task : todoList) {
			String processInstanceId = task.getProcessInstanceId();
			ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();
			String businessKey = processInstance.getBusinessKey();
			OaProduct oaProduct = oaProductDao.get(businessKey);
			if(null==oaProduct){
				continue;
			}
			oaProduct.setTask(task);
			oaProduct.setProcessInstance(processInstance);
			oaProduct.setProcessDefinition(repositoryService.createProcessDefinitionQuery().processDefinitionId((processInstance.getProcessDefinitionId())).singleResult());
			results.add(oaProduct);
		}
		return results;
	}
	
	@Transactional(readOnly = false)
	public void delete(OaProduct oaProduct) {
		super.delete(oaProduct);
	}
	
}