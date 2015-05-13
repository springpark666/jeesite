<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>单表管理</title>
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
	    <li><a href="${ctx}/oa/oaMyleave/task">我的任务</a></li>
	    <li><a href="${ctx}/oa/oaMyleave/task/finished">已完成任务</a></li>
		<li class="active"><a href="${ctx}/oa/oaMyleave/list">我的请假</a></li>
		<shiro:hasPermission name="oa:oaMyleave:edit"><li><a href="${ctx}/oa/oaMyleave/form">请假添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="oaMyleave" action="${ctx}/oa/oaMyleave/" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<ul class="ul-form">
			<li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/></li>
			<li class="clearfix"></li>
		</ul>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead>
			<tr>
				<th>假种</th>
				<th>申请人</th>
				<th>申请原因</th>
				<th>申请时间</th>
				<th>开始时间</th>
				<th>结束时间</th>
				<th>当前节点</th>
				<th>任务创建时间</th>
				<th>流程状态</th>
				<shiro:hasPermission name="oa:oaMyleave:edit"><th>操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${page.list}" var="leave">
		        <c:set var="task" value="${leave.task}" />
				<c:set var="pi" value="${leave.processInstance}" />
			<tr>
				<td>${leave.leaveTypeDictLabel}</td>
					<td>${leave.createBy.name}</td>
					<td>${leave.reason}</td>
					<td><fmt:formatDate value="${leave.createDate}" type="both"/></td>
					<td><fmt:formatDate value="${leave.startTime}" type="both"/></td>
					<td><fmt:formatDate value="${leave.endTime}" type="both"/></td>
					<td>${task.name}</td>
					<td><fmt:formatDate value="${task.createTime}" type="both"/></td>
					<td>${pi.suspended ? "已挂起" : "正常" }；<b title='流程版本号'>V: ${leave.processDefinition.version}</b></td>
				<shiro:hasPermission name="oa:oaMyleave:edit">
					<td>
	    				<a href="${ctx}/oa/oaMyleave/form?id=${leave.id}">修改</a>
						<a href="${ctx}/oa/oaMyleave/delete?id=${leave.id}" onclick="return confirmx('确认要删除该单表吗？', this.href)">删除</a>
						<c:if test="${not empty pi}">
							<a target="_blank" href="${ctx}/act/task/trace/photo/${task.processDefinitionId}/${task.executionId}">跟踪</a>
							<a target="_blank" href="${pageContext.request.contextPath}/act/rest/diagram-viewer?processDefinitionId=${task.processDefinitionId}&processInstanceId=${task.processInstanceId}">跟踪1</a>
					    </c:if>
					    <c:if test="${empty pi}">
							<a target="_blank" href="${ctx}/act/task/trace/photo/${leave.historicProcessInstance.processDefinitionId}/${leave.historicProcessInstance.processDefinitionId}">跟踪</a>
							<a target="_blank" href="${pageContext.request.contextPath}/act/rest/diagram-viewer?processDefinitionId=${leave.historicProcessInstance.processDefinitionId}&processInstanceId=${task.processInstanceId}">跟踪1</a>
					    </c:if>
					</td>
				</shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>