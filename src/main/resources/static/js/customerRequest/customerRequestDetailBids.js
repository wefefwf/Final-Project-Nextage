function formatPrice(price) {
    return new Intl.NumberFormat('ko-KR').format(price);
}

function formatDate(dateStr) {
    const d = new Date(dateStr);
    return d.toLocaleDateString('ko-KR');
}

let currentRequestId = null;

function updateBidStatus(bidId, newStatus) {
    return fetch(`/api/bids/${bidId}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    }).then(res => res.json());
}

function rejectBid(bidId) {
    if (!confirm('정말 거절하시겠습니까?\n거절 시 되돌릴 수 없습니다.')) return;

    updateBidStatus(bidId, 'REJECTED')
        .then(() => refreshBidList(currentRequestId))
        .catch(err => console.error(err));
}

function selectBid(bidId) {
    updateBidStatus(bidId, 'SELECTED')
        .then(() => refreshBidList(currentRequestId))
        .catch(err => console.error(err));
}

function toggleProMenu(event, businessId) {
    event.stopPropagation();

    const menu = document.getElementById(`pro-menu-${businessId}`);

    document.querySelectorAll('.pro-menu').forEach(m => {
        if (m !== menu) m.classList.remove('show');
    });

    menu.classList.toggle('show');
}

document.addEventListener('click', function () {
    document.querySelectorAll('.pro-menu').forEach(m => {
        m.classList.remove('show');
    });
});

function renderBidList(data) {

    const container = document.getElementById('bid-list-container');
    const countDisplay = document.getElementById('bid-count-display');
    const emptyMessage = document.getElementById('bid-empty-message');

    container.innerHTML = '';

    // 1️⃣ HIDDEN 제거
    data = data.filter(b => b.status !== 'HIDDEN');

    countDisplay.innerText = data.length;

    if (data.length === 0) {
        emptyMessage.classList.remove('d-none');
        return;
    }

    emptyMessage.classList.add('d-none');

    // 2️⃣ SELECTED 존재 여부
    const hasSelected = data.some(b => b.status === 'SELECTED');

    // 3️⃣ 상태 정렬
    const order = {
        SELECTED: 0,
        ACTIVE: 1,
        WITHDRAWN: 2,
        REJECTED: 3
    };

    data.sort((a, b) => order[a.status] - order[b.status]);

    data.forEach(bid => {

        const isSelected = bid.status === 'SELECTED';
        const isRejected = bid.status === 'REJECTED';
        const isWithdrawn = bid.status === 'WITHDRAWN';

        const disableButtons =
            (hasSelected && !isSelected) ||
            isRejected ||
            isWithdrawn;

        let statusMessage = '';

        if (isSelected) {
            statusMessage = `<div class="selected-message">✔ 채택완료</div>`;
        }

        if (isWithdrawn) {
            statusMessage = `<div class="bid-status-withdrawn">✖ 철회</div>`;
        }

        if (isRejected) {
            statusMessage = `<div class="bid-status-rejected">✖ 거절</div>`;
        }

        const html = `
        <div class="bid-item 
            ${isSelected ? 'bid-selected' : ''} 
            ${isRejected ? 'bid-rejected' : ''} 
            ${isWithdrawn ? 'bid-withdrawn' : ''}">

            <div class="bid-header">
                <div class="pro-info" onclick="toggleProMenu(event, ${bid.businessId})">
                    <div class="pro-avatar">
                        <img src="/image/default-profile.png">
                    </div>

                    <div class="pro-text">
                        <span class="pro-name">전문가 #${bid.businessId}</span>
                        <span class="bid-date">${formatDate(bid.createdAt)}</span>
                    </div>
					<span class="pro-arrow">▼</span>
                </div>

                ${statusMessage}
            </div>
			
			<div class="pro-menu" id="pro-menu-${bid.businessId}">
			    <a href="/business/${bid.businessId}" class="menu-item">업체정보 보러가기</a>
			    <div class="menu-divider"></div>
			    <a href="/chat/${bid.businessId}" class="menu-item">실시간 채팅하기</a>
			</div>

            <div class="bid-content">
                <p class="description">${bid.description || ''}</p>

                <div class="bid-specs">
                    <div class="spec-item">
                        <span class="label">예상 완료일</span>
                        <span class="value">${bid.expectedDueDate}</span>
                    </div>

                    <div class="spec-item">
                        <span class="label">A/S 여부</span>
                        <span class="value">${bid.asAvailable ? '가능' : '불가'}</span>
                    </div>
                </div>
            </div>

            <div class="bid-footer">

                <div class="bid-price">
                    <span class="label">제안 가격</span>
                    <strong>₩ ${formatPrice(bid.price)}</strong>
                </div>

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

    fetch(`/api/bids/request/${requestId}`)
        .then(res => res.json())
        .then(data => renderBidList(data))
        .catch(err => console.error('입찰 목록 로드 실패:', err));
}