/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.oa.dao;

import com.thinkgem.jeesite.common.persistence.CrudDao;
import com.thinkgem.jeesite.common.persistence.annotation.MyBatisDao;
import com.thinkgem.jeesite.modules.oa.entity.Leave;
import com.thinkgem.jeesite.modules.oa.entity.OaMyleave;

/**
 * 单表生成DAO接口
 * @author haop
 * @version 2015-05-12
 */
@MyBatisDao
public interface OaMyleaveDao extends CrudDao<OaMyleave> {
	/**
	 * 更新流程实例ID
	 * @param leave
	 * @return
	 */
	public int updateProcessInstanceId(OaMyleave oaMyleave);
}