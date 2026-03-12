/* 전역 변수 관리 */
let currentRequestId = null;
let currentBidData = [];
let selectedBidIds = new Set();
let currentSortType = 'priceDesc';
let currentSelectedBid = null;
let currentSelectStep = 1;
const TOTAL_SELECT_STEPS = 4;

let isLoggedIn = false;
let isBusinessUser = false;
let currentBusinessId = null;
let currentHopePrice = 0;
let currentMinAllowedPrice = 0;
let currentMaxAllowedPrice = 0;

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
    isLoggedIn: login,
    isBusinessUser: businessUser,
    currentBusinessId: businessId
}) {
    currentRequestId = requestId;
    currentHopePrice = Number(hopePrice || 0);
    currentMinAllowedPrice = currentHopePrice > 0
        ? Math.ceil(currentHopePrice * 0.95)
        : 0;

    isLoggedIn = login;
    isBusinessUser = businessUser;
    currentBusinessId = businessId;

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

    if (isBusinessUser) {
        section.classList.remove('d-none');
    } else {
        section.classList.add('d-none');
    }
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

function submitBidCreate() {
	if (!confirm('제안을 등록하시겠습니까?')) return;
	
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

    if (!description) {
        alert('제안 설명을 입력해주세요.');
        return;
    }

    if (!validateBidPriceInput({ showAlert: true, refocus: true })) {
        return;
    }

    if (!expectedDueDate) {
        alert('예상 완료일을 선택해주세요.');
        return;
    }

    const today = new Date().toISOString().split('T')[0];
    if (expectedDueDate < today) {
        alert('예상 완료일은 오늘 이후 날짜만 선택할 수 있습니다.');
        return;
    }

    if (asAvailableValue === '') {
        alert('A/S 여부를 선택해주세요.');
        return;
    }

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
        headers: {
            'Content-Type': 'application/json'
        },
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
            alert(data.message || '제안이 등록되었습니다.');
            resetBidCreateForm();
            refreshBidList(currentRequestId);
        })
        .catch(err => {
            console.error(err);
            alert(err.message);
        });
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

    // 스텝 초기화
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

    if (!rawValue) {
        return true;
    }

    const price = parseInt(rawValue, 10);

    if (Number.isNaN(price) || price <= 0) {
        if (showAlert) {
            alert('제안 가격을 올바르게 입력해주세요.');
        }
        if (refocus) {
            setTimeout(() => priceInput.focus(), 0);
        }
        return false;
    }

    if (currentMinAllowedPrice > 0 && price < currentMinAllowedPrice) {
        if (showAlert) {
            alert(`제안 가격은 최소 ₩ ${formatPrice(currentMinAllowedPrice)} 이상이어야 합니다.`);
        }
        if (refocus) {
            setTimeout(() => {
                priceInput.focus();
                priceInput.select();
            }, 0);
        }
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
        if (!checkbox.disabled) {
            checkbox.checked = false;
        }
    });

    syncCompareButton();
    updateCompareButtons();
}

function updateCompareButtons() {
    const checkedCount = document.querySelectorAll('.bid-compare-checkbox:checked').length;
    const clearBtn = document.getElementById('btn-clear-compare');
    const compareBtn = document.getElementById('btn-open-compare');

    if (clearBtn) {
        clearBtn.classList.toggle('d-none', checkedCount < 1);
    }

    if (compareBtn) {
        compareBtn.classList.toggle('d-none', checkedCount < 2);
    }
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
    if (!confirm('정말 거절하시겠습니까?')) return;

    updateBidStatus(bidId, 'REJECTED')
        .then(() => {
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
            alert('업체 비교는 최대 3개까지 가능합니다.');
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
    if (currentSortType === 'priceAsc') {
        return (a.price || 0) - (b.price || 0);
    }

    if (currentSortType === 'priceDesc') {
        return (b.price || 0) - (a.price || 0);
    }

    if (currentSortType === 'oldest') {
        return new Date(a.createdAt) - new Date(b.createdAt);
    }

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

        if (groupDiff !== 0) {
            return groupDiff;
        }

        return compareWithinGroup(a, b);
    });
}

function openCompareModal() {
    const modal = document.getElementById('compare-modal');
    const body = document.getElementById('compare-modal-body');

    const selectedBids = currentBidData.filter(bid => selectedBidIds.has(bid.bidId));

    if (selectedBids.length < 2) {
        alert('비교하려면 최소 2개를 선택해주세요.');
        return;
    }

    const columnsHtml = selectedBids.map(bid => `
				<div class="compare-column">
						<div class="compare-vendor-name">전문가 #${bid.businessId}</div>

						<div class="compare-item">
								<span class="compare-label">제안일</span>
								<span class="compare-value">${formatDate(bid.createdAt)}</span>
						</div>

						<div class="compare-item">
								<span class="compare-label">가격</span>
								<span class="compare-value">₩ ${formatPrice(bid.price)}</span>
						</div>

						<div class="compare-item">
								<span class="compare-label">예상 완료일</span>
								<span class="compare-value">${bid.expectedDueDate || '-'}</span>
						</div>

						<div class="compare-item">
								<span class="compare-label">A/S 여부</span>
								<span class="compare-value">${bid.asAvailable ? '가능' : '불가'}</span>
						</div>

						<div class="compare-item compare-item-description">
								<span class="compare-label">설명</span>
								<span class="compare-value">${bid.description || '-'}</span>
						</div>

						<div class="compare-item">
								<span class="compare-label">상태</span>
								<span class="compare-value">${getStatusText(bid.status)}</span>
						</div>
				</div>
		`).join('');

    body.innerHTML = `
				<div class="compare-grid compare-grid-${selectedBids.length}">
						${columnsHtml}
				</div>
		`;

    modal.classList.remove('hidden');
    document.body.classList.add('modal-open');
}

function closeCompareModal() {
    const modal = document.getElementById('compare-modal');
    if (!modal) return;

    modal.classList.add('hidden');
    document.body.classList.remove('modal-open');
}

function openSelectBidModal(bid) {
    const modal = document.getElementById('select-bid-modal');
    if (!modal) return;

    currentSelectedBid = bid;

    document.getElementById('select-modal-vendor-name').textContent = `전문가 #${bid.businessId}`;
    document.getElementById('select-modal-created-at').textContent = formatDate(bid.createdAt);
    document.getElementById('select-modal-price').textContent = `₩ ${formatPrice(bid.price)}`;
    document.getElementById('select-modal-due-date').textContent = bid.expectedDueDate || '-';
    document.getElementById('select-modal-as-available').textContent = bid.asAvailable ? '가능' : '불가';

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

    document.getElementById('select-modal-size').value = '';
    document.getElementById('select-modal-address').value = '';
    document.getElementById('select-modal-request').value = '';
    document.getElementById('select-modal-agree').checked = false;

    document.getElementById('confirm-size').textContent = '-';
    document.getElementById('confirm-address').textContent = '-';
    document.getElementById('confirm-request').textContent = '-';

    updateSelectStepUI();
}

function updateSelectStepUI() {
	document.querySelectorAll('.select-step-panel').forEach(panel => {
		panel.classList.toggle(
			'active',
			Number(panel.dataset.stepPanel) === currentSelectStep
		);
	});

	// select-stepper 안의 step-dot만 타겟
	document.querySelectorAll('.select-stepper .step-dot').forEach(dot => {
		const step = Number(dot.dataset.step);
		dot.classList.toggle('active', step === currentSelectStep);
		dot.classList.toggle('done', step < currentSelectStep);
	});

	document.querySelectorAll('.select-stepper .step-title').forEach(title => {
		const step = Number(title.dataset.stepTitle);
		title.classList.toggle('active', step === currentSelectStep);
	});

	const prevBtn = document.getElementById('btn-prev-select-step');
	const nextBtn = document.getElementById('btn-next-select-step');
	const submitBtn = document.getElementById('btn-submit-select-bid');

	if (prevBtn) {
		prevBtn.classList.toggle('d-none', currentSelectStep === 1);
	}

	if (nextBtn) {
		nextBtn.classList.toggle('d-none', currentSelectStep === TOTAL_SELECT_STEPS);
	}

	if (submitBtn) {
		submitBtn.classList.toggle('d-none', currentSelectStep !== TOTAL_SELECT_STEPS);
	}
}

function updateSelectConfirmValues() {
    const size = document.getElementById('select-modal-size').value.trim();
    const address = document.getElementById('select-modal-address').value.trim();
    const request = document.getElementById('select-modal-request').value.trim();

    document.getElementById('confirm-size').textContent = size || '-';
    document.getElementById('confirm-address').textContent = address || '-';
    document.getElementById('confirm-request').textContent = request || '-';
}

function validateCurrentStep() {
    if (currentSelectStep === 1) {
        const size = document.getElementById('select-modal-size').value.trim();
        if (!size) {
            alert('치수 정보를 입력해주세요.');
            return false;
        }
    }

    if (currentSelectStep === 2) {
        const address = document.getElementById('select-modal-address').value.trim();
        if (!address) {
            alert('배송지 정보를 입력해주세요.');
            return false;
        }
    }

    if (currentSelectStep === 3) {
        const request = document.getElementById('select-modal-request').value.trim();
        if (!request) {
            alert('요청사항을 입력해주세요.');
            return false;
        }
    }

    return true;
}

function goNextSelectStep() {
    if (!validateCurrentStep()) return;

    if (currentSelectStep === 3) {
        updateSelectConfirmValues();
    }

    if (currentSelectStep < TOTAL_SELECT_STEPS) {
        currentSelectStep += 1;
        updateSelectStepUI();
    }
}

function goPrevSelectStep() {
    if (currentSelectStep > 1) {
        currentSelectStep -= 1;
        updateSelectStepUI();
    }
}

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
            sortButton.innerHTML = `${sortOption.textContent} <span class="sort-arrow">▼</span>`;
            sortButton.classList.remove('active');
        }

        if (sortMenu) {
            sortMenu.classList.remove('show');
        }

        renderBidList(currentBidData);
        return;
    }

    const infoBtn = e.target.closest('.pro-info');
    if (infoBtn) {
        const menu = document.getElementById(`pro-menu-${infoBtn.dataset.businessId}`);

        document.querySelectorAll('.pro-menu.show').forEach(m => {
            if (m !== menu) m.classList.remove('show');
        });

        if (menu) {
            menu.classList.toggle('show');
        }

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
        const agreeChecked = document.getElementById('select-modal-agree').checked;
        const size = document.getElementById('select-modal-size').value.trim();
        const address = document.getElementById('select-modal-address').value.trim();
        const request = document.getElementById('select-modal-request').value.trim();

        if (!currentSelectedBid) {
            alert('선택된 업체 정보가 없습니다.');
            return;
        }

        if (!size) {
            alert('치수 정보를 입력해주세요.');
            currentSelectStep = 1;
            updateSelectStepUI();
            return;
        }

        if (!address) {
            alert('배송지 정보를 입력해주세요.');
            currentSelectStep = 2;
            updateSelectStepUI();
            return;
        }

        if (!request) {
            alert('요청사항을 입력해주세요.');
            currentSelectStep = 3;
            updateSelectStepUI();
            return;
        }

        if (!agreeChecked) {
            alert('약관 동의가 필요합니다.');
            return;
        }

        const detailPayload = {
            size,
            address,
            request
        };

        const detailText = [
            `[치수]`,
            size,
            ``,
            `[배송지]`,
            address,
            ``,
            `[요청사항]`,
            request
        ].join('\n');

        alert(
            `결제 기능은 아직 연결 전입니다.\n` +
            `선택된 업체: 전문가 #${currentSelectedBid.businessId}\n\n` +
            detailText
        );

        console.log('전달 payload 예시:', {
            bidId: currentSelectedBid.bidId,
            detail: detailPayload
        });

        return;
    }
});

function renderBidList(data) {
    const container = document.getElementById('bid-list-container');
    const emptyMessage = document.getElementById('bid-empty-message');

    container.innerHTML = '';

    const activeData = data.filter(b => b.status !== 'HIDDEN');
    currentBidData = [...activeData];

    selectedBidIds = new Set(
        [...selectedBidIds].filter(bidId =>
            activeData.some(b => b.bidId === bidId && b.status !== 'REJECTED' && b.status !== 'WITHDRAWN')
        )
    );

    document.getElementById('bid-count-display').innerText = activeData.length;
    syncCompareButton();
    updateCompareButtons();

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
												${isLoggedIn && !isBusinessUser ? `
														<label class="bid-compare-check">
																<input
																		type="checkbox"
																		class="bid-compare-checkbox"
																		data-bid-id="${bid.bidId}"
																		${checked}
																		${disableCompareCheckbox}
																/>
														</label>
												` : ''}

												<div class="pro-info" data-business-id="${bid.businessId}">
														<div class="pro-avatar">
																<img src="/image/default-profile.png" alt="업체 프로필">
														</div>
														<div class="pro-text">
																<span class="pro-name">전문가 #${bid.businessId}</span>
																<span class="bid-date">${formatDate(bid.createdAt)}</span>
														</div>
														<span class="pro-arrow">▼</span>
												</div>
										</div>

										<div class="pro-menu" id="pro-menu-${bid.businessId}">
												<a href="/business/portfolio/${bid.businessId}" class="menu-item">업체 정보</a>

												${isOtherBidLocked
                ? ``
                : `
																<div class="menu-divider"></div>
																<a href="/chat/${bid.businessId}" class="menu-item">실시간 채팅</a>
																`
            }
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

										<div class="bid-actions">
												${isLoggedIn && !isBusinessUser && bid.status === 'ACTIVE' && !disableActionButtons ? `
														<button class="btn-select-bid" onclick="selectBid(${bid.bidId})">선정</button>
														<button class="btn-reject-bid" onclick="rejectBid(${bid.bidId})">거절</button>
												` : ''}
										</div>
								</div>
						</div>
				`;

        container.insertAdjacentHTML('beforeend', html);
    });
}

function refreshBidList(requestId) {
    currentRequestId = requestId;

    fetch(`/api/bids/request/${requestId}`)
        .then(res => {
            if (!res.ok) {
                throw new Error('제안 목록 조회에 실패했습니다.');
            }
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
                        && bid.status === 'ACTIVE'
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

/* ───── 스텝 위자드 ───── */
let currentBidStep = 1;
const TOTAL_BID_STEPS = 4;
let bidAsValue = '';

function setBidPrice(n) {
    const el = document.getElementById('bid-create-price');
    if (el) el.value = formatPrice(n);
	
	// 에러 해제
	el.classList.remove('error');
	const errEl = document.getElementById('err-price');
	if (errEl) errEl.classList.remove('visible');
}

function setBidAS(val) {
    bidAsValue = val;
    document.getElementById('bid-create-as-available').value = val;
    document.getElementById('as-yes').className = 'toggle-btn' + (val === true ? ' active-yes' : '');
    document.getElementById('as-no').className = 'toggle-btn' + (val === false ? ' active-no' : '');
	
	// 에러 해제
	const errEl = document.getElementById('err-as');
	if (errEl) errEl.classList.remove('visible');
}

function updateBidStepUI() {
	// bid-step-panels 안의 패널만 타겟
	document.querySelectorAll('.bid-step-panels .bid-step-panel').forEach(p => {
		p.classList.toggle('active', Number(p.dataset.panel) === currentBidStep);
	});

	// bid-step-indicator 안의 노드만 타겟
	document.querySelectorAll('#bid-step-indicator .step-node').forEach(node => {
		const n = Number(node.dataset.node);
		node.classList.toggle('active', n === currentBidStep);
		node.classList.toggle('done', n < currentBidStep);
		const dot = node.querySelector('.step-dot');
		if (n < currentBidStep) dot.textContent = '✓';
		else if (n > currentBidStep) dot.textContent = n;
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

    if (currentBidStep === 1) {
        const desc = document.getElementById('bid-create-description');
        if (!desc.value.trim()) {
            desc.classList.add('error');
            showBidError('err-description', '제안 설명을 입력해주세요.');
            desc.focus();
            valid = false;
        }
    }

    if (currentBidStep === 2) {
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

    if (currentBidStep === 3) {
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

    return valid;
}

function goBidNext() {
    if (!validateBidStep()) return;
    if (currentBidStep === 3) fillBidConfirm();
    if (currentBidStep < TOTAL_BID_STEPS) {
        currentBidStep++;
        updateBidStepUI();
		
		// STEP 2로 넘어오면 가격 인풋에 포커스
		if (currentBidStep === 2) {
			setTimeout(() => {
				const priceEl = document.getElementById('bid-create-price');
				if (priceEl) priceEl.focus();
			}, 50);
		}
    }
}

function goBidPrev() {
    if (currentBidStep > 1) {
        currentBidStep--;
        updateBidStepUI();
    }
}

function renderBidPricePresets() {
	const container = document.getElementById('bid-price-presets');
	if (!container) return;

	const base = currentMinAllowedPrice > 0 ? currentMinAllowedPrice : 10000;

	// 최대가격이 있으면 min~max 사이를 균등하게 나눠서 프리셋 생성
	if (currentMaxAllowedPrice > 0 && currentMaxAllowedPrice >= base) {
		const range = currentMaxAllowedPrice - base;
		const candidates = range === 0
			? [base]
			: [
				base,
				Math.round(base + range * 0.5),
				currentMaxAllowedPrice,
			];

		const presets = [...new Set(candidates)];

		container.innerHTML = presets.map(price => `
			<button class="preset-chip" type="button" onclick="setBidPrice(${price})">
				${formatPrice(price)}원
			</button>
		`).join('');
		return;
	}

	// 최대가격 없으면 기존 방식
	const candidates = [
		Math.ceil(base / 10000) * 10000,
		Math.ceil(base * 1.5 / 10000) * 10000,
		Math.ceil(base * 2 / 10000) * 10000,
		Math.ceil(base * 3 / 10000) * 10000,
		Math.ceil(base * 5 / 10000) * 10000,
	];

	const presets = [...new Set(candidates)];

	container.innerHTML = presets.map(price => `
		<button class="preset-chip" type="button" onclick="setBidPrice(${price})">
			${formatPrice(price)}원
		</button>
	`).join('');

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