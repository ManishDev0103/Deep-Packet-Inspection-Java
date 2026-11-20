<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Rule Manager" scope="request"/>
<jsp:include page="layout/header.jsp"/>

<c:if test="${not empty successMsg}">
    <div class="alert alert-success" role="status"><c:out value="${successMsg}"/></div>
</c:if>
<c:if test="${not empty errorMsg}">
    <div class="alert alert-error" role="alert"><c:out value="${errorMsg}"/></div>
</c:if>

<div class="card">
    <h2>Active Rule Stats</h2>
    <div class="stats-grid">
        <div class="stat-box"><div class="stat-label">Blocked IPs</div><div class="stat-val"><c:out value="${ruleStats.blockedIps()}"/></div></div>
        <div class="stat-box"><div class="stat-label">Blocked Applications</div><div class="stat-val"><c:out value="${ruleStats.blockedApps()}"/></div></div>
        <div class="stat-box"><div class="stat-label">Blocked Domains</div><div class="stat-val"><c:out value="${ruleStats.blockedDomains()}"/></div></div>
        <div class="stat-box"><div class="stat-label">Blocked Ports</div><div class="stat-val"><c:out value="${ruleStats.blockedPorts()}"/></div></div>
    </div>
</div>

<div class="card">
    <h2>Add Blocking Rule</h2>
    <form action="/legacy/rules/add" method="post">
        <div class="form-row">
            <select name="ruleType" required aria-label="Rule type">
                <option value="IP">IP Address</option>
                <option value="APP">Application</option>
                <option value="DOMAIN">Domain (wildcards: *.domain.com)</option>
                <option value="PORT">Destination Port</option>
            </select>
            <input type="text" name="ruleValue"
                   placeholder="e.g. 192.168.1.50, YouTube, *.facebook.com, 443"
                   required aria-label="Rule value" style="flex: 1; min-width: 0;">
            <button type="submit">Add Rule</button>
        </div>
    </form>
</div>

<div class="card">
    <h2>Delete Blocking Rule</h2>
    <form action="/legacy/rules/delete" method="post">
        <div class="form-row">
            <select name="ruleType" required aria-label="Rule type">
                <option value="IP">IP Address</option>
                <option value="APP">Application</option>
                <option value="DOMAIN">Domain</option>
                <option value="PORT">Destination Port</option>
            </select>
            <input type="text" name="ruleValue"
                   placeholder="Exact rule value to remove"
                   required aria-label="Rule value" style="flex: 1; min-width: 0;">
            <button type="submit" class="btn-danger">Delete Rule</button>
        </div>
    </form>
</div>

<jsp:include page="layout/footer.jsp"/>
