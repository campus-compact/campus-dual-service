# campus-dual-service

## Preparation

Zertifikatsverknüpfung in einer Console mit Adminrechten:
```
keytool -import -alias example -keystore /path/to/cacerts -file cert.der
```
> Erst mit `cd` in as Verzeichnis des Backend-Projektes gehen `C:\Users\Fabian\IdeaProjects\backend\campus-dual-service`.
> Dann den `/path/to/cacerts` durch den Pfad der `cacerts`-Datei ersetzen.
> Diese liegt im Verzeichnis der verwendeten Java Version, bei mir war es `C:\Program Files\JetBrains\IntelliJ IDEA 2021.1.1\jbr\lib\security\cacerts`.

Informationen dazu: https://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed/

pw: `changeit`

---
## Lecture Service

Die 3 Möglichkeiten der Anfrage:
> 1. Mit dem YYYY-MM-DD-Format:
>
> `{"username":400xxxx,"start":"2018-01-01","end":"2018-12-31"}`

> 2. Mit dem Milliseconds-Format:
>
> `{"username":400xxxx,"start":1514764800000,"end":1546300740000}`

> 3. Ohne Angabe von start und end (gibt alle lectures aus)
>
> `{"username":400xxxx}`

Dabei ist `400xxxx` durch die Matrikelnummer zu ersetzen und das als JsonObject in den `body` hinzuzufügen!
Das Bearer-Token ist im Header mitzugeben.



### Interessantes

> Leider bietet unser BA-Glauchau-CampusDual keine Datums- oder Zeitfilterung bei der http-Anfrage an
>
> Deshalb gibt es einen selbsterstellten Filter, dieser filtert das (Start- + End-) Datum ***nach*** der Abfrage an CampusDual
> und schreibt die passenden Ergebnisse in ein neues JsonArray.

> USER_AGENT sind zwei Varianten implementiert, beide sollen funktionieren.

---
## TODOs

> Logging von Exceptions
> - https://github.com/MicroUtils/kotlin-logging
> - https://github.com/MicroUtils/kotlin-logging/wiki
>
> [04.06.21] Fabian -> Logger ist implementiert (try & catch), Tests ausstehend

> Kontrolle ob Token-des-Users und der User aus body übereinstimmen 

> Statuscode, wenn Login fehlschlägt (Code: 401)

> Implementierung weiterer Campus Funktionen

> (?) Umstellen auf from fields -> derzeit raw json

---
My pw: kjnf!lksdmfg541564dF
