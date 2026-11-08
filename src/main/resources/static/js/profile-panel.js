class ProfilePanel {
  // fields:   [{ key, label, type }]   editable inputs
  // readonly: [{ key, label }]         shown but not editable
  constructor(container, resource, fields, readonly) {
    this.container = container;
    this.resource = resource;
    this.fields = fields;
    this.readonly = readonly || [];
    this.profile = null;
    this.render();
    this.load();
  }

  q(sel) { return this.container.querySelector(sel); }

  render() {
    const inputs = this.fields.map(f =>
      `<label>${esc(f.label)}
         <input name="${f.key}" type="${f.type || 'text'}" ${f.type === 'number' ? 'step="any"' : ''}>
       </label>`).join('');
    const info = this.readonly.map(f =>
      `<p><b>${esc(f.label)}:</b> <span data-ro="${f.key}">-</span></p>`).join('');

    this.container.innerHTML = `
      <div class="card">
        <h3>My information</h3>
        <div class="readonly">${info}</div>
        <div class="grid">${inputs}
          <label>New password (leave blank to keep)
            <input name="password" type="password">
          </label>
        </div>
        <div>
          <button class="save">Save changes</button>
          <button class="danger del">Delete my account</button>
        </div>
      </div>`;

    this.q('.save').addEventListener('click', () => this.save());
    this.q('.del').addEventListener('click', () => this.remove());
  }

  async load() {
    const r = await api('/api/' + this.resource + '/me');
    if (!r.ok) return say(errorText(r), false);
    this.profile = r.data;
    this.fields.forEach(f => {
      this.q('[name="' + f.key + '"]').value = r.data[f.key] ?? '';
    });
    this.readonly.forEach(f => {
      const v = r.data[f.key];
      this.q('[data-ro="' + f.key + '"]').textContent = (v === null || v === undefined || v === '') ? '-' : v;
    });
    this.q('[name="password"]').value = '';
  }

  async save() {
    if (!this.profile) return;
    const body = { username: this.profile.username, password: this.q('[name="password"]').value };
    this.fields.forEach(f => {
      const raw = this.q('[name="' + f.key + '"]').value.trim();
      body[f.key] = f.type === 'number' ? (raw === '' ? null : Number(raw)) : raw;
    });
    const r = await api('/api/' + this.resource + '/me', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!r.ok) return say(errorText(r), false);
    say('Your information was updated', true);
    this.load();
  }

  async remove() {
    if (!confirm('Delete your account permanently? This cannot be undone.')) return;
    const r = await api('/api/' + this.resource + '/me', { method: 'DELETE' });
    if (!r.ok) return say(errorText(r), false);
    location.href = '/index.html';
  }
}