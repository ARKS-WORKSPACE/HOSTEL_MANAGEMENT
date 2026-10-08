/**
 * HOSTEL MANAGEMENT SYSTEM - CORE JAVASCRIPT UTILITIES
 * Shared API client, authentication checks, notifications and UI helpers
 */

const HMS = {
    // Standard API Fetch Wrapper
    async api(url, options = {}) {
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...options.headers
            }
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMessage = (data && data.message) 
                    ? data.message 
                    : (data && data.error) 
                        ? data.error 
                        : `HTTP error ${response.status}: ${response.statusText}`;
                
                const err = new Error(errorMessage);
                err.status = response.status;
                err.data = data;
                throw err;
            }

            return data;
        } catch (error) {
            console.error(`API Call failed [${url}]:`, error);
            throw error;
        }
    },

    // Session Verification
    async checkAuth(requireAuth = true) {
        try {
            const res = await fetch('/api/auth/current');
            const data = await res.json();

            if (data && data.authenticated && data.admin) {
                // Populate Admin UI elements if present
                const adminNameEls = document.querySelectorAll('.js-admin-name');
                adminNameEls.forEach(el => el.textContent = data.admin.fullName || data.admin.username);

                const adminAvatarEls = document.querySelectorAll('.js-admin-avatar');
                adminAvatarEls.forEach(el => {
                    const initials = (data.admin.fullName || data.admin.username)
                        .split(' ')
                        .map(n => n[0])
                        .join('')
                        .toUpperCase()
                        .slice(0, 2);
                    el.textContent = initials || 'AD';
                });

                return data.admin;
            } else {
                if (requireAuth) {
                    window.location.href = 'index.html';
                }
                return null;
            }
        } catch (e) {
            if (requireAuth) {
                window.location.href = 'index.html';
            }
            return null;
        }
    },

    // Logout
    async logout() {
        try {
            await fetch('/api/auth/logout', { method: 'POST' });
        } catch (ignored) {}
        HMS.toast('Logged out successfully', 'info');
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 300);
    },

    // Toast Notifications
    toast(message, type = 'info') {
        let container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        let iconSvg = '';
        if (type === 'success') {
            iconSvg = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>`;
        } else if (type === 'error') {
            iconSvg = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#dc2626" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>`;
        } else if (type === 'warning') {
            iconSvg = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#d97706" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>`;
        } else {
            iconSvg = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`;
        }

        toast.innerHTML = `
            ${iconSvg}
            <div class="toast-message">${message}</div>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(40px)';
            setTimeout(() => toast.remove(), 250);
        }, 4000);
    },

    // Status Badge Helpers matching specification
    getRoomBadge(status) {
        const s = (status || 'AVAILABLE').toUpperCase();
        if (s === 'AVAILABLE') {
            return `<span class="badge badge-available">Available</span>`;
        } else if (s === 'PARTIALLY OCCUPIED' || s === 'PARTIALLY') {
            return `<span class="badge badge-partially">Partially Occupied</span>`;
        } else if (s === 'FULL') {
            return `<span class="badge badge-full">Full</span>`;
        }
        return `<span class="badge badge-neutral">${s}</span>`;
    },

    getComplaintBadge(status) {
        const s = (status || 'PENDING').toUpperCase();
        if (s === 'PENDING') {
            return `<span class="badge badge-pending">Pending</span>`;
        } else if (s === 'IN_PROGRESS' || s === 'IN PROGRESS') {
            return `<span class="badge badge-in_progress">In Progress</span>`;
        } else if (s === 'RESOLVED') {
            return `<span class="badge badge-resolved">Resolved</span>`;
        }
        return `<span class="badge badge-neutral">${s}</span>`;
    },

    getAllocationBadge(status) {
        const s = (status || 'ACTIVE').toUpperCase();
        if (s === 'ACTIVE') {
            return `<span class="badge badge-active">Active</span>`;
        }
        return `<span class="badge badge-cancelled">Cancelled</span>`;
    },

    // Date formatting helper
    formatDate(dateStr) {
        if (!dateStr) return '-';
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return dateStr;
        return d.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    },

    // Modal helpers
    openModal(modalId) {
        const el = document.getElementById(modalId);
        if (el) el.classList.add('active');
    },

    closeModal(modalId) {
        const el = document.getElementById(modalId);
        if (el) el.classList.remove('active');
    },

    // Initialize Global Layout Listeners
    initLayout() {
        // Toggle Mobile Sidebar
        const menuBtn = document.querySelector('.btn-menu-toggle');
        const sidebar = document.querySelector('.sidebar');
        if (menuBtn && sidebar) {
            menuBtn.addEventListener('click', () => {
                sidebar.classList.toggle('open');
            });
        }

        // Close modal on backdrop click
        document.querySelectorAll('.modal-overlay').forEach(overlay => {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) {
                    overlay.classList.remove('active');
                }
            });
        });
    }
};

document.addEventListener('DOMContentLoaded', () => {
    HMS.initLayout();
});
