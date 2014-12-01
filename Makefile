CLASSES=classes
CLASSPATH=jars/snakeyaml-1.12.jar:$(CLASSES)

.PHONY: run_server run_client

.SILENT: classes/com/abreen/dungeon/DungeonServer.class classes/com/abreen/dungeon/DungeonClient.class run_server run_client

all: classes/com/abreen/dungeon/DungeonServer.class classes/com/abreen/dungeon/DungeonClient.class

classes/com/abreen/dungeon/DungeonServer.class:
	-mkdir classes
	javac -Xlint:unchecked -d $(CLASSES) -classpath $(CLASSPATH):. com/abreen/dungeon/DungeonServer.java

classes/com/abreen/dungeon/DungeonClient.class:
	-mkdir classes
	javac -Xlint:unchecked -d $(CLASSES) -classpath $(CLASSPATH):. com/abreen/dungeon/DungeonClient.java

run_server: classes/com/abreen/dungeon/DungeonServer.class
	java -classpath $(CLASSPATH):$(CLASSES) com.abreen.dungeon.DungeonServer

run_client: classes/com/abreen/dungeon/DungeonClient.class
	java -classpath $(CLASSPATH):$(CLASSES) com.abreen.dungeon.DungeonClient

clean:
	-rm -r classes
