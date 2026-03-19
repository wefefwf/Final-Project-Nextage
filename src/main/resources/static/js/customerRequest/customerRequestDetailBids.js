/* 전역 변수 관리 */
let currentRequestId = null;
let currentBidData = [];
let selectedBidIds = new Set();
let currentSortType = 'priceDesc';
let currentSelectedBid = null;
let currentSelectStep = 1;
const TOTAL_SELECT_STEPS = 2;

let isCustomerAdmin = false;
let isLoggedIn = false;
let isBusinessUser = false;
let currentBusinessId = null;
let currentCustomerId = 0;
let requestOwnerCustomerId = 0;

let currentHopePrice = 0;
let currentMinAllowedPrice = 0;
let currentMaxAllowedPrice = 0;
let currentRequestStatus = '';

(function initToastRoot() {
	if (document.getElementById('bid-toast-root')) return;
	const root = document.createElement('div');
	root.id = 'bid-toast-root';
	root.style.cssText = 'position:fixed;top:10%;left:50%;transform:translateX(-50%);...';
	document.body.appendChild(root);
})();

function execSelectDaumPostcode() {
	new daum.Postcode({
		oncomplete: function(data) {
			let addr = '';
			let extraAddr = '';

			if (data.userSelectedType === 'R') {
				addr = data.roadAddress;
			} else {
				addr = data.jibunAddress;
			}

			if (data.userSelectedType === 'R') {
				if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
					extraAddr += data.bname;
				}
				if (data.buildingName !== '' && data.apartment === 'Y') {
					extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
				}
				if (extraAddr !== '') {
					extraAddr = ' (' + extraAddr + ')';
				}
				addr += extraAddr;
			}

			document.getElementById('ci-postcode-input').value = data.zonecode;
			document.getElementById('ci-address-input').value = addr;
			document.getElementById('ci-address-detail-input').focus();
		}
	}).open();
}

function parseCustomerAddress(rawAddress) {
	if (!rawAddress) {
		return {
			postcode: '',
			address: '',
			addressDetail: ''
		};
	}

	const parts = String(rawAddress).split('#');
	return {
		postcode: parts[0] || '',
		address: parts[1] || '',
		addressDetail: parts[2] || ''
	};
}

function formatCustomerAddress(rawAddress) {
	const parsed = parseCustomerAddress(rawAddress);
	const chunks = [parsed.address, parsed.addressDetail].filter(Boolean);
	return chunks.length > 0 ? chunks.join(' ') : '등록된 배송지가 없습니다.';
}

function adminHideBid(bidId) {
    const actionsEl = document.getElementById('bid-actions-' + bidId);
    if (!actionsEl) return;
    if (actionsEl.querySelector('.admin-hide-confirm-zone')) return;

    const adminBtn = actionsEl.querySelector('.btn-admin-delete-bid');
    if (adminBtn) adminBtn.style.display = 'none';

    const zone = document.createElement('div');
    zone.className = 'reject-confirm-zone admin-hide-confirm-zone';
    zone.innerHTML = `
        <span class="reject-confirm-msg">숨김 처리할까요?</span>
        <button class="btn-reject-cancel" onclick="cancelAdminHide(${bidId})">취소</button>
        <button class="btn-reject-confirm" onclick="confirmAdminHide(${bidId})">숨김</button>
    `;
    actionsEl.appendChild(zone);
}

function cancelAdminHide(bidId) {
    const actionsEl = document.getElementById('bid-actions-' + bidId);
    if (!actionsEl) return;
    actionsEl.querySelector('.admin-hide-confirm-zone')?.remove();
    const adminBtn = actionsEl.querySelector('.btn-admin-delete-bid');
    if (adminBtn) adminBtn.style.display = '';
}

function confirmAdminHide(bidId) {
    updateBidStatus(bidId, 'HIDDEN')
        .then(() => {
            showToast('success', '제안을 숨김 처리했습니다.');
            refreshBidList(currentRequestId);
        })
        .catch(err => {
            showToast('error', err.message);
        });
}

function toggleAddressEdit() {
	const zone = document.getElementById('ci-address-edit-zone');
	zone.style.display = zone.style.display === 'none' ? 'block' : 'none';
}

function cancelAddressEdit() {
	document.getElementById('ci-address-edit-zone').style.display = 'none';
}

function saveAddressEdit() {
	const postcode = document.getElementById('ci-postcode-input')?.value.trim() || '';
	const address = document.getElementById('ci-address-input')?.value.trim() || '';
	const addressDetail = document.getElementById('ci-address-detail-input')?.value.trim() || '';

	if (!address) {
		showToast('warn', '기본 주소를 입력해주세요.');
		return;
	}

	fetch('/api/customer/me/address', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			postcode,
			address,
			addressDetail
		})
	})
	.then(async res => {
		const data = await res.json();
		if (!res.ok || data.success === false) {
			throw new Error(data.message || '배송지 저장에 실패했습니다.');
		}
		return data;
	})
	.then(data => {
		const rawAddress = data.address;
		const displayAddress = formatCustomerAddress(rawAddress);

		document.getElementById('ci-address-text').textContent = displayAddress;
		document.querySelector('.btn-address-edit').textContent = '변경';
		document.getElementById('ci-address-edit-zone').style.display = 'none';

		if (cachedSelectInfo && cachedSelectInfo.customer) {
			cachedSelectInfo.customer.address = rawAddress;
		}

		showToast('success', '배송지가 저장되었습니다.');
	})
	.catch(err => {
		showToast('error', err.message);
	});
}

function showToast(type, msg, duration) {
	duration = duration || 3400;
	const root = document.getElementById('bid-toast-root');
	if (!root) return;
	if ([...root.querySelectorAll('.bid-toast-msg')].some(el => el.textContent === msg)) return;
	const icons = { success: '✔', error: '✕', warn: '!' };
	const classes = { success: 'bid-toast-success', error: 'bid-toast-error', warn: 'bid-toast-warn' };
	const el = document.createElement('div');
	el.className = 'bid-toast ' + (classes[type] || 'bid-toast-success');
	el.style.pointerEvents = 'auto';
	el.innerHTML = '<span class="bid-toast-icon">' + icons[type] + '</span><span class="bid-toast-msg">' + msg + '</span><button class="bid-toast-close" onclick="dismissToast(this.closest(\'.bid-toast\'))">✕</button>';
	root.appendChild(el);
	el._toastTimer = setTimeout(function() { dismissToast(el); }, duration);
}

function dismissToast(el) {
	if (!el || el._dismissed) return;
	el._dismissed = true;
	clearTimeout(el._toastTimer);
	el.classList.add('bid-toast-hiding');
	el.addEventListener('animationend', function() { el.remove(); }, { once: true });
}

/* ── 선정 모달 캐시 ── */
let cachedSelectInfo = null;

function moveBidModalsToBody() {
	const compareModal = document.getElementById('compare-modal');
	const selectBidModal = document.getElementById('select-bid-modal');

	if (compareModal && compareModal.parentElement !== document.body) {
		document.body.appendChild(compareModal);
	}

	if (selectBidModal && selectBidModal.parentElement !== document.body) {
		document.body.appendChild(selectBidModal);
	}
}

function initBidSection({
	requestId,
	hopePrice,
	requestStatus,
	requestCustomerId,
	isLoggedIn: login,
	isBusinessUser: businessUser,
	currentBusinessId: businessId,
	currentCustomerId: customerId,
	isCustomerAdmin: admin
}) {
	currentRequestId = requestId;
	currentHopePrice = Number(hopePrice || 0);
	currentRequestStatus = requestStatus || '';
	currentMinAllowedPrice = currentHopePrice > 0
		? Math.ceil(currentHopePrice * 0.95)
		: 0;
	requestOwnerCustomerId = Number(requestCustomerId || 0);
		
	isLoggedIn = login;
	isBusinessUser = businessUser;
	currentBusinessId = businessId;
	currentCustomerId = Number(customerId || 0);
	isCustomerAdmin = admin;

	moveBidModalsToBody();

	toggleBidCreateSection();
	applyBidCreateFormConstraints();
	renderBidPricePresets();

	if (requestId) {
		refreshBidList(requestId);
	}
}

function toggleBidCreateSection() {
	const section = document.getElementById('bid-create-section');
	if (!section) return;

	if (!isLoggedIn) {
		section.classList.add('d-none');
		return;
	}

	if (!isBusinessUser) {
		section.classList.add('d-none');
		return;
	}

	if (currentRequestStatus !== 'OPEN') {
		setBidCreateDisabled('현재 이 의뢰글은 제안 가능한 상태가 아닙니다.');
		return;
	}

	section.classList.remove('d-none');
}

function applyBidCreateFormConstraints() {
	const priceInput = document.getElementById('bid-create-price');
	const dueDateInput = document.getElementById('bid-create-due-date');
	const guide = document.getElementById('bid-price-guide');

	if (priceInput && currentMinAllowedPrice > 0) {
		priceInput.min = currentMinAllowedPrice;
	}

	if (dueDateInput) {
		const today = new Date().toISOString().split('T')[0];
		dueDateInput.min = today;
	}

	if (guide && currentHopePrice > 0 && currentMinAllowedPrice > 0) {
		guide.classList.remove('d-none');
		guide.textContent =
			`희망가격은 ₩ ${formatPrice(currentHopePrice)}이며, 제안 가능 최소 금액은 ₩ ${formatPrice(currentMinAllowedPrice)} 입니다.`;
	}
}

function setBidCreateDisabled(message) {
	const section = document.getElementById('bid-create-section');
	if (!section) return;

	section.innerHTML = `
		<div class="bid-create-card">
			<div class="bid-create-header">
				<h3 class="bid-create-title">제안 상태</h3>
				<p class="bid-create-subtitle">${message}</p>
			</div>
		</div>
	`;
	section.classList.remove('d-none');
}

function doSubmitBidCreate() {
	if (!isBusinessUser) {
		alert('업체 회원만 제안할 수 있습니다.');
		return;
	}
	if (!currentRequestId) {
		alert('의뢰글 정보가 없습니다.');
		return;
	}

	const description = document.getElementById('bid-create-description')?.value.trim() || '';
	const price = getNumericPriceValue();
	const expectedDueDate = document.getElementById('bid-create-due-date')?.value || '';
	const asAvailableValue = document.getElementById('bid-create-as-available')?.value || '';

	if (!description) { alert('제안 설명을 입력해주세요.'); return; }
	if (!validateBidPriceInput({ showAlert: true, refocus: true })) return;
	if (!expectedDueDate) { alert('예상 완료일을 선택해주세요.'); return; }

	const today = new Date().toISOString().split('T')[0];
	if (expectedDueDate < today) {
		alert('예상 완료일은 오늘 이후 날짜만 선택할 수 있습니다.');
		return;
	}
	if (asAvailableValue === '') { alert('A/S 여부를 선택해주세요.'); return; }

	const payload = {
		requestId: currentRequestId,
		businessId: currentBusinessId,
		description,
		price,
		expectedDueDate,
		asAvailable: asAvailableValue === 'true'
	};

	fetch('/api/bids/', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(payload)
	})
		.then(async res => {
			const data = await res.json();
			if (!res.ok || data.success === false) {
				throw new Error(data.message || '제안 등록에 실패했습니다.');
			}
			return data;
		})
		.then(data => {
			showToast('success', data.message || '제안이 등록되었습니다.');
			resetBidCreateForm();
			refreshBidList(currentRequestId);
		})
		.catch(err => {
			showToast('error', err.message);
		});
}

// 등록 버튼 클릭 — 인라인 확인 UI 표시
function submitBidCreate() {
	const submitBtn = document.getElementById('btn-submit-bid-create');
	const actionsEl = submitBtn?.closest('.bid-create-actions');
	if (!submitBtn || !actionsEl) return;

	// 이미 확인 UI가 떠 있으면 중복 생성 방지
	if (document.getElementById('bid-submit-confirm-zone')) return;

	submitBtn.style.display = 'none';

	const confirmZone = document.createElement('div');
	confirmZone.id = 'bid-submit-confirm-zone';
	confirmZone.className = 'bid-submit-confirm-zone';
	confirmZone.innerHTML = `
		<span class="bid-submit-confirm-msg">등록할까요?</span>
		<button type="button" class="btn-bid-submit-cancel">취소</button>
		<button type="button" class="btn-bid-submit-ok">등록</button>
	`;
	actionsEl.appendChild(confirmZone);

	confirmZone.querySelector('.btn-bid-submit-cancel').onclick = () => {
		confirmZone.remove();
		submitBtn.style.display = '';
	};

	confirmZone.querySelector('.btn-bid-submit-ok').onclick = () => {
		confirmZone.remove();
		submitBtn.style.display = '';
		doSubmitBidCreate(); // 실제 등록 실행
	};
}

function resetBidCreateForm() {
	const descriptionEl = document.getElementById('bid-create-description');
	const priceEl = document.getElementById('bid-create-price');
	const dueDateEl = document.getElementById('bid-create-due-date');
	const asAvailableEl = document.getElementById('bid-create-as-available');

	if (descriptionEl) descriptionEl.value = '';
	if (priceEl) priceEl.value = '';
	if (dueDateEl) dueDateEl.value = '';
	if (asAvailableEl) asAvailableEl.value = '';

	currentBidStep = 1;
	bidAsValue = '';
	document.getElementById('as-yes').className = 'toggle-btn';
	document.getElementById('as-no').className = 'toggle-btn';
	updateBidStepUI();
}

function formatPrice(price) {
	return new Intl.NumberFormat('ko-KR').format(price || 0);
}

function sanitizePriceInputValue(value) {
	return String(value || '').replace(/[^0-9]/g, '');
}

function formatPriceInputValue(value) {
	const onlyNumber = sanitizePriceInputValue(value);
	if (!onlyNumber) return '';
	return new Intl.NumberFormat('ko-KR').format(Number(onlyNumber));
}

function getNumericPriceValue() {
	const priceInput = document.getElementById('bid-create-price');
	if (!priceInput) return NaN;

	const rawValue = sanitizePriceInputValue(priceInput.value);
	if (!rawValue) return NaN;

	return parseInt(rawValue, 10);
}

function validateBidPriceInput({ showAlert = false, refocus = false } = {}) {
	const priceInput = document.getElementById('bid-create-price');
	if (!priceInput) return true;

	const rawValue = sanitizePriceInputValue(priceInput.value);

	if (!rawValue) return true;

	const price = parseInt(rawValue, 10);

	if (Number.isNaN(price) || price <= 0) {
		if (showAlert) alert('제안 가격을 올바르게 입력해주세요.');
		if (refocus) setTimeout(() => priceInput.focus(), 0);
		return false;
	}

	if (currentMinAllowedPrice > 0 && price < currentMinAllowedPrice) {
		if (showAlert) alert(`제안 가격은 최소 ₩ ${formatPrice(currentMinAllowedPrice)} 이상이어야 합니다.`);
		if (refocus) setTimeout(() => { priceInput.focus(); priceInput.select(); }, 0);
		return false;
	}

	return true;
}

function formatDate(dateStr) {
	if (!dateStr) return '-';
	return new Date(dateStr).toLocaleDateString('ko-KR');
}

function clearSelectedBids() {
	selectedBidIds.clear();

	document.querySelectorAll('.bid-compare-checkbox').forEach(checkbox => {
		if (!checkbox.disabled) checkbox.checked = false;
	});

	syncCompareButton();
	updateCompareButtons();
}

function updateCompareButtons() {
	const checkedCount = document.querySelectorAll('.bid-compare-checkbox:checked').length;
	const clearBtn = document.getElementById('btn-clear-compare');
	const compareBtn = document.getElementById('btn-open-compare');

	if (clearBtn) clearBtn.classList.toggle('d-none', checkedCount < 1);
	if (compareBtn) compareBtn.classList.toggle('d-none', checkedCount < 2);
}

function updateBidStatus(bidId, newStatus) {
	return fetch(`/api/bids/${bidId}/status`, {
		method: 'PATCH',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ status: newStatus })
	}).then(async res => {
		const data = await res.json();
		if (!res.ok || data.success === false) {
			throw new Error(data.message || '상태 변경에 실패했습니다.');
		}
		return data;
	});
}

function rejectBid(bidId) {
	const actionsEl = document.getElementById(`bid-actions-${bidId}`);
	if (!actionsEl) return;

	actionsEl.innerHTML = `
		<div class="reject-confirm-zone" id="reject-zone-${bidId}">
			<span class="reject-confirm-msg">정말 거절할까요?</span>
			<button class="btn-reject-cancel" onclick="cancelReject(${bidId})">취소</button>
			<button class="btn-reject-confirm" onclick="confirmReject(${bidId})">거절</button>
		</div>
	`;
}

function cancelReject(bidId) {
	const actionsEl = document.getElementById(`bid-actions-${bidId}`);
	if (!actionsEl) return;

	actionsEl.innerHTML = `
		<button class="btn-select-bid" onclick="selectBid(${bidId})">선정</button>
		<button class="btn-reject-bid" onclick="rejectBid(${bidId})">거절</button>
	`;
}

function confirmReject(bidId) {
	updateBidStatus(bidId, 'REJECTED')
		.then(() => {
			showToast('success', '제안을 거절했습니다.');
			clearSelectedBids();
			refreshBidList(currentRequestId);
		})
		.catch(err => {
			alert(err.message);
			console.error(err);
		});
}

function selectBid(bidId) {
	const bid = currentBidData.find(item => item.bidId === bidId);
	if (!bid) {
		alert('선택한 제안 정보를 찾을 수 없습니다.');
		return;
	}
	openSelectBidModal(bid);
}

function syncCompareButton() {
	const compareBtn = document.getElementById('btn-open-compare');
	const clearBtn = document.getElementById('btn-clear-compare');

	if (compareBtn) {
		compareBtn.disabled = selectedBidIds.size < 2;
		compareBtn.textContent = `비교하기 (${selectedBidIds.size})`;
	}

	if (clearBtn) {
		clearBtn.disabled = selectedBidIds.size === 0;
	}
}

function handleBidCheckboxChange(checkbox, bidId) {
	if (checkbox.checked) {
		if (selectedBidIds.size >= 3) {
			showToast('warn', '업체 비교는 최대 3개까지 가능합니다.');
			checkbox.checked = false;
			return;
		}
		selectedBidIds.add(bidId);
	} else {
		selectedBidIds.delete(bidId);
	}

	syncCompareButton();
	updateCompareButtons();
}

function compareWithinGroup(a, b) {
	if (currentSortType === 'priceAsc') return (a.price || 0) - (b.price || 0);
	if (currentSortType === 'priceDesc') return (b.price || 0) - (a.price || 0);
	if (currentSortType === 'oldest') return new Date(a.createdAt) - new Date(b.createdAt);
	return new Date(b.createdAt) - new Date(a.createdAt);
}

function getBidGroupOrder(status) {
	if (status === 'SELECTED') return 0;
	if (status === 'ACTIVE') return 1;
	return 2;
}

function applyStatusPriority(data) {
	return [...data].sort((a, b) => {
		const groupDiff = getBidGroupOrder(a.status) - getBidGroupOrder(b.status);
		if (groupDiff !== 0) return groupDiff;
		return compareWithinGroup(a, b);
	});
}

/* ── 비교 모달 ── */
function openCompareModal() {
	const modal = document.getElementById('compare-modal');
	const body  = document.getElementById('compare-modal-body');

	const selectedBids = currentBidData.filter(bid => selectedBidIds.has(bid.bidId));
	if (selectedBids.length < 2) {
		showToast('warn', '비교하려면 최소 2개를 선택해주세요.');
		return;
	}

	const minPrice = Math.min(...selectedBids.map(b => b.price));

	const vendorHeaders = selectedBids.map(bid => {
		const isBest = bid.price === minPrice;
		return `
			<th class="cmp-th ${isBest ? 'cmp-th-best' : ''}">
				${isBest ? '<span class="cmp-best-badge">👑 최저가</span>' : ''}
				<span class="cmp-vendor">${bid.companyName || bid.businessLoginId || '전문가 #' + bid.businessId}</span>
			</th>`;
	}).join('');

	const rows = [
		{ label: '제안일',	 fn: b => formatDate(b.createdAt) },
		{ label: '가격',	   fn: b => `<span class="${b.price === minPrice ? 'cmp-price-best' : 'cmp-price'}">₩ ${formatPrice(b.price)}</span>` },
		{ label: '예상 완료일', fn: b => b.expectedDueDate || '-' },
		{ label: 'A/S',	   fn: b => b.asAvailable
			? '<span class="cmp-badge-yes">✔ 가능</span>'
			: '<span class="cmp-badge-no">✖ 불가</span>' },
		{ label: '설명',	   fn: b => `<span class="cmp-desc">${b.description || '-'}</span>` },
		{ label: '상태',	   fn: b => getStatusText(b.status) },
	].map(row => `
		<tr>
			<td class="cmp-label-cell">${row.label}</td>
			${selectedBids.map(bid => `<td class="cmp-td ${bid.price === minPrice ? 'cmp-td-best' : ''}">${row.fn(bid)}</td>`).join('')}
		</tr>`
	).join('');

	body.innerHTML = `
		<div class="cmp-wrap">
			<table class="cmp-table">
				<thead>
					<tr>
						<th class="cmp-label-cell cmp-th-label"></th>
						${vendorHeaders}
					</tr>
				</thead>
				<tbody>${rows}</tbody>
			</table>
		</div>`;

	modal.classList.remove('hidden');
	document.body.classList.add('modal-open');
}

function closeCompareModal() {
	const modal = document.getElementById('compare-modal');
	if (!modal) return;
	modal.classList.add('hidden');
	document.body.classList.remove('modal-open');
}

/* ── 선정 모달 ── */
function openSelectBidModal(bid) {
	const modal = document.getElementById('select-bid-modal');
	if (!modal) return;

	currentSelectedBid = bid;
	cachedSelectInfo = null;
	resetSelectBidForm();

	modal.classList.remove('hidden');
	document.body.classList.add('modal-open');
}

function closeSelectBidModal() {
	const modal = document.getElementById('select-bid-modal');
	if (!modal) return;

	modal.classList.add('hidden');
	currentSelectedBid = null;
	currentSelectStep = 1;
	cachedSelectInfo = null;
	document.body.classList.remove('modal-open');
}

function getStatusText(status) {
	switch (status) {
		case 'SELECTED': return '채택완료';
		case 'REJECTED': return '거절';
		case 'WITHDRAWN': return '철회';
		case 'ACTIVE': return '진행중';
		default: return status || '-';
	}
}

function resetSelectBidForm() {
	currentSelectStep = 1;

	const sizeEl = document.getElementById('select-modal-size');
	const agreeEl = document.getElementById('select-modal-agree');
	if (sizeEl) sizeEl.value = '';
	if (agreeEl) agreeEl.checked = false;

	updateSelectStepUI();
}

function updateSelectStepUI() {
	// 패널 토글
	document.querySelectorAll('.select-step-panel').forEach(panel => {
		panel.classList.toggle('active', Number(panel.dataset.stepPanel) === currentSelectStep);
	});

	// 스텝 닷
	document.querySelectorAll('.select-stepper .step-dot').forEach(dot => {
		const s = Number(dot.dataset.step);
		dot.classList.toggle('active', s === currentSelectStep);
		dot.classList.toggle('done', s < currentSelectStep);
	});

	// 스텝 타이틀
	document.querySelectorAll('.select-stepper .step-title').forEach(title => {
		title.classList.toggle('active', Number(title.dataset.stepTitle) === currentSelectStep);
	});

	// 헤더 타이틀
	const headerTitle = document.getElementById('select-modal-title-text');
	if (headerTitle) {
		headerTitle.textContent = currentSelectStep === 1 ? '치수 입력' : '최종 확인 및 결제';
	}

	// 버튼
	const prevBtn = document.getElementById('btn-prev-select-step');
	const nextBtn = document.getElementById('btn-next-select-step');
	const submitBtn = document.getElementById('btn-submit-select-bid');

	prevBtn?.classList.toggle('d-none', currentSelectStep === 1);
	nextBtn?.classList.toggle('d-none', currentSelectStep === TOTAL_SELECT_STEPS);
	submitBtn?.classList.toggle('d-none', currentSelectStep !== TOTAL_SELECT_STEPS);
}

function goNextSelectStep() {
	if (currentSelectStep === 1) {
		const size = document.getElementById('select-modal-size');
		if (!size.value.trim()) {
			size.classList.add('error');
			document.getElementById('err-select-size').classList.add('visible');
			return;
		}
		size.classList.remove('error');
		document.getElementById('err-select-size').classList.remove('visible');
		currentSelectStep = 2;
		updateSelectStepUI();
		loadSelectInfo();
	}
}

function goPrevSelectStep() {
	if (currentSelectStep > 1) {
		currentSelectStep--;
		updateSelectStepUI();
	}
}

function loadSelectInfo() {
	const loading = document.getElementById('select-confirm-loading');
	const content = document.getElementById('select-confirm-content');

	loading.classList.remove('d-none');
	content.classList.add('d-none');

	// 캐시 있으면 재사용
	if (cachedSelectInfo) {
		fillSelectConfirm(cachedSelectInfo);
		return;
	}

	fetch(`/api/bids/${currentSelectedBid.bidId}/select-info`)
		.then(res => {
			if (!res.ok) throw new Error('정보 조회에 실패했습니다.');
			return res.json();
		})
		.then(data => {
			cachedSelectInfo = data;
			fillSelectConfirm(data);
		})
		.catch(err => {
			alert(err.message);
			// 실패 시 step 1로 복귀
			currentSelectStep = 1;
			updateSelectStepUI();
		});
}

function fillSelectConfirm(info) {
	const loading = document.getElementById('select-confirm-loading');
	const content = document.getElementById('select-confirm-content');

	// 의뢰 정보
	document.getElementById('ci-request-title').textContent = info.request?.title || '❌';
	document.getElementById('ci-request-desc').textContent = info.request?.description || '❌';
	document.getElementById('ci-hope-price').textContent =
		info.request?.hopePrice ? `₩ ${formatPrice(info.request.hopePrice)}` : '❌';
	document.getElementById('ci-requested-due-date').textContent = info.request?.requestedDueDate || '❌';
	document.getElementById('ci-dimensions').textContent =
		document.getElementById('select-modal-size').value.trim() || '-';

	// 업체 제안
	document.getElementById('ci-vendor-name').textContent =
		info.bid?.companyName || info.bid?.businessLoginId || '❌'
	document.getElementById('ci-bid-price').textContent =
		info.bid?.price ? `₩ ${formatPrice(info.bid.price)}` : '❌';
	document.getElementById('ci-bid-due-date').textContent = info.bid?.expectedDueDate || '❌';
	document.getElementById('ci-as-available').textContent =
		info.bid == null ? '❌' : (info.bid.asAvailable ? '✔ 가능' : '✖ 불가');
	document.getElementById('ci-bid-desc').textContent = info.bid?.description || '❌';

	// 배송 정보
	const rawAddress = info.customer?.address || '';
	const parsedAddress = parseCustomerAddress(rawAddress);
	const displayAddress = formatCustomerAddress(rawAddress);
	
	const addressEl = document.getElementById('ci-address');
	addressEl.innerHTML = `
		<span id="ci-address-text">${displayAddress}</span>
		<button type="button" class="btn-address-edit" onclick="toggleAddressEdit()">
			${parsedAddress.address ? '변경' : '입력'}
		</button>
		<div id="ci-address-edit-zone" style="display:none; margin-top:8px;">
			<div style="display:flex; gap:8px; align-items:center;">
			  <input type="text" id="ci-postcode-input" class="ci-address-input" 
			    placeholder="우편번호" value="${parsedAddress.postcode}" readonly
			    style="flex:1; min-width:0; width:auto !important;" />
			  <button type="button" onclick="execSelectDaumPostcode()"
			    style="flex-shrink:0; width:90px; height:36px; border-radius:8px; 
			           border:1px solid #d580c0; color:#c055a5; background:transparent; 
			           cursor:pointer; font-size:13px; font-weight:500; white-space:nowrap; padding:0;">
			    주소 찾기
			  </button>
			</div>
			<input type="text" id="ci-address-input" class="ci-address-input"
			  placeholder="기본 주소" value="${parsedAddress.address}" readonly />
			<input type="text" id="ci-address-detail-input" class="ci-address-input"
			  placeholder="상세 주소를 입력해주세요." value="${parsedAddress.addressDetail}" />
			<div style="display:flex; gap:6px; margin-top:6px; justify-content:flex-end;">
			    <button type="button" class="btn-address-cancel" onclick="cancelAddressEdit()">취소</button>
			    <button type="button" class="btn-address-save" onclick="saveAddressEdit()">확인</button>
			</div>
		</div>
	`;
	document.getElementById('ci-phone').textContent = info.customer?.phoneNumber || '';

	loading.classList.add('d-none');
	content.classList.remove('d-none');
}

function submitSelectBid() {
	const agreeChecked = document.getElementById('select-modal-agree').checked;
	if (!agreeChecked) {
		document.getElementById('err-select-agree').classList.add('visible');
		return;
	}

	const size = document.getElementById('select-modal-size').value.trim();
	if (!size) {
		alert('치수 정보가 없습니다.');
		currentSelectStep = 1;
		updateSelectStepUI();
		return;
	}

	if (!currentSelectedBid) {
		alert('선택된 업체 정보가 없습니다.');
		return;
	}

	// 결제 API
	const submitBtn = document.getElementById('btn-submit-select-bid');
	submitBtn.disabled = true;
	submitBtn.textContent = '처리 중...';

	// 1단계: 주문 사전 생성
	fetch('/api/bids/payment/create', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({
			bidId	  : currentSelectedBid.bidId,
			totalAmount: currentSelectedBid.price
		})
	})
	.then(res => res.json())
	.then(data => {
		if (!data.orderNo) throw new Error('주문 생성에 실패했습니다.');
		return requestBidPayment(data.orderNo, size);
	})
	.catch(err => {
		showToast('error', err.message);
		submitBtn.disabled = false;
		submitBtn.textContent = '💳 결제';
	});
}

function requestBidPayment(orderNo, dimensions) {
	const submitBtn = document.getElementById('btn-submit-select-bid');

	// 2단계: 포트원 SDK 결제창
	PortOne.requestPayment({
		storeId	: 'store-86babd64-b87f-4d03-8a11-45d711b38212',
		channelKey : 'channel-key-0b04d459-1cc2-4ee0-83a8-b8fef7d753eb',
		paymentId  : orderNo,
		orderName  : `${currentSelectedBid.businessLoginId || currentSelectedBid.businessId}`,
		totalAmount: currentSelectedBid.price,
		currency   : 'KRW',
		payMethod  : 'EASY_PAY',
		easyPay: { easyPayProvider: 'KAKAOPAY' },
		customer: {
			fullName   : '구매자',
			email	  : 'test@nextage.com',
			phoneNumber: '010-0000-0000',
		}
	})
	.then(rsp => {
		// 결제 취소 or 실패
		if (rsp.code !== undefined) {
			showToast('warn', rsp.message || '결제가 취소되었습니다.');
			submitBtn.disabled = false;
			submitBtn.textContent = '💳 결제';
			return;
		}

		// 3단계: 서버 검증
		return fetch('/api/bids/payment/verify', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({
				impUid	  : rsp.paymentId,
				orderNo,
				totalAmount : currentSelectedBid.price,
				bidId	   : currentSelectedBid.bidId,
				dimensions  : { raw: dimensions }
			})
		});
	})
	.then(res => {
		if (!res) return;
		return res.json();
	})
	.then(data => {
		if (!data) return;
		if (data.success) {
			closeSelectBidModal();
			showToast('success', '선정 및 결제가 완료되었습니다!');
			refreshBidList(currentRequestId);
		} else {
			showToast('error', data.message || '결제 검증에 실패했습니다.');
			submitBtn.disabled = false;
			submitBtn.textContent = '💳 결제';
		}
	})
	.catch(err => {
		showToast('error', err.message);
		submitBtn.disabled = false;
		submitBtn.textContent = '💳 결제';
	});
}

function confirmSelectBid(dimensions) {
	const payload = {
		bidId: currentSelectedBid.bidId,
		dimensions: { raw: dimensions }  // request.dimensions JSON 컬럼에 저장
	};

	fetch(`/api/bids/${currentSelectedBid.bidId}/select`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(payload)
	})
		.then(async res => {
			const data = await res.json();
			if (!res.ok || data.success === false) {
				throw new Error(data.message || '선정 처리에 실패했습니다.');
			}
			return data;
		})
		.then(() => {
			closeSelectBidModal();
			alert('선정이 완료되었습니다!');
			refreshBidList(currentRequestId);
		})
		.catch(err => {
			alert(err.message);
		});
}

/* ── 이벤트 리스너 ── */
document.addEventListener('keyup', (e) => {
	if (e.target.id !== 'bid-create-price') return;

	const raw = sanitizePriceInputValue(e.target.value);
	const price = Number(raw);
	const errEl = document.getElementById('err-price');

	if (!raw || price <= 0) {
		e.target.classList.remove('error');
		if (errEl) errEl.classList.remove('visible');
	} else if (currentMinAllowedPrice > 0 && price < currentMinAllowedPrice) {
		e.target.classList.add('error');
		if (errEl) {
			errEl.textContent = `최소 제안 가격은 ₩ ${formatPrice(currentMinAllowedPrice)} 입니다.`;
			errEl.classList.add('visible');
		}
	} else if (currentMaxAllowedPrice > 0 && price > currentMaxAllowedPrice) {
		e.target.classList.add('error');
		if (errEl) {
			errEl.textContent = `현재 최저 제안가 ₩ ${formatPrice(currentMaxAllowedPrice)} 보다 낮게 입력해주세요.`;
			errEl.classList.add('visible');
		}
	} else {
		e.target.classList.remove('error');
		if (errEl) errEl.classList.remove('visible');
	}
});

document.addEventListener('input', (e) => {
	if (e.target.id === 'bid-create-price') {
		const cursorEnd = e.target.selectionEnd;
		const beforeLength = e.target.value.length;

		e.target.value = formatPriceInputValue(e.target.value);

		const afterLength = e.target.value.length;
		const diff = afterLength - beforeLength;
		const nextPos = Math.max((cursorEnd || afterLength) + diff, 0);

		requestAnimationFrame(() => {
			e.target.setSelectionRange(nextPos, nextPos);
		});
	}
	
	if (e.target.id === 'select-modal-size') {
		if (e.target.value.trim()) {
			e.target.classList.remove('error');
			document.getElementById('err-select-size').classList.remove('visible');
		}
	}
	
});

document.addEventListener('blur', (e) => {
	if (e.target.id === 'bid-create-price') {
		const rawValue = sanitizePriceInputValue(e.target.value);
		e.target.value = formatPriceInputValue(rawValue);
	}
}, true);

document.addEventListener('change', (e) => {
	if (e.target.matches('.bid-compare-checkbox')) {
		const bidId = Number(e.target.dataset.bidId);
		handleBidCheckboxChange(e.target, bidId);
	}

	if (e.target.id === 'bid-create-due-date') {
		const errEl = document.getElementById('err-due-date');
		const today = new Date().toISOString().split('T')[0];

		if (!e.target.value) {
			e.target.classList.add('error');
			if (errEl) {
				errEl.textContent = '예상 완료일을 선택해주세요.';
				errEl.classList.add('visible');
			}
		} else if (e.target.value < today) {
			e.target.classList.add('error');
			if (errEl) {
				errEl.textContent = '오늘 이후 날짜를 선택해주세요.';
				errEl.classList.add('visible');
			}
		} else {
			e.target.classList.remove('error');
			if (errEl) errEl.classList.remove('visible');
		}
	}
	
	if (e.target.id === 'select-modal-agree') {
		if (e.target.checked) {
			document.getElementById('err-select-agree').classList.remove('visible');
		}
	}
	
});

document.addEventListener('click', (e) => {
	const sortButton = document.getElementById('bid-sort-button');
	const sortMenu = document.getElementById('bid-sort-menu');

	if (e.target.id === 'btn-submit-bid-create') {
		submitBidCreate();
		return;
	}

	if (e.target.id === 'btn-bid-next') {
		goBidNext();
		return;
	}

	if (e.target.id === 'btn-bid-back') {
		goBidPrev();
		return;
	}

	if (sortButton && sortButton.contains(e.target)) {
		document.querySelectorAll('.pro-menu.show').forEach(menu => {
			menu.classList.remove('show');
		});
		const isOpen = sortMenu.classList.toggle('show');
		sortButton.classList.toggle('active', isOpen);
		return;
	}

	const sortOption = e.target.closest('.sort-option');
	if (sortOption) {
		const sortType = sortOption.dataset.sort;
		currentSortType = sortType;

		document.querySelectorAll('.sort-option').forEach(option => {
			option.classList.remove('active');
		});
		sortOption.classList.add('active');

		if (sortButton) {
			sortButton.innerHTML = `${sortOption.textContent}`;
			sortButton.classList.remove('active');
		}
		if (sortMenu) sortMenu.classList.remove('show');

		renderBidList(currentBidData);
		return;
	}

	const infoBtn = e.target.closest('.pro-info');
	if (infoBtn) {
		const menu = document.getElementById(`pro-menu-${infoBtn.dataset.businessId}`);

		document.querySelectorAll('.pro-menu.show').forEach(m => {
			if (m !== menu) m.classList.remove('show');
		});

		if (menu) menu.classList.toggle('show');
		if (sortMenu) sortMenu.classList.remove('show');
		if (sortButton) sortButton.classList.remove('active');
		return;
	}

	if (e.target.id === 'btn-open-compare') {
		openCompareModal();
		return;
	}

	if (e.target.id === 'btn-clear-compare') {
		clearSelectedBids();
		return;
	}

	if (
		e.target.id === 'btn-close-compare' ||
		e.target.classList.contains('compare-modal-backdrop')
	) {
		closeCompareModal();
		return;
	}

	if (!e.target.closest('.pro-menu')) {
		document.querySelectorAll('.pro-menu.show').forEach(menu => {
			menu.classList.remove('show');
		});
	}

	if (!e.target.closest('.bid-sort-dropdown')) {
		if (sortMenu) sortMenu.classList.remove('show');
		if (sortButton) sortButton.classList.remove('active');
	}

	if (e.target.id === 'btn-prev-select-step') {
		goPrevSelectStep();
		return;
	}

	if (e.target.id === 'btn-next-select-step') {
		goNextSelectStep();
		return;
	}

	if (
		e.target.id === 'btn-close-select-bid-modal' ||
		e.target.classList.contains('select-bid-modal-backdrop')
	) {
		closeSelectBidModal();
		return;
	}

	if (e.target.id === 'btn-submit-select-bid') {
		submitSelectBid();
		return;
	}
});

/* ── 제안 리스트 렌더링 ── */
function renderBidList(data) {
	const container = document.getElementById('bid-list-container');
	const emptyMessage = document.getElementById('bid-empty-message');
	const isRequestOwnerCustomer = isLoggedIn && !isBusinessUser && Number(currentCustomerId) === Number(requestOwnerCustomerId);
	
	container.innerHTML = '';

	const activeData = data.filter(b => b.status !== 'HIDDEN');
	currentBidData = [...activeData];

	selectedBidIds = new Set(
		[...selectedBidIds].filter(bidId =>
			activeData.some(b => b.bidId === bidId && b.status !== 'REJECTED' && b.status !== 'WITHDRAWN')
		)
	);

	document.getElementById('bid-count-display').innerText = activeData.length;

	if (activeData.length === 0) {
		emptyMessage.classList.remove('d-none');
	} else {
		emptyMessage.classList.add('d-none');
	}

	const hasSelected = activeData.some(b => b.status === 'SELECTED');
	const sortedData = applyStatusPriority(activeData);

	sortedData.forEach(bid => {
		const isMyBid = isBusinessUser && Number(currentBusinessId) === Number(bid.businessId);
		const isSelected = bid.status === 'SELECTED';
		const isRejected = bid.status === 'REJECTED';
		const isWithdrawn = bid.status === 'WITHDRAWN';
		const checked = selectedBidIds.has(bid.bidId) ? 'checked' : '';
		const isOtherBidLocked = hasSelected && !isSelected;
		const disableActionButtons = (hasSelected && !isSelected) || isRejected || isWithdrawn;
		const disableCompareCheckbox = isRejected || isWithdrawn ? 'disabled' : '';
		
		const actionBtns = isRequestOwnerCustomer && bid.status === 'ACTIVE' && !disableActionButtons
			? `<button class="btn-select-bid" onclick="selectBid(${bid.bidId})">선정</button>
			   <button class="btn-reject-bid" onclick="rejectBid(${bid.bidId})">거절</button>`
			: '';

		const adminBtn = isCustomerAdmin
			? `<button class="btn-admin-delete-bid" onclick="adminHideBid(${bid.bidId})">숨김</button>`
			: '';
		
		
		let statusMsg = '';
		if (isSelected) {
			statusMsg = `<span class="selected-message">✔ 채택완료</span>`;
		} else if (isWithdrawn) {
			statusMsg = `<span class="bid-status-withdrawn">✖ 철회</span>`;
		} else if (isRejected) {
			statusMsg = `<span class="bid-status-rejected">✖ 거절</span>`;
		}

		const html = `
			<div class="bid-item ${isSelected ? 'bid-selected' : ''}">
				<div class="bid-header">
					<div class="bid-header-left">
						${isLoggedIn && !isBusinessUser && activeData.length >= 2 && !hasSelected ? `
							<label class="bid-compare-check">
								<input type="checkbox" class="bid-compare-checkbox" data-bid-id="${bid.bidId}" ${checked} ${disableCompareCheckbox} />
							</label>
						` : ''}

						<div class="pro-info" data-business-id="${bid.businessId}">
							<div class="pro-avatar">
								<img src="/image/default-profile.png" alt="업체 프로필">
							</div>
							<div class="pro-text">
								<span class="pro-name">${bid.companyName || bid.businessLoginId || '전문가 #' + bid.businessId}</span>
								<span class="bid-date">${formatDate(bid.createdAt)}</span>
							</div>
							<span class="pro-arrow">▼</span>
						</div>
					</div>

					<div class="pro-menu" id="pro-menu-${bid.businessId}">
						<a href="/business/portfolio/${bid.businessId}" class="menu-item">업체 정보</a>
						${isOtherBidLocked ? `` : `
							<div class="menu-divider"></div>
							<a href="javascript:void(0);" onclick="goToChat(${bid.bidId}, ${requestOwnerCustomerId}, ${bid.businessId})" class="menu-item">실시간 채팅</a>
						`}
					</div>

					<div class="bid-header-right">
						${isMyBid ? `<span class="bid-my-badge">내 제안</span>` : ''}
						${statusMsg}
					</div>
				</div>

				<div class="bid-content">
					<p class="description">${bid.description || ''}</p>
					<div class="bid-specs">
						<div class="spec-item">
							<span class="label">예상 완료일</span>
							<span class="value">${bid.expectedDueDate || '-'}</span>
						</div>
						<div class="spec-item">
							<span class="label">A/S</span>
							<span class="value">${bid.asAvailable ? '가능' : '불가'}</span>
						</div>
					</div>
				</div>

				<div class="bid-footer">
					<div class="bid-price">
						<span class="label">제안 가격 &nbsp;:&nbsp;</span>
						<strong>₩ ${formatPrice(bid.price)}</strong>
					</div>
					<div class="bid-actions" id="bid-actions-${bid.bidId}">
						${actionBtns}
						${adminBtn}
					</div>
				</div>
			</div>
		`;

		container.insertAdjacentHTML('beforeend', html);
	});
	
	syncCompareButton();
	updateCompareButtons();
		
}

function refreshBidList(requestId) {
	currentRequestId = requestId;

	fetch(`/api/bids/request/${requestId}`)
		.then(res => {
			if (!res.ok) throw new Error('제안 목록 조회에 실패했습니다.');
			return res.json();
		})
		.then(data => {
			currentBidData = data;
			renderBidList(data);

			// 기존 ACTIVE 제안 중 최저가 계산
			const activeBids = data.filter(b => b.status === 'ACTIVE');
			if (activeBids.length > 0) {
				currentMaxAllowedPrice = Math.min(...activeBids.map(b => b.price));
			} else {
				currentMaxAllowedPrice = 0;
			}

			renderBidPricePresets();

			if (isBusinessUser && currentBusinessId) {
				const alreadySubmitted = data.some(
					bid => Number(bid.businessId) === Number(currentBusinessId)
				);
				if (alreadySubmitted) {
					setBidCreateDisabled('이미 이 의뢰글에 제안을 등록했습니다.');
				}
			}
		})
		.catch(err => {
			console.error(err);
			alert(err.message);
		});
}

/* ── 제안 등록 스텝 위자드 ── */
let currentBidStep = 1;
const TOTAL_BID_STEPS = 4;
let bidAsValue = '';

function setBidPrice(n) {
	const el = document.getElementById('bid-create-price');
	if (el) el.value = formatPrice(n);

	el.classList.remove('error');
	const errEl = document.getElementById('err-price');
	if (errEl) errEl.classList.remove('visible');
}

function setBidAS(val) {
	bidAsValue = val;
	document.getElementById('bid-create-as-available').value = val;
	document.getElementById('as-yes').className = 'toggle-btn' + (val === true ? ' active-yes' : '');
	document.getElementById('as-no').className = 'toggle-btn' + (val === false ? ' active-no' : '');

	const errEl = document.getElementById('err-as');
	if (errEl) errEl.classList.remove('visible');
}

function updateBidStepUI() {
	document.querySelectorAll('.bid-step-panels .bid-step-panel').forEach(p => {
		p.classList.toggle('active', Number(p.dataset.panel) === currentBidStep);
	});

	document.querySelectorAll('#bid-step-indicator .step-node').forEach(node => {
		const n = Number(node.dataset.node);
		node.classList.toggle('active', n === currentBidStep);
		node.classList.toggle('done', n < currentBidStep);
		const dot = node.querySelector('.step-dot');
		if (n < currentBidStep) dot.textContent = '✓';
		else dot.textContent = n;
	});

	const fill = document.getElementById('bid-progress-fill');
	if (fill) fill.style.width = (currentBidStep / TOTAL_BID_STEPS * 100) + '%';

	const backBtn = document.getElementById('btn-bid-back');
	const nextBtn = document.getElementById('btn-bid-next');
	const submitBtn = document.getElementById('btn-submit-bid-create');

	if (backBtn) backBtn.classList.toggle('d-none', currentBidStep === 1);
	if (nextBtn) nextBtn.classList.toggle('d-none', currentBidStep === TOTAL_BID_STEPS);
	if (submitBtn) submitBtn.classList.toggle('d-none', currentBidStep !== TOTAL_BID_STEPS);
}

function fillBidConfirm() {
	const desc = document.getElementById('bid-create-description').value.trim();
	const rawPrice = sanitizePriceInputValue(document.getElementById('bid-create-price').value);
	const dueDate = document.getElementById('bid-create-due-date').value;

	document.getElementById('bid-confirm-description').textContent = desc || '-';
	document.getElementById('bid-confirm-price').textContent =
		rawPrice ? '₩ ' + formatPrice(Number(rawPrice)) : '-';
	document.getElementById('bid-confirm-due-date').textContent =
		dueDate ? new Date(dueDate).toLocaleDateString('ko-KR') : '-';
	document.getElementById('bid-confirm-as').textContent =
		bidAsValue === true ? '✔ 가능' : bidAsValue === false ? '✖ 불가' : '-';
}

function showBidError(id, msg) {
	const el = document.getElementById(id);
	if (!el) return;
	el.textContent = msg;
	el.classList.add('visible');
}

function clearBidErrors() {
	document.querySelectorAll('.field-error').forEach(el => el.classList.remove('visible'));
	document.querySelectorAll('.bid-create-input, .bid-create-textarea').forEach(el => {
		el.classList.remove('error');
	});
}

function validateBidStep() {
	clearBidErrors();
	let valid = true;

	if (currentBidStep === 1) {  // 가격
		const priceEl = document.getElementById('bid-create-price');
		const raw = sanitizePriceInputValue(priceEl.value);
		if (!raw || Number(raw) <= 0) {
			priceEl.classList.add('error');
			showBidError('err-price', '제안 가격을 입력해주세요.');
			priceEl.focus();
			valid = false;
		} else if (currentMinAllowedPrice > 0 && Number(raw) < currentMinAllowedPrice) {
			priceEl.classList.add('error');
			showBidError('err-price', `최소 제안 가격은 ₩ ${formatPrice(currentMinAllowedPrice)} 입니다.`);
			priceEl.focus();
			valid = false;
		} else if (currentMaxAllowedPrice > 0 && Number(raw) > currentMaxAllowedPrice) {
			priceEl.classList.add('error');
			showBidError('err-price', `현재 최저 제안가 ₩ ${formatPrice(currentMaxAllowedPrice)} 보다 낮게 입력해주세요.`);
			priceEl.focus();
			valid = false;
		}
	}

	if (currentBidStep === 2) {  // 일정 · A/S
		const dateEl = document.getElementById('bid-create-due-date');
		const today = new Date().toISOString().split('T')[0];
		if (!dateEl.value) {
			dateEl.classList.add('error');
			showBidError('err-due-date', '예상 완료일을 선택해주세요.');
			valid = false;
		} else if (dateEl.value < today) {
			dateEl.classList.add('error');
			showBidError('err-due-date', '오늘 이후 날짜를 선택해주세요.');
			valid = false;
		}
		if (bidAsValue === '') {
			showBidError('err-as', 'A/S 여부를 선택해주세요.');
			valid = false;
		}
	}

	if (currentBidStep === 3) {  // 제안 설명
		const desc = document.getElementById('bid-create-description');
		if (!desc.value.trim()) {
			desc.classList.add('error');
			showBidError('err-description', '제안 설명을 입력해주세요.');
			desc.focus();
			valid = false;
		}
	}

	return valid;
}

function goBidNext() {
	if (!validateBidStep()) return;
	if (currentBidStep === 3) fillBidConfirm();
	if (currentBidStep < TOTAL_BID_STEPS) {
		currentBidStep++;
		updateBidStepUI();
	}
}

function goBidPrev() {
	if (currentBidStep > 1) {
		currentBidStep--;
		updateBidStepUI();
	}
}

/* ── 가격 프리셋 ── */
function renderBidPricePresets() {
	const container = document.getElementById('bid-price-presets');
	if (!container) return;

	const base = currentMinAllowedPrice > 0 ? currentMinAllowedPrice : 10000;

	if (currentMaxAllowedPrice > 0 && currentMaxAllowedPrice >= base) {
		const range = currentMaxAllowedPrice - base;
		const candidates = range === 0
			? [base]
			: [base, Math.round(base + range * 0.5), currentMaxAllowedPrice];

		const presets = [...new Set(candidates)];
		container.innerHTML = presets.map(price => `
			<button class="preset-chip" type="button" onclick="setBidPrice(${price})">
				${formatPrice(price)}원
			</button>
		`).join('');
		return;
	}

	const candidates = [
		Math.ceil(base / 10000) * 10000,
		Math.ceil(base * 1.5 / 10000) * 10000,
		Math.ceil(base * 2 / 10000) * 10000,
		Math.ceil(base * 3 / 10000) * 10000,
		Math.ceil(base * 5 / 10000) * 10000,
	];

	const presets = [...new Set(candidates)];

	if (presets.length === 0) {
		container.innerHTML = '';
		return;
	}

	container.innerHTML = presets.map(price => `
		<button class="preset-chip" type="button" onclick="setBidPrice(${price})">
			${formatPrice(price)}원
		</button>
	`).join('');
}