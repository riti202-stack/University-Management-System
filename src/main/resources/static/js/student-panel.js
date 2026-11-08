class StudentPanel {
  constructor(container, onChange) {
    this.container = container;
    this.onChange = onChange || (() => {});
    this.rows = [];
    this.editingId = null;
    this.render();
    this.load();
  }

  q(sel) { return this.container.querySelector(sel); }
  f(name) { return this.q('[name="' + name + '"]'); }

  render() {
    this.container.innerHTML = `
      <div class="card">
        <h3 class="ftitle"></h3>
        <input name="name" placeholder="Full name">
        <input name="roll" placeholder="Roll">
        <input name="age" type="number" min="1" max="120" placeholder="Age">
        <input name="batch" placeholder="Batch (e.g. 2022)">
        <input name="className" placeholder="Class (e.g. CSE-A)">
        <input name="gpa" type="number" step="0.01" min="0" max="4" placeholder="GPA (0 - 4)">
        <input name="department" placeholder="Department">
        <input name="email" placeholder="Email">
        <input name="username" placeholder="Username">
        <input name="password" type="password" placeholder="Password">
        <div>
          <button class="save">Save</button>
          <button class="light clear">Clear</button>
        </div>
      </div>
      <div class="card">
        <h3>Students <span class="count"></span></h3>
        <select class="fbatch"></select>
        <select class="fclass"></select>
        <input class="search" placeholder="Search name, roll, email...">
        <table>
          <thead><tr>
            <th>Roll</th><th>Name</th><th>Age</th><th>Batch</th><th>Class</th><th>GPA</th>
            <th>Department</th><th>Email</th><th>Username</th><th>Actions</th>
          </tr></thead>
          <tbody></tbody>
        </table>
      </div>`;

    this.q('.save').addEventListener('click', () => this.save());
    this.q('.clear').addEventListener('click', () => this.reset());
    this.q('.search').addEventListener('input', () => this.draw());
    this.q('.fbatch').addEventListener('change', () => this.draw());
    this.q('.fclass').addEventListener('change', () => this.draw());
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
    const r = await api('/api/students');
    if (!r.ok) { say(errorText(r), false); this.rows = []; }
    else { this.rows = r.data; }
    this.fillFilters();
    this.draw();
    this.onChange(this.rows.length);
  }

  fillFilters() {
    const fill = (sel, key, label) => {
      const el = this.q(sel);
      const keep = el.value;
      const vals = [...new Set(this.rows.map(r => r[key]).filter(Boolean))].sort();
      el.innerHTML = '<option value="">' + label + '</option>' +
        vals.map(v => '<option>' + esc(v) + '</option>').join('');
      el.value = vals.includes(keep) ? keep : '';
    };
    fill('.fbatch', 'batch', 'All batches');
    fill('.fclass', 'className', 'All classes');
  }

  draw() {
    const batch = this.q('.fbatch').value;
    const cls = this.q('.fclass').value;
    const term = this.q('.search').value.trim().toLowerCase();

    const list = this.rows
      .filter(p => (!batch || p.batch === batch) && (!cls || p.className === cls))
      .filter(p => !term || [p.name, p.roll, p.email, p.username, p.department]
        .some(v => String(v ?? '').toLowerCase().includes(term)))
      .sort((a, z) => String(a.roll ?? '').localeCompare(String(z.roll ?? ''), undefined, { numeric: true }));

    const gpas = list.map(p => p.gpa).filter(g => g !== null && g !== undefined);
    const avg = gpas.length ? (gpas.reduce((s, g) => s + g, 0) / gpas.length).toFixed(2) : '-';
    this.q('.count').textContent = '(' + list.length + ' shown, average GPA ' + avg + ')';

    this.q('tbody').innerHTML = list.length
      ? list.map(p => `
          <tr>
            <td>${esc(p.roll)}</td>
            <td>${esc(p.name)}</td>
            <td>${esc(p.age)}</td>
            <td>${esc(p.batch)}</td>
            <td>${esc(p.className)}</td>
            <td>${p.gpa !== null && p.gpa !== undefined ? Number(p.gpa).toFixed(2) : ''}</td>
            <td>${esc(p.department)}</td>
            <td>${esc(p.email)}</td>
            <td>${esc(p.username)}</td>
            <td>
              <button class="small edit" data-id="${esc(p.id)}">Edit</button>
              <button class="small danger del" data-id="${esc(p.id)}">Delete</button>
            </td>
          </tr>`).join('')
      : '<tr><td colspan="10" class="empty">No students</td></tr>';
  }

  reset() {
    this.editingId = null;
    this.q('.ftitle').textContent = 'Add new student';
    ['name', 'roll', 'age', 'batch', 'className', 'gpa', 'department', 'email', 'username', 'password']
      .forEach(n => this.f(n).value = '');
    this.f('password').placeholder = 'Password (min 6 characters)';
  }

  edit(id) {
    const p = this.rows.find(x => x.id === id);
    if (!p) return;
    this.editingId = id;
    this.q('.ftitle').textContent = 'Edit student #' + id;
    this.f('name').value = p.name || '';
    this.f('roll').value = p.roll || '';
    this.f('age').value = p.age ?? '';
    this.f('batch').value = p.batch || '';
    this.f('className').value = p.className || '';
    this.f('gpa').value = p.gpa ?? '';
    this.f('department').value = p.department || '';
    this.f('email').value = p.email || '';
    this.f('username').value = p.username || '';
    this.f('password').value = '';
    this.f('password').placeholder = 'New password (leave blank to keep)';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async save() {
    const num = el => { const t = el.value.trim(); return t === '' ? null : Number(t); };
    const body = {
      name: this.f('name').value,
      roll: this.f('roll').value,
      age: num(this.f('age')),
      batch: this.f('batch').value,
      className: this.f('className').value,
      gpa: num(this.f('gpa')),
      department: this.f('department').value,
      email: this.f('email').value,
      username: this.f('username').value,
      password: this.f('password').value
    };

    const url = '/api/students' + (this.editingId ? '/' + this.editingId : '');
    const r = await api(url, {
      method: this.editingId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!r.ok) return say(errorText(r), false);
    say(this.editingId ? 'Student updated' : 'Student created', true);
    this.reset();
    this.load();
  }

  async remove(id) {
    if (!confirm('Delete student #' + id + '?')) return;
    const r = await api('/api/students/' + id, { method: 'DELETE' });
    if (!r.ok) return say(errorText(r), false);
    say('Student deleted', true);
    this.load();
  }
}