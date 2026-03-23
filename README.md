# 🧵 Nextage - 수선·리폼 역경매 플랫폼

---

## 팀 구성

| 역할 | GitHub |
|---|---|
| PM | wefefwf |
| PL | jpark281 |
| Member | Hyein-Park-git |
| Member | osjqa0918 |
| Member | devDuck-9 |
| Member | 5x5cube |

---

## 프로젝트 소개

Nextage는 의뢰자(Customer)와 전문 공급업체(Business)를 실시간으로 연결하는 **수선·리폼 역경매 플랫폼**입니다.

기존 수선·리폼 시장의 불투명한 가격 구조와 정보 비대칭 문제를 해결하고자,
의뢰자가 직접 의뢰글을 등록하면 전문 업체들이 입찰 제안을 제출하고,
의뢰자가 가격·포트폴리오·리뷰를 비교해 최적의 업체를 선택할 수 있는 구조를 구현했습니다.

단순한 거래를 넘어, 고객과 전문가가 함께 만드는 나만의 작품 — Nextage.

---

## 핵심 기능

**의뢰자(Customer)**
- 의뢰글 등록 (제목, 사진, 카테고리, 희망가격)
- 입찰 제안 비교 및 낙찰 선택
- 의뢰 상태 확인 (제안 받는 중 → 거래 중 → 거래 완료)
- 업체와 1:1 실시간 채팅
- 샘플 키트·줄자 구매 (키트 샵)
- 마이페이지 (주문 내역, 의뢰 목록, 회원 정보 수정)

**공급업체(Business)**
- 역경매 입찰 제안서 작성
- 제작·배송 상태 업데이트
- 포트폴리오 및 경력 관리
- 일정 관리 (캘린더·리스트)
- 수익 통계 및 정산 관리

**공통**
- 역할 기반 접근 제어 (Customer / Business / Admin)
- Spring Security 기반 인증·인가
- WebSocket 기반 1:1 실시간 채팅
- 결제 및 거래 완료 처리
- 관리자 페이지 (공지사항, 회원 관리, 정산)

---

## 사용 기술

| 구분 | 기술 |
|---|---|
| Frontend | HTML, CSS, JavaScript, jQuery, Bootstrap, Thymeleaf |
| Backend | Java, Spring Boot, Spring Security, Spring MVC |
| Database | MySQL, MyBatis |
| 협업 | GitHub, Figma, Jira |

---

## 시스템 아키텍처

- **MVC 패턴** 기반 Controller / Service / Mapper 구조
- **MySQL InnoDB** 트랜잭션을 통한 데이터 무결성 보장
- **역할별 화면·권한 분리** (Customer / Business / Admin)
- **WebSocket** 기반 실시간 채팅 구현
- **REST API** 기반 비동기 통신

---

## 참고 서비스

- [숨고](https://soomgo.com/)
- [윗치폼](https://witchform.com/w/main?type=commission)
- [헬로우샵](https://www.hellowshop.com/index.html)
- [CosplayFu](https://kr-m.cosplayfu.com/)
