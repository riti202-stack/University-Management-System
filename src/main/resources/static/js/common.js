const $ = s => document.querySelector(s);

function esc(v) {
  return String(v ?? '').replace(/[&<>"']/g,
    c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function say(text, ok) {
  const m = $('#msg');
  if (!m) return;
  m.className = ok ? 'ok' : 'err';
  m.textContent = text;
}

async function api(path, opts = {}) {
  const res = await fetch(path, { credentials: 'same-origin', ...opts });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  return { ok: res.ok, status: res.status, data };
}

function errorText(r) {
  if (r.status === 401) return 'You are not logged in (401)';
  if (r.status === 403) return 'Access denied (403): your role cannot do this';
  if (r.data && typeof r.data === 'object') {
    return Object.entries(r.data).map(([k, v]) => k + ': ' + v).join(' | ');
  }
  return 'Error ' + r.status;
}

// Used by each portal page: checks the logged-in user has the right role.
async function requireRole(role) {
  const me = await api('/api/me');
  if (!me.ok) { location.href = '/index.html'; return null; }
  const roles = me.data.roles.map(r => r.replace('ROLE_', ''));
  if (!roles.includes(role)) { location.href = '/index.html?denied=1'; return null; }
  $('#who').textContent = me.data.username + ' (' + role + ')';
  $('#logout').addEventListener('click', logout);
  return me.data;
}

async function logout() {
  await api('/api/auth/logout', { method: 'POST' });
  location.href = '/index.html';
}

// A complete list + add/edit/delete panel for one resource.
class Panel {
  constructor(container, resource, label, hasDept, onChange) {
    this.container = container;
    this.resource = resource;
    this.label = label;
    this.hasDept = hasDept;
    this.onChange = onChange || (() => {});
    this.rows = [];
    this.editingId = null;
    this.render();
    this.load();
  }

  q(sel) { return this.container.querySelector(sel); }
  field(name) { return this.q('[name="' + name + '"]'); }

  render() {
    const dept = this.hasDept ? '<input name="department" placeholder="Department">' : '';
    const deptTh = this.hasDept ? '<th>Department</th>' : '';
    this.container.innerHTML = `
      <div class="card">
        <h3 class="ftitle"></h3>
        <input name="name" placeholder="Full name">
        <input name="email" placeholder="Email">
        <input name="username" placeholder="Username">
        <input name="password" type="password" placeholder="Password">
        ${dept}
        <div>
          <button class="save">Save</button>
          <button class="light clear">Clear</button>
        </div>
      </div>
      <div class="card">
        <h3>${esc(this.label)}s <span class="count"></span></h3>
        <input class="search" placeholder="Search...">
        <table>
          <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Username</th>${deptTh}<th>Actions</th></tr></thead>
          <tbody></tbody>
        </table>
      </div>`;

    this.q('.save').addEventListener('click', () => this.save());
    this.q('.clear').addEventListener('click', () => this.reset());
    this.q('.search').addEventListener('input', () => this.draw());
    this.q('tbody').addEventListener('click', e => {
      const btn = e.target.closest('button');
      if (!btn) return;
      const id = Number(btn.dataset.id);
      if (btn.classList.contains('edit')) this.edit(id);
      if (btn.classList.contains('del')) this.remove(id);
    });
    this.reset();
  }

  async load() {
    const r = await api('/api/' + this.resource);
    if (!r.ok) { say(errorText(r), false); this.rows = []; }
    else { this.rows = r.data; }
    this.draw();
    this.onChange(this.rows.length);
  }

  draw() {
    const term = this.q('.search').value.trim().toLowerCase();
    const list = this.rows.filter(p => !term ||
      [p.name, p.email, p.username, p.department]
        .some(v => String(v ?? '').toLowerCase().includes(term)));
    const cols = this.hasDept ? 6 : 5;

    this.q('tbody').innerHTML = list.length
      ? list.map(p => `
          <tr>
            <td>${esc(p.id)}</td>
            <td>${esc(p.name)}</td>
            <td>${esc(p.email)}</td>
            <td>${esc(p.username)}</td>
            ${this.hasDept ? '<td>' + esc(p.department) + '</td>' : ''}
            <td>
              <button class="small edit" data-id="${esc(p.id)}">Edit</button>
              <button class="small danger del" data-id="${esc(p.id)}">Delete</button>
            </td>
          </tr>`).join('')
      : '<tr><td colspan="' + cols + '" class="empty">No records</td></tr>';

    this.q('.count').textContent = '(' + list.length + ' of ' + this.rows.length + ')';
  }

  reset() {
    this.editingId = null;
    this.q('.ftitle').textContent = 'Add new ' + this.label.toLowerCase();
    ['name', 'email', 'username', 'password', 'department'].forEach(f => {
      const el = this.field(f);
      if (el) el.value = '';
    });
    this.field('password').placeholder = 'Password (min 6 characters)';
  }

  edit(id) {
    const p = this.rows.find(x => x.id === id);
    if (!p) return;
    this.editingId = id;
    this.q('.ftitle').textContent = 'Edit ' + this.label.toLowerCase() + ' #' + id;
    this.field('name').value = p.name || '';
    this.field('email').value = p.email || '';
    this.field('username').value = p.username || '';
    this.field('password').value = '';
    this.field('password').placeholder = 'New password (leave blank to keep)';
    if (this.hasDept) this.field('department').value = p.department || '';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async save() {
    const body = {
      name: this.field('name').value,
      email: this.field('email').value,
      username: this.field('username').value,
      password: this.field('password').value
    };
    if (this.hasDept) body.department = this.field('department').value;

    const url = '/api/' + this.resource + (this.editingId ? '/' + this.editingId : '');
    const r = await api(url, {
      method: this.editingId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!r.ok) return say(errorText(r), false);
    say(this.editingId ? 'Updated successfully' : 'Created successfully', true);
    this.reset();
    this.load();
  }

  async remove(id) {
    if (!confirm('Delete record #' + id + '?')) return;
    const r = await api('/api/' + this.resource + '/' + id, { method: 'DELETE' });
    if (!r.ok) return say(errorText(r), false);
    say('Deleted', true);
    this.load();
  }
}