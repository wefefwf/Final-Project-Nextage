document.addEventListener("DOMContentLoaded", function() {
    
    const newPassword = document.getElementById("newPassword");
    const passwordCheck = document.getElementById("passwordCheck");
    const checkLabel = document.querySelector('label[for="passwordCheck"]');

    function validatePassword() {
        if(newPassword.value === "" && passwordCheck.value === "") {
            checkLabel.innerText = "새 비밀번호 확인";
            checkLabel.style.color = "var(--optional-color)";
            passwordCheck.style.borderColor = "";
            return;
        }

        if(newPassword.value === passwordCheck.value) {
            checkLabel.innerText = "비밀번호가 일치합니다.";
            checkLabel.style.color = "#198754";
            passwordCheck.style.borderColor = "#198754";
        } else {
            checkLabel.innerText = "비밀번호가 일치하지 않습니다.";
            checkLabel.style.color = "red";
            passwordCheck.style.borderColor = "red";
        }
    }

    newPassword.addEventListener("input", validatePassword);
    passwordCheck.addEventListener("input", validatePassword);


    const form = document.getElementById("editForm");
    form.addEventListener("submit", function(event) {
        if(newPassword.value !== passwordCheck.value) {
            event.preventDefault();
            alert("새 비밀번호가 일치하지 않습니다. 다시 확인해주세요.");
            passwordCheck.focus();
        }
    });

});


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

            if (data.userSelectedType === 'R') { addr = data.roadAddress; } 
            else { addr = data.jibunAddress; }

            if(data.userSelectedType === 'R'){
                if(data.bname !== '' && /[동로가]$/.test(data.bname)){ extraAddr += data.bname; }
                if(data.buildingName !== '' && data.apartment === 'Y'){ extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName); }
                if(extraAddr !== ''){ extraAddr = ' (' + extraAddr + ')'; }
                addr += extraAddr;
            }
            
            document.getElementById('postcode').value = data.zonecode;
            document.getElementById("address").value = addr;
            document.getElementById("addressDetail").focus();
        }
    }).open();
}


function withdrawCustomer() {

    if(confirm("정말로 회원 탈퇴를 진행하시겠습니까?\n탈퇴 시 모든 정보가 삭제되며 복구할 수 없습니다.")) {

        const form = document.createElement("form");
        form.method = "POST";
        form.action = "/customer/mypage/withdrawProc";
        document.body.appendChild(form);
        form.submit();
    }
}