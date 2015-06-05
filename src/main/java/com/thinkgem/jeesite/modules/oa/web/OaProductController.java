/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.oa.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Maps;
import com.thinkgem.jeesite.common.config.Global;
import com.thinkgem.jeesite.common.persistence.Page;
import com.thinkgem.jeesite.common.web.BaseController;
import com.thinkgem.jeesite.common.utils.StringUtils;
import com.thinkgem.jeesite.modules.act.service.ActTaskService;
import com.thinkgem.jeesite.modules.act.utils.Variable;
import com.thinkgem.jeesite.modules.oa.entity.OaMyleave;
import com.thinkgem.jeesite.modules.oa.entity.OaProduct;
import com.thinkgem.jeesite.modules.oa.service.OaProductService;
import com.thinkgem.jeesite.modules.sys.utils.UserUtils;

/**
 * 商品Controller
 * @author haop
 * @version 2015-06-04
 */
@Controller
@RequestMapping(value = "${adminPath}/oa/oaProduct")
public class OaProductController extends BaseController {

	@Autowired
	private OaProductService oaProductService;
	@Autowired
	private ActTaskService actTaskService;
	@ModelAttribute
	public OaProduct get(@RequestParam(required=false) String id) {
		OaProduct entity = null;
		if (StringUtils.isNotBlank(id)){
			entity = oaProductService.get(id);
		}
		if (entity == null){
			entity = new OaProduct();
		}
		return entity;
	}
	
	@RequiresPermissions("oa:oaProduct:view")
	@RequestMapping(value = "task")
	public String task(OaMyleave oaMyleave, HttpServletRequest request, HttpServletResponse response, Model model) {
		String userId = UserUtils.getUser().getLoginName();//ObjectUtils.toString(UserUtils.getUser().getId());
		List<OaProduct> results = oaProductService.findTodoTasks(userId);
		model.addAttribute("products", results);
		return "modules/oa/oaProductTask";
	}
	
	@RequiresPermissions("oa:oaProduct:view")
	@RequestMapping(value = "task/finished")
	public String finishedTask(OaMyleave oaMyleave, HttpServletRequest request, HttpServletResponse response, Model model) {
		String userId = UserUtils.getUser().getLoginName();//ObjectUtils.toString(UserUtils.getUser().getId());
		List<OaProduct> results = oaProductService.findFinishedTasks();
		model.addAttribute("products", results);
		return "modules/oa/oaProductTaskFinished";
	}
	
	@RequiresPermissions("oa:oaProduct:view")
	@RequestMapping(value = {"list", ""})
	public String list(OaProduct oaProduct, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<OaProduct> page = oaProductService.findPage(new Page<OaProduct>(request, response), oaProduct); 
		model.addAttribute("page", page);
		return "modules/oa/oaProductList";
	}

	@RequiresPermissions("oa:oaProduct:view")
	@RequestMapping(value = "form")
	public String form(OaProduct oaProduct, Model model) {
		model.addAttribute("oaProduct", oaProduct);
		return "modules/oa/oaProductForm";
	}

	@RequiresPermissions("oa:oaProduct:edit")
	@RequestMapping(value = "save")
	public String save(OaProduct oaProduct, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, oaProduct)){
			return form(oaProduct, model);
		}
		Map<String, Object> variables = Maps.newHashMap();
		oaProductService.save(oaProduct,variables);
		addMessage(redirectAttributes, "保存商品成功");
		return "redirect:"+Global.getAdminPath()+"/oa/oaProduct/task/?repage";
	}
	
	@RequiresPermissions("oa:oaProduct:edit")
	@RequestMapping(value = "delete")
	public String delete(OaProduct oaProduct, RedirectAttributes redirectAttributes) {
		oaProductService.delete(oaProduct);
		addMessage(redirectAttributes, "删除商品成功");
		return "redirect:"+Global.getAdminPath()+"/oa/oaProduct/?repage";
	}
	
	 @RequestMapping(value = "complete", method = {RequestMethod.POST, RequestMethod.GET})
	    @ResponseBody
	    public String complete(String taskId,String pid,Variable var) {
	        try {
	            Map<String, Object> variables = var.getVariableMap();
	            // 提交流程任务
	            actTaskService.complete(taskId,pid,"true".equals(var.getValues())? "审核通过" : var.getValues().split(",")[1], variables);
	            return "success";
	        } catch (Exception e) {
	            logger.error("error on complete task {}, variables={}", new Object[]{taskId, var.getVariableMap(), e});
	            return "error";
	        }
	    }

}