package com.nextage.web.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatGptService {

    // API 통신을 안 하므로 RestTemplate은 선언만 해둡니다 (에러 방지용)
    public ChatGptService(RestTemplate restTemplate) {}

    public String getChatResponse(String userMessage, String userType) {
        try {
            // 챗봇이 읽는 척하는 딜레이 (시연용 디테일)
            Thread.sleep(600); 

            // 공통 인사말
            if (userMessage.contains("안녕") || userMessage.contains("하이")) {
                return "안녕하세요! B_and_C 플랫폼 통합 지원 센터입니다. 무엇을 도와드릴까요?";
            }

            // 1. 일반 고객(CUSTOMER) 전용 답변
            if ("CUSTOMER".equals(userType)) {
                if (userMessage.contains("환불") || userMessage.contains("취소")) {
                    return "[고객 안내] 환불은 서비스 이용 전 7일 이내 신청 시 100% 환불 가능합니다. 마이페이지 > 이용내역에서 신청해 주세요.";
                }
                if (userMessage.contains("절차") || userMessage.contains("방법")) {
                    return "[고객 안내] 견적 요청 -> 파트너 매칭 -> 채팅 상담 -> 결제 -> 서비스 진행 순으로 이루어집니다.";
                }
                if (userMessage.contains("수수료") || userMessage.contains("비용")) {
                    return "[고객 안내] 고객님께 부과되는 중개 수수료는 0원입니다. 견적서에 기재된 금액만 결제하시면 됩니다.";
                }
                if (userMessage.contains("결제")) {
                    return "[고객 안내] 신용카드, 계좌이체 및 간편결제(카카오페이, 네이버페이)를 지원합니다.";
                }
            } 

            // 2. 사업자(BUSINESS) 전용 답변
            else if ("BUSINESS".equals(userType) || "BADMIN".equals(userType)) {
                if (userMessage.contains("수수료")) {
                    return "[파트너 안내] B_and_C 플랫폼의 중개 수수료는 낙찰 금액의 10%(VAT 별도)입니다.";
                }
                if (userMessage.contains("정산")) {
                    return "[파트너 안내] 정산은 서비스 완료 확정 후 영업일 기준 3일 이내에 등록하신 계좌로 입금됩니다.";
                }
                if (userMessage.contains("입찰") || userMessage.contains("견적서")) {
                    return "[파트너 안내] 견적 발송 시 보유하신 '캐시'가 차감되며, 고객이 수락하지 않을 경우 일부 환급됩니다.";
                }
                if (userMessage.contains("클레임") || userMessage.contains("신고")) {
                    return "[파트너 안내] 고객 클레임 3회 이상 누적 시 플랫폼 이용이 일시적으로 제한될 수 있으니 주의 바랍니다.";
                }
            }

            // 기본 답변 (못 알아들었을 때)
            return "답변이 어려운 질문입니다. '환불 규정', '이용 절차', '수수료 안내' 등의 질문에 대해 안내가 가능합니다."
            		+ "<br>별도의 질문이 필요 한 경우 고객상담 전화번호 1234-5678로 연락 부탁드립니다. "
            		+ "<br>감사합니다.";

        } catch (Exception e) {
            return "잠시 후 다시 시도해주세요. (Local Mock Mode)";
        }
    }
}