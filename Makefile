VERSION=LOCAL

default:
	@#copy logback file into resources so that it is embedded into war
	@cp etc/logback-production.xml src/main/resources/logback.xml
	@mvn -DVERSION=$(VERSION) -DDEBUG=false clean package
	@rm src/main/resources/logback.xml

debug:
	@#copy logback file into resources so that it is embedded into war
	@cp etc/logback-local.xml src/main/resources/logback.xml
	@mvn -DVERSION=$(VERSION) -DDEBUG=true clean package
	@rm src/main/resources/logback.xml

