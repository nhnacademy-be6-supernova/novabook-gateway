# Novabook Gateway

Client 요청은 Gateway을 거쳐 핵심 서버인 Store로 로드 밸런싱 합니다.



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



## 프로젝트 구조

- **Gateway**: 클라이언트 요청을 수신하고 로드 밸런싱을 통해 Store 서버로 전달합니다.
- **Store 서버**: 핵심 비즈니스 로직이 실행되는 서버입니다.

---
