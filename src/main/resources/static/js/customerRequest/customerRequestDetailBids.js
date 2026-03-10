function formatPrice(price) { return new Intl.NumberFormat('ko-KR').format(price); }
function formatDate(dateStr) { return new Date(dateStr).toLocaleDateString('ko-KR'); }

let currentRequestId = null;

function updateBidStatus(bidId, newStatus) {
    return fetch(`/api/bids/${bidId}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    }).then(res => res.json());
}

function rejectBid(bidId) {
    if (!confirm('정말 거절하시겠습니까?')) return;
    updateBidStatus(bidId, 'REJECTED').then(() => refreshBidList(currentRequestId));
}

function selectBid(bidId) {
    updateBidStatus(bidId, 'SELECTED').then(() => refreshBidList(currentRequestId));
}

document.addEventListener('click', (e) => {
    const infoBtn = e.target.closest('.pro-info');
    
    if (infoBtn) {
        const menu = document.getElementById(`pro-menu-${infoBtn.dataset.businessId}`);
        document.querySelectorAll('.pro-menu.show').forEach(m => {
            if (m !== menu) m.classList.remove('show');
        });
        if (menu) menu.classList.toggle('show');
        return;
    }

    if (!e.target.closest('.pro-menu')) {
        document.querySelectorAll('.pro-menu.show').forEach(m => m.classList.remove('show'));
    }
});

function renderBidList(data) {
    const container = document.getElementById('bid-list-container');
    container.innerHTML = '';
    const activeData = data.filter(b => b.status !== 'HIDDEN');
    document.getElementById('bid-count-display').innerText = activeData.length;

    if (activeData.length === 0) {
        document.getElementById('bid-empty-message').classList.remove('d-none');
        return;
    }
    document.getElementById('bid-empty-message').classList.add('d-none');

    const hasSelected = activeData.some(b => b.status === 'SELECTED');
    activeData.sort((a, b) => {
        const order = { SELECTED: 0, ACTIVE: 1, WITHDRAWN: 2, REJECTED: 3 };
        return order[a.status] - order[b.status];
    });

    activeData.forEach(bid => {
        const isSelected = bid.status === 'SELECTED';
        const isRejected = bid.status === 'REJECTED';
        const isWithdrawn = bid.status === 'WITHDRAWN';
        const disableButtons = (hasSelected && !isSelected) || isRejected || isWithdrawn;

        let statusMsg = isSelected ? '<div class="selected-message">✔ 채택완료</div>' : 
                        isWithdrawn ? '<div class="bid-status-withdrawn">✖ 철회</div>' :
                        isRejected ? '<div class="bid-status-rejected">✖ 거절</div>' : '';

        const html = `
        <div class="bid-item ${isSelected ? 'bid-selected' : ''}">
            <div class="bid-header">
                <div class="pro-info" data-business-id="${bid.businessId}">
                    <div class="pro-avatar"><img src="/image/default-profile.png"></div>
                    <div class="pro-text">
                        <span class="pro-name">전문가 #${bid.businessId}</span>
                        <span class="bid-date">${formatDate(bid.createdAt)}</span>
                    </div>
                    <span class="pro-arrow">▼</span>
                </div>
				
	            <div class="pro-menu" id="pro-menu-${bid.businessId}">
	                <a href="/business/${bid.businessId}" class="menu-item">업체정보 보러가기</a>
	                <div class="menu-divider"></div>
	                <a href="/chat/${bid.businessId}" class="menu-item">실시간 채팅하기</a>
	            </div>
				
                ${statusMsg}
            </div>
            

            <div class="bid-content">
                <p class="description">${bid.description || ''}</p>
                <div class="bid-specs">
                    <div class="spec-item"><span class="label">예상 완료일</span><span class="value">${bid.expectedDueDate}</span></div>
                    <div class="spec-item"><span class="label">A/S</span><span class="value">${bid.asAvailable ? '가능' : '불가'}</span></div>
                </div>
            </div>

            <div class="bid-footer">
                <div class="bid-price"><span class="label">제안 가격 &nbsp;:&nbsp;</span><strong> ₩ ${formatPrice(bid.price)}</strong></div>
                <div class="bid-actions">
                    ${bid.status === 'ACTIVE' && !disableButtons ? `
                        <button class="btn-select-bid" onclick="selectBid(${bid.bidId})">선정하기</button>
                        <button class="btn-reject-bid" onclick="rejectBid(${bid.bidId})">거절하기</button>
                    ` : ''}
                </div>
            </div>
        </div>`;
        container.insertAdjacentHTML('beforeend', html);
    });
}

function refreshBidList(requestId) {
    currentRequestId = requestId;
    fetch(`/api/bids/request/${requestId}`).then(res => res.json()).then(renderBidList);
}