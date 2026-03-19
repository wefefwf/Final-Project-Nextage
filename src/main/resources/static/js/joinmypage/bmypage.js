function withdrawBusiness() {
    if(confirm("정말로 기업 파트너 탈퇴를 진행하시겠습니까?\n 한달 후 정보가 삭제됩니다.")) {
        const form = document.createElement("form");
        form.method = "POST";
        form.action = "/business/mypage/withdrawProc";
        document.body.appendChild(form);
        form.submit();
    }
}


document.addEventListener("DOMContentLoaded", function() {

		    const passwordInput = document.getElementById("newPassword");
		    const passwordLabel = document.querySelector('label[for="newPassword"]');
		    
		    const passwordCheckInput = document.getElementById("passwordCheck");
		    const passwordCheckLabel = document.querySelector('label[for="passwordCheck"]');

		    // 1️⃣ 비밀번호 입력 시 유효성 검사
		    passwordInput.addEventListener("input", function() {
		        const pwdValue = passwordInput.value;

		        // 다 지웠을 때는 원래 라벨로 복구
		        if (pwdValue.length === 0) {
		            setValidationFeedback(passwordInput, passwordLabel, "비밀번호", "");
		            validatePasswordMatch(); // 비밀번호 확인칸도 재검사
		            return;
		        }

		        // 6글자 미만일 때
		        if (pwdValue.length < 6) {
		            setValidationFeedback(passwordInput, passwordLabel, "6글자 이상 입력해주세요.", "red");
		        } else {
		            // 영어(대소문자)와 숫자가 포함되어 있는지 정규식으로 검사
		            const hasLetter = /[a-zA-Z]/.test(pwdValue);
		            const hasNumber = /[0-9]/.test(pwdValue);

		            if (!hasLetter || !hasNumber) {
		                // 영어나 숫자 중 하나라도 없으면
		                setValidationFeedback(passwordInput, passwordLabel, "숫자+영어조합으로 하셔야합니다.", "red");
		            } else {
		                // 모든 조건(6글자 이상, 영어+숫자 포함) 만족
		                setValidationFeedback(passwordInput, passwordLabel, "안전한 비밀번호입니다.", "green");
		            }
		        }

		        // 비밀번호를 수정하면 '비밀번호 확인' 칸도 일치하는지 실시간 재검사
		        if (passwordCheckInput.value.length > 0) {
		            validatePasswordMatch();
		        }
		    });
			    // 2️⃣ 비밀번호 확인 입력 시 일치 검사
			    passwordCheckInput.addEventListener("input", validatePasswordMatch);

			    // 일치/불일치 검사 함수
			    function validatePasswordMatch() {
			        const pwdValue = passwordInput.value;
			        const checkValue = passwordCheckInput.value;

			        // 다 지웠을 때는 원래 라벨로 복구
			        if (checkValue.length === 0) {
			            setValidationFeedback(passwordCheckInput, passwordCheckLabel, "비밀번호 확인", "");
			            return;
			        }

			        if (pwdValue === checkValue) {
			            setValidationFeedback(passwordCheckInput, passwordCheckLabel, "비밀번호가 일치합니다.", "green");
			        } else {
			            setValidationFeedback(passwordCheckInput, passwordCheckLabel, "비밀번호가 일치하지 않습니다.", "red");
			        }
			    }

			    // 3️⃣ 입력창 색상 및 메시지를 변경하는 공통 함수
			    function setValidationFeedback(inputElement, labelElement, message, color) {
			        labelElement.innerText = message;
			        
			        if (color === "red") {
			            labelElement.style.color = "red";
			            inputElement.style.borderColor = "red";
			            inputElement.style.boxShadow = "0 0 0 0.25rem rgba(255, 0, 0, 0.25)";
			        } else if (color === "green") {
			            labelElement.style.color = "#198754";
			            inputElement.style.borderColor = "#198754";
			            inputElement.style.boxShadow = "0 0 0 0.25rem rgba(25, 135, 84, 0.25)";
			        } else {
			            // 빈 문자열("")이 들어오면 원래 Bootstrap 기본 디자인으로 원상복구
			            labelElement.style.color = "";
			            inputElement.style.borderColor = "";
			            inputElement.style.boxShadow = "";
			        }
			    }
			});

			
			document.addEventListener("DOMContentLoaded", function () {

				    const phoneNumberInput = document.getElementById("phoneNumber");
				    const phoneNumberLabel = document.querySelector('label[for="phoneNumber"]');

					const originalPhoneNumber = phoneNumberInput.value.trim();
					
				    let typingTimer;
				    const doneTypingInterval = 300;

				    phoneNumberInput.addEventListener("input", function () {
				        clearTimeout(typingTimer);

				        const phoneNumberValue = phoneNumberInput.value.trim();

				        // 아무것도 없으면 초기화
				        if (phoneNumberValue.length === 0 || phoneNumberValue === originalPhoneNumber) {
				            setFeedback("담당자 연락처 ('-' 제외)", "");
				            return;
				        }

				        // 숫자만 체크
				        const isOnlyNumber = /^[0-9]+$/.test(phoneNumberValue);

				        if (!isOnlyNumber) {
				            setFeedback("' - ' 없이 숫자만 입력해주세요.", "red");
				            return;
				        }

				        // 길이 체크
				        if (phoneNumberValue.length < 9) {
				            setFeedback("번호가 너무 짧습니다. (최소 9자리)", "red");
				            return;
				        }

				        if (phoneNumberValue.length > 11) {
				            setFeedback("번호가 너무 깁니다. (최대 11자리)", "red");
				            return;
				        }

				        // 서버 요청 (디바운싱)
				        typingTimer = setTimeout(() => {
				            checkDuplicatePhoneNumber(phoneNumberValue);
				        }, doneTypingInterval);
				    });
					
					phoneNumberInput.addEventListener("blur", function() {
					        if (phoneNumberInput.value.trim() === "") {
					            phoneNumberInput.value = originalPhoneNumber;
					            setFeedback("담당자 연락처", ""); 
					        }
					    });

				    function checkDuplicatePhoneNumber(phoneNumberValue) {
				        fetch(`/auth/business/check/phone?phoneNumber=${encodeURIComponent(phoneNumberValue)}`)
				            .then(response => response.text())
				            .then(data => {

				                if (data.trim() === "true") {
				                    setFeedback("등록된 번호입니다.", "red");
				                } else if (data.trim() === "false") {
				                    setFeedback("사용 가능한 번호입니다.", "#198754");
				                } else {
				                    console.warn("예상치 못한 데이터:", data);
				                }
				            })
				            .catch(error => {
				                console.error("번호 중복 확인 중 에러 발생:", error);
				            });
				    }

				    function setFeedback(message, color) {
				        phoneNumberLabel.innerText = message;

				        if (color === "red") {
				            phoneNumberLabel.style.color = "red";
				            phoneNumberInput.style.borderColor = "red";
				            phoneNumberInput.style.boxShadow = "0 0 0 0.25rem rgba(255, 0, 0, 0.25)";
				        } else if (color === "#198754") {
				            phoneNumberLabel.style.color = "#198754";
				            phoneNumberInput.style.borderColor = "#198754";
				            phoneNumberInput.style.boxShadow = "0 0 0 0.25rem rgba(25, 135, 84, 0.25)";
				        } else {
				            phoneNumberLabel.style.color = "";
				            phoneNumberInput.style.borderColor = "";
				            phoneNumberInput.style.boxShadow = "";
				        }
				    }
				});
				
				document.addEventListener("DOMContentLoaded", function() {
				    
				    const companyNameInput = document.getElementById("companyName");
				    const companyNameLabel = document.querySelector('label[for="companyName"]');
					
					const originalCompanyName =companyNameInput.value.trim();
				    
				    // 타자 칠 때마다 서버에 요청을 보내면 과부하가 오므로, 타이핑을 멈추고 0.3초 뒤에 검사하도록 타이머 설정 (디바운싱)
				    let typingTimer;
				    const doneTypingInterval = 300; 

				    // 아이디 입력칸에 타자를 칠 때마다 발생하는 이벤트
				   companyNameInput.addEventListener("input", function() {
				        clearTimeout(typingTimer); // 이전 타이머 취소
				        
				        const companyNameValue = companyNameInput.value.trim();

						if (companyNameValue.length === 0 || companyNameValue === originalCompanyName) {
						            setFeedback("닉네임", "");
						            return;
						        }


				        if (companyNameValue.length < 2) {
				            setFeedback("2글자이상으로 입력해주세요", "red");
				            return;
				        }

				        typingTimer = setTimeout(() => {
				            checkDuplicateCompanyName(companyNameValue);
				        }, doneTypingInterval);
				    });

					companyNameInput.addEventListener("blur", function() {
					        if (companyNameInput.value.trim() === "") {
					           companyNameInput.value = originalCompanyName; 
					            setFeedback("닉네임", ""); 
					        }
					    });
					
					
				    function checkDuplicateCompanyName(companyNameValue) {

				        fetch(`/auth/business/check/name?companyName=${encodeURIComponent(companyNameValue)}`)
				            .then(response => {
				        
				                return response.text(); 
				            })
				            .then(data => {

								if (data.trim() === "true") {
								                   setFeedback("중복된 닉네임입니다.", "red");
								               } else if (data.trim() === "false") {
								                   setFeedback("사용 가능한 닉네임입니다.", "#198754");
								               } else {
								                   // true/false가 아닌 이상한 값이 오면 콘솔에 경고 표시
								                   console.warn("예상치 못한 데이터:", data);
								               }
								           })
				            .catch(error => {
				                console.error("닉네임 중복 확인 중 에러 발생:", error);
				            });
				    }


				    function setFeedback(message, color) {
				        companyNameLabel.innerText = message;
				        
				        if (color === "red") {
				            companyNameLabel.style.color = "red";
				            companyNameInput.style.borderColor = "red";
				            companyNameInput.style.boxShadow = "0 0 0 0.25rem rgba(255, 0, 0, 0.25)"; 
				        } else if (color === "#198754") {
				            companyNameLabel.style.color = "#198754";
				            companyNameInput.style.borderColor = "#198754";
				           companyNameInput.style.boxShadow = "0 0 0 0.25rem rgba(25, 135, 84, 0.25)"; 
				        } else {
				            companyNameLabel.style.color = ""; 
				            companyNameInput.style.borderColor = "";
				            companyNameInput.style.boxShadow = "";
				        }
				    }
				});
			
				
				document.addEventListener("DOMContentLoaded", function() {
				    const form = document.getElementById("editForm");
				    
				    if (form) {
				        form.addEventListener("submit", function(event) {
				            
				           
				            function isRed(inputId) {
				                const input = document.getElementById(inputId);
				                return input && input.style.borderColor === "red";
				            }


				            if (isRed("companyName")) {
				                event.preventDefault();
				                alert("기업명 / 상표명을 확인해주세요.");
				                document.getElementById("nickname").focus();
				                return;
				            }
				            if (isRed("phoneNumber")) {
				                event.preventDefault();
				                alert("전화번호를 확인해주세요.");
				                document.getElementById("phoneNumber").focus();
				                return;
				            }	if (isRed("newPassword") || isRed("passwordCheck")) {
				                event.preventDefault();
				                alert("비밀번호를 확인해주세요.");
				                document.getElementById("newPassword").focus();
				                return;
				            }



				         
				        });
				    }
				});

