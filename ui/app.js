const currency = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 });
const state = {
    projects: [],
    tasks: [],
    resources: [],
    milestones: [],
    expenses: [],
    filters: {
        projectId: "",
        taskProjectId: "",
        resourceProjectId: "",
        milestoneProjectId: "",
        expenseProjectId: ""
    }
};

const modal = document.getElementById("edit-modal");
const modalTitle = document.getElementById("modal-title");
const modalForm = document.getElementById("modal-form");

async function request(url, options = {}) {
    const response = await fetch(url, options);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || "Request failed");
    }
    return data;
}

async function refreshAll() {
    const [dashboard, projects, tasks, resources, milestones, expenses] = await Promise.all([
        request("/api/dashboard"),
        request("/api/projects"),
        request("/api/tasks"),
        request("/api/resources"),
        request("/api/milestones"),
        request("/api/expenses")
    ]);

    Object.assign(state, { projects, tasks, resources, milestones, expenses });
    renderDashboard(dashboard);
    populateProjectSelects();
    renderProjects();
    renderTasks();
    renderResources();
    renderMilestones();
    renderExpenses();
}

function renderDashboard(dashboard) {
    document.getElementById("metric-total-projects").textContent = dashboard.totalProjects;
    document.getElementById("metric-in-progress-projects").textContent = dashboard.inProgressProjects;
    document.getElementById("metric-completed-projects").textContent = dashboard.completedProjects;
    document.getElementById("metric-overdue-tasks").textContent = dashboard.overdueTasks;
    document.getElementById("dashboard-budget-fill").style.width = `${dashboard.budgetUsagePercent}%`;
    document.getElementById("dashboard-budget-label").textContent = `${dashboard.budgetUsagePercent}%`;
}

function getProjectName(projectId) {
    return state.projects.find((project) => project.id === projectId)?.name || projectId;
}

function getVisibleProjects() {
    return state.filters.projectId
        ? state.projects.filter((project) => project.id === state.filters.projectId)
        : state.projects;
}

function getItemsForProject(items, projectId) {
    return projectId ? items.filter((item) => item.projectId === projectId) : items;
}

function renderProjects() {
    const projects = getVisibleProjects();
    document.getElementById("projects-table").innerHTML = projects.map((project) => `
        <tr>
            <td>${project.id}</td>
            <td><strong>${project.name}</strong><br><span class="muted">${project.objectives}</span></td>
            <td>${project.managerName}</td>
            <td>${formatDate(project.startDate)} to ${formatDate(project.endDate)}</td>
            <td>${project.status}</td>
            <td class="progress-cell">
                <strong>${project.progressPercent}%</strong>
                <div class="progress-bar compact"><div class="progress-fill" style="width:${project.progressPercent}%"></div></div>
                <span class="muted">${currency.format(project.budget.spentAmount)} spent of ${currency.format(project.budget.totalAmount)}</span>
            </td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="openProjectEdit('${project.id}')">Edit</button>
                    <button class="action-btn" onclick="showProjectReport('${project.id}')">Report</button>
                    <button class="action-btn danger" onclick="deleteRecord('/api/projects/${project.id}', 'project-message', 'Project deleted')">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderTasks() {
    const tasks = getItemsForProject(state.tasks, state.filters.taskProjectId);
    document.getElementById("tasks-table").innerHTML = tasks.map((task) => `
        <tr class="${isOverdue(task.dueDate, task.status) ? "row-danger" : ""}">
            <td>${task.id}</td>
            <td>${getProjectName(task.projectId)}</td>
            <td><strong>${task.name}</strong><br><span class="muted">${task.description}</span></td>
            <td>${task.assignedTo}</td>
            <td>${formatDate(task.startDate)} to ${formatDate(task.dueDate)}</td>
            <td>${task.status}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="openTaskEdit('${task.id}')">Edit</button>
                    <button class="action-btn" onclick="advanceTask('${task.id}')">Advance</button>
                    <button class="action-btn danger" onclick="deleteRecord('/api/tasks/${task.id}', 'task-message', 'Task deleted')">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderResources() {
    const resources = getItemsForProject(state.resources, state.filters.resourceProjectId);
    document.getElementById("resources-table").innerHTML = resources.map((resource) => `
        <tr>
            <td>${resource.id}</td>
            <td>${getProjectName(resource.projectId)}</td>
            <td>${resource.name}</td>
            <td>${resource.role}</td>
            <td>${resource.availability ? "Available" : "Unavailable"}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="openResourceEdit('${resource.id}')">Edit</button>
                    <button class="action-btn danger" onclick="deleteRecord('/api/resources/${resource.id}', 'resource-message', 'Resource deleted')">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderMilestones() {
    const milestones = getItemsForProject(state.milestones, state.filters.milestoneProjectId);
    document.getElementById("milestones-table").innerHTML = milestones.map((milestone) => `
        <tr>
            <td>${milestone.id}</td>
            <td>${getProjectName(milestone.projectId)}</td>
            <td>${milestone.name}</td>
            <td>${formatDate(milestone.targetDate)}</td>
            <td>${milestone.completionStatus ? "Completed" : "Pending"}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="openMilestoneEdit('${milestone.id}')">Edit</button>
                    <button class="action-btn" onclick="toggleMilestone('${milestone.id}', ${!milestone.completionStatus})">${milestone.completionStatus ? "Reopen" : "Complete"}</button>
                    <button class="action-btn danger" onclick="deleteRecord('/api/milestones/${milestone.id}', 'milestone-message', 'Milestone deleted')">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderExpenses() {
    const expenses = getItemsForProject(state.expenses, state.filters.expenseProjectId);
    document.getElementById("expenses-table").innerHTML = expenses.map((expense) => `
        <tr>
            <td>${expense.id}</td>
            <td>${getProjectName(expense.projectId)}</td>
            <td>${expense.description}</td>
            <td>${expense.category}</td>
            <td>${currency.format(expense.amount)}</td>
            <td>${formatDate(expense.expenseDate)}</td>
            <td>
                <div class="action-group">
                    <button class="action-btn" onclick="openExpenseEdit('${expense.id}')">Edit</button>
                    <button class="action-btn danger" onclick="deleteRecord('/api/expenses/${expense.id}', 'expense-message', 'Expense deleted')">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function populateProjectSelects() {
    const allOptions = ['<option value="">All Projects</option>', ...state.projects.map((project) => `<option value="${project.id}">${project.name} (${project.id})</option>`)].join("");
    const createOptions = ['<option value="">Select project</option>', ...state.projects.map((project) => `<option value="${project.id}">${project.name} (${project.id})</option>`)].join("");

    [
        ["project-screen-filter", allOptions],
        ["task-project-id", createOptions],
        ["resource-project-id", createOptions],
        ["milestone-project-id", createOptions],
        ["expense-project-id", createOptions],
        ["report-project-id", allOptions],
        ["task-screen-filter", allOptions],
        ["resource-screen-filter", allOptions],
        ["milestone-screen-filter", allOptions],
        ["expense-screen-filter", allOptions]
    ].forEach(([id, options]) => {
        const select = document.getElementById(id);
        if (!select) {
            return;
        }
        const previousValue = select.value;
        select.innerHTML = options;
        if (previousValue && state.projects.some((project) => project.id === previousValue)) {
            select.value = previousValue;
        } else if (id in {
            "task-screen-filter": true,
            "resource-screen-filter": true,
            "milestone-screen-filter": true,
            "expense-screen-filter": true
        }) {
            select.value = state.filters[id.replace("-screen-filter", "ProjectId").replace("-", "")] || "";
        }
    });

    document.getElementById("project-screen-filter").value = state.filters.projectId;
    document.getElementById("task-screen-filter").value = state.filters.taskProjectId;
    document.getElementById("resource-screen-filter").value = state.filters.resourceProjectId;
    document.getElementById("milestone-screen-filter").value = state.filters.milestoneProjectId;
    document.getElementById("expense-screen-filter").value = state.filters.expenseProjectId;
}

function openModal(title, fields, original, onSubmit) {
    modalTitle.textContent = title;
    modalForm.innerHTML = fields.map((field) => buildField(field, original[field.name])).join("") + "<button type='submit'>Save Changes</button>";
    modal.classList.remove("hidden");
    modalForm.onsubmit = async (event) => {
        event.preventDefault();
        const current = Object.fromEntries(new FormData(modalForm).entries());
        const patch = {};
        fields.forEach((field) => {
            const nextValue = normalizeField(field, current[field.name]);
            const previousValue = normalizeField(field, original[field.name]);
            if (String(nextValue) !== String(previousValue)) {
                patch[field.name] = nextValue;
            }
        });
        if (!Object.keys(patch).length) {
            closeModal();
            return;
        }
        await onSubmit(patch);
        closeModal();
        await refreshAll();
    };
}

function buildField(field, value) {
    if (field.type === "select") {
        return `<label class="field-group"><span>${field.label}</span><select name="${field.name}">${field.options.map((option) => `<option value="${option}" ${String(value) === String(option) ? "selected" : ""}>${option}</option>`).join("")}</select></label>`;
    }
    return `<label class="field-group"><span>${field.label}</span><input type="${field.type}" name="${field.name}" value="${escapeValue(field.type === "date" ? formatDate(value) : value)}"></label>`;
}

function normalizeField(field, value) {
    if (field.type === "select" && field.booleanSelect) {
        return value === "true";
    }
    if (field.type === "number") {
        return Number(value);
    }
    return value;
}

function closeModal() {
    modal.classList.add("hidden");
}

async function deleteRecord(url, messageId, successMessage) {
    if (!confirm("Delete this record now?")) {
        return;
    }
    try {
        await request(url, { method: "DELETE" });
        showMessage(messageId, successMessage, true);
        await refreshAll();
    } catch (error) {
        showMessage(messageId, error.message, false);
    }
}

async function advanceTask(taskId) {
    const task = state.tasks.find((item) => item.id === taskId);
    const nextStatus = task.status === "PLANNED" ? "IN_PROGRESS" : "COMPLETED";
    await request(`/api/tasks/${taskId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: nextStatus })
    });
    showMessage("task-message", "Task updated. Project progress was recalculated.", true);
    await refreshAll();
}

async function toggleMilestone(milestoneId, completionStatus) {
    await request(`/api/milestones/${milestoneId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ completionStatus })
    });
    showMessage("milestone-message", "Milestone updated. Project progress was recalculated.", true);
    await refreshAll();
}

function openProjectEdit(projectId) {
    const project = state.projects.find((item) => item.id === projectId);
    openModal("Edit Project", [
        { name: "name", label: "Project Name", type: "text" },
        { name: "description", label: "Description", type: "text" },
        { name: "managerName", label: "Manager", type: "text" },
        { name: "objectives", label: "Objectives", type: "text" },
        { name: "startDate", label: "Start Date", type: "date" },
        { name: "endDate", label: "End Date", type: "date" },
        { name: "budgetTotal", label: "Budget Total", type: "number" },
        { name: "budgetSpent", label: "Budget Spent", type: "number" }
    ], { ...project, budgetTotal: project.budget.totalAmount, budgetSpent: project.budget.spentAmount }, async (patch) => {
        await request(`/api/projects/${projectId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(patch)
        });
        showMessage("project-message", "Project updated", true);
    });
}

function openTaskEdit(taskId) {
    const task = state.tasks.find((item) => item.id === taskId);
    openModal("Edit Task", [
        { name: "name", label: "Task Name", type: "text" },
        { name: "description", label: "Description", type: "text" },
        { name: "assignedTo", label: "Assigned To", type: "text" },
        { name: "startDate", label: "Start Date", type: "date" },
        { name: "dueDate", label: "Due Date", type: "date" },
        { name: "priority", label: "Priority", type: "select", options: ["HIGH", "MEDIUM", "LOW"] },
        { name: "status", label: "Status", type: "select", options: ["PLANNED", "IN_PROGRESS", "COMPLETED"] }
    ], task, async (patch) => {
        await request(`/api/tasks/${taskId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(patch)
        });
        showMessage("task-message", "Task updated", true);
    });
}

function openResourceEdit(resourceId) {
    const resource = state.resources.find((item) => item.id === resourceId);
    openModal("Edit Resource", [
        { name: "name", label: "Name", type: "text" },
        { name: "role", label: "Role", type: "text" },
        { name: "skillSet", label: "Skill Set", type: "text" },
        { name: "availability", label: "Availability", type: "select", options: ["true", "false"], booleanSelect: true }
    ], { ...resource, availability: String(resource.availability) }, async (patch) => {
        await request(`/api/resources/${resourceId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(patch)
        });
        showMessage("resource-message", "Resource updated", true);
    });
}

function openMilestoneEdit(milestoneId) {
    const milestone = state.milestones.find((item) => item.id === milestoneId);
    openModal("Edit Milestone", [
        { name: "name", label: "Milestone Name", type: "text" },
        { name: "description", label: "Description", type: "text" },
        { name: "targetDate", label: "Target Date", type: "date" },
        { name: "completionStatus", label: "Completed", type: "select", options: ["true", "false"], booleanSelect: true }
    ], { ...milestone, completionStatus: String(milestone.completionStatus) }, async (patch) => {
        await request(`/api/milestones/${milestoneId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(patch)
        });
        showMessage("milestone-message", "Milestone updated", true);
    });
}

function openExpenseEdit(expenseId) {
    const expense = state.expenses.find((item) => item.id === expenseId);
    openModal("Edit Expense", [
        { name: "description", label: "Description", type: "text" },
        { name: "category", label: "Category", type: "text" },
        { name: "amount", label: "Amount", type: "number" },
        { name: "expenseDate", label: "Expense Date", type: "date" }
    ], expense, async (patch) => {
        await request(`/api/expenses/${expenseId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(patch)
        });
        showMessage("expense-message", "Expense updated", true);
    });
}

async function showProjectReport(projectId) {
    activateSection("reports");
    document.getElementById("report-project-id").value = projectId;
    await generateReport(projectId);
}

async function generateReport(projectId = document.getElementById("report-project-id").value) {
    if (!projectId) {
        showMessage("report-message", "Choose a project first.", false);
        return;
    }
    const report = await request(`/api/projects/${projectId}/report`);
    document.getElementById("report-output").innerHTML = `
        <div class="report-grid">
            <article class="stat-card"><span>Project</span><strong>${report.projectName}</strong></article>
            <article class="stat-card"><span>Total Tasks</span><strong>${report.totalTasks}</strong></article>
            <article class="stat-card"><span>Completed</span><strong>${report.completedTasks}</strong></article>
            <article class="stat-card"><span>In Progress</span><strong>${report.inProgressTasks}</strong></article>
            <article class="stat-card"><span>Planned</span><strong>${report.plannedTasks}</strong></article>
            <article class="stat-card"><span>Progress</span><strong>${report.overallProgress}%</strong></article>
            <article class="stat-card"><span>Budget</span><strong>${currency.format(report.budgetTotal)}</strong></article>
            <article class="stat-card"><span>Spent</span><strong>${currency.format(report.totalExpenses)}</strong></article>
            <article class="stat-card"><span>Remaining</span><strong>${currency.format(report.remainingBudget)}</strong></article>
        </div>
        <div class="table-card">
            <h3>Milestones</h3>
            <table><thead><tr><th>Name</th><th>Target Date</th><th>Status</th></tr></thead><tbody>
                ${report.milestoneStatus.map((milestone) => `<tr><td>${milestone.name}</td><td>${formatDate(milestone.targetDate)}</td><td>${milestone.completionStatus ? "Completed" : "Pending"}</td></tr>`).join("")}
            </tbody></table>
        </div>
        <div class="table-card">
            <h3>Task Details</h3>
            <table><thead><tr><th>Task</th><th>Owner</th><th>Dates</th><th>Status</th><th>Priority</th></tr></thead><tbody>
                ${report.taskDetails.map((task) => `<tr><td><strong>${task.taskName}</strong><br><span class="muted">${task.description}</span></td><td>${task.assignedTo}</td><td>${formatDate(task.startDate)} to ${formatDate(task.dueDate)}</td><td>${task.status}</td><td>${task.priority}</td></tr>`).join("")}
            </tbody></table>
        </div>
    `;
    showMessage("report-message", "Report generated", true);
}

function submitCreateForm(formId, url, messageId) {
    document.getElementById(formId).addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = normalizePayload(Object.fromEntries(new FormData(event.target).entries()));
        try {
            await request(url, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            showMessage(messageId, "Created successfully", true);
            event.target.reset();
            await refreshAll();
        } catch (error) {
            showMessage(messageId, error.message, false);
        }
    });
}

function normalizePayload(payload) {
    const normalized = {};
    Object.entries(payload).forEach(([key, value]) => {
        if (value === "true" || value === "false") {
            normalized[key] = value === "true";
        } else if (["amount", "budgetTotal", "budgetSpent"].includes(key)) {
            normalized[key] = Number(value);
        } else {
            normalized[key] = value;
        }
    });
    return normalized;
}

function showMessage(id, text, success) {
    const node = document.getElementById(id);
    if (!node) {
        return;
    }
    node.textContent = text;
    node.style.color = success ? "#3d7a66" : "#9a4137";
}

function activateSection(sectionId) {
    document.querySelectorAll(".nav-link").forEach((node) => node.classList.toggle("active", node.dataset.section === sectionId));
    document.querySelectorAll(".screen").forEach((screen) => screen.classList.toggle("active", screen.id === sectionId));
}

function setupNavigation() {
    document.querySelectorAll(".nav-link").forEach((button) => {
        button.addEventListener("click", () => activateSection(button.dataset.section));
    });
}

function setupFilters() {
    document.getElementById("project-screen-filter").addEventListener("change", (event) => {
        state.filters.projectId = event.target.value;
        renderProjects();
    });

    document.getElementById("task-screen-filter").addEventListener("change", (event) => {
        state.filters.taskProjectId = event.target.value;
        renderTasks();
    });

    document.getElementById("resource-screen-filter").addEventListener("change", (event) => {
        state.filters.resourceProjectId = event.target.value;
        renderResources();
    });

    document.getElementById("milestone-screen-filter").addEventListener("change", (event) => {
        state.filters.milestoneProjectId = event.target.value;
        renderMilestones();
    });

    document.getElementById("expense-screen-filter").addEventListener("change", (event) => {
        state.filters.expenseProjectId = event.target.value;
        renderExpenses();
    });
}

function formatDate(value) {
    return value ? String(value).split("T")[0] : "";
}

function isOverdue(dateValue, status) {
    return formatDate(dateValue) < new Date().toISOString().slice(0, 10) && status !== "COMPLETED";
}

function escapeValue(value) {
    return String(value ?? "").replace(/"/g, "&quot;");
}

document.getElementById("modal-close").addEventListener("click", closeModal);
document.getElementById("generate-report").addEventListener("click", () => generateReport());
submitCreateForm("project-form", "/api/projects", "project-message");
submitCreateForm("task-form", "/api/tasks", "task-message");
submitCreateForm("resource-form", "/api/resources", "resource-message");
submitCreateForm("milestone-form", "/api/milestones", "milestone-message");
submitCreateForm("expense-form", "/api/expenses", "expense-message");
setupNavigation();
setupFilters();
refreshAll().catch((error) => showMessage("project-message", error.message, false));
