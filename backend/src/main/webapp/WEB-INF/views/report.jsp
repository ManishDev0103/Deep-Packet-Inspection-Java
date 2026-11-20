<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inspection Report" scope="request"/>
<jsp:include page="layout/header.jsp"/>

<c:if test="${not empty successMsg}">
    <div class="alert alert-success" role="status"><c:out value="${successMsg}"/></div>
</c:if>
<c:if test="${not empty errorMsg}">
    <div class="alert alert-error" role="alert"><c:out value="${errorMsg}"/></div>
</c:if>

<div class="card">
    <h2>DPI Rule Overview</h2>
    <div class="stats-grid">
        <div class="stat-box"><div class="stat-label">Blocked IPs</div><div class="stat-val"><c:out value="${ruleStats.blockedIps()}"/></div></div>
        <div class="stat-box"><div class="stat-label">Blocked Apps</div><div class="stat-val"><c:out value="${ruleStats.blockedApps()}"/></div></div>
        <div class="stat-box"><div class="stat-label">Blocked Domains</div><div class="stat-val"><c:out value="${ruleStats.blockedDomains()}"/></div></div>
        <div class="stat-box"><div class="stat-label">Blocked Ports</div><div class="stat-val"><c:out value="${ruleStats.blockedPorts()}"/></div></div>
    </div>
</div>

<div class="card">
    <h2>Recent Inspected Flows</h2>
    <c:choose>
        <c:when test="${empty connections}">
            <p style="color: #94a3b8; margin-top: 0.5rem;">No flow records found. Process a PCAP file to view flows.</p>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th><th>Source</th><th>Destination</th><th>Protocol</th>
                        <th>State</th><th>App</th><th>SNI / Host</th><th>Packets</th><th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="conn" items="${connections}">
                        <tr>
                            <td><c:out value="${conn.id}"/></td>
                            <td><c:out value="${conn.srcIp}"/>:<c:out value="${conn.srcPort}"/></td>
                            <td><c:out value="${conn.dstIp}"/>:<c:out value="${conn.dstPort}"/></td>
                            <td><c:out value="${conn.protocol == 6 ? 'TCP' : (conn.protocol == 17 ? 'UDP' : conn.protocol)}"/></td>
                            <td><c:out value="${conn.state}"/></td>
                            <td><c:out value="${conn.appType}"/></td>
                            <td><c:out value="${conn.sni}"/></td>
                            <td><c:out value="${conn.packetsIn + conn.packetsOut}"/></td>
                            <td style="color: ${conn.action == 'FORWARD' ? '#4ade80' : '#f87171'}"><c:out value="${conn.action}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="layout/footer.jsp"/>
