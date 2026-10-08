/**
 * STUDENT MANAGEMENT SCRIPT
 * Handles Student CRUD actions, real-time search, and validation
 */

let allStudents = [];

async function loadStudents(search = '') {
    const tbody = document.getElementById('studentTableBody');
    try {
        const url = search ? `/api/students?search=${encodeURIComponent(search)}` : '/api/students';
        allStudents = await HMS.api(url);

        document.getElementById('studentCount').textContent = allStudents.length;

        if (allStudents.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7">
                        <div class="empty-state">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="8" y1="12" x2="16" y2="12"></line>
                            </svg>
                            <h4>No students found</h4>
                            <p>${search ? 'Try adjusting your search criteria.' : 'Click "Add New Student" to register a resident.'}</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = allStudents.map(s => {
            const initials = s.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
            const allocationBadge = s.roomNo 
                ? `<span class="badge badge-active">Room ${s.roomNo} ${s.bedNo ? '(' + s.bedNo + ')' : ''}</span>`
                : `<span class="badge badge-neutral">Not Allocated</span>`;

            return `
                <tr>
                    <td>
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div class="admin-avatar" style="background: linear-gradient(135deg, #4f46e5, #06b6d4); font-size: 0.8rem; width: 34px; height: 34px;">
                                ${initials}
                            </div>
                            <div>
                                <div style="font-weight: 600; color: var(--text-heading);">${s.name}</div>
                                <div style="font-size: 0.76rem; color: var(--text-muted);">${s.email}</div>
                            </div>
                        </div>
                    </td>
                    <td><strong style="color: var(--primary); font-family: monospace; font-size: 0.85rem;">${s.rollNo}</strong></td>
                    <td>
                        <div style="font-weight: 500;">${s.course} - ${s.branch}</div>
                        <div style="font-size: 0.76rem; color: var(--text-muted);">Year ${s.year}</div>
                    </td>
                    <td>
                        <div>${s.phone}</div>
                    </td>
                    <td>
                        <div style="font-weight: 500;">${s.guardianName}</div>
                        <div style="font-size: 0.76rem; color: var(--text-muted);">${s.guardianPhone}</div>
                    </td>
                    <td>${allocationBadge}</td>
                    <td style="text-align: right; white-space: nowrap;">
                        <button class="btn btn-secondary btn-sm" onclick="openEditStudentModal(${s.id})" title="Edit Student">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                            </svg>
                            <span>Edit</span>
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="confirmDeleteStudent(${s.id}, '${s.name.replace(/'/g, "\\'")}')" title="Delete Student" style="margin-left: 6px;">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="3 6 5 6 21 6"></polyline>
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            </svg>
                            <span>Delete</span>
                        </button>
                    </td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--status-red); padding: 20px;">Failed to load students: ${err.message}</td></tr>`;
    }
}

function openAddStudentModal() {
    document.getElementById('studentForm').reset();
    document.getElementById('studentId').value = '';
    document.getElementById('modalStudentTitle').textContent = 'Add New Student';
    HMS.openModal('studentModal');
}

async function openEditStudentModal(id) {
    try {
        const student = await HMS.api(`/api/students/${id}`);
        document.getElementById('studentId').value = student.id;
        document.getElementById('studentName').value = student.name;
        document.getElementById('studentRollNo').value = student.rollNo;
        document.getElementById('studentEmail').value = student.email;
        document.getElementById('studentPhone').value = student.phone;
        document.getElementById('studentCourse').value = student.course;
        document.getElementById('studentBranch').value = student.branch;
        document.getElementById('studentYear').value = student.year;
        document.getElementById('guardianName').value = student.guardianName;
        document.getElementById('guardianPhone').value = student.guardianPhone;
        document.getElementById('studentAddress').value = student.address || '';

        document.getElementById('modalStudentTitle').textContent = 'Edit Student Details';
        HMS.openModal('studentModal');
    } catch (err) {
        HMS.toast('Failed to load student details: ' + err.message, 'error');
    }
}

async function handleStudentSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('studentId').value;
    const studentData = {
        name: document.getElementById('studentName').value.trim(),
        rollNo: document.getElementById('studentRollNo').value.trim(),
        email: document.getElementById('studentEmail').value.trim(),
        phone: document.getElementById('studentPhone').value.trim(),
        course: document.getElementById('studentCourse').value,
        branch: document.getElementById('studentBranch').value.trim(),
        year: parseInt(document.getElementById('studentYear').value, 10),
        guardianName: document.getElementById('guardianName').value.trim(),
        guardianPhone: document.getElementById('guardianPhone').value.trim(),
        address: document.getElementById('studentAddress').value.trim()
    };

    const btn = document.getElementById('btnSaveStudent');
    const origText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span>Saving...</span>';

    try {
        if (id) {
            await HMS.api(`/api/students/${id}`, {
                method: 'PUT',
                body: JSON.stringify(studentData)
            });
            HMS.toast('Student record updated successfully', 'success');
        } else {
            await HMS.api('/api/students', {
                method: 'POST',
                body: JSON.stringify(studentData)
            });
            HMS.toast('Student registered successfully', 'success');
        }

        HMS.closeModal('studentModal');
        loadStudents();
    } catch (err) {
        HMS.toast(err.message || 'Operation failed', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = origText;
    }
}

async function confirmDeleteStudent(id, studentName) {
    if (!confirm(`Are you sure you want to delete student "${studentName}"? This action cannot be undone.`)) {
        return;
    }

    try {
        await HMS.api(`/api/students/${id}`, { method: 'DELETE' });
        HMS.toast(`Student "${studentName}" deleted successfully`, 'success');
        loadStudents();
    } catch (err) {
        HMS.toast(err.message || 'Failed to delete student', 'error');
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await HMS.checkAuth(true);
    loadStudents();

    // Attach form submit
    document.getElementById('studentForm').addEventListener('submit', handleStudentSubmit);

    // Live search listener with debounce
    let searchTimeout;
    const searchInput = document.getElementById('studentSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                loadStudents(e.target.value);
            }, 250);
        });
    }
});
