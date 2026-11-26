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
    <form id="add-rule-form" action="/legacy/rules/add" method="post">
        <div class="form-row">
            <div class="form-field">
                <label for="add-rule-type">Rule type</label>
                <select id="add-rule-type" name="ruleType" required aria-describedby="add-rule-type-help">
                    <option value="IP">IP Address</option>
                    <option value="APP">Application</option>
                    <option value="DOMAIN">Domain (wildcards: *.domain.com)</option>
                    <option value="PORT">Destination Port</option>
                </select>
                <p class="field-help" id="add-rule-type-help">Choose which traffic property this rule will block.</p>
            </div>
            <div class="form-field">
                <label for="add-rule-value">Rule value</label>
                <input id="add-rule-value" type="text" name="ruleValue" required
                       aria-describedby="add-rule-value-help" style="flex: 1;">
                <p class="field-help" id="add-rule-value-help">Enter an IP address, application, domain, or destination port.</p>
            </div>
            <button type="submit">Add Rule</button>
        </div>
    </form>
</div>

<div class="card">
    <h2>Delete Blocking Rule</h2>
    <form id="delete-rule-form" action="/legacy/rules/delete" method="post">
        <div class="form-row">
            <div class="form-field">
                <label for="delete-rule-type">Rule type</label>
                <select id="delete-rule-type" name="ruleType" required aria-describedby="delete-rule-type-help">
                    <option value="IP">IP Address</option>
                    <option value="APP">Application</option>
                    <option value="DOMAIN">Domain</option>
                    <option value="PORT">Destination Port</option>
                </select>
                <p class="field-help" id="delete-rule-type-help">Choose the type of the rule to remove.</p>
            </div>
            <div class="form-field">
                <label for="delete-rule-value">Rule value</label>
                <input id="delete-rule-value" type="text" name="ruleValue" required
                       aria-describedby="delete-rule-value-help" style="flex: 1;">
                <p class="field-help" id="delete-rule-value-help">Enter the exact stored rule value to remove.</p>
            </div>
            <button type="submit" class="btn-danger">Delete Rule</button>
        </div>
        <div id="delete-confirmation" class="delete-confirmation" role="group"
             aria-labelledby="delete-confirmation-text" aria-describedby="delete-confirmation-help" hidden>
            <p id="delete-confirmation-text"></p>
            <p class="field-help" id="delete-confirmation-help">Review the rule value before confirming deletion.</p>
            <div class="delete-confirmation-actions">
                <button type="button" id="keep-rule">Keep Blocking Rule</button>
                <button type="button" id="confirm-delete" class="btn-danger">Delete Rule</button>
            </div>
        </div>
    </form>
</div>

<script>
(function () {
    var form = document.getElementById('delete-rule-form');
    var typeField = document.getElementById('delete-rule-type');
    var valueField = document.getElementById('delete-rule-value');
    var confirmation = document.getElementById('delete-confirmation');
    var confirmationText = document.getElementById('delete-confirmation-text');
    var keepButton = document.getElementById('keep-rule');
    var confirmButton = document.getElementById('confirm-delete');
    var deleteButton = form.querySelector('button[type="submit"]');
    var confirmed = false;

    form.addEventListener('submit', function (event) {
        if (confirmed) {
            confirmed = false;
            return;
        }

        event.preventDefault();
        var exactValue = valueField.value.trim();
        if (!exactValue) return;

        typeField.disabled = true;
        valueField.disabled = true;
        confirmationText.textContent = 'Delete rule "' + exactValue + '"?';
        confirmation.hidden = false;
        confirmButton.focus();
    });

    keepButton.addEventListener('click', function () {
        typeField.disabled = false;
        valueField.disabled = false;
        confirmation.hidden = true;
        deleteButton.focus();
    });

    confirmButton.addEventListener('click', function () {
        valueField.value = valueField.value.trim();
        typeField.disabled = false;
        valueField.disabled = false;
        confirmed = true;
        form.requestSubmit();
    });
})();
</script>

<jsp:include page="layout/footer.jsp"/>
