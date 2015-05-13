<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>请假一览</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
        	return false;
        }
		function claim(taskId) {
			$.get('${ctx}/act/task/claim' ,{taskId: taskId}, function(data) {
	        	top.$.jBox.tip('签收完成');
	            //location = '${pageContext.request.contextPath}' + data;
	        	location.reload();
		    });
		}
		$(document).ready(function() {
		$(".handle").click(function(){
			var obj = $(this);
			var taskId = obj.data("tid");
			var leaveId = obj.data("id");
			var tkey=obj.data("tkey");
			//部门领导审批
			if(tkey=="approve") {
				$.getJSON("${ctx}/oa/leave/detail/" + leaveId , function(data){
					 var html= Mustache.render($("#auditTemplate").html(),data);
					 top.$.jBox(html, { title: "流程办理["+obj.data("tname") + "]",buttons:{"同意":"yes","驳回":"no","取消":"cancel"},submit: function (v, h, f) {
						 //同意
						 if(v=="yes") {
								complete(taskId, [{
									key: 'deptLeaderPass',
									value: true,
									type: 'B'
								}]);
						//驳回
						 } else if (v=="no") {
							 top.$.jBox("<div style='padding:10px;'><textarea id='leaderBackReason' style='width: 250px; height: 60px;'></textarea></div>", { title: "请填写驳回理由", submit: function () {
								 var leaderBackReason=top.$("#leaderBackReason").val();
								 //必须填写驳回理由
								 if($.trim(leaderBackReason)=="") {
									 top.$.jBox.error('请填写驳回理由', '错误');
									 return false;
								 } else {
										complete(taskId, [{
											key: 'deptLeaderPass',
											value: false,
											type: 'B'
										}, {
											key: 'leaderBackReason',
											value: leaderBackReason,
											type: 'S'
										}]);
								 }
							 }
							});
						 }
					 }
					 });
				});
			}
			//人事审批
			if(tkey=="hrAudit") {
				$.getJSON("${ctx}/oa/leave/detail/" + leaveId , function(data){
					 var html= Mustache.render($("#auditTemplate").html(),data);
					 top.$.jBox(html, { title: "流程办理["+obj.data("tname") + "]",buttons:{"同意":"yes","驳回":"no","取消":"cancel"},submit: function (v, h, f) {
						 //同意
						 if(v=="yes") {
								complete(taskId, [{
									key: 'hrPass',
									value: true,
									type: 'B'
								}]);
						 }
						 //驳回
						 else if (v=="no") {
							 top.$.jBox("<div style='padding:10px;'><textarea id='hrBackReason' style='width: 250px; height: 60px;'></textarea></div>", { title: "请填写驳回理由", submit: function () {
								 var hrBackReason=top.$("#hrBackReason").val();
								 //必须填写驳回理由
								 if($.trim(hrBackReason)=="") {
									 top.$.jBox.error('请填写驳回理由', '错误');
									 return false;
								 } else {
									complete(taskId, [{
										key: 'hrPass',
										value: false,
										type: 'B'
									}, {
										key: 'hrBackReason',
										value: hrBackReason,
										type: 'S'
									}]);
								 }
							   }
							});
						 }
					  }
				   });
				});
			}
			//调整申请
			if(tkey=="modifyApply") {
				$.getJSON("${ctx}/oa/leave/detail-with-vars/" + leaveId + "/" + taskId, function(data){
					 var html= Mustache.render($("#modifyApplyTemplate").html(),data);
					 top.$.jBox(html, { title: "流程办理["+obj.data("tname") + "]",buttons:{"重新申请":"yes","放弃申请":"no","取消":"cancel"},submit: function (v, h, f) {
						 //重新申请或者取消申请
						 var reApply=true;
						 if(v=="no") {
							 reApply=false;
						 }
						 if(v=="yes"|| v=="no") {
							complete(taskId, [{
								key: 'reApply',
								value: reApply,
								type: 'B'
							}, {
								key: 'leaveType',
								value: top.$('#modifyApplyContent #leaveType').val(),
								type: 'S'
							}, {
								key: 'startTime',
								value: top.$('#modifyApplyContent #startTime').val(),
								type: 'D'
							}, {
								key: 'endTime',
								value: top.$('#modifyApplyContent #endTime').val(),
								type: 'D'
							}, {
								key: 'reason',
								value: top.$('#modifyApplyContent #reason').val(),
								type: 'S'
							}]);
						 } 
					 	}
					 });
					 top.$("#leaveType").val(data.leaveType);
				});
			}
			
			//销假
			if(tkey=="reportBack") {
				$.getJSON("${ctx}/oa/leave/detail/" +leaveId , function(data){
					 var html= Mustache.render($("#reportBackTemplate").html(),data);
					 top.$.jBox(html, { title: "流程办理["+obj.data("tname") + "]",buttons:{"提交":"yes","取消":"cancel"},submit: function (v, h, f) {
						 //同意
						 if(v=="yes") {
							var realityStartTime = top.$('#realityStartTime').val();
							var realityEndTime = top.$('#realityEndTime').val();	
							if (realityStartTime == '' || realityEndTime=="") {
								top.$.jBox.error('请选择实际开始时间和实际结束日期！');
								return false;
							} else {
								complete(taskId, [{
									key: 'realityStartTime',
									value: realityStartTime,
									type: 'D'
								}, {
									key: 'realityEndTime',
									value: realityEndTime,
									type: 'D'
								}]);
							}
						 }
					 }
					 });
				});
			}
			
		})
	});
	
	/**
	 * 完成任务
	 * @param {Object} taskId
	 */
	function complete(taskId, variables) {
		// 转换JSON为字符串
	    var keys = "", values = "", types = "";
		if (variables) {
			$.each(variables, function(idx) {
				if (keys != "") {
					keys += ",";
					values += ",";
					types += ",";
				}
				keys += this.key;
				values += this.value;
				types += this.type;
			});
		}
		// 发送任务完成请求
	    $.post('${ctx}/act/task/complete/', {
	    	taskId: taskId,
	        "vars.keys": keys,
	        "vars.values": values,
	        "vars.types": types
	    }, function(data) {
	        top.$.jBox.tip('任务完成');
	        //location = '${pageContext.request.contextPath}' + data;
	        location.reload();
	    });
	}
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/oa/leave/">待办任务</a></li>
		<li class="active"><a href="${ctx}/oa/leave/list">所有任务</a></li>
		<shiro:hasPermission name="oa:leave:edit"><li><a href="${ctx}/oa/leave/form">请假申请</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="leave" action="${ctx}/oa/leave/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<div>
			<label>请假编号：&nbsp;</label><form:input path="ids" htmlEscape="false" maxlength="50" class="input-medium" placeholder="多个用逗号或空格隔开"/>
		</div>
		<div style="margin-top:8px;">
			<label>创建时间：</label>
			<input id="createDateStart"  name="createDateStart"  type="text" readonly="readonly" maxlength="20" class="input-medium Wdate" style="width:163px;"
				value="<fmt:formatDate value="${leave.createDateStart}" pattern="yyyy-MM-dd"/>"
					onclick="WdatePicker({dateFmt:'yyyy-MM-dd'});"/>
				　--　
			<input id="createDateEnd" name="createDateEnd" type="text" readonly="readonly" maxlength="20" class="input-medium Wdate" style="width:163px;"
				value="<fmt:formatDate value="${leave.createDateEnd}" pattern="yyyy-MM-dd"/>"
					onclick="WdatePicker({dateFmt:'yyyy-MM-dd'});"/>
			&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead><tr>
			<th>请假编号</th>
			<th>创建人</th>
			<th>创建时间</th>
			<th>请假原因</th>
			<th>当前节点</th>
			<th>操作</th>
		</tr></thead>
		<tbody>
		<c:forEach items="${page.list}" var="leave">
			<c:set var="task" value="${leave.task }" />
			<c:set var="pi" value="${leave.processInstance }" />
			<c:set var="hpi" value="${leave.historicProcessInstance }" />
			<tr>
				<td>${leave.id}</td>
				<td>${leave.createBy.name}</td>
				<td><fmt:formatDate value="${leave.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td>${leave.reason}</td>
				<c:if test="${not empty task}">
					<td>${task.name}</td>
					<td>
					<c:if test="${empty leave.task.assignee}">
							<a class="claim" href="#" onclick="javescript:claim('${task.id}');">签收</a>
				</c:if>
				<c:if test="${not empty leave.task.assignee}">
					<%-- 此处用tkey记录当前节点的名称 --%>
					<a class="handle" href="#" data-tkey="${task.taskDefinitionKey}" data-tname="${task.name}"  data-id="${leave.id}"  data-tid="${task.id}">办理</a>
				</c:if>
					<a target="_blank" href="${ctx}/act/task/trace/photo/${task.processDefinitionId}/${task.executionId}">跟踪</a></td>
				</c:if>
				<c:if test="${empty task}">
					<td>已结束</td>
					<td>&nbsp;</td>
				</c:if>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
