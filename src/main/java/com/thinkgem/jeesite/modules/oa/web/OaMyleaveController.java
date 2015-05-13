/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.oa.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.TaskService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Maps;
import com.thinkgem.jeesite.common.config.Global;
import com.thinkgem.jeesite.common.mapper.JsonMapper;
import com.thinkgem.jeesite.common.persistence.Page;
import com.thinkgem.jeesite.common.web.BaseController;
import com.thinkgem.jeesite.common.utils.StringUtils;
import com.thinkgem.jeesite.modules.act.service.ActTaskService;
import com.thinkgem.jeesite.modules.act.utils.Variable;
import com.thinkgem.jeesite.modules.oa.entity.OaMyleave;
import com.thinkgem.jeesite.modules.oa.service.OaMyleaveService;
import com.thinkgem.jeesite.modules.sys.utils.UserUtils;

/**
 * 单表生成Controller
 * @author haop
 * @version 2015-05-12
 */
@Controller
@RequestMapping(value = "${adminPath}/oa/oaMyleave")
public class OaMyleaveController extends BaseController {

	@Autowired
	private OaMyleaveService oaMyleaveService;
	@Autowired
	protected TaskService taskService;
	@Autowired
	private ActTaskService actTaskService;
	@ModelAttribute
	public OaMyleave get(@RequestParam(required=false) String id) {
		OaMyleave entity = null;
		if (StringUtils.isNotBlank(id)){
			entity = oaMyleaveService.get(id);
		}
		if (entity == null){
			entity = new OaMyleave();
		}
		return entity;
	}
	
	@RequiresPermissions("oa:oaMyleave:view")
	@RequestMapping(value = {"task", ""})
	public String task(OaMyleave oaMyleave, HttpServletRequest request, HttpServletResponse response, Model model) {
		String userId = UserUtils.getUser().getLoginName();//ObjectUtils.toString(UserUtils.getUser().getId());
		List<OaMyleave> results = oaMyleaveService.findTodoTasks(userId);
		model.addAttribute("leaves", results);
		return "modules/oa/oaMyleaveTask";
	}
	
	@RequiresPermissions("oa:oaMyleave:view")
	@RequestMapping(value = "task/finished")
	public String finishedTask(OaMyleave oaMyleave, HttpServletRequest request, HttpServletResponse response, Model model) {
		String userId = UserUtils.getUser().getLoginName();//ObjectUtils.toString(UserUtils.getUser().getId());
		List<OaMyleave> results = oaMyleaveService.findFinishedTasks();
		model.addAttribute("leaves", results);
		return "modules/oa/oaMyleaveTaskFinished";
	}
	
	@RequiresPermissions("oa:oaMyleave:view")
	@RequestMapping(value = "list")
	public String list(OaMyleave oaMyleave, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<OaMyleave> page = oaMyleaveService.findPage(new Page<OaMyleave>(request, response), oaMyleave); 
		model.addAttribute("page", page);
		return "modules/oa/oaMyleaveList";
	}

	@RequiresPermissions("oa:oaMyleave:view")
	@RequestMapping(value = "form")
	public String form(OaMyleave oaMyleave, Model model) {
		model.addAttribute("oaMyleave", oaMyleave);
		return "modules/oa/oaMyleaveForm";
	}

	@RequiresPermissions("oa:oaMyleave:edit")
	@RequestMapping(value = "save")
	public String save(OaMyleave oaMyleave, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, oaMyleave)){
			return form(oaMyleave, model);
		}
		Map<String, Object> variables = Maps.newHashMap();
		oaMyleaveService.save(oaMyleave, variables);
		addMessage(redirectAttributes, "保存单表成功");
		return "redirect:"+Global.getAdminPath()+"/oa/oaMyleave/?repage";
	}
	
	@RequiresPermissions("oa:oaMyleave:edit")
	@RequestMapping(value = "delete")
	public String delete(OaMyleave oaMyleave, RedirectAttributes redirectAttributes) {
		oaMyleaveService.delete(oaMyleave);
		addMessage(redirectAttributes, "删除单表成功");
		return "redirect:"+Global.getAdminPath()+"/oa/oaMyleave/?repage";
	}
	
	/**
	 * 读取详细数据
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "detail/{id}")
	@ResponseBody
	public String getLeave(@PathVariable("id") String id) {
		OaMyleave leave = oaMyleaveService.get(id);
		return JsonMapper.getInstance().toJson(leave);
	}

	/**
	 * 读取详细数据
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "detail-with-vars/{id}/{taskId}")
	@ResponseBody
	public String getLeaveWithVars(@PathVariable("id") String id, @PathVariable("taskId") String taskId) {
		OaMyleave leave = oaMyleaveService.get(id);
		Map<String, Object> variables = taskService.getVariables(taskId);
		leave.setVariables(variables);
		return JsonMapper.getInstance().toJson(leave);
	}
	
	/**
     * 完成任务
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "complete", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String complete(String taskId,String pid,Variable var) {
        try {
            Map<String, Object> variables = var.getVariableMap();
            // taskService.complete(taskId, variables);
            // 提交流程任务
            if(null!=variables.get("change")){
            	
            	actTaskService.complete(taskId,pid,"已调整", variables);
            }else{
            	actTaskService.complete(taskId,pid,"true".equals(var.getValues())? "同意" : var.getValues().split(",")[1], variables);
            }
            return "success";
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[]{taskId, var.getVariableMap(), e});
            return "error";
        }
    }

}