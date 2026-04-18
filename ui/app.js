const currency = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
});

let projects = [];

async function request(url, options = {}) {
    const response = await fetch(url, options);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || "Request failed");
    }
    return data;
}

async function refreshAll() {
    const [dashboard, projectData, tasks, resources, milestones, expenses, monitoring, reports] = await Promise.all([
        request("/api/dashboard"),
        request("/api/projects"),
        request("/api/tasks"),
        request("/api/resources"),
        request("/api/milestones"),
        request("/api/expenses"),
        request("/api/monitoring"),
        request("/api/reports")
    ]);

    projects = projectData;
    renderDashboard(dashboard);
    renderProjects(projectData);
    renderTasks(tasks, projectData);
    renderResources(resources);
    renderMilestones(milestones);
    renderExpenses(expenses, projectData);
    renderMonitoring(monitoring);
    renderReports(reports);
}

function renderDashboard(data) {
    document.getElementById("metric-total-projects").textContent = data.totalProjects;
    document.getElementById("metric-active-projects").textContent = data.activeProjects;
    document.getElementById("metric-completed-projects").textContent = data.completedProjects;
    document.getElementById("metric-risk-alerts").textContent = data.riskAlerts;
    document.getElementById("dashboard-budget-fill").style.width = `${data.budgetUsagePercent}%`;
    document.getElementById("dashboard-budget-label").textContent = `${data.budgetUsagePercent}%`;
}

function renderProjects(items) {
    const body = document.getElementById("projects-table");
    body.innerHTML = items.map((project) => `
        <tr>
            <td>PRJ-${String(project.id).padStart(3, "0")}</td>
            <td><strong>${project.name}</strong><br><span class="muted">${project.description}</span></td>
            <td>${project.status}</td>
            <td>${project.startDate}</td>
            <td>${project.endDate}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="viewProject(${project.id})">View</button>
                    <button class="action-btn" onclick="editProject(${project.id})">Edit</button>
                    <button class="action-btn" onclick="deleteProject(${project.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderTasks(items, projectData) {
    const projectNames = Object.fromEntries(projectData.map((project) => [project.id, project.name]));
    const today = new Date().toISOString().slice(0, 10);
    document.getElementById("tasks-table").innerHTML = items.map((task) => `
        <tr class="${task.endDate < today && task.status !== "COMPLETED" ? "row-danger" : ""}">
            <td>TSK-${String(task.id).padStart(3, "0")}</td>
            <td>${projectNames[task.projectId] || task.projectId}</td>
            <td>${task.name}</td>
            <td>${task.startDate}</td>
            <td>${task.endDate}</td>
            <td>${task.status}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="editTask(${task.id})">Edit</button>
                    <button class="action-btn" onclick="updateTaskStatus(${task.id}, 'IN_PROGRESS')">In Progress</button>
                    <button class="action-btn" onclick="updateTaskStatus(${task.id}, 'COMPLETED')">Complete</button>
                    <button class="action-btn" onclick="deleteTask(${task.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderResources(items) {
    document.getElementById("resources-table").innerHTML = items.map((resource) => `
        <tr>
            <td>RES-${String(resource.id).padStart(3, "0")}</td>
            <td>${resource.name}</td>
            <td>${resource.role}</td>
            <td>${resource.available ? "Available" : "Busy"}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="editResource(${resource.id})">Edit</button>
                    <button class="action-btn" onclick="deleteResource(${resource.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderMilestones(items) {
    const today = new Date().toISOString().slice(0, 10);
    document.getElementById("milestones-table").innerHTML = items.map((milestone) => `
        <tr class="${milestone.targetDate < today && !milestone.completed ? "row-danger" : ""}">
            <td>MLS-${String(milestone.id).padStart(3, "0")}</td>
            <td>${milestone.projectId}</td>
            <td>${milestone.name}</td>
            <td>${milestone.targetDate}</td>
            <td>${milestone.completed ? "Completed" : "Pending"}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="editMilestone(${milestone.id})">Edit</button>
                    <button class="action-btn" onclick="completeMilestone(${milestone.id})">Mark Complete</button>
                    <button class="action-btn" onclick="deleteMilestone(${milestone.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderExpenses(items, projectData) {
    let total = 0;
    let remaining = 0;
    const projectNames = Object.fromEntries(projectData.map((project) => [project.id, project.name]));
    projectData.forEach((project) => {
        total += project.budget.totalAmount;
        remaining += project.budget.remainingAmount;
    });
    const used = total - remaining;
    document.getElementById("budget-total-all").textContent = currency.format(total);
    document.getElementById("budget-used-all").textContent = currency.format(used);
    document.getElementById("budget-remaining-all").textContent = currency.format(remaining);
    document.getElementById("budget-status").textContent = remaining < 0 ? "Exceeded" : "Healthy";

    document.getElementById("expenses-table").innerHTML = items.map((expense) => `
        <tr>
            <td>EXP-${String(expense.id).padStart(3, "0")}</td>
            <td>${projectNames[expense.projectId] || expense.projectId}</td>
            <td>${currency.format(expense.amount)}</td>
            <td>${expense.category}</td>
            <td>${expense.date}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="editExpense(${expense.id})">Edit</button>
                    <button class="action-btn" onclick="deleteExpense(${expense.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderMonitoring(data) {
    document.getElementById("monitor-delayed").textContent = data.delayedTaskCount;
    document.getElementById("monitor-overrun").textContent = data.budgetOverrunCount;
    document.getElementById("monitor-risks").textContent = data.riskAlertCount;
    document.getElementById("monitor-health").textContent = data.health;
}

function renderReports(data) {
    document.getElementById("report-projects").textContent = data.projectReports;
    document.getElementById("report-tasks").textContent = data.taskReports;
    document.getElementById("report-budgets").textContent = data.budgetReports;
}

async function submitForm(formId, url, messageId) {
    const form = document.getElementById(formId);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = Object.fromEntries(new FormData(form).entries());
        try {
            const result = await request(url, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            showMessage(messageId, result.message, true);
            form.reset();
            await refreshAll();
        } catch (error) {
            showMessage(messageId, error.message, false);
        }
    });
}

async function submitPutForm(formId, buildUrl, messageId) {
    const form = document.getElementById(formId);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = Object.fromEntries(new FormData(form).entries());
        try {
            const result = await request(buildUrl(payload), {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            showMessage(messageId, result.message, true);
            form.reset();
            await refreshAll();
        } catch (error) {
            showMessage(messageId, error.message, false);
        }
    });
}

function showMessage(id, text, success) {
    const node = document.getElementById(id);
    node.textContent = text;
    node.style.color = success ? "#3d7a66" : "#9a4137";
}

async function deleteProject(projectId) {
    await request(`/api/projects/${projectId}`, { method: "DELETE" });
    showMessage("project-message", "Project deleted", true);
    await refreshAll();
}

function viewProject(projectId) {
    const project = projects.find((entry) => entry.id === projectId);
    alert(`${project.name}\n\n${project.description}\n\nTasks: ${project.tasks.length}\nMilestones: ${project.milestones.length}\nRemaining Budget: ${currency.format(project.budget.remainingAmount)}`);
}

function editProject(projectId) {
    const project = projects.find((entry) => entry.id === projectId);
    const name = prompt("Updated project name", project.name);
    if (name === null) return;
    const description = prompt("Updated description", project.description);
    if (description === null) return;
    const startDate = prompt("Updated start date (YYYY-MM-DD)", project.startDate);
    if (startDate === null) return;
    const endDate = prompt("Updated end date (YYYY-MM-DD)", project.endDate);
    if (endDate === null) return;
    const status = prompt("Updated status (PLANNED/IN_PROGRESS/COMPLETED)", project.status);
    if (status === null) return;
    const budget = prompt("Updated budget amount", project.budget.totalAmount);
    if (budget === null) return;

    request(`/api/projects/${projectId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, description, startDate, endDate, status, budget })
    }).then(() => {
        showMessage("project-message", "Project updated", true);
        return refreshAll();
    }).catch((error) => showMessage("project-message", error.message, false));
}

function editTask(taskId) {
    const row = document.querySelector(`#task-edit-form input[name="id"]`);
    if (row) row.value = taskId;
}

async function updateTaskStatus(taskId, status) {
    await request(`/api/tasks/${taskId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status })
    });
    showMessage("task-message", "Task updated", true);
    await refreshAll();
}

async function deleteTask(taskId) {
    await request(`/api/tasks/${taskId}`, { method: "DELETE" });
    showMessage("task-message", "Task deleted", true);
    await refreshAll();
}

async function deleteResource(resourceId) {
    await request(`/api/resources/${resourceId}`, { method: "DELETE" });
    showMessage("resource-message", "Resource deleted", true);
    await refreshAll();
}

function editResource(resourceId) {
    const row = document.querySelector(`#resource-edit-form input[name="id"]`);
    if (row) row.value = resourceId;
}

async function completeMilestone(milestoneId) {
    await request(`/api/milestones/${milestoneId}`, { method: "PUT" });
    showMessage("milestone-message", "Milestone marked complete", true);
    await refreshAll();
}

async function deleteMilestone(milestoneId) {
    await request(`/api/milestones/${milestoneId}`, { method: "DELETE" });
    showMessage("milestone-message", "Milestone deleted", true);
    await refreshAll();
}

function editMilestone(milestoneId) {
    const row = document.querySelector(`#milestone-edit-form input[name="id"]`);
    if (row) row.value = milestoneId;
}

async function deleteExpense(expenseId) {
    await request(`/api/expenses/${expenseId}`, { method: "DELETE" });
    showMessage("expense-message", "Expense deleted", true);
    await refreshAll();
}

function editExpense(expenseId) {
    const row = document.querySelector(`#expense-edit-form input[name="id"]`);
    if (row) row.value = expenseId;
}

function setupNavigation() {
    document.querySelectorAll(".nav-link").forEach((button) => {
        button.addEventListener("click", () => {
            document.querySelectorAll(".nav-link").forEach((node) => node.classList.remove("active"));
            document.querySelectorAll(".screen").forEach((screen) => screen.classList.remove("active"));
            button.classList.add("active");
            document.getElementById(button.dataset.section).classList.add("active");
        });
    });
}

submitForm("project-form", "/api/projects", "project-message");
submitForm("task-form", "/api/tasks", "task-message");
submitPutForm("task-edit-form", (payload) => `/api/tasks/${payload.id}`, "task-message");
submitForm("resource-form", "/api/resources", "resource-message");
submitPutForm("resource-edit-form", (payload) => `/api/resources/${payload.id}`, "resource-message");
submitForm("milestone-form", "/api/milestones", "milestone-message");
submitPutForm("milestone-edit-form", (payload) => `/api/milestones/${payload.id}`, "milestone-message");
submitForm("expense-form", "/api/expenses", "expense-message");
submitPutForm("expense-edit-form", (payload) => `/api/expenses/${payload.id}`, "expense-message");
setupNavigation();
refreshAll().catch((error) => showMessage("project-message", error.message, false));
