# backend-server  

# 🌟 희소식 (HeeSoSik)

**희소식(喜消息)** = **기쁜 소식, 좋은 소식**  
분노를 넘어, 긍정을 잇다  
당신의 하루에 따뜻함을 전하는 희소식  

---

## 📌 Overview
최근 연이어 발생하는 사건‧사고와 분노 유발 콘텐츠의 증가로 사람들은 **‘분노 피로감(Outrage Fatigue)’**을 경험하고 있습니다.  
유튜브, SNS 알고리즘은 분노를 유발하는 콘텐츠를 더 많이 노출하여 피로감을 심화시키고 있습니다.  

**희소식**은 이러한 문제를 해결하기 위해  
👉 _맞춤형 긍정 뉴스 추천_  
👉 _긍정 경험 공유 커뮤니티_  
👉 _감정 변화 시각화_  
를 제공하는 애플리케이션입니다.

## 💡 Key Features

### 1) 메인 화면 & 긍정 피드
- 사용자가 선택한 **긍정 키워드 기반 맞춤형 뉴스 추천**
- 뉴스 **감정 평가 및 스크랩 기능**
- 평가 결과는 **나의 감정 그래프**에 반영

### 2) 희소식 커뮤니티
- 사용자가 직접 **긍정적인 경험/뉴스 공유**
- 답글 & 좋아요를 통한 공감과 소통
- 주제별 **플레이스 생성 및 참여** 가능

### 3) 나의 감정 그래프
- 일정 기간 동안의 **긍정 감정 변화 시각화**
- 주/월/6개월/전체 기간 비교 가능
- 긍정 뉴스 소비 지속을 위한 **동기 부여**

### 4) 추가 기능
- **푸시 알림**: 사용자가 뉴스를 자주 보는 시간대에 긍정 뉴스 제공  
- **검색 기능**: 긍정 피드와 희소식에서 키워드별 검색 및 최근 검색어 관리  



### 💙 팀원 소개

| 장서원 | 김채원 | 
|--------|--------|
|  | <img width="396" height="391" alt="image" src="https://github.com/user-attachments/assets/61d388ce-1b35-4521-b9ce-43ba91e5e35f" />| 
| [@oculo0204](https://github.com/oculo0204) | [@wonee1](https://github.com/wonee1) |
| 로그인/회원가입, 회원 정보, 희소식, 댓글, 기사, 서버 배포 | 플레이스, 희소식, 댓글 기능 및 서버 배포| 



## 🛠 기술 스택 및 환경

- **Backend**  
  - Java 17  
  - Spring Boot 3.4.7  
  - Gradle 8.14.2  
  - Hibernate ORM 6.0.2  

- **Database**  
  - MariaDB 3.3.3 (JDBC 드라이버)  

- **보안 및 인증**  
  - Spring Security  
  - JWT (jjwt 라이브러리)  

- **API 문서화**  
  - Notion

- **UI 템플릿**  
  - Thymeleaf + Spring Security  

- **클라우드 & 인프라**  
  - AWS EC2, S3, Route53  

- **CI/CD**  
  - GitHub Actions  

- **주요 라이브러리**  
  - Lombok, Jsoup
  - 
- **Gradle 설정**  
  - `java`, `org.springframework.boot`, `io.spring.dependency-management` 플러그인 적용  

---

## 📂 프로젝트 구조  
```
```



## 📌 Branch 전략 ##
## Branch

본 프로젝트는 Gitflow 브랜치 전략을 따릅니다.


<div align=center>
    <img src="https://techblog.woowahan.com/wp-content/uploads/img/2017-10-30/git-flow_overall_graph.png" width=50% alt="브랜치 전략 설명 이미지"/>
</div>

모든 기능 개발은 다음 흐름을 따릅니다.

1. 개발하고자 하는 기능에 대한 이슈를 등록하여 번호를 발급합니다.
2. `main` 브랜치로부터 분기하여 이슈 번호를 사용해 이름을 붙인 `feature` 브랜치를 만든 후 작업합니다.
3. 작업이 완료되면 `develop` 브랜치에 풀 요청을 작성하고, 팀원의 동의를 얻으면 병합합니다.

# Branch	종류
- main	기능 개발 통합 브랜치 (pull request하고 동료들에게 merge요청, 확인이 오래걸리면 스스로 merge) 
데모용 프로젝트이기 때문에 배포용 브랜치를 따로 두지 않습니다.
- feature/{이슈번호}{간단한설명}	새로운 기능 개발 브랜치
- fix/{이슈번호}{간단한설명}	버그 수정 브랜치
- hotfix/{이슈번호}{간단한설명}	긴급 수정 브랜치
- refactor/{이슈번호}{간단한설명}	리팩토링 브랜치
- chore/{이슈번호}{간단한설명}	기타 설정, 패키지 변경 등
# Branch    설명
1. 기능개발이 완료된 브랜치는 develop브랜치에 merge합니다.
2. merge된 Branch는 삭제합니다.
</br></br>
✅ 예시
- feature/#12-login-api
- fix/#17-cors-error
- chore/#20-env-setting
</br></br>
✅ Git 사용 규칙
# 커밋 메시지 형식
- #이슈번호 <타입>: <변경 요약> 
</br>
- <타입> 종류</br>
태그 이름	설명</br>
[init] 초기설정</br>
[chore]	코드 수정, 내부 파일 수정</br>
[feat]	새로운 기능 구현</br>
[add]	FEAT 이외의 부수적인 코드 추가, 라이브러리 추가, 새로운 파일 생성</br>
[hotfix]	issue나 QA에서 급한 버그 수정에 사용</br>
[fix]	버그, 오류 해결</br>
[del]	쓸모 없는 코드 삭제</br>
[docs]	README나 WIKI 등의 문서 개정</br>
[correct]	주로 문법의 오류나 타입의 변경, 이름 변경에 사용</br>
[move]	프로젝트 내 파일이나 코드의 이동</br>
[rename]	파일 이름 변경이 있을 때 사용</br>
[improve]	향상이 있을 때 사용</br>
[refactor]	전면 수정이 있을 때 사용</br>
[test]	테스트 코드 추가 시 사용 </br>

# 💙서비스 아키텍처

# erd
