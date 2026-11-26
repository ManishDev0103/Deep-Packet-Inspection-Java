<%@ page import="java.time.Year" %>
<%
    request.setAttribute("currentYear", Year.now().getValue());
%>
</div>
<footer>
    <p>Deep Packet Inspection System &copy; ${currentYear} &mdash;
       <a href="/">React Dashboard</a> &nbsp;|&nbsp;
       <a href="/legacy/report">Inspection Report</a> &nbsp;|&nbsp;
       <a href="/legacy/rules">Rule Manager</a>
    </p>
    <p style="margin-top:0.25rem; font-size:0.75rem; color:#475569;">Java 21 &amp; Spring Boot 3.x WAR &mdash; Processing stays on this machine.</p>
</footer>
</body>
</html>
