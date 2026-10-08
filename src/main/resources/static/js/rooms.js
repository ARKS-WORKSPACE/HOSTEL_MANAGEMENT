/**
 * ROOM MANAGEMENT SCRIPT
 * Handles Room inventory CRUD actions, block/status filtering, and capacity meters
 */

let allRooms = [];

async function loadRooms() {
    const tbody = document.getElementById('roomTableBody');
    const block = document.getElementById('filterBlock').value;
    const status = document.getElementById('filterStatus').value;

    try {
        let url = `/api/rooms?block=${encodeURIComponent(block)}&status=${encodeURIComponent(status)}`;
        allRooms = await HMS.api(url);

        document.getElementById('roomCount').textContent = allRooms.length;

        if (allRooms.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7">
                        <div class="empty-state">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M3 3h18v18H3zM3 9h18M9 21V9"></path>
                            </svg>
                            <h4>No rooms found</h4>
                            <p>Try clearing filters or click "Add New Room" to create one.</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = allRooms.map(r => {
            const availableBeds = Math.max(0, r.capacity - r.occupiedBeds);
            const percentage = Math.round((r.occupiedBeds / r.capacity) * 100);
            
            let progressClass = 'progress-green';
            if (percentage >= 100) {
                progressClass = 'progress-red';
            } else if (percentage > 0) {
                progressClass = 'progress-amber';
            }

            return `
                <tr>
                    <td>
                        <strong style="font-size: 0.95rem; color: var(--text-heading); font-family: monospace;">Room ${r.roomNo}</strong>
                    </td>
                    <td>
                        <div style="font-weight: 500;">${r.block}</div>
                        <div style="font-size: 0.76rem; color: var(--text-muted);">Floor ${r.floor}</div>
                    </td>
                    <td>
                        <span style="font-weight: 600;">${r.capacity} beds</span>
                    </td>
                    <td>
                        <span style="font-weight: 600; color: ${r.occupiedBeds > 0 ? 'var(--text-heading)' : 'var(--text-muted)'};">${r.occupiedBeds} beds</span>
                    </td>
                    <td>
                        <span style="font-weight: 600; color: ${availableBeds > 0 ? 'var(--status-green)' : 'var(--status-red)'};">${availableBeds} beds</span>
                    </td>
                    <td>
                        <div class="occupancy-bar-container">
                            <div class="progress-track">
                                <div class="progress-fill ${progressClass}" style="width: ${percentage}%;"></div>
                            </div>
                            <span class="occupancy-text">${percentage}%</span>
                            ${HMS.getRoomBadge(r.status)}
                        </div>
                    </td>
                    <td style="text-align: right; white-space: nowrap;">
                        <button class="btn btn-secondary btn-sm" onclick="openEditRoomModal(${r.id})" title="Edit Room">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                            </svg>
                            <span>Edit</span>
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="confirmDeleteRoom(${r.id}, '${r.roomNo}', ${r.occupiedBeds})" title="Delete Room" style="margin-left: 6px;">
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
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--status-red); padding: 20px;">Failed to load rooms: ${err.message}</td></tr>`;
    }
}

function openAddRoomModal() {
    document.getElementById('roomForm').reset();
    document.getElementById('roomId').value = '';
    document.getElementById('modalRoomTitle').textContent = 'Add New Room';
    HMS.openModal('roomModal');
}

async function openEditRoomModal(id) {
    try {
        const room = await HMS.api(`/api/rooms/${id}`);
        document.getElementById('roomId').value = room.id;
        document.getElementById('roomNo').value = room.roomNo;
        document.getElementById('roomBlock').value = room.block;
        document.getElementById('roomFloor').value = room.floor;
        document.getElementById('roomCapacity').value = room.capacity;

        document.getElementById('modalRoomTitle').textContent = 'Edit Room ' + room.roomNo;
        HMS.openModal('roomModal');
    } catch (err) {
        HMS.toast('Failed to load room details: ' + err.message, 'error');
    }
}

async function handleRoomSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('roomId').value;
    const roomData = {
        roomNo: document.getElementById('roomNo').value.trim(),
        block: document.getElementById('roomBlock').value,
        floor: parseInt(document.getElementById('roomFloor').value, 10),
        capacity: parseInt(document.getElementById('roomCapacity').value, 10)
    };

    const btn = document.getElementById('btnSaveRoom');
    const origText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span>Saving...</span>';

    try {
        if (id) {
            await HMS.api(`/api/rooms/${id}`, {
                method: 'PUT',
                body: JSON.stringify(roomData)
            });
            HMS.toast('Room updated successfully', 'success');
        } else {
            await HMS.api('/api/rooms', {
                method: 'POST',
                body: JSON.stringify(roomData)
            });
            HMS.toast('Room created successfully', 'success');
        }

        HMS.closeModal('roomModal');
        loadRooms();
    } catch (err) {
        HMS.toast(err.message || 'Operation failed', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = origText;
    }
}

async function confirmDeleteRoom(id, roomNo, occupiedBeds) {
    if (occupiedBeds > 0) {
        HMS.toast(`Cannot delete room "${roomNo}" because it currently has ${occupiedBeds} occupant(s). Cancel allocations first.`, 'warning');
        return;
    }

    if (!confirm(`Are you sure you want to delete room "${roomNo}"?`)) {
        return;
    }

    try {
        await HMS.api(`/api/rooms/${id}`, { method: 'DELETE' });
        HMS.toast(`Room "${roomNo}" deleted successfully`, 'success');
        loadRooms();
    } catch (err) {
        HMS.toast(err.message || 'Failed to delete room', 'error');
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await HMS.checkAuth(true);
    loadRooms();

    document.getElementById('roomForm').addEventListener('submit', handleRoomSubmit);
    document.getElementById('filterBlock').addEventListener('change', loadRooms);
    document.getElementById('filterStatus').addEventListener('change', loadRooms);
});
