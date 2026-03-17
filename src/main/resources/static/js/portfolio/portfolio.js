let selectedReviewId = null;
let selectedStatus = null;
let sliderImages = []; // 이미지 주소를 담을 전역 배열

// 1. 카드 클릭 시 데이터를 미리 준비하는 함수
function fillModal(element) {
    selectedReviewId = element.dataset.id;
    selectedStatus = element.getAttribute('data-status');

    const img1 = element.querySelector('.card-img-holder img')?.src;
    const img2 = element.querySelector('.h-img2')?.value;
    const img3 = element.querySelector('.h-img3')?.value;
    const content = element.querySelector('.h-content')?.value;

    // 텍스트 세팅
    $('#modalTitle').text(element.querySelector('.card-text')?.innerText || '');
    $('#mOrderNum').text(element.querySelector('.order-num')?.innerText || '');
    $('#mPeriod').text(element.querySelector('.work-period')?.innerText || '');
    $('#mReview').text(content || '');

    const btn = document.getElementById('btnToggleStatus');
    if(btn) btn.innerText = (selectedStatus === 'ACTIVE') ? '비공개로 전환' : '공개로 전환';

    // 이미지 배열 준비
    sliderImages = [];

    const isValid = (v) => v && v !== 'null' && v !== 'undefined' && v.trim() !== '';

    if (isValid(img1)) sliderImages.push(img1);
    if (isValid(img2)) sliderImages.push(img2);
    if (isValid(img3)) sliderImages.push(img3);

    console.log("배열 확인:", sliderImages);
}

// 2. 모달 열릴 때 Slick 처리
$(document).ready(function() {

    const $slider = $('.modal-slider');

    $('#workModal').on('shown.bs.modal', function () {

        // 기존 slick 제거
        if ($slider.hasClass('slick-initialized')) {
            $slider.slick('unslick');
        }

        // 기본 이미지 숨김
        $('#mImg1').parent().hide();
        $('#mImg2').parent().hide();
        $('#mImg3').parent().hide();

        // 이미지 세팅
        if(sliderImages[0]){
            $('#mImg1').attr('src', sliderImages[0]).parent().show();
        }

        if(sliderImages[1]){
            $('#mImg2').attr('src', sliderImages[1]).parent().show();
        }

        if(sliderImages[2]){
            $('#mImg3').attr('src', sliderImages[2]).parent().show();
        }

        // 이미지 2개 이상이면 slick 실행
        if(sliderImages.length >= 2){

            // 숨겨진 슬라이드 완전히 제거
            $slider.find('.slide-item:hidden').remove();

            $slider.slick({
                dots: true,
                arrows: false,
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                speed: 300,
                fade: true
            });

            $slider.slick('setPosition');
        }
    });

    // 모달 닫힐 때 slick 제거 + 슬라이드 복원
	// 모달 닫힐 때 slick 제거 + 슬라이드 복원
	$('#workModal').on('hidden.bs.modal', function () {
	    if ($slider.hasClass('slick-initialized')) {
	        $slider.slick('unslick');
	    }
	    $slider.html(`
	        <div class="slide-item"><img id="mImg1" src="" class="square-img"></div>
	        <div class="slide-item"><img id="mImg2" src="" class="square-img"></div>
	        <div class="slide-item"><img id="mImg3" src="" class="square-img"></div>
	    `);
	});

	}); // ← 이 닫는 괄호가 빠져있어요! 첫 번째 $(document).ready 닫기

// 3. 상태 변경
function toggleStatus() {

    const nextStatus = (selectedStatus === 'ACTIVE') ? 'HIDDEN' : 'ACTIVE';

    if(!confirm(`상태를 ${nextStatus === 'HIDDEN' ? '비공개' : '공개'}로 변경하시겠습니까?`)) return;

    $.ajax({
        url: '/business/portfolio/updateStatus',
        type: 'POST',
        data: { reviewId: selectedReviewId, status: nextStatus },
        success: function(){
            alert('변경되었습니다.');
            location.reload();
        },
        error: function(){
            alert('오류 발생');
        }
    });
}
	//위치 변경이나 추가
	function editLocation(btn) {
	    const box = btn.closest('.info-box');
	    const span = box.querySelector('span');
	    const originalText = span.innerText === '등록된 위치 정보가 없습니다.' ? '' : span.innerText;
	
	    // 1. 입력창으로 전환 (CSS 클래스 부여)
	    span.style.flex = "1";
	    span.innerHTML = `<input type="text" value="${originalText}" class="location-edit-input" placeholder="위치 정보를 입력하세요">`;
	    
	    const input = span.querySelector('input');
	    input.focus();
	
	    // 2. 엔터 키 이벤트 핸들러
	    input.addEventListener('keypress', function(e) {
	        if (e.key === 'Enter') {
	            const updatedLocation = this.value.trim();
	            
	            if (updatedLocation === "") {
	                alert("내용을 입력해주세요.");
	                return;
	            }
	
	            // 3. 서버로 UPDATE 요청 (Ajax)
	            $.ajax({
	                url: '/business/portfolio/updateLocation', // 컨트롤러에 만들 주소
	                type: 'POST',
	                data: { location: updatedLocation},
	                success: function(response) {
	                    // 성공 시: input 제거하고 텍스트로 복구
	                    span.innerText = updatedLocation;
	                    span.classList.remove('empty-text');
	                    // 수정/추가 버튼 텍스트도 '수정'으로 통일
	                    box.querySelector('.box-actions button:first-child').innerText = '수정';
	                },
	                error: function() {
	                    alert("위치 정보 업데이트에 실패했습니다.");
	                }
	            });
	        }
	    });
	
	    // 4. 포커스 잃으면 원래대로 (선택 사항)
	    input.addEventListener('blur', function() {
	        if (this.value.trim() === originalText) {
	            span.innerText = originalText || '등록된 위치 정보가 없습니다.';
	        }
	    });
	}
	//프로필. 위치. 경력 삭제
	function deleteItem(type, id) {

	    if(!confirm("삭제하시겠습니까?")) return;

	    $.ajax({
	        url: '/business/portfolio/delete',
	        type: 'POST',
	        data: { type:type, id:id },
	        success: function(){
	            alert("삭제되었습니다.");
	            location.reload();
	        },
	        error: function(){
	            alert("삭제 실패");
	        }
	    });
	}
	
	//커리어 수정
	function editCareer(btn, careerId) {
	    const box = btn.closest('.career-item');
	    if (!box) return;

	    const span = box.querySelector('span');
	    const originalText = span.innerText;

	    let isSubmitting = false;

	    span.style.flex = "1";
	    span.innerHTML = `<input type="text" value="${originalText}" class="career-edit-input" style="width:100%;">`;
	    
	    const input = span.querySelector('input');
	    input.focus();

	    input.addEventListener('keypress', function(e) {
	        if (e.key === 'Enter') {
	            const updatedText = this.value.trim();
	            if(updatedText === "" || updatedText === originalText) {
	                span.innerText = originalText;
	                return;
	            }

	            isSubmitting = true;

	            $.ajax({
	                url: '/business/portfolio/updateCareer',
	                type: 'POST',
	                data: { 
	                    careerId: careerId, 
	                    workDescription: updatedText 
	                },
					success: function(response) { 
					    console.log("서버 응답:", response);
					    location.reload(); 
					},
	                error: function(response){ 
	                    alert("경력 수정 실패"); 
						console.log("서버 응답:", response);
	                    span.innerText = originalText;
	                }
	            });
	        }
	    });

	    input.addEventListener('blur', function(){
	        if(!isSubmitting) {
	            span.innerText = originalText;
	        }
	    });
	}
	//경력 추가
	$('.btn-add-career').click(function() {
	    const container = $('.career-container');
	    
	    if ($('.career-new-input').length > 0) return;

	    const newBox = $(`
	        <div class="info-box career-item">
	            <span style="flex:1;">
	                <input type="text" class="career-edit-input career-new-input" placeholder="새로운 경력을 입력하고 엔터를 치세요">
	            </span>
	        </div>
	    `);
	    
	    if ($('.empty-box').length > 0) $('.empty-box').hide();
	    
	    container.append(newBox);
	    const input = newBox.find('input');
	    input.focus();

	    input.on('keypress', function(e) {
	        if (e.key === 'Enter') {
	            const text = $(this).val().trim();
	            if(!text) return alert("내용을 입력해주세요.");

	            $.ajax({
	                url: '/business/portfolio/addCareer',
	                type: 'POST',
	                data: { workDescription: text },
	                success: function() {
	                    location.reload();
	                }
	            });
	        }
	    });
	});
	//이미지 추가,수정
	function uploadProfileImage(input) {
	    if (!input.files || !input.files[0]) return;

	    const file = input.files[0];
	    
	    if (!file.type.match('image.*')) {
	        alert("이미지 파일만 업로드 가능합니다.");
	        return;
	    }

	    const formData = new FormData();
	    formData.append("profileImage", file); 

	    $.ajax({
	        url: '/business/portfolio/updateProfileImage',
	        type: 'POST',
	        data: formData,
	        processData: false,
	        contentType: false,
	        success: function(response) {
	            alert("프로필 이미지가 변경되었습니다.");
	            location.reload();
	        },
	        error: function() {
	            alert("이미지 업로드 실패");
	        }
	    });
	}
	//메인에서 포폴 눌렀을떄
	$(document).ready(function() {
	    const urlParams = new URLSearchParams(window.location.search);
	    const reviewId = urlParams.get('reviewId');

	    if (reviewId) {
	        const $targetCard = $(`.work-card[data-id="${reviewId}"]`);
	        
	        if ($targetCard.length > 0) {
	            setTimeout(function() {
	                $targetCard.get(0).click(); 
	                console.log(reviewId + "번 포트폴리오 모달을 자동으로 엽니다.");
	            }, 500);
	        }
	    }
	});