/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.oa.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thinkgem.jeesite.common.config.Global;
import com.thinkgem.jeesite.common.persistence.Page;
import com.thinkgem.jeesite.common.web.BaseController;
import com.thinkgem.jeesite.common.utils.StringUtils;
import com.thinkgem.jeesite.modules.oa.entity.OaProduct;
import com.thinkgem.jeesite.modules.oa.service.OaProductService;

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
		oaProductService.save(oaProduct);
		addMessage(redirectAttributes, "保存商品成功");
		return "redirect:"+Global.getAdminPath()+"/oa/oaProduct/?repage";
	}
	
	@RequiresPermissions("oa:oaProduct:edit")
	@RequestMapping(value = "delete")
	public String delete(OaProduct oaProduct, RedirectAttributes redirectAttributes) {
		oaProductService.delete(oaProduct);
		addMessage(redirectAttributes, "删除商品成功");
		return "redirect:"+Global.getAdminPath()+"/oa/oaProduct/?repage";
	}

}