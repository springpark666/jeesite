<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>商品管理</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			
		});
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
        	return false;
        }
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
	    <li><a href="${ctx}/oa/oaProduct/task">我的任务</a></li>
	    <li  class="active"><a href="${ctx}/oa/oaProduct/task/finished">已完成任务</a></li>
		<li><a href="${ctx}/oa/oaProduct/">商品列表</a></li>
		<shiro:hasPermission name="oa:oaProduct:edit"><li><a href="${ctx}/oa/oaProduct/form">商品添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="oaProduct" action="${ctx}/oa/oaProduct/" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<ul class="ul-form">
			<li><label>名称：</label>
				<form:input path="name" htmlEscape="false" maxlength="200" class="input-medium"/>
			</li>
			<li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/></li>
			<li class="clearfix"></li>
		</ul>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead>
			<tr>
				<th>名称</th>
				<th>更新时间</th>
				<th>备注信息</th>
				<th>当前节点</th>
				<th>任务创建时间</th>
				<th>流程状态</th>
				<th>操作</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${products}" var="oaProduct">
			 <c:set var="task" value="${oaProduct.task}" />
				<c:set var="pi" value="${oaProduct.processInstance}" />
			<tr>
				<td><a href="${ctx}/oa/oaProduct/form?id=${oaProduct.id}">
					${oaProduct.name}
				</a></td>
				<td>
					<fmt:formatDate value="${oaProduct.updateDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
				</td>
				<td>
					${oaProduct.remarks}
				</td>
				<td>${task.name}</td>
					<td><fmt:formatDate value="${task.createTime}" type="both"/></td>
					<td>${pi.suspended ? "已挂起" : "正常" }；<b title='流程版本号'>V: ${oaProduct.processDefinition.version}</b></td>
					<td>
					<a href="${ctx}/oa/oaProduct/form?id=${oaProduct.id}">详情</a>
						<c:if test="${empty pi}">
							<a target="_blank" href="${ctx}/act/task/trace/photo/${oaProduct.historicProcessInstance.processDefinitionId}/${oaProduct.historicProcessInstance.processDefinitionId}">跟踪</a>
							<a target="_blank" href="${pageContext.request.contextPath}/act/rest/diagram-viewer?processDefinitionId=${oaProduct.historicProcessInstance.processDefinitionId}&processInstanceId=${task.processInstanceId}">跟踪1</a>
					    </c:if>
						<c:if test="${not empty task.assignee}">
							<%-- 此处用tkey记录当前节点的名称 --%>
							<a class="handle" href="#" data-tkey="${task.taskDefinitionKey}" data-pid="${oaProduct.processInstanceId}"  data-tname="${task.name}"  data-id="${oaProduct.id}"  data-tid="${task.id}">办理</a>
						</c:if>
					</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>