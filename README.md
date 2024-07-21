Novabook Gateway
===========
<p>
  <img src="https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=flat-square&logo=Spring&logoColor=white"/>
  <img src="https://img.shields.io/badge/Github-181717?style=flat-square&logo=Github&logoColor=white"/>
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=Spring%20Boot&logoColor=white"/>
  <img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=JSON%20web%20tokens&logoColor=white"/>
</p>


`Novabook Gateway`는 모든 클라이언트 요청을 수신하고, 이를 핵심 서버 및 여러 마이크로서비스로 로드 밸런싱하는 역할을 합니다. 이 게이트웨이는 또한 JWT 토큰을 사용한 인증 및 블랙리스트 관리(로그아웃)를 처리합니다.


<br>

# 아키텍처 다이어그램
![스크린샷 2024-07-21 오후 7 30 33](https://github.com/user-attachments/assets/f306c6e5-9c40-4d13-ba66-108f44637264)


# 주요 기능 

## 로드밸런싱
`Novabook Gateway`는 Spring Cloud Gateway를 사용하여 여러 마이크로서비스 간의 로드 밸런싱을 제공합니다.
모든 클라이언트 요청은 Gateway를 통해 Auth, Store, Coupon 등 여러 서비스로 전달됩니다.

## JWT 인증 및 인가
- 클라이언트는 요청에 JWT 토큰을 포함하여 Gateway로 보냅니다.
- Gateway는 JWT 토큰의 유효성을 검사합니다 (예: 만료 여부, 적절한 키 사용 등).
- 블랙리스트에 등록된 토큰인지 확인하여 로그아웃된 토큰을 차단합니다.
<br>
