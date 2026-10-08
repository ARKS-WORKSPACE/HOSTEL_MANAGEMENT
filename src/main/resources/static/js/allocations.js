/**
 * ROOM ALLOCATION SCRIPT
 * Handles room assignment, bed conflict checks, and allocation cancellation
 */

let allocationsList = [];

async function loadAllocations() {
    const tbody = document.getElementById('allocationTableBody');
    try {
        allocationsList = await HMS.api('/api/allocations');
        document.getElementById('allocCount').textContent = allocationsList.length;

        if (allocationsList.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7">
                        <div class="empty-state">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"></path>
                                <rect x="8" y="2" width="8" height="4" rx="1" ry="1"></rect>
                            </svg>
                            <h4>No room allocations found</h4>
                            <p>Click "Allocate Room / Bed" to assign a resident to an available bed.</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = allocationsList.map(a => {
            const isActive = (a.status || 'ACTIVE').toUpperCase() === 'ACTIVE';
            const actionButton = isActive
                ? `<button class="btn btn-danger btn-sm" onclick="cancelAllocation(${a.id}, '${(a.studentName || '').replace(/'/g, "\\'")}', '${a.roomNo || a.roomId}')" title="Cancel Allocation">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="15" y1="9" x2="9" y2="15"></line>
                            <line x1="9" y1="9" x2="15" y2="15"></line>
                        </svg>
                        <span>Cancel</span>
                   </button>`
                : `<span style="font-size: 0.78rem; color: var(--text-muted);">Cancelled</span>`;

            return `
                <tr>
                    <td><strong style="font-family: monospace; color: var(--text-muted);">#AL-${a.id}</strong></td>
                    <td>
                        <div style="font-weight: 600; color: var(--text-heading);">${a.studentName || 'Student ID: ' + a.studentId}</div>
                        <div style="font-size: 0.76rem; color: var(--text-muted);">${a.studentRollNo || ''}</div>
                    </td>
                    <td>
                        <strong style="color: var(--primary);">Room ${a.roomNo || a.roomId}</strong>
                        <div style="font-size: 0.76rem; color: var(--text-muted);">${a.roomBlock || ''}</div>
                    </td>
                    <td>
                        <span class="badge badge-neutral" style="font-family: monospace; font-size: 0.8rem;">${a.bedNo}</span>
                    </td>
                    <td style="font-size: 0.82rem; color: var(--text-muted);">${HMS.formatDate(a.allocationDate)}</td>
                    <td>${HMS.getAllocationBadge(a.status)}</td>
                    <td style="text-align: right;">${actionButton}</td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--status-red); padding: 20px;">Failed to load allocations: ${err.message}</td></tr>`;
    }
}

async function openAllocateModal() {
    try {
        // Fetch Students and Rooms in parallel
        const [students, rooms] = await Promise.all([
            HMS.api('/api/students'),
            HMS.api('/api/rooms')
        ]);

        // Populate Students dropdown
        const studentSelect = document.getElementById('allocStudentId');
        studentSelect.innerHTML = '<option value="">-- Choose Student --</option>';
        students.forEach(s => {
            const isAllocated = !!s.roomNo;
            const text = `${s.name} (${s.rollNo}) - ${s.branch} ${isAllocated ? ' [Has Room ' + s.roomNo + ']' : ''}`;
            const opt = document.createElement('option');
            opt.value = s.id;
            opt.textContent = text;
            if (isAllocated) {
                opt.style.color = '#94a3b8';
            }
            studentSelect.appendChild(opt);
        });

        // Populate Rooms dropdown
        const roomSelect = document.getElementById('allocRoomId');
        roomSelect.innerHTML = '<option value="">-- Choose Available Room --</option>';
        rooms.forEach(r => {
            const avail = Math.max(0, r.capacity - r.occupiedBeds);
            const isFull = (r.status || '').toUpperCase() === 'FULL' || avail <= 0;
            const text = `Room ${r.roomNo} (${r.block}) — ${avail}/${r.capacity} beds available ${isFull ? '[FULL]' : ''}`;
            const opt = document.createElement('option');
            opt.value = r.id;
            opt.textContent = text;
            if (isFull) {
                opt.disabled = true;
                opt.style.color = '#ef4444';
            }
            roomSelect.appendChild(opt);
        });

        // Set default date to today
        document.getElementById('allocDate').value = new Date().toISOString().split('T')[0];

        HMS.openModal('allocationModal');
    } catch (err) {
        HMS.toast('Failed to prepare allocation form: ' + err.message, 'error');
    }
}

async function handleAllocationSubmit(e) {
    e.preventDefault();

    const studentId = parseInt(document.getElementById('allocStudentId').value, 10);
    const roomId = parseInt(document.getElementById('allocRoomId').value, 10);
    const bedNo = document.getElementById('allocBedNo').value;
    const allocationDate = document.getElementById('allocDate').value;

    if (!studentId || !roomId || !bedNo || !allocationDate) {
        HMS.toast('Please fill all required allocation fields', 'warning');
        return;
    }

    const btn = document.getElementById('btnSubmitAlloc');
    const origText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span>Processing...</span>';

    try {
        await HMS.api('/api/allocations', {
            method: 'POST',
            body: JSON.stringify({
                studentId,
                roomId,
                bedNo,
                allocationDate
            })
        });

        HMS.toast('Student allocated to room successfully!', 'success');
        HMS.closeModal('allocationModal');
        loadAllocations();
    } catch (err) {
        HMS.toast(err.message || 'Allocation failed', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = origText;
    }
}

async function cancelAllocation(id, studentName, roomNo) {
    if (!confirm(`Are you sure you want to cancel the allocation for "${studentName}" in Room ${roomNo}?\nThis will decrement room occupancy and free up the bed.`)) {
        return;
    }

    try {
        await HMS.api(`/api/allocations/${id}`, { method: 'DELETE' });
        HMS.toast(`Allocation for "${studentName}" cancelled. Room ${roomNo} bed freed.`, 'success');
        loadAllocations();
    } catch (err) {
        HMS.toast(err.message || 'Failed to cancel allocation', 'error');
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await HMS.checkAuth(true);
    loadAllocations();

    document.getElementById('allocationForm').addEventListener('submit', handleAllocationSubmit);
});
