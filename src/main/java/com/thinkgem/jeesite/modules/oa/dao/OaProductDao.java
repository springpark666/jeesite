/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.oa.dao;

import com.thinkgem.jeesite.common.persistence.CrudDao;
import com.thinkgem.jeesite.common.persistence.annotation.MyBatisDao;
import com.thinkgem.jeesite.modules.oa.entity.OaProduct;

/**
 * 商品DAO接口
 * @author haop
 * @version 2015-06-04
 */
@MyBatisDao
public interface OaProductDao extends CrudDao<OaProduct> {
	
}