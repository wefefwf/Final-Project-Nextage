/**
 * 
 */
function updateDomain() {
            const domainSelect = document.getElementById("domainSelect");
            const emailDomainInput = document.getElementById("emailDomain");

            if (domainSelect.value === "type") {
                emailDomainInput.value = "";
                emailDomainInput.removeAttribute("readonly");
                emailDomainInput.focus(); 
            } else {
                emailDomainInput.value = domainSelect.value;
                emailDomainInput.setAttribute("readonly", "readonly"); 
            }
        }
        
        function execDaumPostcode() {
            new daum.Postcode({
                oncomplete: function(data) {
    
                    var addr = ''; 
                    var extraAddr = ''; 

                    if (data.userSelectedType === 'R') { 
                        addr = data.roadAddress;
                    } else { 
                        addr = data.jibunAddress;
                    }

                    if(data.userSelectedType === 'R'){
                        if(data.bname !== '' && /[동로가]$/.test(data.bname)){
                            extraAddr += data.bname;
                        }

                        if(data.buildingName !== '' && data.apartment === 'Y'){
                            extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                        }

                        if(extraAddr !== ''){
                            extraAddr = ' (' + extraAddr + ')';
                        }
                        addr += extraAddr;
                        }
                    document.getElementById('postcode').value = data.zonecode;
                    document.getElementById("address").value = addr;
                    document.getElementById("addressDetail").focus();
                }
            }).open();
        }
		
		
		// HTML이 모두 로드된 후 실행되도록 설정
		document.addEventListener("DOMContentLoaded", function() {
		    
		    const loginIdInput = document.getElementById("loginId");
		    const loginIdLabel = document.querySelector('label[for="loginId"]');
		    
		    // 타자 칠 때마다 서버에 요청을 보내면 과부하가 오므로, 타이핑을 멈추고 0.3초 뒤에 검사하도록 타이머 설정 (디바운싱)
		    let typingTimer;
		    const doneTypingInterval = 300; 

		    // 아이디 입력칸에 타자를 칠 때마다 발생하는 이벤트
		    loginIdInput.addEventListener("input", function() {
		        clearTimeout(typingTimer); // 이전 타이머 취소
		        
		        const idValue = loginIdInput.value.trim();


		        if (idValue.length === 0) {
		            setFeedback("아이디", "");
		            return;
		        }


		        if (idValue.length < 4) {
		            setFeedback("아이디가 너무 짧습니다.", "red");
		            return;
		        }

		        typingTimer = setTimeout(() => {
		            checkDuplicateId(idValue);
		        }, doneTypingInterval);
		    });

	
		    function checkDuplicateId(idValue) {

		        fetch(`/auth/customer/check?loginId=${encodeURIComponent(idValue)}`)
		            .then(response => {
		        
		                return response.text(); 
		            })
		            .then(data => {

						if (data.trim() === "true") {
						                   setFeedback("중복된 아이디입니다.", "red");
						               } else if (data.trim() === "false") {
						                   setFeedback("사용 가능한 아이디입니다.", "#198754");
						               } else {
						                   // true/false가 아닌 이상한 값이 오면 콘솔에 경고 표시
						                   console.warn("예상치 못한 데이터:", data);
						               }
						           })
		            .catch(error => {
		                console.error("아이디 중복 확인 중 에러 발생:", error);
		            });
		    }


		    function setFeedback(message, color) {
		        loginIdLabel.innerText = message;
		        
		        if (color === "red") {
		            loginIdLabel.style.color = "red";
		            loginIdInput.style.borderColor = "red";
		            loginIdInput.style.boxShadow = "0 0 0 0.25rem rgba(255, 0, 0, 0.25)"; 
		        } else if (color === "#198754") {
		            loginIdLabel.style.color = "#198754";
		            loginIdInput.style.borderColor = "#198754";
		            loginIdInput.style.boxShadow = "0 0 0 0.25rem rgba(25, 135, 84, 0.25)"; 
		        } else {
		            loginIdLabel.style.color = ""; 
		            loginIdInput.style.borderColor = "";
		            loginIdInput.style.boxShadow = "";
		        }
		    }
		});
		
		
		
		
		document.addEventListener("DOMContentLoaded", function() {

		    const passwordInput = document.getElementById("passwordHash");
		    const passwordLabel = document.querySelector('label[for="passwordHash"]');
		    
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
		
		
			// HTML이 모두 로드된 후 실행되도록 설정
			document.addEventListener("DOMContentLoaded", function() {
			    
			    const nicknameInput = document.getElementById("nickname");
			    const nicknameLabel = document.querySelector('label[for="nickname"]');
			    
			    // 타자 칠 때마다 서버에 요청을 보내면 과부하가 오므로, 타이핑을 멈추고 0.3초 뒤에 검사하도록 타이머 설정 (디바운싱)
			    let typingTimer;
			    const doneTypingInterval = 300; 

			    // 아이디 입력칸에 타자를 칠 때마다 발생하는 이벤트
			    nicknameInput.addEventListener("input", function() {
			        clearTimeout(typingTimer); // 이전 타이머 취소
			        
			        const nicknameValue = nicknameInput.value.trim();

			        if (nicknameValue.length === 0) {
			            setFeedback("닉네임", "");
			            return;
			        }


			        if (nicknameValue.length < 2) {
			            setFeedback("2글자이상으로 입력해주세요", "red");
			            return;
			        }

			        typingTimer = setTimeout(() => {
			            checkDuplicateNickname(nicknameValue);
			        }, doneTypingInterval);
			    });


			    function checkDuplicateNickname(nicknameValue) {

			        fetch(`/auth/customer/check/name?nickname=${encodeURIComponent(nicknameValue)}`)
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
			        nicknameLabel.innerText = message;
			        
			        if (color === "red") {
			            nicknameLabel.style.color = "red";
			            nicknameInput.style.borderColor = "red";
			            nicknameInput.style.boxShadow = "0 0 0 0.25rem rgba(255, 0, 0, 0.25)"; 
			        } else if (color === "#198754") {
			            nicknameLabel.style.color = "#198754";
			            nicknameInput.style.borderColor = "#198754";
			            nicknameInput.style.boxShadow = "0 0 0 0.25rem rgba(25, 135, 84, 0.25)"; 
			        } else {
			            nicknameLabel.style.color = ""; 
			            nicknameInput.style.borderColor = "";
			            nicknameInput.style.boxShadow = "";
			        }
			    }
			});
			
		
			// HTML이 모두 로드된 후 실행되도록 설정------------------------
			document.addEventListener("DOMContentLoaded", function () {

			    const phoneNumberInput = document.getElementById("phoneNumber");
			    const phoneNumberLabel = document.querySelector('label[for="phoneNumber"]');

			    let typingTimer;
			    const doneTypingInterval = 300;

			    phoneNumberInput.addEventListener("input", function () {
			        clearTimeout(typingTimer);

			        const phoneNumberValue = phoneNumberInput.value.trim();

			        // 아무것도 없으면 초기화
			        if (phoneNumberValue.length === 0) {
			            setFeedback("전화번호('-' 제외)", "");
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

			    function checkDuplicatePhoneNumber(phoneNumberValue) {
			        fetch(`/auth/customer/check/phone?phoneNumber=${encodeURIComponent(phoneNumberValue)}`)
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
			    
			    const emailInput = document.getElementById("email");            
			    const emailDomainInput = document.getElementById("emailDomain"); 
			    const domainSelect = document.getElementById("domainSelect");    
			    
			    const emailLabel = document.querySelector('label[for="email"]');
			    
			    let emailTimer;
			    const doneTypingInterval = 300; 

			    emailInput.addEventListener("input", triggerEmailCheck);
			    emailDomainInput.addEventListener("input", triggerEmailCheck);
			    domainSelect.addEventListener("change", triggerEmailCheck);

			    function triggerEmailCheck() {
			        clearTimeout(emailTimer); 
			        
			        const emailId = emailInput.value.trim();
			        const emailDomain = emailDomainInput.value.trim();


			        if (emailId.length === 0 && emailDomain.length === 0) {
			            setEmailFeedback("이메일", "");
			            return;
			        }

			        if (emailId.length === 0 || emailDomain.length === 0) {
			            setEmailFeedback("이메일 입력 중...", ""); 
			            return; 
			        }
					if (emailId.length < 3) {
					        setEmailFeedback("너무 짧습니다.", "red");
					        return;
					    }

					    if (emailDomain.length < 5) {
					        setEmailFeedback("도메인오류", "red");
					        return;
					    }


					    const domainParts = emailDomain.split(".");

					    if (domainParts.length < 2) {
					        setEmailFeedback("도메인오류", "red");
					        return;
					    }

					    const front = domainParts[0];
					    const back = domainParts[1];

					    if (front.length < 2 || back.length < 2) {
					        setEmailFeedback("도메인오류", "red");
					        return;
					    }


			        const fullEmail = (emailId + "@" + emailDomain).toLowerCase();


			        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
			        if (!emailRegex.test(fullEmail)) {
			            setEmailFeedback("이메일 형식 오류.", "red");
			            return;
			        }

			        emailTimer = setTimeout(() => {
			            checkDuplicateEmail(fullEmail);
			        }, doneTypingInterval);
			    }

			    function checkDuplicateEmail(fullEmail) {
			        fetch(`/auth/customer/check/email?email=${encodeURIComponent(fullEmail)}`)
			            .then(response => response.text())
			            .then(data => {
			                if (data.trim() === "true") {
			                    setEmailFeedback("이미 가입된 이메일입니다.", "red");
			                } else if (data.trim() === "false") {
			                    setEmailFeedback("사용 가능한 이메일입니다.", "#198754");
			                } else {
			                    console.warn("예상치 못한 데이터:", data);
			                }
			            })
			            .catch(error => {
			                console.error("이메일 중복 확인 중 에러 발생:", error);
			            });
			    }

			    // 이메일 입력칸 2개(앞, 뒤)의 색상을 동시에 변경해주는 헬퍼 함수
			    function setEmailFeedback(message, color) {
			        emailLabel.innerText = message;
			        
			        if (color === "red") {
			            emailLabel.style.color = "red";
			            emailInput.style.borderColor = "red";
			            emailDomainInput.style.borderColor = "red";
			            emailInput.style.boxShadow = "0 0 0 0.25rem rgba(255, 0, 0, 0.25)"; 
			            emailDomainInput.style.boxShadow = "0 0 0 0.25rem rgba(255, 0, 0, 0.25)"; 
			        } else if (color === "#198754") {
			            emailLabel.style.color = "#198754";
			            emailInput.style.borderColor = "#198754";
			            emailDomainInput.style.borderColor = "#198754";
			            emailInput.style.boxShadow = "0 0 0 0.25rem rgba(25, 135, 84, 0.25)"; 
			            emailDomainInput.style.boxShadow = "0 0 0 0.25rem rgba(25, 135, 84, 0.25)"; 
			        } else {
			            // 원상 복구 (핑크색 선택항목 라벨 색상 유지)
			            emailLabel.style.color = "var(--optional-color)"; 
			            emailInput.style.borderColor = "";
			            emailDomainInput.style.borderColor = "";
			            emailInput.style.boxShadow = "";
			            emailDomainInput.style.boxShadow = "";
			        }
			    }
			});
			
			
			
			document.addEventListener("DOMContentLoaded", function() {
			    
			    
			    const form = document.querySelector("form");

			    form.addEventListener("submit", function(event) {
			        
	
			        event.preventDefault();

	
			        function isGreen(labelForId) {
			            const label = document.querySelector(`label[for="${labelForId}"]`);
			            if (!label) return false;
			            
	
			            const color = label.style.color.replace(/\s/g, ""); 
			            return color === "#198754" || color === "rgb(25,135,84)";
			        }

			    
			        const isIdValid = isGreen("loginId");
					const isPassword1Valid = isGreen("passwordHash"); 
			        const isPassword2Valid = isGreen("passwordCheck"); 
			        const isNicknameValid = isGreen("nickname");
					const isPhoneValid = isGreen("phoneNumber");	
			        const isEmailValid = isGreen("email");
			        

			        if (!isIdValid) {
			            alert("아이디 확인해주세요.");
			            document.getElementById("loginId").focus();
			            return; // 폼 제출 중단
			        }
			        if (!isPassword1Valid) {
			            alert("비밀번호를 확인해주세요.");
			            document.getElementById("passwordHash").focus();
			            return;
			        }if (!isPassword2Valid) {
			            alert("비밀번호 확인 맞게 입력하세요.");
			            document.getElementById("passwordCheck").focus();
			            return;
			        }
			        if (!isNicknameValid) {
			            alert("닉네임을 확인해주세요.");
			            document.getElementById("nickname").focus();
			            return;
			        }
			        if (!isPhoneValid) {
			            alert("올바른 전화번호를 입력해주세요.");
			            document.getElementById("phoneNumber").focus();
			            return;
			        }
			        if (!isEmailValid) {
			            alert("이메일 확인해주세요.");
			            document.getElementById("email").focus();
			            return;
			        }

			        
			        alert("회원가입을 환영합니다!");
			        form.submit(); 
			    });
			});
			