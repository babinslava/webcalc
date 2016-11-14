I made this project using Spring Framework. You can start it using `java -jar webcalculator-0.0.1-SNAPSHOT.war`.
It will run on 8081 port by default.
Examples of using:
http://localhost:8081/add/1.2/2.3/3
{"result":6.5}
http://localhost:8081/subtract/1.2/2.3/3
{"result":-4.1}
http://localhost:8081/multiply/1.2/2.3/3
{"result":8.28}
http://localhost:8081/multiply/1.2/-0.3/0
{"result":-0.0}
http://localhost:8081/divide/1.2/2
{"result":0.6}
http://localhost:8081/divide/1.2/0
{"result":"Infinity"}
http://localhost:8081/divide/-1.2/0
{"result":"-Infinity"}
http://localhost:8081/divide/0/0
{"result":"NaN"}

I added git in this project and made commits so you can see git log.
I used ehcache in this project.
I used Rest Assured to test this project.
I used spring actuator to check cache work in http://localhost:8081/metrics (cache.calc.* values)