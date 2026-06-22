'use strict';

// ===== 상수 =====
const GRADE_CSS = { '레전드리':'legendary','유니크':'unique','에픽':'epic','레어':'rare' };
const GRADE_COLOR = { '레전드리':'#00ccff','유니크':'#ffcc00','에픽':'#bb00ff','레어':'#22cc44' };
const GRADE_BADGE_CLASS = { '레전드리':'grade-badge-legendary','유니크':'grade-badge-unique','에픽':'grade-badge-epic','레어':'grade-badge-rare' };

// 장비 슬롯 그리드 배치 (4열)
const SLOT_GRID = [
  ['반지 1','반지 2','반지 3','반지 4'],
  ['펜던트','펜던트 2','포켓 아이템',null],
  ['모자','얼굴 장식','눈 장식','귀고리'],
  ['어깨장식','상의','하의','신발'],
  ['장갑','망토','벨트','훈장'],
  ['무기','보조무기','엠블렘','배지'],
];

// ===== 탭 =====
function switchTab(tab) {
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  document.querySelectorAll('.tab-content').forEach(c => c.classList.add('hidden'));
  document.querySelector(`[onclick="switchTab('${tab}')"]`).classList.add('active');
  document.getElementById('tab-' + tab).classList.remove('hidden');
}

// ===== 캐릭터 정보 탭 =====
document.getElementById('info-input').addEventListener('keydown', e => {
  if (e.key === 'Enter') searchCharacter();
});

async function searchCharacter() {
  const nickname = document.getElementById('info-input').value.trim();
  if (!nickname) return;

  document.getElementById('char-detail').classList.add('hidden');
  hideError('info');
  showLoading('info', true);

  try {
    const res = await fetch(`/api/character?nickname=${encodeURIComponent(nickname)}`);
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || `오류 (${res.status})`);
    }
    const data = await res.json();
    if (data.hasError) throw new Error(data.error);
    renderDetail(data);
    document.getElementById('char-detail').classList.remove('hidden');
  } catch (e) {
    showError('info', e.message);
  } finally {
    showLoading('info', false);
  }
}

// ── 전체 캐릭터 상세 렌더링 ──
function renderDetail(r) {
  // 프로필
  const img = document.getElementById('prof-img');
  img.src = r.imageUrl || '';
  img.onerror = () => { img.style.display='none'; };
  document.getElementById('prof-name').textContent   = r.nickname || '';
  document.getElementById('prof-class').textContent  = r.characterClass || '';
  document.getElementById('prof-world').textContent  = r.worldName || '';
  document.getElementById('prof-meta').textContent   =
    `Lv.${r.level}` + (r.guildName ? `  ·  <${r.guildName}>` : '');
  document.getElementById('prof-converted').textContent = fmt(r.convertedMainStat);
  document.getElementById('prof-stat-type').textContent = r.mainStatType || '';

  // 핵심 스탯 요약
  renderStatSummary(r);

  // 장비 그리드 + 아이템 리스트
  renderEquipGrid(r.equipment || []);
  renderItemList(r.equipment || []);

  // 어빌리티
  renderAbility(r.abilities || [], r.abilityGrade);

  // 심볼
  renderSymbol(r.symbols || []);
}

function renderStatSummary(r) {
  const stats = [
    { label: '주스탯', value: fmt(r.mainStat), cls: '' },
    { label: '공격/마력', value: fmt(r.attackOrMagic), cls: 'purple' },
    { label: '데미지', value: pct(r.damage), cls: '' },
    { label: '보스데미지', value: pct(r.bossDamage), cls: 'accent' },
    { label: '방어율무시', value: pct(r.defIgnore), cls: '' },
    { label: '크리티컬', value: pct(r.criticalDamage), cls: '' },
    { label: '최종데미지', value: pct(r.finalDamage), cls: '' },
  ];
  const container = document.getElementById('stat-summary');
  container.innerHTML = stats.map(s => `
    <div class="stat-box">
      <div class="stat-box-label">${s.label}</div>
      <div class="stat-box-value ${s.cls}">${s.value}</div>
    </div>
  `).join('');
}

// ── 장비 그리드 ──
function renderEquipGrid(equipment) {
  const grid = document.getElementById('equip-grid');
  const slotMap = {};
  (equipment || []).forEach(item => {
    const s = item.item_equipment_slot;
    if (s) slotMap[s] = item;
    if (s === '한벌옷') { slotMap['상의'] = item; slotMap['하의'] = item; }
  });

  grid.innerHTML = '';
  SLOT_GRID.forEach(row => {
    row.forEach(slotName => {
      const cell = document.createElement('div');
      if (!slotName) { cell.className = 'equip-slot empty'; cell.style.visibility='hidden'; grid.appendChild(cell); return; }

      const item = slotMap[slotName];
      const grade = item?.potential_option_grade;
      const gradeClass = grade ? 'grade-' + (GRADE_CSS[grade] || '') : '';
      cell.className = 'equip-slot' + (item ? ` ${gradeClass}` : ' empty');
      cell.title = item ? (item.item_name || slotName) : slotName;

      if (item?.item_icon) {
        cell.innerHTML = `<img src="${escHtml(item.item_icon)}" alt="${escHtml(item.item_name||'')}" onerror="this.parentElement.innerHTML='<div class=\\'equip-slot-label\\'>${escHtml(slotName)}</div>'" />`;
        const sf = parseInt(item.starforce || '0');
        if (sf > 0) cell.innerHTML += `<div class="equip-slot-star">★${sf}</div>`;
      } else {
        cell.innerHTML = `<div class="equip-slot-label">${escHtml(slotName)}</div>`;
      }
      grid.appendChild(cell);
    });
  });
}

// ── 아이템 리스트 ──
function renderItemList(equipment) {
  const list = document.getElementById('item-list');
  if (!equipment || equipment.length === 0) {
    list.innerHTML = '<div style="color:var(--text3);text-align:center;padding:20px">장비 정보 없음</div>';
    return;
  }

  const slotOrder = SLOT_GRID.flat().filter(Boolean);
  const sorted = [...equipment].sort((a, b) => {
    const ai = slotOrder.indexOf(a.item_equipment_slot);
    const bi = slotOrder.indexOf(b.item_equipment_slot);
    return (ai === -1 ? 99 : ai) - (bi === -1 ? 99 : bi);
  });

  list.innerHTML = sorted.map(item => {
    const sf = parseInt(item.starforce || '0');
    const starStr = sf > 0 ? `<span class="item-star">★${sf}</span>` : '';
    const potGrade = item.potential_option_grade;
    const addGrade = item.additional_potential_option_grade;
    const potCss = GRADE_CSS[potGrade] ? `pot-${GRADE_CSS[potGrade]}` : '';
    const gradeClass = GRADE_BADGE_CLASS[potGrade] || '';
    const addGradeClass = GRADE_BADGE_CLASS[addGrade] || '';

    const potentials = [item.potential_option_1, item.potential_option_2, item.potential_option_3]
      .filter(p => p && p.trim())
      .map(p => `<div class="potential-line">· ${escHtml(p)}</div>`)
      .join('');
    const additionals = [item.additional_potential_option_1, item.additional_potential_option_2, item.additional_potential_option_3]
      .filter(p => p && p.trim())
      .map(p => `<div class="potential-line">· ${escHtml(p)}</div>`)
      .join('');

    const potBlock = potGrade ? `
      <div class="item-potential">
        <div class="potential-grade-badge ${gradeClass}">${escHtml(potGrade)}</div>
        ${potentials}
        ${addGrade ? `<div class="additional-divider"></div>
          <div class="potential-grade-badge ${addGradeClass}">${escHtml(addGrade)} (에디셔널)</div>
          ${additionals}` : ''}
      </div>` : '';

    const opt = item.item_total_option || {};
    const chips = [];
    if (+opt.str  > 0) chips.push(`<span class="item-stat-chip">STR <span>+${opt.str}</span></span>`);
    if (+opt.dex  > 0) chips.push(`<span class="item-stat-chip">DEX <span>+${opt.dex}</span></span>`);
    if (+opt['int'] > 0) chips.push(`<span class="item-stat-chip">INT <span>+${opt['int']}</span></span>`);
    if (+opt.luk  > 0) chips.push(`<span class="item-stat-chip">LUK <span>+${opt.luk}</span></span>`);
    if (+opt.max_hp > 0) chips.push(`<span class="item-stat-chip">HP <span>+${Number(opt.max_hp).toLocaleString('ko-KR')}</span></span>`);
    if (+opt.attack_power > 0) chips.push(`<span class="item-stat-chip combat">공격력 <span>+${opt.attack_power}</span></span>`);
    if (+opt.magic_power  > 0) chips.push(`<span class="item-stat-chip combat">마력 <span>+${opt.magic_power}</span></span>`);
    if (+opt.all_stat     > 0) chips.push(`<span class="item-stat-chip combat">올스탯 <span>+${opt.all_stat}</span></span>`);
    if (+opt.boss_damage  > 0) chips.push(`<span class="item-stat-chip combat">보스 <span>+${opt.boss_damage}%</span></span>`);
    if (+opt.ignore_monster_armor > 0) chips.push(`<span class="item-stat-chip combat">방무 <span>+${opt.ignore_monster_armor}%</span></span>`);
    if (+opt.damage       > 0) chips.push(`<span class="item-stat-chip combat">데미지 <span>+${opt.damage}%</span></span>`);
    const statsHtml = chips.length > 0 ? `<div class="item-stats-row">${chips.join('')}</div>` : '';

    const iconHtml = item.item_icon
      ? `<img class="item-icon" src="${escHtml(item.item_icon)}" alt="" onerror="this.style.display='none'" />`
      : `<div class="item-icon-placeholder">⚔</div>`;

    return `
      <div class="item-entry ${potCss}">
        <div class="item-icon-wrap">${iconHtml}</div>
        <div class="item-body">
          <div class="item-name-row">
            <span class="item-name ${potCss}">${escHtml(item.item_name || '')}</span>
            ${starStr}
            <span class="item-slot">${escHtml(item.item_equipment_slot || item.item_equipment_part || '')}</span>
          </div>
          ${statsHtml}
          ${potBlock}
        </div>
      </div>`;
  }).join('');
}

// ── 어빌리티 ──
function renderAbility(abilities, grade) {
  const block = document.getElementById('ability-block');
  const content = document.getElementById('ability-content');
  if (!abilities || abilities.length === 0) { block.classList.add('hidden'); return; }
  block.classList.remove('hidden');

  const gradeClass = GRADE_BADGE_CLASS[grade] || '';
  content.innerHTML = `
    <div class="ability-grade-badge ${gradeClass}">${escHtml(grade || '')}</div>
    ${abilities.map(a => {
      const ac = GRADE_BADGE_CLASS[a.ability_grade] || '';
      return `<div class="ability-item">
        <span class="potential-grade-badge ${ac}" style="font-size:.65rem;padding:1px 5px">${escHtml(a.ability_grade||'')}</span>
        ${escHtml(a.ability_value || '')}
      </div>`;
    }).join('')}
  `;
}

// ── 심볼 ──
function renderSymbol(symbols) {
  const block   = document.getElementById('symbol-block');
  const content = document.getElementById('symbol-content');
  if (!symbols || symbols.length === 0) { block.classList.add('hidden'); return; }
  block.classList.remove('hidden');

  content.innerHTML = symbols.map(s => {
    const req = s.symbol_require_growth_count;
    const cur = s.symbol_growth_count;
    const pct = req > 0 ? Math.min(100, Math.round(cur / req * 100)) : 100;
    const statStr = [
      s.symbol_str && +s.symbol_str > 0 ? `STR +${s.symbol_str}` : '',
      s.symbol_dex && +s.symbol_dex > 0 ? `DEX +${s.symbol_dex}` : '',
      s.symbol_int && +s.symbol_int > 0 ? `INT +${s.symbol_int}` : '',
      s.symbol_luk && +s.symbol_luk > 0 ? `LUK +${s.symbol_luk}` : '',
      s.symbol_hp  && +s.symbol_hp  > 0 ? `HP +${s.symbol_hp}`   : '',
    ].filter(Boolean).join('  ');

    const imgHtml = s.symbol_icon
      ? `<img class="symbol-img" src="${escHtml(s.symbol_icon)}" alt="${escHtml(s.symbol_name||'')}" onerror="this.outerHTML='<div class=\\'symbol-img-placeholder\\'>🔮</div>'" />`
      : `<div class="symbol-img-placeholder">🔮</div>`;

    return `
      <div class="symbol-item">
        ${imgHtml}
        <div class="symbol-info">
          <div class="symbol-name">${escHtml(s.symbol_name||'')}</div>
          <div class="symbol-level">Lv.${s.symbol_level}  ·  포스 ${escHtml(s.symbol_force||'0')}</div>
          ${statStr ? `<div class="symbol-stat">${escHtml(statStr)}</div>` : ''}
          <div class="symbol-progress">
            <div class="symbol-progress-bar" style="width:${pct}%"></div>
          </div>
        </div>
      </div>`;
  }).join('');
}

// ===== 비교 탭 =====
let rowCounter = 1;

function addRow() {
  const list = document.getElementById('nickname-list');
  if (list.children.length >= 10) { alert('최대 10명까지 가능합니다.'); return; }
  rowCounter++;
  const row = document.createElement('div');
  row.className = 'nickname-row'; row.dataset.id = rowCounter;
  row.innerHTML = `
    <input type="text" class="nickname-input" placeholder="닉네임 입력" maxlength="15"/>
    <button class="btn-remove" onclick="removeRow(${rowCounter})">✕</button>`;
  list.appendChild(row);
  row.querySelector('input').focus();
}

function removeRow(id) {
  const list = document.getElementById('nickname-list');
  if (list.children.length <= 1) return;
  list.querySelector(`[data-id="${id}"]`)?.remove();
}

function getNicknames() {
  return Array.from(document.querySelectorAll('.nickname-input'))
    .map(i => i.value.trim()).filter(Boolean);
}

document.addEventListener('keydown', e => {
  if (e.key === 'Enter' && e.target.classList.contains('nickname-input')) compare();
});

async function compare() {
  const nicknames = getNicknames();
  if (!nicknames.length) { showError('cmp','닉네임을 입력해 주세요.'); return; }

  clearCompare();
  hideError('cmp');
  showLoading('cmp', true);

  try {
    const res = await fetch('/api/compare', {
      method:'POST', headers:{'Content-Type':'application/json'},
      body: JSON.stringify({ nicknames })
    });
    if (!res.ok) { const e = await res.json().catch(()=>{}); throw new Error(e?.error||`오류 ${res.status}`); }
    const results = await res.json();
    renderResults(results);
  } catch(e) {
    showError('cmp', e.message);
  } finally {
    showLoading('cmp', false);
  }
}

function renderResults(results) {
  document.getElementById('results-section').classList.remove('hidden');
  renderCards(results);
  renderTable(results);
  renderContribBars(results);
}

function renderCards(results) {
  const c = document.getElementById('character-cards');
  c.innerHTML = '';
  results.forEach((r, i) => {
    const rank = i + 1;
    const rc = rank <= 3 ? `rank-${rank}` : 'rank-n';
    const el = document.createElement('div');
    el.className = 'char-card' + (r.hasError?' error-card':'') + (rank===1&&!r.hasError?' rank-1-card':'');

    const imgHtml = r.imageUrl && !r.hasError
      ? `<div class="char-img-wrap"><img class="char-img" src="${escHtml(r.imageUrl)}" alt="" onerror="this.parentElement.innerHTML='<div class=\\'char-img-placeholder\\'>🍁</div>'" /></div>`
      : `<div class="char-img-wrap"><div class="char-img-placeholder">🍁</div></div>`;

    el.innerHTML = `
      <div class="rank-badge ${rc}">${rank}</div>
      ${imgHtml}
      <div class="char-name">${escHtml(r.nickname)}</div>
      ${r.hasError
        ? `<div class="char-class" style="color:var(--red)">조회 실패</div><div class="char-level" style="color:var(--text3);font-size:.7rem">${escHtml(r.error||'')}</div>`
        : `<div class="char-class">${escHtml(r.characterClass||'')}</div>
           <div class="char-level">Lv.${r.level} · ${escHtml(r.worldName||'')}</div>
           <div class="char-converted">
             <div class="char-converted-label">환산 주스탯</div>
             <div class="char-converted-value">${fmt(r.convertedMainStat)}</div>
           </div>`}
    `;
    c.appendChild(el);
  });
}

function renderTable(results) {
  const tbody = document.getElementById('compare-tbody');
  tbody.innerHTML = '';
  results.forEach((r, i) => {
    const rank = i + 1;
    const rc = rank <= 3 ? `r${rank}` : '';
    const tr = document.createElement('tr');
    if (r.hasError) tr.className = 'error-row';
    tr.innerHTML = r.hasError ? `
      <td class="rank-col ${rc}">${rank}</td>
      <td class="td-name">${escHtml(r.nickname)}</td>
      <td colspan="13" style="text-align:left;color:var(--red)">조회 실패: ${escHtml(r.error||'')}</td>` : `
      <td class="rank-col ${rc}">${rank}</td>
      <td class="td-name">${escHtml(r.nickname)}</td>
      <td class="td-class">${escHtml(r.characterClass||'')}</td>
      <td class="td-level">${r.level}</td>
      <td class="td-world">${escHtml(r.worldName||'')}</td>
      <td style="color:var(--accent2);font-size:.8rem">${escHtml(r.mainStatType||'')}</td>
      <td style="font-weight:700">${fmt(r.mainStat)}</td>
      <td>${fmt(r.subStatContrib)}</td>
      <td>${fmt(r.atkContrib)}</td>
      <td>${pct(r.damage)}</td>
      <td>${pct(r.bossDamage)}</td>
      <td>${pct(r.defIgnore)}</td>
      <td>${pct(r.criticalDamage)}</td>
      <td>${pct(r.finalDamage)}</td>
      <td class="td-total">${fmt(r.convertedMainStat)}</td>`;
    tbody.appendChild(tr);
  });
}

function renderContribBars(results) {
  const container = document.getElementById('contribution-bars');
  container.innerHTML = '';
  const valid = results.filter(r => !r.hasError);
  if (!valid.length) return;
  const maxVal = Math.max(...valid.map(r => r.convertedMainStat), 1);

  const cats = [
    { key:'mainStatContrib',    label:'주스탯',    cls:'bar-main'  },
    { key:'subStatContrib',     label:'부스탯 환산', cls:'bar-sub'   },
    { key:'atkContrib',         label:'공/마 환산',  cls:'bar-atk'   },
    { key:'damageContrib',      label:'데미지%',    cls:'bar-dmg'   },
    { key:'bossDamageContrib',  label:'보스데미지%', cls:'bar-boss'  },
    { key:'critContrib',        label:'크리티컬%',  cls:'bar-crit'  },
    { key:'finalDamageContrib', label:'최종데미지%', cls:'bar-final' },
  ];

  cats.forEach(cat => {
    if (!valid.some(r => (r[cat.key]||0) > 0)) return;
    const row = document.createElement('div');
    row.className = 'contrib-row';

    const nameEl = document.createElement('div');
    nameEl.className = 'contrib-name';
    nameEl.textContent = cat.label;

    const wrap = document.createElement('div');
    wrap.className = 'contrib-bar-wrap';

    valid.forEach(r => {
      const val = r[cat.key] || 0;
      const w = Math.round(val / maxVal * 100);
      const lbl = document.createElement('div'); lbl.className='contrib-char-label'; lbl.textContent=r.nickname;
      const track = document.createElement('div'); track.className='bar-track';
      const fill  = document.createElement('div'); fill.className=`bar-fill ${cat.cls}`; fill.style.width='0%';
      track.appendChild(fill);
      wrap.appendChild(lbl); wrap.appendChild(track);
      requestAnimationFrame(() => setTimeout(() => { fill.style.width = w + '%'; }, 80));
    });

    const valEl = document.createElement('div');
    valEl.className = 'contrib-value';
    valEl.textContent = fmt(valid[0][cat.key] || 0);

    row.appendChild(nameEl); row.appendChild(wrap); row.appendChild(valEl);
    container.appendChild(row);
  });
}

// ===== 유틸 =====
function showLoading(scope, on) {
  document.getElementById(scope + '-loading').classList.toggle('hidden', !on);
}
function showError(scope, msg) {
  document.getElementById(scope + '-error-msg').textContent = msg;
  document.getElementById(scope + '-error').classList.remove('hidden');
}
function hideError(scope) {
  document.getElementById(scope + '-error').classList.add('hidden');
}
function clearCompare() {
  document.getElementById('results-section').classList.add('hidden');
  document.getElementById('character-cards').innerHTML = '';
  document.getElementById('compare-tbody').innerHTML = '';
  document.getElementById('contribution-bars').innerHTML = '';
}

function fmt(n) {
  if (n == null || isNaN(n)) return '-';
  return Number(n).toLocaleString('ko-KR');
}
function pct(n) {
  if (!n || isNaN(n) || +n === 0) return `<span style="color:var(--text3)">-</span>`;
  return `${(+n).toFixed(1)}%`;
}
function escHtml(s) {
  if (s == null) return '';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
}
