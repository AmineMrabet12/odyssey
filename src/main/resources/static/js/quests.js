/* ===== QUESTS PAGE ===== */
let currentUser = null;
let allQuests = [];
let allCategories = [];
let selectedCategories = new Set();
let pendingMissions = [];
let currentFilter = 'ALL';
let currentDiffFilter = null;

async function init() {
  currentUser = await refreshUser();
  if (!currentUser) return;
  await Promise.all([loadCategories(), loadQuests()]);
}

async function loadCategories() {
  allCategories = await api.get('/categories');
}

async function loadQuests() {
  try {
    allQuests = await api.get(`/quests/user/${currentUser.id}`);
    renderQuests();
  } catch {
    document.getElementById('quests-container').innerHTML =
      `<div class="empty-state"><div class="empty-title">Failed to load quests</div></div>`;
  }
}

function renderQuests() {
  let quests = [...allQuests];
  if (currentFilter !== 'ALL') quests = quests.filter(q => q.status === currentFilter);
  if (currentDiffFilter) quests = quests.filter(q => q.difficulty === currentDiffFilter);

  if (!quests.length) {
    document.getElementById('quests-container').innerHTML = `
      <div class="empty-state" style="grid-column:1/-1">
        <div class="empty-icon">⚔️</div>
        <div class="empty-title">No quests here</div>
        <div class="empty-sub">Start your journey by creating a new quest!</div>
        <button class="btn btn-primary" onclick="openCreateQuest()">+ New Quest</button>
      </div>`;
    return;
  }

  const diffColor = { EASY:'var(--diff-easy)', MEDIUM:'var(--diff-medium)', HARD:'var(--diff-hard)', LEGENDARY:'var(--legendary)' };
  document.getElementById('quests-container').innerHTML = quests.map(q => {
    const pct = q.progress || 0;
    const cats = (q.categories || []).map(c => `<span class="cat-chip">${c.icon||''} ${c.name}</span>`).join('');
    return `
      <div class="card quest-card slide-up" onclick="openQuestDetail(${q.id})" style="border-left:3px solid ${diffColor[q.difficulty]}">
        <div class="quest-card-header">
          <div class="quest-title-text">${q.title}</div>
          <div style="display:flex;flex-direction:column;gap:0.3rem;align-items:flex-end">
            ${diffBadge(q.difficulty)}${statusBadge(q.status)}
          </div>
        </div>
        <div class="quest-desc">${q.description || 'No description.'}</div>
        <div class="progress-wrap">
          <div style="display:flex;justify-content:space-between;font-size:0.75rem;color:var(--text-dim);margin-bottom:0.3rem">
            <span>Progress</span><span>${pct}%</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" style="width:${pct}%;background:${pct===100?'var(--green)':'linear-gradient(90deg,var(--purple),var(--gold))'}"></div>
          </div>
        </div>
        <div class="quest-meta">
          <div class="quest-categories">${cats}</div>
          <div class="quest-xp">⭐ ${q.xpReward} XP</div>
        </div>
        <div style="display:flex;gap:0.4rem;margin-top:0.75rem;flex-wrap:wrap" onclick="event.stopPropagation()">
          ${q.status !== 'COMPLETED' ? `<button class="btn btn-ghost btn-xs" onclick="markComplete(${q.id})">✅ Complete</button>` : ''}
          ${q.status === 'ACTIVE' ? `<button class="btn btn-ghost btn-xs" onclick="changeStatus(${q.id},'PAUSED')">⏸ Pause</button>` : ''}
          ${q.status === 'PAUSED' ? `<button class="btn btn-ghost btn-xs" onclick="changeStatus(${q.id},'ACTIVE')">▶️ Resume</button>` : ''}
          <button class="btn btn-danger btn-xs" onclick="deleteQuest(${q.id})">🗑 Delete</button>
        </div>
      </div>`;
  }).join('');
}

async function openQuestDetail(questId) {
  const quest = allQuests.find(q => q.id === questId);
  if (!quest) return;
  const missions = await api.get(`/missions/quest/${questId}`).catch(() => []);
  const total = missions.length;
  const done = missions.filter(m => m.completed).length;

  document.getElementById('detail-content').innerHTML = `
    <div class="modal-title">${quest.title}</div>
    <div style="display:flex;gap:0.5rem;margin-bottom:1rem;flex-wrap:wrap">
      ${diffBadge(quest.difficulty)} ${statusBadge(quest.status)}
      ${(quest.categories||[]).map(c=>`<span class="cat-chip">${c.icon||''} ${c.name}</span>`).join('')}
    </div>
    <p style="color:var(--text-dim);font-size:0.9rem;margin-bottom:1rem">${quest.description||'No description.'}</p>
    <div class="progress-wrap">
      <div style="display:flex;justify-content:space-between;font-size:0.8rem;color:var(--text-dim);margin-bottom:0.4rem">
        <span>Progress: ${done}/${total} missions</span><span>${quest.progress||0}%</span>
      </div>
      <div class="progress-bar" style="height:8px">
        <div class="progress-fill" style="width:${quest.progress||0}%;background:linear-gradient(90deg,var(--purple),var(--gold))"></div>
      </div>
    </div>
    <div style="display:flex;justify-content:space-between;margin:1rem 0 0.5rem;align-items:center">
      <div class="section-title" style="margin:0">⚡ Missions</div>
      <span class="quest-xp">⭐ ${quest.xpReward} XP reward</span>
    </div>
    <div id="missions-list">
      ${missions.length ? missions.map(m => `
        <div class="mission-item" id="mission-${m.id}">
          <div class="mission-check ${m.completed?'done':''}" onclick="toggleMission(${m.id},${questId})"></div>
          <div class="mission-title ${m.completed?'done':''}">${m.title}</div>
          <button class="btn btn-danger btn-xs" onclick="deleteMission(${m.id},${questId})">✕</button>
        </div>`).join('')
      : '<div style="color:var(--text-dim);font-size:0.875rem;padding:0.5rem 0">No missions yet. Add some below!</div>'}
    </div>
    <div style="display:flex;gap:0.5rem;margin-top:0.75rem">
      <input class="form-input" id="new-mission-input" placeholder="Add a mission..." style="flex:1" onkeypress="if(event.key==='Enter')addMission(${questId})" />
      <button class="btn btn-primary btn-sm" onclick="addMission(${questId})">+</button>
    </div>`;

  document.getElementById('detail-modal').classList.add('open');
}

async function toggleMission(missionId, questId) {
  try {
    const updated = await api.patch(`/missions/${missionId}/toggle`);
    const el = document.getElementById(`mission-${missionId}`);
    if (el) {
      const check = el.querySelector('.mission-check');
      const title = el.querySelector('.mission-title');
      check.classList.toggle('done', updated.completed);
      title.classList.toggle('done', updated.completed);
    }
    if (updated.completed) showToast('Mission completed! +XP ⭐', 'xp');
    await loadQuests();
  } catch (e) { showToast(e.message, 'error'); }
}

async function addMission(questId) {
  const input = document.getElementById('new-mission-input');
  const title = input.value.trim();
  if (!title) return;
  try {
    await api.post(`/missions?questId=${questId}`, { title });
    input.value = '';
    showToast('Mission added!', 'success');
    await openQuestDetail(questId);
    await loadQuests();
  } catch (e) { showToast(e.message, 'error'); }
}

async function deleteMission(missionId, questId) {
  try {
    await api.delete(`/missions/${missionId}`);
    await openQuestDetail(questId);
    await loadQuests();
  } catch (e) { showToast(e.message, 'error'); }
}

async function markComplete(questId) {
  try {
    await api.patch(`/quests/${questId}/status?status=COMPLETED`);
    showToast('Quest completed! XP earned! ⭐', 'xp');
    await loadQuests();
  } catch (e) { showToast(e.message, 'error'); }
}

async function changeStatus(questId, status) {
  try {
    await api.patch(`/quests/${questId}/status?status=${status}`);
    showToast(`Quest ${status.toLowerCase()}!`, 'success');
    await loadQuests();
  } catch (e) { showToast(e.message, 'error'); }
}

async function deleteQuest(questId) {
  if (!confirm('Abandon this quest forever?')) return;
  try {
    await api.delete(`/quests/${questId}`);
    showToast('Quest abandoned.', 'success');
    await loadQuests();
  } catch (e) { showToast(e.message, 'error'); }
}

function filterQuests(status, btn) {
  currentFilter = status;
  currentDiffFilter = null;
  document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderQuests();
}

function filterDiff(diff, btn) {
  currentDiffFilter = currentDiffFilter === diff ? null : diff;
  btn.classList.toggle('active');
  renderQuests();
}

function openCreateQuest() {
  selectedCategories.clear();
  pendingMissions = [];
  document.getElementById('q-title').value = '';
  document.getElementById('q-desc').value = '';
  document.getElementById('q-diff').value = 'MEDIUM';
  document.getElementById('q-xp').value = 250;
  document.getElementById('q-missions-list').innerHTML = '';
  document.getElementById('q-mission-input').value = '';

  document.getElementById('q-categories').innerHTML = allCategories.map(c => `
    <button class="btn btn-ghost btn-xs cat-toggle" data-id="${c.id}" onclick="toggleCategory(this,${c.id})">${c.icon||''} ${c.name}</button>
  `).join('');

  document.getElementById('quest-modal').classList.add('open');
}

function toggleCategory(btn, id) {
  if (selectedCategories.has(id)) { selectedCategories.delete(id); btn.style.background=''; btn.style.borderColor=''; btn.style.color=''; }
  else { selectedCategories.add(id); btn.style.background='rgba(130,80,255,0.2)'; btn.style.borderColor='var(--purple)'; btn.style.color='var(--purple-light)'; }
}

function addMissionToForm() {
  const input = document.getElementById('q-mission-input');
  const title = input.value.trim();
  if (!title) return;
  pendingMissions.push(title);
  input.value = '';
  const list = document.getElementById('q-missions-list');
  const idx = pendingMissions.length - 1;
  const el = document.createElement('div');
  el.className = 'mission-item';
  el.innerHTML = `<div class="mission-check"></div><div class="mission-title">${title}</div>
    <button class="btn btn-danger btn-xs" onclick="this.parentElement.remove();pendingMissions.splice(${idx},1)">✕</button>`;
  list.appendChild(el);
}

async function saveQuest() {
  const title = document.getElementById('q-title').value.trim();
  if (!title) return showToast('Enter a quest title', 'error');
  const difficulty = document.getElementById('q-diff').value;
  const xpReward = parseInt(document.getElementById('q-xp').value) || 250;
  const description = document.getElementById('q-desc').value;
  try {
    const quest = await api.post(`/quests?userId=${currentUser.id}`, { title, description, difficulty, xpReward });
    // Add categories
    for (const catId of selectedCategories) {
      await api.post(`/quests/${quest.id}/categories/${catId}`, null).catch(() => {});
    }
    // Add missions
    for (let i = 0; i < pendingMissions.length; i++) {
      await api.post(`/missions?questId=${quest.id}`, { title: pendingMissions[i], orderIndex: i + 1 }).catch(() => {});
    }
    closeModal('quest-modal');
    showToast(`Quest "${title}" forged! ⚔️`, 'success');
    await loadQuests();
  } catch (e) { showToast(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', init);
