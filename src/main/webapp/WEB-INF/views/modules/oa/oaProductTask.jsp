<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>商品管理</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$(".handle").click(function(){
				var obj = $(this);
				var taskId = obj.data("tid");
				var id = obj.data("id");
				var tkey=obj.data("tkey");
				var pid=obj.data("pid");
				
				alert(tkey);
				if(tkey=="productapprove") {
					// 可根据需求仿上例子定义按钮
					$.jBox.warning("是否通过审核？","提示",function(v,h,f){
					    if (v == 'yes') {
					    	complete(taskId,pid,[{
								key: 'productpass',
								value: true,
								type: 'B'
							}]);
					    }
					    if (v == 'no') {
					    	complete(taskId,pid,[{
								key: 'productpass',
								value: false,
								type: 'B'
							},{
								key: 'productnopass',
								value:"没有通过审核",
								type: 'S'
							}]);
					    }
					    if (v == 'cancel') {
					    }
					    return true;
					});
				}else if(tkey=="productedit"){
					
					// 可根据需求仿上例子定义按钮
					$.jBox.warning("是否重新修改商品？","提示",function(v,h,f){
					    if (v == 'yes'){
					    	location.href="${ctx}/oa/oaProduct/form?id="+id;
					    }
					    if (v == 'no'){
					    	complete(taskId,pid,[{
								key: 'edityes',
								value: false,
								type: 'B'
							},{
								key: 'abc',
								value:"取消商品上架",
								type: 'S'
							}]);
					    }
					    if (v == 'cancel') {
					    }
					    return true;
					});
					
				}else if(tkey=="reltype"){
					complete(taskId,pid,[{
						key: 'relok',
						value: true,
						type: 'B'
					}]);
				}
				
			});
		});
		
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
		
		function complete(taskId,pid, variables) {
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
		    $.post('${ctx}/oa/oaProduct/complete/', {
		    	"taskId": taskId,
		    	"pid":pid,
		        "keys": keys,
		        "values": values,
		        "types": types
		    }, function(data) {
		        top.$.jBox.tip('操作成功');
		        //location = '${pageContext.request.contextPath}' + data;
		        location.reload();
		    });
		}
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
	    <li  class="active"><a href="${ctx}/oa/oaProduct/task">我的任务</a></li>
	    <li><a href="${ctx}/oa/oaProduct/task/finished">已完成任务</a></li>
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
						<a target="_blank" href="${ctx}/act/task/trace/photo/${task.processDefinitionId}/${task.executionId}">跟踪</a>
						<c:if test="${empty task.assignee}">
							<a class="claim" href="#" onclick="javescript:claim('${task.id}');">签收</a>
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