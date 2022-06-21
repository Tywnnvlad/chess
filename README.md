# Chess
Group:
- Nikolas Haugrud (<!---780-885-3439,--> nikolas.haugrud.stu@kingsu.ca, n.haugrud@hotmail.com)
- Chelsie Bajic (<!---587-987-8869,--> chelsie.bajic.stu@kingsu.ca)
- Will Ames (<!---780-217-9522,--> w_ames@outlook.com (Primary), william.ames.stu@kingsu.ca (Secondary) )
- Gregory Cal (<!---780-318-3353,--> gregory.cal.stu@kingsu.ca, gred_cal@yahoo.com)

# Overview

This project contains a java-based chess game, using a Swing GUI. The game is made
for players of various chess-ability, having features including: move highlights,
move hints, move undo/redo, a piece information query button, AI player
functionality, PGN loading and saving functionality, and more.

# Build Information

This project was created and tested with OpenJDK Java 11.0.11. Other versions
are untested and may or may not work. In addition to Java, `ant` is required
to build this project. `ant` may be installed with the `apt` package manager
via:

```
sudo apt install ant
```

## Building

To build, navigate to the root directory of this repository and run:

```
ant jar
```

## Running

To run via `ant`, use:

```
ant
```

This will also automatically build the project as needed. Additionally, the
chess game may be run via the distributable jar file found from the root
directory of this repository at `build/jar/chess.jar` after building. The jar
may then be ran via the `java` command, or by navigating the file explorer and
double clicking the jar. Here is how to run the jar file from the root directory
of the repository once the project is built, with the jar being in its default
build directory:

```
java -jar build/jar/chess.jar
```

#### Javadoc

To generate the Javadoc documentation for this project, navigate to the
repository's root directory and run:

```
ant javadoc
```

The documentation is then created in `doc/`.

#### Testing

To run unit tests for this project, run:

```
ant test
```

Test result are created in `report/`.

##### Cleaning

To clean (remove all built project files), run:

```
ant clean
```
