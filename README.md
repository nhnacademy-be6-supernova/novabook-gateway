Novabook Gateway
===========
* 
* Client 모든 요청은 Gateway을 거쳐 핵심 서버인 Store로 로드 밸런싱 합니다.
* Gateway에서 필터를 활용하여 JWT토큰 인가 및 블랙리스트 관리(로그아웃)



<br>


## 기술 스택

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=flat-square&logo=Spring&logoColor=white"/>
  <img src="https://img.shields.io/badge/Github-181717?style=flat-square&logo=Github&logoColor=white"/>
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=Spring%20Boot&logoColor=white"/>
  <img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=JSON%20web%20tokens&logoColor=white"/>
</p>



<br>


로드밸런싱
=========


<br>




인가
=========

<br>

![스크린샷 2024-07-21 오후 7 30 33](https://github.com/user-attachments/assets/f306c6e5-9c40-4d13-ba66-108f44637264)

<br>

### JWT 토큰
* 프론트에서 보낸 요청은 JWT토큰을 요청에 넣어서 게이트웨이로 보냄
* JWT토큰 인가 작업 수행(만료, 부적절한 키 등)
* 로그아웃된 토큰인지 확인

<br>
