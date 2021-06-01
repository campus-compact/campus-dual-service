# campus-dual-service

## Preparation

Zertifikatsverknüpfung in einer Console mit Adminrechten:
```
keytool -import -alias example -keystore  /path/to/cacerts -file cert.der
```
> Erst mit `cd` in as Verzeichnis des Backend-Projektes gehen `C:\Users\Fabian\IdeaProjects\backend\campus-dual-service`.
> Dann den `/path/to/cacerts` durch den Pfad der `cacerts`-Datei ersetzen. 
> Diese liegt im Verzeichnis der verwendeten Java Version, bei mir war es `C:\Program Files\JetBrains\IntelliJ IDEA 2021.1.1\jbr\lib\security`. 

Informationen dazu: https://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed/

---
##Lecture Service

Die 3 Möglichkeiten der Anfrage:
> 1. Mit dem YYY-MM-DD-Format: 
> 
> `{"username":$MNr,"hash":"$hash","start":"2018-01-01","end":"2018-12-01"}`

> 2. Mit dem Milliseconds-Format: 
>
> `{"username":§MNr,"hash":"$hash","start":"1514764800000","end":"1546300740000"}`

> 3. Ohne Angabe von start und end (gibt alle lectures aus)
> 
> `{"username":§MNr,"hash":"$hash"}`

Dabei ist `§MNr` durch die Matrikelnummer (400xxxx) zu ersetzen und `$hash` durch den entsprechenden Hashwert, den man von Login bekommt.

Das JsonObject muss dem `body` hinzugefügt werden!


###Interessantes

> Leider bietet unser BA-Glauchau-CampusDual keine Datums- oder Zeitfilterung bei der http-Anfrage an
>
> Deshalb gibt es einen selbsterstellten Filter, dieser filtert das (Start- + End-) Datum ***nach*** der Abfrage an CampusDual
> und schreibt die passenden Ergebnisse in ein neues JsonArray.

> USER_AGENT sind zwei Varianten implementiert, beide sollen funktionieren.

---
My pw: kjnf!lksdmfg541564dF