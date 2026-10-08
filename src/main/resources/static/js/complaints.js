/**
 * COMPLAINTS QUEUE SCRIPT
 * Handles filing complaints, status updates, technician resolution notes, and deletion
 */

let complaintsList = [];

async function loadComplaints() {
    const tbody = document.getElementById('complaintTableBody');
    const status = document.getElementById('filterComplaintStatus').value;
    const category = document.getElementById('filterComplaintCategory').value;

    try {
        let url = `/api/complaints?status=${encodeURIComponent(status)}&category=${encodeURIComponent(category)}`;
        complaintsList = await HMS.api(url);

        document.getElementById('complaintCount').textContent = complaintsList.length;

        if (complaintsList.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8">
                        <div class="empty-state">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="8" x2="12" y2="12"></line>
                                <line x1="12" y1="16" x2="12.01" y2="16"></line>
                            </svg>
                            <h4>No complaints found</h4>
                            <p>No issues matching selected filters.</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = complaintsList.map(c => {
            const resolutionText = c.resolution 
                ? `<span style="font-size: 0.8rem; color: #15803d; font-weight: 500;">${c.resolution}</span>`
                : `<span style="font-size: 0.78rem; color: var(--text-muted); font-style: italic;">Awaiting action</span>`;

            return `
                <tr>
                    <td><strong style="font-family: monospace; color: var(--text-muted);">#TK-${c.id}</strong></td>
                    <td>
                        <div style="font-weight: 600; color: var(--text-heading);">${c.studentName || 'Student ID ' + c.studentId}</div>
                        <div style="font-size: 0.76rem; color: var(--text-muted);">${c.roomNo ? 'Room ' + c.roomNo : (c.studentRollNo || '')}</div>
                    </td>
                    <td><span class="badge badge-neutral">${c.category}</span></td>
                    <td>
                        <div style="font-size: 0.84rem; max-width: 240px; color: var(--text-body);" title="${c.description}">
                            ${c.description}
                        </div>
                    </td>
                    <td style="font-size: 0.82rem; color: var(--text-muted);">${HMS.formatDate(c.complaintDate)}</td>
                    <td>${HMS.getComplaintBadge(c.status)}</td>
                    <td>${resolutionText}</td>
                    <td style="text-align: right; white-space: nowrap;">
                        <button class="btn btn-secondary btn-sm" onclick="openResolveModal(${c.id}, '${c.status}', '${(c.resolution || '').replace(/'/g, "\\'")}')" title="Update Status">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="9 11 12 14 22 4"></polyline>
                                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
                            </svg>
                            <span>Update</span>
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="confirmDeleteComplaint(${c.id})" title="Delete Ticket" style="margin-left: 6px;">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="3 6 5 6 21 6"></polyline>
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            </svg>
                        </button>
                    </td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color: var(--status-red); padding: 20px;">Failed to load complaints: ${err.message}</td></tr>`;
    }
}

async function openAddComplaintModal() {
    try {
        const students = await HMS.api('/api/students');
        const select = document.getElementById('complaintStudentId');
        select.innerHTML = '<option value="">-- Choose Student --</option>';

        students.forEach(s => {
            const opt = document.createElement('option');
            opt.value = s.id;
            opt.textContent = `${s.name} (${s.rollNo}) ${s.roomNo ? '- Room ' + s.roomNo : ''}`;
            select.appendChild(opt);
        });

        document.getElementById('complaintDate').value = new Date().toISOString().split('T')[0];
        document.getElementById('complaintDesc').value = '';

        HMS.openModal('complaintModal');
    } catch (err) {
        HMS.toast('Failed to load students list: ' + err.message, 'error');
    }
}

async function handleComplaintSubmit(e) {
    e.preventDefault();

    const studentId = parseInt(document.getElementById('complaintStudentId').value, 10);
    const category = document.getElementById('complaintCategory').value;
    const complaintDate = document.getElementById('complaintDate').value;
    const description = document.getElementById('complaintDesc').value.trim();

    if (!studentId || !description) {
        HMS.toast('Please select a student and describe the issue.', 'warning');
        return;
    }

    const btn = document.getElementById('btnSubmitComplaint');
    const origText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span>Saving...</span>';

    try {
        await HMS.api('/api/complaints', {
            method: 'POST',
            body: JSON.stringify({
                studentId,
                category,
                complaintDate,
                description,
                status: 'PENDING'
            })
        });

        HMS.toast('Complaint filed successfully with PENDING status', 'success');
        HMS.closeModal('complaintModal');
        loadComplaints();
    } catch (err) {
        HMS.toast(err.message || 'Failed to file complaint', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = origText;
    }
}

function openResolveModal(id, currentStatus, currentResolution) {
    document.getElementById('resolveComplaintId').value = id;
    document.getElementById('resolveStatus').value = currentStatus || 'PENDING';
    document.getElementById('resolveRemarks').value = (currentResolution && currentResolution !== 'null') ? currentResolution : '';

    HMS.openModal('resolveModal');
}

async function handleResolveSubmit(e) {
    e.preventDefault();

    const id = document.getElementById('resolveComplaintId').value;
    const status = document.getElementById('resolveStatus').value;
    const resolution = document.getElementById('resolveRemarks').value.trim();

    const btn = document.getElementById('btnSubmitResolve');
    const origText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span>Updating...</span>';

    try {
        await HMS.api(`/api/complaints/${id}`, {
            method: 'PUT',
            body: JSON.stringify({
                status,
                resolution
            })
        });

        HMS.toast('Complaint ticket updated successfully', 'success');
        HMS.closeModal('resolveModal');
        loadComplaints();
    } catch (err) {
        HMS.toast(err.message || 'Failed to update ticket', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = origText;
    }
}

async function confirmDeleteComplaint(id) {
    if (!confirm(`Are you sure you want to delete complaint ticket #TK-${id}?`)) {
        return;
    }

    try {
        await HMS.api(`/api/complaints/${id}`, { method: 'DELETE' });
        HMS.toast(`Complaint ticket #TK-${id} deleted successfully`, 'success');
        loadComplaints();
    } catch (err) {
        HMS.toast(err.message || 'Failed to delete complaint', 'error');
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await HMS.checkAuth(true);
    loadComplaints();

    document.getElementById('complaintForm').addEventListener('submit', handleComplaintSubmit);
    document.getElementById('resolveForm').addEventListener('submit', handleResolveSubmit);
    document.getElementById('filterComplaintStatus').addEventListener('change', loadComplaints);
    document.getElementById('filterComplaintCategory').addEventListener('change', loadComplaints);
});
