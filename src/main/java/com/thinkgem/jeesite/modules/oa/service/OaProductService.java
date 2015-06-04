/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.oa.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thinkgem.jeesite.common.persistence.Page;
import com.thinkgem.jeesite.common.service.CrudService;
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
	public void save(OaProduct oaProduct) {
		super.save(oaProduct);
	}
	
	@Transactional(readOnly = false)
	public void delete(OaProduct oaProduct) {
		super.delete(oaProduct);
	}
	
}