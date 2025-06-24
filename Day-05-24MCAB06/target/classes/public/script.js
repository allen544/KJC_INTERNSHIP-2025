let manageMode = false;
let isUpdating = false;
let currentId = null;

function toggleEmployeeList() {
  document.getElementById("employeeTable").classList.toggle("hidden");
}

function toggleManageMode() {
  manageMode = !manageMode;
  document.querySelectorAll(".manageCol").forEach(col => {
    col.classList.toggle("hidden");
  });
  loadEmployees();
}

async function loadEmployees() {
  const res = await fetch("/api/employees");
  const data = await res.json();
  const tbody = document.querySelector("#employeeTable tbody");
  tbody.innerHTML = "";

  data.forEach(emp => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${emp.name}</td>
      <td>${emp.email}</td>
      <td>${emp.department}</td>
      <td>${(emp.skills || []).join(', ')}</td>
      <td>${(emp.joiningDate || '').substring(0, 10)}</td>
      <td class="manageCol hidden"><span class="icon-btn" onclick="deleteEmployee('${emp._id.$oid}')">🗑️</span></td>
      <td class="manageCol hidden"><span class="icon-btn" onclick='editEmployee(${JSON.stringify(emp).replace(/"/g, "&quot;")})'>✏️</span></td>
    `;
    tbody.appendChild(row);
  });

  if (manageMode) {
    document.querySelectorAll(".manageCol").forEach(col => col.classList.remove("hidden"));
  }
}

async function deleteEmployee(id) {
  const res = await fetch(`/api/delete-employee?id=${id}`, { method: 'DELETE' });
  const result = await res.json();
  alert(result.message);
  loadEmployees();
}

function editEmployee(emp) {
  const form = document.getElementById("employeeForm");
  form._id.value = emp._id.$oid;
  form.name.value = emp.name;
  form.email.value = emp.email;
  form.department.value = emp.department;
  form.skills.value = (emp.skills || []).join(", ");
  form.joiningDate.value = emp.joiningDate?.substring(0, 10);
  document.getElementById("submitBtn").innerText = "Update Employee";
  isUpdating = true;
}

document.getElementById("employeeForm").addEventListener("submit", async e => {
  e.preventDefault();
  const form = e.target;

  const employee = {
    name: form.name.value,
    email: form.email.value,
    department: form.department.value,
    skills: form.skills.value.split(",").map(s => s.trim()),
    joiningDate: form.joiningDate.value
  };

  let res, result;
  if (isUpdating) {
    employee._id = form._id.value;
    res = await fetch("/api/update-employee", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(employee)
    });
    result = await res.json();
    isUpdating = false;
    form._id.value = "";
    document.getElementById("submitBtn").innerText = "Add Employee";
  } else {
    res = await fetch("/api/add-employee", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(employee)
    });
    result = await res.json();
  }

  document.getElementById("responseMsg").innerText = result.message;
  form.reset();
  loadEmployees();
});

loadEmployees();






function toggleEmailDelete() {
  const wrapper = document.getElementById("emailDeleteWrapper");
  wrapper.classList.toggle("hidden");

  const input = document.getElementById("emailDeleteInput");

  // Attach listener once
  if (!input.dataset.listenerAdded) {
    input.addEventListener("keydown", async function (event) {
      if (event.key === "Enter") {
        event.preventDefault();

        const email = event.target.value.trim();
        if (!email) {
          alert("Please enter an email.");
          return;
        }

        const res = await fetch(`/api/delete-by-email?email=${encodeURIComponent(email)}`, {
          method: "DELETE"
        });

        const result = await res.json();
        alert(result.message);
        event.target.value = "";
        loadEmployees();
      }
    });
    input.dataset.listenerAdded = "true";
  }

  if (!wrapper.classList.contains("hidden")) {
    input.focus();
  }
}




async function searchEmployees() {
  const name = document.getElementById("searchName").value;
  const department = document.getElementById("searchDepartment").value;
  const skill = document.getElementById("searchSkill").value;
  const from = document.getElementById("searchFrom").value;
  const to = document.getElementById("searchTo").value;

  const params = new URLSearchParams();
  if (name) params.append("name", name);
  if (department) params.append("department", department);
  if (skill) params.append("skill", skill);
  if (from) params.append("from", from);
  if (to) params.append("to", to);

  const res = await fetch(`/api/search-employees?${params.toString()}`);
  const data = await res.json();

  const tbody = document.querySelector("#employeeTable tbody");
  tbody.innerHTML = "";

  data.forEach(emp => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${emp.name}</td>
      <td>${emp.email}</td>
      <td>${emp.department}</td>
      <td>${(emp.skills || []).join(', ')}</td>
      <td>${(emp.joiningDate || '').substring(0, 10)}</td>
      <td class="manageCol hidden"><span class="icon-btn" onclick="deleteEmployee('${emp._id.$oid}')">🗑️</span></td>
      <td class="manageCol hidden"><span class="icon-btn" onclick='editEmployee(${JSON.stringify(emp).replace(/"/g, "&quot;")})'>✏️</span></td>
    `;
    tbody.appendChild(row);
  });

  if (manageMode) {
    document.querySelectorAll(".manageCol").forEach(col => col.classList.remove("hidden"));
  }
}

function resetSearch() {
  document.getElementById("searchName").value = "";
  document.getElementById("searchDepartment").value = "";
  document.getElementById("searchSkill").value = "";
  document.getElementById("searchFrom").value = "";
  document.getElementById("searchTo").value = "";
  loadEmployees(); // reload full list
}


let currentPage = 1;
let totalPages = 1;
let currentSort = "name";
let currentOrder = "asc";

async function fetchPaginatedEmployees(page) {
  currentSort = document.getElementById("sortBy").value;
  const res = await fetch(`/api/employees-paginated?page=${page}&size=5&sort=${currentSort}&order=${currentOrder}`);
  const data = await res.json();

  const tbody = document.querySelector("#employeeTable tbody");
  tbody.innerHTML = "";

  data.data.forEach(emp => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${emp.name}</td>
      <td>${emp.email}</td>
      <td>${emp.department}</td>
      <td>${(emp.skills || []).join(', ')}</td>
      <td>${(emp.joiningDate || '').substring(0, 10)}</td>
      <td class="manageCol hidden"><span class="icon-btn" onclick="deleteEmployee('${emp._id.$oid}')">🗑️</span></td>
      <td class="manageCol hidden"><span class="icon-btn" onclick='editEmployee(${JSON.stringify(emp).replace(/"/g, "&quot;")})'>✏️</span></td>
    `;
    tbody.appendChild(row);
  });

  currentPage = data.currentPage;
  totalPages = data.totalPages;
  document.getElementById("pageInfo").textContent = `Page ${currentPage} of ${totalPages}`;
}

function prevPage() {
  if (currentPage > 1) {
    fetchPaginatedEmployees(currentPage - 1);
  }
}

function nextPage() {
  if (currentPage < totalPages) {
    fetchPaginatedEmployees(currentPage + 1);
  }
}

// Call once on page load
fetchPaginatedEmployees(1);

async function fetchDepartmentStats() {
  const res = await fetch("/api/department-stats");
  const data = await res.json();

  const container = document.getElementById("departmentStats");
  container.innerHTML = "<h3>Department Statistics</h3>";

  const table = document.createElement("table");
  table.classList.add("stats-table");
  table.innerHTML = `
    <tr><th>Department</th><th>Count</th></tr>
    ${data.map(d => `<tr><td>${d.department}</td><td>${d.count}</td></tr>`).join("")}
  `;

  container.appendChild(table);
}

async function loadDashboardStats() {
  try {
    const res = await fetch("/api/dashboard-stats");
    const data = await res.json();

    document.getElementById("totalEmployees").innerText = data.totalEmployees;
    document.getElementById("totalDepartments").innerText = data.totalDepartments;
    document.getElementById("newHires").innerText = data.newHires;
  } catch (err) {
    console.error("Error loading dashboard stats", err);
  }
}


window.addEventListener("DOMContentLoaded", () => {
  loadDashboardStats();
});


function scrollToAnalyticsSection() {
  // Trigger the fetch function
  fetchDepartmentStats();

  // Scroll to analytics section smoothly
  const section = document.querySelector(".analytics-section");
  if (section) {
    section.scrollIntoView({ behavior: "smooth" });
  }
}


function scrollToEmployeeSection() {
  // Toggle employee list
  toggleEmployeeList();

  // Scroll to the employee section
  const section = document.querySelector(".employee-section"); // Make sure this is correct
  if (section) {
    section.scrollIntoView({ behavior: "smooth" });
  }
}

function scrollToDashboard() {
  // Optional: Refresh dashboard stats if needed
  if (typeof loadDashboardStats === "function") {
    loadDashboardStats(); // Make sure this exists
  }

  // Scroll to the stats section
  const section = document.querySelector(".stats-section");
  if (section) {
    section.scrollIntoView({ behavior: "smooth" });
  }
}

