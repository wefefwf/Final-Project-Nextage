/* 전역 변수 관리 */
let currentRequestId = null;
let currentBidData = [];
let selectedBidIds = new Set();
let currentSortType = 'priceDesc';
let currentSelectedBid = null;
let currentSelectStep = 1;
const TOTAL_SELECT_STEPS = 4;

function formatPrice(price) {
    return new Intl.NumberFormat('ko-KR').format(price || 0);
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
}

function updateCompareButtons() {

    const checkedCount = document.querySelectorAll('.bid-compare-checkbox:checked').length;

    const clearBtn = document.getElementById('btn-clear-compare');
    const compareBtn = document.getElementById('btn-open-compare');

    // 선택해제 (1개 이상)
    if (checkedCount >= 1) {
        clearBtn.classList.remove('d-none');
    } else {
        clearBtn.classList.add('d-none');
    }

    // 비교하기 (2개 이상)
    if (checkedCount >= 2) {
        compareBtn.classList.remove('d-none');
    } else {
        compareBtn.classList.add('d-none');
    }
}

function updateBidStatus(bidId, newStatus) {
    return fetch(`/api/bids/${bidId}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    }).then(res => {
        if (!res.ok) {
            throw new Error('상태 변경에 실패했습니다.');
        }
        return res.json();
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

    // latest
    return new Date(b.createdAt) - new Date(a.createdAt);
}

function getBidGroupOrder(status) {
    if (status === 'SELECTED') return 0;
    if (status === 'ACTIVE') return 1;

    // REJECTED, WITHDRAWN 등 비활성 상태는 모두 같은 그룹
    return 2;
}

function applyStatusPriority(data) {
    return [...data].sort((a, b) => {
        const groupDiff = getBidGroupOrder(a.status) - getBidGroupOrder(b.status);

        // 그룹이 다르면 그룹 순서 우선
        if (groupDiff !== 0) {
            return groupDiff;
        }

        // 같은 그룹이면 정렬 기준 적용
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

    document.querySelectorAll('.step-dot').forEach(dot => {
        const step = Number(dot.dataset.step);
        dot.classList.toggle('active', step === currentSelectStep);
        dot.classList.toggle('done', step < currentSelectStep);
    });

    document.querySelectorAll('.step-title').forEach(title => {
        const step = Number(title.dataset.stepTitle);
        title.classList.toggle('active', step === currentSelectStep);
    });

    const prevBtn = document.getElementById('btn-prev-select-step');
    const nextBtn = document.getElementById('btn-next-select-step');
    const submitBtn = document.getElementById('btn-submit-select-bid');

    if (prevBtn) {
        prevBtn.style.visibility = currentSelectStep === 1 ? 'hidden' : 'visible';
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

document.addEventListener('change', (e) => {
    if (e.target.matches('.bid-compare-checkbox')) {
        const bidId = Number(e.target.dataset.bidId);
        handleBidCheckboxChange(e.target, bidId);
    }
	
	if (e.target.classList.contains('bid-compare-checkbox')) {
        updateCompareButtons();
    }
});

document.getElementById('btn-clear-compare').addEventListener('click', function () {

    document.querySelectorAll('.bid-compare-checkbox:checked').forEach(cb => {
        cb.checked = false;
    });

    updateCompareButtons();
});

document.addEventListener('click', (e) => {
    const sortButton = document.getElementById('bid-sort-button');
    const sortMenu = document.getElementById('bid-sort-menu');

	// 1. 정렬 버튼 클릭
	if (sortButton && sortButton.contains(e.target)) {
	    // 업체 메뉴 먼저 닫기
	    document.querySelectorAll('.pro-menu.show').forEach(menu => {
	        menu.classList.remove('show');
	    });

	    const isOpen = sortMenu.classList.toggle('show');
	    sortButton.classList.toggle('active', isOpen);
	    return;
	}

    // 2. 정렬 옵션 클릭
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

    // 3. 업체 정보 드롭다운 클릭
    const infoBtn = e.target.closest('.pro-info');
    if (infoBtn) {
        const menu = document.getElementById(`pro-menu-${infoBtn.dataset.businessId}`);

        document.querySelectorAll('.pro-menu.show').forEach(m => {
            if (m !== menu) m.classList.remove('show');
        });

        if (menu) {
            menu.classList.toggle('show');
        }

        // 정렬 메뉴는 닫기
        if (sortMenu) sortMenu.classList.remove('show');
        if (sortButton) sortButton.classList.remove('active');
        return;
    }

    // 4. 비교 버튼 클릭
    if (e.target.id === 'btn-open-compare') {
        openCompareModal();
        return;
    }

    // 5. 선택해제 버튼 클릭
    if (e.target.id === 'btn-clear-compare') {
        clearSelectedBids();
        return;
    }

    // 6. 비교 모달 닫기
    if (
        e.target.id === 'btn-close-compare' ||
        e.target.classList.contains('compare-modal-backdrop')
    ) {
        closeCompareModal();
        return;
    }

    // 7. 바깥 클릭 시 업체 메뉴 닫기
    if (!e.target.closest('.pro-menu')) {
        document.querySelectorAll('.pro-menu.show').forEach(menu => {
            menu.classList.remove('show');
        });
    }

    // 8. 바깥 클릭 시 정렬 메뉴 닫기
    if (!e.target.closest('.bid-sort-dropdown')) {
        if (sortMenu) sortMenu.classList.remove('show');
        if (sortButton) sortButton.classList.remove('active');
    }
	
	// 선정 모달 이전 버튼
	if (e.target.id === 'btn-prev-select-step') {
	    goPrevSelectStep();
	    return;
	}

	// 선정 모달 다음 버튼
	if (e.target.id === 'btn-next-select-step') {
	    goNextSelectStep();
	    return;
	}
	
	// 선정 모달 닫기
	if (
	    e.target.id === 'btn-close-select-bid-modal' ||
	    e.target.classList.contains('select-bid-modal-backdrop')
	) {
	    closeSelectBidModal();
	    return;
	}

	// 선정 모달 결제 버튼
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

    if (activeData.length === 0) {
        emptyMessage.classList.remove('d-none');
        return;
    }

    emptyMessage.classList.add('d-none');

    const hasSelected = activeData.some(b => b.status === 'SELECTED');
    const sortedData = applyStatusPriority(activeData);

    sortedData.forEach(bid => {
        const isSelected = bid.status === 'SELECTED';
        const isRejected = bid.status === 'REJECTED';
        const isWithdrawn = bid.status === 'WITHDRAWN';
        const checked = selectedBidIds.has(bid.bidId) ? 'checked' : '';
		const isOtherBidLocked = hasSelected && !isSelected;

        const disableActionButtons = (hasSelected && !isSelected) || isRejected || isWithdrawn;
        const disableCompareCheckbox = isRejected || isWithdrawn ? 'disabled' : '';

        let statusMsg = '';
        if (isSelected) {
            statusMsg = `<div class="selected-message">✔ 채택완료</div>`;
        } else if (isWithdrawn) {
            statusMsg = `<div class="bid-status-withdrawn">✖ 철회</div>`;
        } else if (isRejected) {
            statusMsg = `<div class="bid-status-rejected">✖ 거절</div>`;
        }

        const html = `
            <div class="bid-item ${isSelected ? 'bid-selected' : ''}">
                <div class="bid-header">
                    <div class="bid-header-left">
                        <label class="bid-compare-check">
                            <input
                                type="checkbox"
                                class="bid-compare-checkbox"
                                data-bid-id="${bid.bidId}"
                                ${checked}
                                ${disableCompareCheckbox}
                            />
                        </label>

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
					    <a href="/business/${bid.businessId}" class="menu-item">업체 정보</a>

					    ${
					        isOtherBidLocked
					            ? ``
					            : ` <div class="menu-divider"></div>
					                <a href="/chat/${bid.businessId}" class="menu-item">실시간 채팅</a>
					            `
					    }
					</div>

                    ${statusMsg}
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
                        ${bid.status === 'ACTIVE' && !disableActionButtons ? `
                            <button class="btn-select-bid" onclick="selectBid(${bid.bidId})">선정하기</button>
                            <button class="btn-reject-bid" onclick="rejectBid(${bid.bidId})">거절하기</button>
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
        })
        .catch(err => {
            console.error(err);
            alert(err.message);
        });
}