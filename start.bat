javac -cp .;lib/* -d target/classes src/com/circles/rippers/TorRange/*.java
java -cp target/classes/;lib/* com.circles.rippers.TorRange.Main example.ini
